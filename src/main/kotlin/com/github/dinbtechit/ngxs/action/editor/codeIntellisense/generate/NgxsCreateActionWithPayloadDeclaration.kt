package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.generate


import com.github.dinbtechit.ngxs.action.editor.psi.actions.NgxsActionsPsiFileFactory
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class NgxsCreateActionWithPayloadDeclaration : CodeInsightAction() {

    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        return  file.name.contains(Regex("(.*).action(s).ts"))
    }

    fun invoke(project: Project, editor: Editor, file: PsiFile) {
        NgxsActionsPsiFileFactory.createActionDeclarationFromActionFile(
            file, withPayload = true
        )
    }

    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                this@NgxsCreateActionWithPayloadDeclaration.invoke(project, editor, file)
            }

            override fun startInWriteAction(): Boolean {
                return true
            }
        }
    }


}
