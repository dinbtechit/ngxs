package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.generate


import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsActionType
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiFileFactory
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class NgxsCreateAction : CodeInsightAction() {

    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        return  NgxsStatePsiUtil.isCursorWithinStateClass(editor, file)
    }

    fun invoke(project: Project, editor: Editor, file: PsiFile) {
        NgxsStatePsiFileFactory.createActionMethodLiveTemplates(
            editor, file, NgxsActionType.WITHOUT_PAYLOAD
        )
    }

    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                this@NgxsCreateAction.invoke(project, editor, file)
            }

            override fun startInWriteAction(): Boolean {
                return true
            }
        }
    }


}
