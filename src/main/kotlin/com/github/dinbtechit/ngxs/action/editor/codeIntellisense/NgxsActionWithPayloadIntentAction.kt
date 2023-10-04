package com.github.dinbtechit.ngxs.action.editor.codeIntellisense

import com.github.dinbtechit.ngxs.action.editor.NgxsActionType
import com.github.dinbtechit.ngxs.action.editor.NgxsStatePsiFile
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class NgxsActionWithPayloadIntentAction : BaseIntentionAction() {

    override fun startInWriteAction(): Boolean = true

    override fun getText(): String {
        return "Create new @Action with payload"
    }

    override fun getFamilyName(): String {
        return "Ngxs actions with payload"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.virtualFile == null || editor == null) return false
        return  NgxsStatePsiFile(file.virtualFile, project).isCursorWithinStateClass(editor, file)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (file?.virtualFile == null || editor == null) return
        NgxsStatePsiFile(file.virtualFile, project).createActionMethodLiveTemplates(
            editor, file, NgxsActionType.WITH_PAYLOAD
        )
    }

}
