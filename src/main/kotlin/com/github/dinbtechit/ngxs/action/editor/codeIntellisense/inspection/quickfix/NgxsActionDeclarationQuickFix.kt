package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.inspection.quickfix

import com.github.dinbtechit.ngxs.action.editor.NgxsActionUtil
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class NgxsActionDeclarationQuickFix(
    private val actionsFileName: String,
    actionTypeRef: PsiElement
) : LocalQuickFixOnPsiElement(actionTypeRef) {

    override fun getFamilyName(): String {
        return "Ngxs"
    }

    override fun getText(): String {
        return "Create Action class '${this.startElement.text}' in $actionsFileName"
    }

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        NgxsActionUtil.createActionDeclaration(startElement)
    }

}
