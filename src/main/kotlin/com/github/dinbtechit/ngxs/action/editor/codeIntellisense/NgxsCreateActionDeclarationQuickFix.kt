package com.github.dinbtechit.ngxs.action.editor.codeIntellisense

import com.github.dinbtechit.ngxs.action.editor.NgxsActionUtil
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class NgxsCreateActionDeclarationQuickFix(
    private val actionsFileName: String,
    private val actionFile: VirtualFile?,
    private val actionTypeRef: PsiElement
) : BaseIntentionAction(){
    override fun getText(): String {
        return "Fix: Create Action class '${actionTypeRef.text} in $actionsFileName'"
    }
    override fun getFamilyName(): String {
        return "Ngxs_Create_Action_Declaration"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return actionFile != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || actionFile == null) return
        NgxsActionUtil.createActionDeclaration(editor, actionFile, actionTypeRef )
    }

}
