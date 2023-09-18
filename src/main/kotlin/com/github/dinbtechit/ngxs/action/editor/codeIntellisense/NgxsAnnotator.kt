package com.github.dinbtechit.ngxs.action.editor.codeIntellisense

import com.github.dinbtechit.ngxs.action.editor.NgxsActionUtil
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.types.TypeScriptClassElementType
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset


class NgxsAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        var isImplementationExist = true
        var problemType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        var range = TextRange(element.textRange.startOffset, element.textRange.endOffset)
        var actionPsiClass: PsiElement? = null
        var actionName: String? = null
        var actionFileName: String? = null
        var actionVirtualFile: VirtualFile? = null

        if (NgxsActionUtil.isActionClass(element)) {
            isImplementationExist = NgxsActionUtil.isActionImplExist(element)
            problemType = ProblemHighlightType.LIKE_UNUSED_SYMBOL
            try {
                val classNamePsiElement = NgxsActionUtil.getActionClassPsiElement(element)
                if (classNamePsiElement != null) {
                    range = TextRange(classNamePsiElement.startOffset, classNamePsiElement.endOffset)
                    actionPsiClass = classNamePsiElement
                    actionName = classNamePsiElement.text
                    actionFileName = classNamePsiElement.containingFile.name
                    actionVirtualFile = classNamePsiElement.containingFile.containingDirectory.virtualFile
                }
            } catch (e: Exception) {
                throw Exception("NgxsAnnotator - Unable to establish range for the ActionClass - ${element.text}")
            }

        } else if (NgxsActionUtil.isActionDispatched(element)) {
            isImplementationExist = NgxsActionUtil.isActionImplExist(element)
            val refElement = element.parent.reference?.resolve()
            if (refElement != null) {
                val classNamePsiElement =  NgxsActionUtil.getActionClassPsiElement(refElement)
                if (classNamePsiElement != null) {
                    actionName = classNamePsiElement.text
                    actionPsiClass = classNamePsiElement
                    actionFileName = refElement.containingFile.name
                    actionVirtualFile = classNamePsiElement.containingFile.containingDirectory.virtualFile
                }
            }
        }

        if (!isImplementationExist ) {
            val stateFileName = if (actionFileName != null)
                "${actionFileName.split(".")[0]}.state.ts"
            else "*.state.ts"
            val stateFile = LocalFileSystem.getInstance().findFileByPath("${actionVirtualFile?.path}/$stateFileName")
            if (stateFile != null) {
                val stateClassPsi = PsiManager.getInstance(element.project).findFile(stateFile)?.children?.firstOrNull {
                    it.elementType is TypeScriptClassElementType
                            && it.children[0] is JSAttributeList
                            && it.text.contains("@State")
                }
                if (stateClassPsi != null) {
                    if (stateClassPsi.node.lastChildNode.text == "}") {
                        stateClassPsi.node.lastChildNode
                    }

                }
            }

            if (actionName == null) actionName = element.text
            holder.newAnnotation(HighlightSeverity.WARNING, "@Action(${actionName}) not found in $stateFileName")
                .range(range)
                .highlightType(problemType)
                .withFix(
                    NgxsCreateActionQuickFix("Create @Action(${actionName}) in $stateFileName.",
                    actionPsiClass!!, stateFile))
                .create()
        }

    }
}

