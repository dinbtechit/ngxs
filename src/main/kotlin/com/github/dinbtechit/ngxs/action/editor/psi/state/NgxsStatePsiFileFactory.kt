package com.github.dinbtechit.ngxs.action.editor.psi.state

import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.LiveTemplateOptions
import com.github.dinbtechit.ngxs.action.editor.psi.actions.NgxsActionsPsiUtil
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFunctionImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.refactoring.suggested.endOffset
import java.util.*

enum class NgxsActionType {
    WITHOUT_PAYLOAD,
    WITH_PAYLOAD
}

object NgxsStatePsiFileFactory {

    fun createActionMethodLiveTemplates(editor: Editor, file: PsiFile,
                                        actionType: NgxsActionType,
                                        liveTemplateOptions: LiveTemplateOptions?) {
        if (file.virtualFile == null) return
        val isNgxsState = NgxsStatePsiUtil.isNgxsStateFile(
            editor.project ?:return,
            file.virtualFile)

        if (isNgxsState) {
            val stateModel = NgxsStatePsiUtil.getTypeFromStateAnnotation(editor.project!!, file.virtualFile)

            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(editor.project)
                val template = when (actionType) {
                    NgxsActionType.WITHOUT_PAYLOAD -> createActionMethod(templateManager, stateModel)
                    NgxsActionType.WITH_PAYLOAD -> createActionMethodWithPayload(
                        templateManager,
                        stateModel
                    )
                    else -> return
                }

                var methodName = "methodName"
                var actionName = "ActionName"
                var editMode = true

                if (liveTemplateOptions != null) {
                    methodName = if(liveTemplateOptions.methodName.isNullOrBlank()) methodName
                    else liveTemplateOptions.methodName
                    actionName = if(liveTemplateOptions.className.isNullOrBlank()) actionName
                    else liveTemplateOptions.className
                    editMode = liveTemplateOptions.editMode
                }

                val defaultName = ConstantNode(methodName)
                val defaultAction = ConstantNode(actionName)

                template.addVariable("action", defaultAction, defaultAction, editMode)
                template.addVariable("name", defaultName, defaultName, editMode)
                templateManager.startTemplate(editor, template)
            }

        }
    }

    fun createSelectorMethodLiveTemplates(editor: Editor, file: PsiFile,
                                          methodNameAndClassName: LiveTemplateOptions? = null) {
        if (file.virtualFile == null || editor.project == null) return
        val isNgxsState = NgxsStatePsiUtil.isNgxsStateFile(editor.project!!, file.virtualFile)

        if (isNgxsState) {
            val stateModel = NgxsStatePsiUtil.getTypeFromStateAnnotation(editor.project!!, file.virtualFile)
            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(editor.project)
                val template = createMetaSelectorMethod(templateManager, stateModel, methodNameAndClassName)
                templateManager.startTemplate(editor, template)
            }
        }
    }

    fun createSelectorsMethodLiveTemplates(editor: Editor, file: PsiFile,
                                           methodNameAndClassName: LiveTemplateOptions? = null) {
        if (file.virtualFile == null || editor.project == null) return
        val isNgxsState = NgxsStatePsiUtil.isNgxsStateFile(editor.project!!, file.virtualFile)

        if (isNgxsState) {
            val stateModel = NgxsStatePsiUtil.getTypeFromStateAnnotation(editor.project!!, file.virtualFile)
            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(editor.project)
                val template = createSelectorMethod(templateManager, stateModel, methodNameAndClassName)
                templateManager.startTemplate(editor, template)
            }
        }
    }

    private fun createActionMethodWithPayload(templateManager: TemplateManager, stateModel: String): Template {
        return templateManager.createTemplate(
            "ngxs-action", "Ngxs",
            """
                    @Action(${"$"}action${"$"})
                    ${"$"}name${"$"}({ patchState }: StateContext<$stateModel>, payload: ${"$"}action${"$"}) {
                        // TODO - Implement action
                    }
                    """.trimIndent()
        )
    }

    private fun createActionMethod(templateManager: TemplateManager, stateModel: String): Template {
        return templateManager.createTemplate(
            "ngxs-action", "Ngxs",
            """
                    @Action(${"$"}action${"$"})
                    ${"$"}name${"$"}({ patchState }: StateContext<$stateModel>) {
                        // TODO - Implement action
                    }
                    """.trimIndent()
        )
    }

    private fun createSelectorMethod(templateManager: TemplateManager, stateModel: String,
                                     liveTemplateOptions: LiveTemplateOptions? = null): Template {
        var methodName = stateModel.toCamelCase().replace("Model", "")
        var editMode = true
        if (liveTemplateOptions?.methodName != null) {
            methodName = liveTemplateOptions.methodName
            editMode = liveTemplateOptions.editMode
        }

        val template = templateManager.createTemplate(
            "ngxs-meta-selector", "Ngxs",
            """
            @Selector([])
            static ${"$"}methodName${"$"}(state: $stateModel) {
               // TODO - return a slice of the State
            }
            """.trimIndent()
        )
        val defaultName = ConstantNode(methodName)
        template.addVariable("methodName", defaultName, defaultName, editMode)
        return template
    }

    private fun createMetaSelectorMethod(templateManager: TemplateManager,
                                         stateModel: String,
                                         liveTemplateOptions: LiveTemplateOptions? = null): Template {
        var methodName = stateModel.toCamelCase().replace("Model", "")
        var editMode = true
        if (liveTemplateOptions?.methodName != null) {
            methodName = liveTemplateOptions.methodName
            editMode = liveTemplateOptions.editMode
        }
        val template = templateManager.createTemplate(
            "ngxs-meta-selector", "Ngxs",
            """
            @Selector()
            static ${"$"}methodName${"$"}(state: $stateModel) {
              return state;
            }
            """.trimIndent()
        )
        val defaultName = ConstantNode(methodName)
        template.addVariable("methodName", defaultName, defaultName, editMode)
        return template
    }

    fun createActionMethod(actionPsiElement: PsiElement, ngxsStatePsiFile: VirtualFile): PsiElement? {
        val stateClassPsi = NgxsStatePsiUtil.getStateClassElement(actionPsiElement.project, ngxsStatePsiFile)
        if (stateClassPsi != null) {
            if (stateClassPsi.node.lastChildNode.text == "}") {
                val lastFunction = stateClassPsi.children.lastOrNull { it is TypeScriptFunctionImpl }
                val stateModelType = NgxsStatePsiUtil.getTypeFromStateAnnotation(actionPsiElement.project, ngxsStatePsiFile)
                if (lastFunction != null) {
                    val actionMethod = """
                            @Action(${actionPsiElement.text})
                            ${actionPsiElement.text.toCamelCase()}(ctx: StateContext<${stateModelType}>) {
                              // TODO implement action
                            }
                           """.trimIndent()

                    val actionMethodWithPayload = """
                            @Action(${actionPsiElement.text})
                            ${actionPsiElement.text.toCamelCase()}(ctx: StateContext<${stateModelType}>, payload: ${actionPsiElement.text}) {
                              // TODO implement action
                            }
                           """.trimIndent()

                    val document: Document =
                        FileDocumentManager.getInstance().getDocument(ngxsStatePsiFile) ?: return null

                    WriteCommandAction.runWriteCommandAction(actionPsiElement.project) {
                        // Check where to insert the new code
                        val insertOffset: Int = lastFunction.endOffset
                        // Insert the new code
                        if (NgxsActionsPsiUtil.hasPayload(actionPsiElement)) {
                            document.insertString(insertOffset, "\n${actionMethodWithPayload}")
                        } else {
                            document.insertString(insertOffset, "\n${actionMethod}")
                        }
                        PsiDocumentManager.getInstance(actionPsiElement.project).commitDocument(document)
                        PsiManager.getInstance(actionPsiElement.project).findFile(ngxsStatePsiFile)?.let { psiFile ->
                            val length = psiFile.textLength
                            val range = TextRange.from(insertOffset, length - insertOffset)

                            CodeStyleManager.getInstance(actionPsiElement.project)
                                .reformatText(psiFile, range.startOffset, range.endOffset)
                        }
                    }
                    FileDocumentManager.getInstance().saveDocument(document)
                    return stateClassPsi.children.lastOrNull { it is TypeScriptFunctionImpl }
                }
            }
        }
        return null
    }

    private fun String.toCamelCase(): String = split(" ").joinToString("") {
        it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }.replaceFirstChar { it.lowercase(Locale.getDefault()) }
}
