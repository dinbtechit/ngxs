package com.github.dinbtechit.ngxs.action.editor.psi.state

import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.types.TypeScriptClassElementType
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.elementType

object NgxsStatePsiUtil {
    fun isNgxsStateFile(project: Project, psiFile: VirtualFile): Boolean {
        return PsiManager.getInstance(project).findFile(psiFile)
            ?.children?.any {
                it.elementType is TypeScriptClassElementType
                        && it.children[0] is JSAttributeList
                        && it.text.contains("@State")
            } == true
    }

    fun isCursorWithinStateClass(editor: Editor, file: PsiFile): Boolean {
        if (file.virtualFile == null || editor.project == null) return false
        val ngxsStateElement = NgxsStatePsiUtil.getStateClassElement(editor.project!!, file.virtualFile)

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

            val currentPositionElement = file.findElementAt(caretModel.offset) ?: return false
            val isWhiteSpace = currentPositionElement is PsiWhiteSpace
            val isParentStateElement = currentPositionElement.parent is TypeScriptClass
            return isWhiteSpace && isParentStateElement && lineNumber in (startLine + 1) until endLine
        }

        return false
    }

    fun getTypeFromStateAnnotation(project: Project, ngxsStatePsiFile: VirtualFile ): String? {
        val stateClassPsi = getStateClassElement(project, ngxsStatePsiFile)?.children?.firstOrNull()
        if (stateClassPsi !is JSAttributeList) return null
        val regex = Regex("<(.*?)>")
        val matchResult = regex.find(stateClassPsi.text)
        return matchResult?.groups?.get(1)?.value
    }

    fun getStateClassElement(project: Project, ngxsStatePsiFile: VirtualFile ): PsiElement? {
        return PsiManager.getInstance(project).findFile(ngxsStatePsiFile)
            ?.children?.firstOrNull {
                it.elementType is TypeScriptClassElementType
                        && it.children[0] is JSAttributeList
                        && it.text.contains("@State")
            }
    }

    fun getTypeInActionActionDecoratorElement(element: PsiElement, project: Project, stateVirtualFile: VirtualFile): PsiElement? {
        getStateClassElement(project, stateVirtualFile) ?: return null
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
}
