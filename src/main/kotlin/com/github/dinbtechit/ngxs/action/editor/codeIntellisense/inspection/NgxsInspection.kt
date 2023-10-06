package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.inspection

import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.inspection.quickfix.NgxsActionDeclarationQuickFix
import com.github.dinbtechit.ngxs.action.editor.psi.actions.NgxsActionsPsiUtil
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSParameterList
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterListImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class NgxsActionDecoratorVisitor(private val holder: ProblemsHolder): JSElementVisitor() {
    override fun visitES6Decorator(decorator: ES6Decorator?) {
        if (decorator != null) {
            checkIfActionTypeDeclarationExist(decorator, holder)
        }
    }

    override fun visitJSParameterList(node: JSParameterList?) {
        if(node == null || node.children.isEmpty()) return
        if (node.children.size < 2) return
        val actionTypePsiElement = PsiTreeUtil.getChildrenOfType(node.children.lastOrNull(), TypeScriptSingleType::class.java)?.firstOrNull()
        registerProblemWithQuickFix(actionTypePsiElement, holder, withPayload = true)
    }

    private fun checkIfActionTypeDeclarationExist(element: ES6Decorator, holder: ProblemsHolder) {
        val project = element.project
        val stateVirtualFile = element.containingFile.virtualFile
        val isNgxsStateFile = NgxsStatePsiUtil.isNgxsStateFile(project, stateVirtualFile)

        if (isNgxsStateFile) {
            for (el in element.children) {
                val actionTypePsiElement = NgxsStatePsiUtil.getTypeInActionActionDecoratorElement(el, el.project, stateVirtualFile)
                val parameters = PsiTreeUtil.getChildrenOfType(element.owner, TypeScriptParameterListImpl::class.java)?.firstOrNull()
                val withPayload  = if (parameters == null) false else parameters.children.size >= 2
                registerProblemWithQuickFix(actionTypePsiElement, holder, withPayload)
            }
        }
    }

    private fun registerProblemWithQuickFix(actionTypePsiElement: PsiElement?, holder: ProblemsHolder, withPayload: Boolean = false) {
        if (actionTypePsiElement != null && NgxsActionsPsiUtil.findActionDeclaration(actionTypePsiElement) == null) {
            val stateFileName = actionTypePsiElement.containingFile.name
            val computedActionFileName = "${stateFileName.split(".")[0]}.actions.ts"
            holder.registerProblem(
                actionTypePsiElement,
                "NGXS: Cannot find Action class '${actionTypePsiElement.text}'",
                ProblemHighlightType.ERROR,
                NgxsActionDeclarationQuickFix(computedActionFileName, actionTypePsiElement, withPayload)
            )

        }
    }

}

class NgxsInspection : JSInspection() {

    override fun getShortName(): String {
        return "NgxsInspection"
    }

    override fun createVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        if (!NgxsStatePsiUtil.isNgxsStateFile(holder.project, holder.file.virtualFile))
            return PsiElementVisitor.EMPTY_VISITOR
        return NgxsActionDecoratorVisitor(holder)
    }
}
