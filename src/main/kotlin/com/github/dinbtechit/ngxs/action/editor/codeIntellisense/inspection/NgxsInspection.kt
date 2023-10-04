package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.inspection

import com.github.dinbtechit.ngxs.action.editor.NgxsActionUtil
import com.github.dinbtechit.ngxs.action.editor.NgxsStatePsiFile
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElementVisitor

class NgxsActionDecoratorVisitor(private val holder: ProblemsHolder): JSElementVisitor() {
    override fun visitES6Decorator(decorator: ES6Decorator?) {
        if (decorator != null) {
            isActionDeclarationExist(decorator, holder)
        }
    }
    private fun isActionDeclarationExist(element: ES6Decorator, holder: ProblemsHolder) {
        val project = element.project
        val stateVirtualFile = element.containingFile.virtualFile
        val isNgxsStateFile = NgxsStatePsiFile.isNgxsStateFile(project, stateVirtualFile)

        if (isNgxsStateFile) {
            for (el in element.children) {
                val actionTypePsiElement = NgxsStatePsiFile(stateVirtualFile, project).getTypeInActionActionDecoratorElement(el)
                if (actionTypePsiElement != null && NgxsActionUtil.findActionDeclaration(actionTypePsiElement) == null) {
                    val stateFileName = element.containingFile.name
                    val computedActionFileName = "${stateFileName.split(".")[0]}.actions.ts"
                    val actionsFile = LocalFileSystem.getInstance().findFileByPath(
                        "${element.containingFile.containingDirectory.virtualFile.path}/$computedActionFileName"
                    )
                    holder.registerProblem(
                        actionTypePsiElement,
                        "Cannot find Action class '${actionTypePsiElement.text}'",
                        ProblemHighlightType.ERROR,
                        NgxsActionDeclarationQuickFix(computedActionFileName, actionsFile, actionTypePsiElement)
                    )

                }
            }
        }
    }

}

class NgxsInspection : JSInspection() {
    override fun createVisitor(holder: ProblemsHolder, p1: LocalInspectionToolSession): PsiElementVisitor {
        if (!NgxsStatePsiFile.isNgxsStateFile(holder.project, holder.file.virtualFile))
            return PsiElementVisitor.EMPTY_VISITOR
        return NgxsActionDecoratorVisitor(holder)
    }


}
