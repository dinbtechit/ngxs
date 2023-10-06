package com.github.dinbtechit.ngxs.action.editor.psi.actions

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.psi.PsiElement

object NgxsActionsPsiFileFactory {
    fun createActionDeclaration(
        actionClassRef: PsiElement,
        withPayload: Boolean = true,
        editMode: Boolean = true
    ) {
        val stateName = actionClassRef.containingFile.name.split(".")[0]
        val computedActionFileName = "$stateName.actions.ts"
        val actionFile = actionClassRef.containingFile.containingDirectory?.files?.firstOrNull {
            it.name == computedActionFileName }?.virtualFile ?: return

        val editorFactory = EditorFactory.getInstance()
        val document =
            FileDocumentManager.getInstance().getDocument(actionFile) ?: return
        document.insertString(document.textLength, "\n\n")

        val newEditor: Editor = if (editMode) {
            val editorManager = FileEditorManager.getInstance(actionClassRef.project)
            editorManager.openFile(actionFile, true)
            (editorManager.getSelectedEditor(actionFile) as TextEditor).editor
        } else {
            editorFactory.createEditor(document, actionClassRef.project)
        }

        newEditor.caretModel.moveToOffset(document.textLength)

        val template = createActionDeclaration(actionClassRef, stateName, withPayload, editMode)
        val templateManager = TemplateManager
            .getInstance(actionClassRef.project)
        templateManager.startTemplate(newEditor, template)

        if(!editMode) {
            editorFactory.releaseEditor(newEditor)
        }
    }

    private fun createActionDeclaration(
        actionClassRef: PsiElement,
        stateName: String,
        withPayload: Boolean,
        editMode: Boolean,
    ): Template {
        val templateManager = TemplateManager.getInstance(actionClassRef.project)
        val template = templateManager.createTemplate(
            "ngxs-action-declaration", "Ngxs",
            """
            export class ${actionClassRef.text} {
              static readonly type = '[$stateName] ${"$"}actionType${"$"}';
              ${if (withPayload)
                """
              constructor(public ${"$"}payloadName${"$"}: ${"$"}payloadType${"$"}) {
              }
              """.trimStart()
            else ""}    
            }
            """.trimIndent()
        )

        val defaultActionType = ConstantNode(actionClassRef.text)
        template.addVariable("actionType", defaultActionType, defaultActionType, editMode)
        if (withPayload) {
            val defaultPayloadName = ConstantNode("payload")
            template.addVariable("payloadName", defaultPayloadName, defaultPayloadName, editMode)
            val defaultPayloadType = ConstantNode("unknown")
            template.addVariable("payloadType", defaultPayloadType, defaultPayloadType, editMode)
        }
        return template
    }
}
