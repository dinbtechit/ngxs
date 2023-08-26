package com.github.dinbtechit.ngxs.action.cli

import com.github.dinbtechit.ngxs.NgxsBundle
import com.github.dinbtechit.ngxs.action.cli.store.Action
import com.github.dinbtechit.ngxs.action.cli.util.CliParameterUtil.convertToCli
import com.github.dinbtechit.ngxs.action.cli.util.CliParameterUtil.convertToString
import com.github.dinbtechit.ngxs.action.cli.util.CliParameterUtil.update
import com.github.dinbtechit.ngxs.action.cli.util.NgxsGeneratorFileUtil
import com.github.dinbtechit.ngxs.common.ui.TextIconField
import com.github.dinbtechit.ngxs.action.cli.store.CLIState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class GenerateCLIDialog(private val project: Project, e: AnActionEvent) : DialogWrapper(project, true) {
    private var autoCompleteField = TextFieldWithAutoCompletion(
        project,
        CLIOptionsCompletionProvider(CLIOptionsCompletionProvider.options.keys.toList()), false,
        null
    ).apply {
        setPlaceholder("--options")
    }

    private val ngxsStoreService = project.service<CLIState>()
    private val store = ngxsStoreService.store

    private val nameField = JBTextField()

    private val virtualFile: VirtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
    private val directory = when {
        virtualFile.isDirectory -> virtualFile // If it's directory, use it
        else -> virtualFile.parent // Otherwise, get its parent directory
    }

    private val pathField = TextIconField(AllIcons.Actions.GeneratedFolder)

    private val state = ngxsStoreService.store.getState()

    init {
        title = "NGXS CLI/Schematics Generate"
        autoCompleteField.text = state.parameter
        autoCompleteField.isEnabled = state.module != null
        pathField.apply {
            val relativePath = NgxsGeneratorFileUtil.getRelativePath(project, directory)
            text = when (relativePath) {
                "" -> project.guessProjectDir()?.path
                else -> relativePath
            }
            isEnabled = true
            isEditable = false
        }
        nameField.text = state.name
        nameField.document.addDocumentListener(object : DocumentListener {
            private fun update() {
                ApplicationManager.getApplication().invokeLater {
                    nameField.text = nameField.text.replace(" ", "-")
                    val cli = autoCompleteField.text.convertToCli().toMutableMap()
                    var updateFolderName = false
                    if (!cli.containsKey("folder-name") || cli["name"] == cli["folder-name"]) {
                        updateFolderName = true
                    }
                    cli.update("name", nameField.text.trim())
                    if (updateFolderName) cli.update("folder-name", nameField.text.trim())

                    store.dispatch(Action.UpdateParameter(nameField.text, cli.convertToString()))
                    autoCompleteField.text = cli.convertToString()
                }
            }

            override fun insertUpdate(e: DocumentEvent?) {
                update()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                update()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                update()
            }

        })
        autoCompleteField.document.addDocumentListener(object: com.intellij.openapi.editor.event.DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                ApplicationManager.getApplication().invokeLater {
                    val cli = autoCompleteField.text.convertToCli().toMutableMap()
                    if(cli.containsKey("name") && cli["name"] != null) nameField.text = cli["name"]
                }
            }
        })
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            group(NgxsBundle.message("generateInPath")) {
                row {
                    cell(pathField).align(Align.FILL)
                }
            }
            separator()
            row(NgxsBundle.message("name")) {}.topGap(TopGap.SMALL)
            row {
                cell(nameField).align(
                    Align.FILL
                ).focused()
            }
            row(NgxsBundle.message("parameters")) {}.topGap(TopGap.SMALL)
            row {
                cell(autoCompleteField).align(
                    Align.FILL
                ).apply {
                    comment("(--name name --folder-name name --options)")
                }
            }
            window.minimumSize = Dimension(500, super.getPreferredSize().height)
        }
    }

    override fun doValidate(): ValidationInfo? {
        val parameters = autoCompleteField.text
        var invalidFileName = false
        if (parameters.isNotBlank()) {
            invalidFileName = true
        }
        return if (parameters.isBlank() || autoCompleteField.text.isBlank()) {
            ValidationInfo(NgxsBundle.message("parameterBlankErrorMessage"), autoCompleteField)
        } else null
    }


    override fun doOKAction() {
        store.dispatch(
            Action.GenerateCLIAction(
                options = autoCompleteField.text,
                filePath = directory.path,
                project = project,
                workingDir = directory,
                module = state.module!!
            )
        )
        super.doOKAction()
    }

}
