package com.github.dinbtechit.ngxs.action.cli

import com.github.dinbtechit.ngxs.NgxsBundle
import com.github.dinbtechit.ngxs.action.cli.store.CLIActions
import com.github.dinbtechit.ngxs.action.cli.store.CLIState
import com.github.dinbtechit.ngxs.action.cli.util.NgxsGeneratorFileUtil
import com.github.dinbtechit.ngxs.common.services.NgxsCliService
import com.github.dinbtechit.ngxs.common.ui.TextIconField
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class GenerateCLIDialogV2(private val project: Project, e: AnActionEvent) : DialogWrapper(project) {

    private val ngxsStoreService = project.service<CLIState>()
    private val store = ngxsStoreService.store
    private val state = ngxsStoreService.store.getState()
    private val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
    private val directory = when {
        virtualFile == null -> null
        virtualFile.isDirectory -> virtualFile // If it's directory, use it
        else -> virtualFile.parent // Otherwise, get its parent directory
    }
    private val ngxsCliService = project.service<NgxsCliService>()
    private val optionTypes = ngxsCliService.getTypeOptions()
    private val comboBoxModel = DefaultComboBoxModel(
        optionTypes.keys.toTypedArray()
    )
    private val schematicTypeComboBox = ComboBox(comboBoxModel).apply {
        setRenderer(GenerateTypeComboRenderer(project))
    }

    private var autoCompleteField = TextFieldWithAutoCompletion(
        project, CLIOptionsCompletionProviderV2(
            project, listOf()
        ), false, null
    ).apply {
        setPlaceholder(if (state.hasDefaultNameParameter) "name --options" else "--options")
    }

    private val pathField = TextIconField(AllIcons.Actions.GeneratedFolder)
    private val helpTextLabel = JBLabel(if (store.getState().hasDefaultNameParameter) "[name] [--options]" else "[--options]")


    init {
        title = NgxsBundle.message("dialog.title")
        schematicTypeComboBox.item = state.selectedSchematicType
        store.dispatch(CLIActions.LoadTypesAction(cliTypeOptions = optionTypes))
        store.dispatch(
            CLIActions.SelectSchematicType(
                selectedSchematicType = schematicTypeComboBox.item,
                hasDefaultNameParameter = ngxsCliService.hasDefaultNameParameter(schematicTypeComboBox.item),
                selectedSchematicParameters = ngxsCliService.getSchematicsParameters(schematicTypeComboBox.item)
            )
        )

        autoCompleteField.text = state.parameter
        autoCompleteField.isEnabled = state.module != null
        autoCompleteField.installProvider(
            CLIOptionsCompletionProviderV2(
                project,
                ngxsStoreService.store.getState().selectedSchematicParameters.keys.toList()
            )
        )

        pathField.apply {
            val relativePath = NgxsGeneratorFileUtil.getRelativePath(project, directory!!)
            text = when (relativePath) {
                "" -> project.guessProjectDir()?.path
                else -> relativePath
            }
            isEnabled = true
            isEditable = false
        }

        autoCompleteField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                updateErrorInfo(listOf())
            }
        })

        schematicTypeComboBox.addItemListener {
            if (it?.stateChange == ItemEvent.SELECTED) {
                store.dispatch(
                    CLIActions.SelectSchematicType(
                        selectedSchematicType = schematicTypeComboBox.item,
                        hasDefaultNameParameter = ngxsCliService.hasDefaultNameParameter(schematicTypeComboBox.item),
                        selectedSchematicParameters = ngxsCliService
                            .getSchematicsParameters(schematicTypeComboBox.item)
                    )
                )

                autoCompleteField.apply { setPlaceholder(if (store.getState().hasDefaultNameParameter) "name --options" else "--options") }
                helpTextLabel.apply {
                    text = if (store.getState().hasDefaultNameParameter) "[name] [--options]" else "[--options]"
                }

                autoCompleteField.installProvider(
                    CLIOptionsCompletionProviderV2(
                        project,
                        ngxsStoreService.store.getState()
                            .selectedSchematicParameters.keys.toList()
                    )
                )
            }
            updateErrorInfo(listOf())
        }
        ComboboxSpeedSearch.installSpeedSearch(schematicTypeComboBox) { schematicTypeComboBox.item }
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            group(NgxsBundle.message("dialog.generateInPath")) {
                row {
                    cell(pathField).align(Align.FILL)
                }
            }
            separator()
            row(NgxsBundle.message("dialog.schematicType")) {}.topGap(TopGap.SMALL)
            row {
                cell(schematicTypeComboBox)
                    .focused()
                    .align(Align.FILL)
            }
            row(NgxsBundle.message("dialog.parameters")) {}.topGap(TopGap.SMALL)
            row {
                cell(autoCompleteField).align(
                    Align.FILL
                )
            }

            row {
                cell(helpTextLabel.apply {
                    font = UIUtil.getLabelFont(UIUtil.FontSize.SMALL)
                })
            }

            window.minimumSize = Dimension(500, super.getPreferredSize().height)
        }
    }

    override fun doValidate(): ValidationInfo? {
        val fileName = autoCompleteField.text.split(" ")[0]
        var invalidFileName = false
        if (fileName.isNotBlank() &&
            fileName.startsWith("-", ignoreCase = true)) {
            invalidFileName = store.getState().hasDefaultNameParameter
        }
        return if ((store.getState().hasDefaultNameParameter && fileName.isBlank()) || autoCompleteField.text.isBlank()) {
            ValidationInfo(NgxsBundle.message("dialog.parameterBlankErrorMessage"), autoCompleteField)
        } else if (invalidFileName) {
            ValidationInfo("$fileName in an invalid filename", autoCompleteField)
        } else null
    }


    override fun doOKAction() {
        store.dispatch(
            CLIActions.GenerateCLIAction(
                options = autoCompleteField.text,
                filePath = directory!!.path,
                project = project,
                workingDir = directory,
                module = state.module!!
            )
        )
        super.doOKAction()
    }

}

private class TextComponentPredicate(
    private val component: TextFieldWithAutoCompletion<String>,
    private val predicate: (String) -> Boolean
) : ComponentPredicate() {
    override fun invoke(): Boolean = predicate(component.text)

    override fun addListener(listener: (Boolean) -> Unit) {
        component.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                listener(invoke())
            }
        })
    }

}
