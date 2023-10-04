package com.github.dinbtechit.ngxs.action.editor

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFunctionImpl
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.types.TypeScriptClassElementType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import java.util.*

enum class NgxsActionType {
    WITH_PAYLOAD,
    WITHOUT_PAYLOAD
}

class NgxsStatePsiFile(
    private val ngxsStatePsiFile: VirtualFile,
    val project: Project
) {

    companion object {
        fun isNgxsStateFile(project: Project, psiFile: VirtualFile): Boolean {
            return PsiManager.getInstance(project).findFile(psiFile)
                ?.children?.any {
                    it.elementType is TypeScriptClassElementType
                            && it.children[0] is JSAttributeList
                            && it.text.contains("@State")
                } == true
        }
    }

    fun getTypeFromStateAnnotation(): String? {
        val stateClassPsi = getStateClassElement()?.children?.firstOrNull()
        if (stateClassPsi !is JSAttributeList) return null
        val regex = Regex("<(.*?)>")
        val matchResult = regex.find(stateClassPsi.text)
        return matchResult?.groups?.get(1)?.value
    }

    fun getStateClassElement(): PsiElement? {
        return PsiManager.getInstance(project).findFile(ngxsStatePsiFile)
            ?.children?.firstOrNull {
                it.elementType is TypeScriptClassElementType
                        && it.children[0] is JSAttributeList
                        && it.text.contains("@State")
            }
    }

    fun isActionMethodElement(element: PsiElement): Boolean {
        getStateClassElement() ?: return false
        val function = PsiTreeUtil.getParentOfType(element, TypeScriptFunction::class.java)
        val decorator = PsiTreeUtil.getParentOfType(element, ES6Decorator::class.java, false, TypeScriptFunction::class.java)
        val actionDecorator = decorator?.text?.contains("@Action") ?: false
        val actionDecoratorElement = !(element.text.contains("Action") && element.parent.parent?.prevSibling?.text == "@")
        val isReferenceExpression = element.parent is JSReferenceExpression
        return function != null && decorator != null && actionDecorator &&  isReferenceExpression && actionDecoratorElement
    }

    fun getTypeInActionActionDecoratorElement(element: PsiElement): PsiElement? {
        getStateClassElement() ?: return null
        if (element is JSCallExpression) {
            for (el in element.children) {
                if (el is JSArgumentList) {
                   for (e in el.children) {
                       if(e is JSReferenceExpression) return e
                   }
                }
            }
        }

        return null
    }

    fun isCursorWithinStateClass(editor: Editor, file: PsiFile): Boolean {
        if (file.virtualFile == null || editor.project == null) return false
        val ngxsStateElement = NgxsStatePsiFile(file.virtualFile, editor.project!!).getStateClassElement()

        if (ngxsStateElement != null) {
            val caretModel: CaretModel = editor.caretModel
            val logicalPosition: LogicalPosition = caretModel.logicalPosition
            val lineNumber: Int = logicalPosition.line

            val documentManager = PsiDocumentManager.getInstance(ngxsStateElement.project)
            val document: Document? = documentManager.getDocument(file)

            val startOffset = ngxsStateElement.textRange.startOffset
            val endOffset = ngxsStateElement.textRange.endOffset

            val startLine = document?.getLineNumber(startOffset)?.plus(1) ?: -1 // 1-indexed
            val endLine = document?.getLineNumber(endOffset)?.plus(1) ?: -1// 1-indexed

            return lineNumber in (startLine + 1) until endLine
        }

        return false
    }

    fun createActionMethodLiveTemplates(editor: Editor, file: PsiFile, actionType: NgxsActionType) {
        if (file.virtualFile == null) return
        val isNgxsState = isNgxsStateFile(project, file.virtualFile)

        if (isNgxsState) {
            val ngxsState = NgxsStatePsiFile(file.virtualFile, project)
            val stateModel = ngxsState.getTypeFromStateAnnotation()

            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(project)
                val template = when (actionType) {
                    NgxsActionType.WITHOUT_PAYLOAD -> createActionMethod(templateManager, stateModel)
                    NgxsActionType.WITH_PAYLOAD -> createActionMethodWithPayload(
                        templateManager,
                        stateModel
                    )
                    else -> return
                }

                val defaultName = ConstantNode("methodName")
                val defaultAction = ConstantNode("ActionName")

                template.addVariable("action", defaultAction, defaultAction, true)
                template.addVariable("name", defaultName, defaultName, true)
                templateManager.startTemplate(editor, template)
            }

        }
    }

    fun createSelectorMethodLiveTemplates(editor: Editor, file: PsiFile) {
        if (file.virtualFile == null) return
        val isNgxsState = isNgxsStateFile(project, file.virtualFile)

        if (isNgxsState) {
            val ngxsState = NgxsStatePsiFile(file.virtualFile, project)
            val stateModel = ngxsState.getTypeFromStateAnnotation()

            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(project)
                val template = createMetaSelectorMethod(templateManager, stateModel)
                templateManager.startTemplate(editor, template)
            }
        }
    }

    fun createSelectorsMethodLiveTemplates(editor: Editor, file: PsiFile) {
        if (file.virtualFile == null) return
        val isNgxsState = isNgxsStateFile(project, file.virtualFile)

        if (isNgxsState) {
            val ngxsState = NgxsStatePsiFile(file.virtualFile, project)
            val stateModel = ngxsState.getTypeFromStateAnnotation()

            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(project)
                val template = createSelectorMethod(templateManager, stateModel)
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

    private fun createSelectorMethod(templateManager: TemplateManager, stateModel: String): Template {
        val methodName = stateModel.toCamelCase().replace("Model", "")
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
        template.addVariable("methodName", defaultName, defaultName, true)
        return template
    }

    private fun createMetaSelectorMethod(templateManager: TemplateManager, stateModel: String): Template {
        val methodName = stateModel.toCamelCase().replace("Model", "")
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
        template.addVariable("methodName", defaultName, defaultName, true)
        return template
    }

    fun createActionMethod(actionPsiElement: PsiElement): PsiElement? {
        val stateClassPsi = getStateClassElement()
        if (stateClassPsi != null) {
            if (stateClassPsi.node.lastChildNode.text == "}") {
                val lastFunction = stateClassPsi.children.lastOrNull { it is TypeScriptFunctionImpl }
                if (lastFunction != null) {
                    val actionMethod = """
                            @Action(${actionPsiElement.text})
                            ${actionPsiElement.text.toCamelCase()}(ctx: StateContext<${getTypeFromStateAnnotation()}>) {
                              // TODO implement action
                            }
                           """.trimIndent()

                    val actionMethodWithPayload = """
                            @Action(${actionPsiElement.text})
                            ${actionPsiElement.text.toCamelCase()}(ctx: StateContext<${getTypeFromStateAnnotation()}>, payload: ${actionPsiElement.text}) {
                              // TODO implement action
                            }
                           """.trimIndent()

                    val document: Document =
                        FileDocumentManager.getInstance().getDocument(ngxsStatePsiFile) ?: return null

                    WriteCommandAction.runWriteCommandAction(project) {
                        // Check where to insert the new code
                        val insertOffset: Int = lastFunction.endOffset
                        // Insert the new code
                        if (NgxsActionUtil.hasPayload(actionPsiElement)) {
                            document.insertString(insertOffset, "\n${actionMethodWithPayload}")
                        } else {
                            document.insertString(insertOffset, "\n${actionMethod}")
                        }
                        PsiDocumentManager.getInstance(project).commitDocument(document)
                        PsiManager.getInstance(project).findFile(ngxsStatePsiFile)?.let { psiFile ->
                            val length = psiFile.textLength
                            val range = TextRange.from(insertOffset, length - insertOffset)

                            CodeStyleManager.getInstance(project)
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
