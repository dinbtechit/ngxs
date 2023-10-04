package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.intention

import com.github.dinbtechit.ngxs.action.editor.NgxsActionType
import com.github.dinbtechit.ngxs.action.editor.NgxsStatePsiFile
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class NgxsActionIntentionAction : BaseIntentionAction() {

    override fun startInWriteAction(): Boolean = true

    override fun getText(): String {
        return "Create new @Action"
    }

    override fun getFamilyName(): String {
        return "Ngxs actions without payload"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.virtualFile == null || editor == null) return false
        return  NgxsStatePsiFile(file.virtualFile, project).isCursorWithinStateClass(editor, file)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (file?.virtualFile == null || editor == null) return
        NgxsStatePsiFile(file.virtualFile, project).createActionMethodLiveTemplates(
            editor, file, NgxsActionType.WITHOUT_PAYLOAD
        )
    }

}
