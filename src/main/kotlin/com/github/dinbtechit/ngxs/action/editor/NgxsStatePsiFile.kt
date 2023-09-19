package com.github.dinbtechit.ngxs.action.editor

import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFunctionImpl
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.types.TypeScriptClassElementType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import java.util.*

class NgxsStatePsiFile(
    private val ngxsStatePsiFile: VirtualFile,
    val project: Project
) {

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

                    val document: Document = FileDocumentManager.getInstance().getDocument(ngxsStatePsiFile) ?: return null

                    WriteCommandAction.runWriteCommandAction(project) {
                        // Check where to insert the new code
                        val insertOffset: Int = lastFunction.endOffset
                        // Insert the new code
                        if(NgxsActionUtil.hasPayload(actionPsiElement)) {
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

    private fun String.toCamelCase(): String = split(" ").joinToString("") { it.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    } }.replaceFirstChar { it.lowercase(Locale.getDefault()) }

}
