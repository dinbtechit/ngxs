package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.intention

import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsActionType
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiFileFactory
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
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
        return "Create action with payload"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.virtualFile == null || editor == null) return false
        return  NgxsStatePsiUtil.isCursorWithinStateClass(editor, file)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (file?.virtualFile == null || editor == null) return
        NgxsStatePsiFileFactory.createActionMethodLiveTemplates(
            editor, file, NgxsActionType.WITH_PAYLOAD, null
        )
    }

}
