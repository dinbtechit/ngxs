package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.inspection

import com.github.dinbtechit.ngxs.action.editor.NgxsActionUtil
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

class NgxsActionDeclarationQuickFix(
    private val actionsFileName: String,
    private val actionFile: VirtualFile?,
    private val actionTypeRef: PsiElement
) : LocalQuickFix {
    
    override fun getName(): String {
        return "Fix: Create Action class '${actionTypeRef.text} in $actionsFileName'"
    }

    override fun getFamilyName(): String {
        return "Ngxs"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (actionFile == null) return
        NgxsActionUtil.createActionDeclaration(null, actionFile, actionTypeRef )
    }
}
