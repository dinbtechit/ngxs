package com.github.dinbtechit.ngxs.action.editor.codeIntellisense

import com.github.dinbtechit.ngxs.action.editor.NgxsStatePsiFile
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class NgxsCreateActionQuickFix(private val key: String,
                               private val actionPsiElement: PsiElement,
                               private val ngxsStateVirtualFile: VirtualFile?) : BaseIntentionAction() {

    override fun getText(): String {
        return key
    }


    override fun getFamilyName(): String {
        return "Create action"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return ngxsStateVirtualFile != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val actionFunction = NgxsStatePsiFile(ngxsStateVirtualFile!!, project).createActionMethod(actionPsiElement)
        val fileEditorManager = FileEditorManager.getInstance(project)
        val textEditor = fileEditorManager.openTextEditor(
            OpenFileDescriptor(
                project,
                ngxsStateVirtualFile
            ), true
        )

        val start = actionFunction?.textRange?.startOffset ?: 0
        textEditor?.caretModel?.moveToOffset(start)
        textEditor?.scrollingModel?.scrollToCaret(ScrollType.CENTER)

    }
}
