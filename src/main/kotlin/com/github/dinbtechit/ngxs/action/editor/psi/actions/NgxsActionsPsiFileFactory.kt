package com.github.dinbtechit.ngxs.action.editor.psi.actions

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset

object NgxsActionsPsiFileFactory {

    fun createActionDeclarationFromActionFile(file: PsiFile, withPayload: Boolean = false,
                                              editMode: Boolean = true, addNewLine: Boolean = true) {
        val stateName = file.name.split(".")[0]
        createActionDeclaration(
            "NewAction", stateName, file.project, file.virtualFile,
            withPayload, editMode, editMode, addNewLine
        )
    }

    fun createActionDeclaration(className: String?, parameters: CompletionParameters, withPayload: Boolean = false) {
        if (className != null && parameters.editor.project != null) {
            val actionClassDeclaration = NgxsActionsPsiUtil.isActionDeclarationExist(className, parameters.editor.project!!)
            if (actionClassDeclaration == null) {
                createActionDeclarationFromStateFile(
                    parameters.originalFile,
                    className,
                    parameters.originalFile.project,
                    withPayload
                )
            }
        }
    }

    private fun createActionDeclarationFromStateFile(
        stateFile: PsiFile,
        actionClassName: String,
        project: Project,
        withPayload: Boolean = false,
        editMode: Boolean = false
    ) {
        val stateName = stateFile.name.split(".")[0]
        val computedActionFileName = "$stateName.actions.ts"
        val actionFile = stateFile.containingDirectory?.files?.firstOrNull {
            it.name == computedActionFileName
        }?.virtualFile ?: return
        createActionDeclaration(
            actionClassName,
            stateName,
            project,
            actionFile,
            withPayload,
            false,
            editMode
        )
    }

    fun createActionDeclarationFromStateFile(
        actionClassRef: PsiElement,
        withPayload: Boolean = false,
        editMode: Boolean = true
    ) {
        val stateName = actionClassRef.containingFile.name.split(".")[0]
        val computedActionFileName = "$stateName.actions.ts"
        val actionFile = actionClassRef.containingFile.containingDirectory?.files?.firstOrNull {
            it.name == computedActionFileName
        }?.virtualFile ?: return
        createActionDeclaration(
            actionClassRef.text,
            stateName,
            actionClassRef.project,
            actionFile,
            withPayload,
            false,
            editMode
        )
    }


    private fun createActionDeclaration(
        actionClassName: String,
        stateName: String,
        project: Project,
        actionFile: VirtualFile,
        withPayload: Boolean = true,
        editingClassName: Boolean = false,
        editMode: Boolean = true,
        addNewLine: Boolean = true,
    ) {
        val editorFactory = EditorFactory.getInstance()
        val document =
            FileDocumentManager.getInstance().getDocument(actionFile) ?: return
        val psiFile = PsiManager.getInstance(project).findFile(actionFile) ?: return
        val newEditor: Editor = if (editMode) {
            val editorManager = FileEditorManager.getInstance(project)
            editorManager.openFile(actionFile, true)
            (editorManager.getSelectedEditor(actionFile) as TextEditor).editor
        } else {
            editorFactory.createEditor(document, project)
        }

        if (addNewLine) {
            val elements = PsiTreeUtil.collectElements(psiFile) { true }
            val lastNonWhiteSpaceElement = elements.reversed().find {
                it != null && it.text.trim().isNotEmpty()
            }
            val lastLineNumber = document.getLineNumber(lastNonWhiteSpaceElement?.endOffset ?: 0)
            if ((lastLineNumber + 2) >= document.lineCount) {
                document.insertString(document.textLength, "\n\n")
            }
            val newOffset = document.getLineStartOffset(lastLineNumber + 2)
            newEditor.caretModel.moveToOffset(newOffset)
        }

        val templateManager = TemplateManager
            .getInstance(project)
        val template = createActionDeclaration(
            templateManager, actionClassName, stateName,
            withPayload = withPayload,
            editingClassName,
            editMode = editMode
        )
        templateManager.startTemplate(newEditor, template)

        if (!editMode) {
            editorFactory.releaseEditor(newEditor)
        }
    }

    private fun createActionDeclaration(
        templateManager: TemplateManager,
        actionClassName: String,
        stateName: String,
        withPayload: Boolean,
        editingClassName: Boolean = false,
        editMode: Boolean,
    ): Template {
        val template = templateManager.createTemplate(
            "ngxs-action-declaration", "Ngxs",
            """
            export class ${"$"}actionName${"$"} {
              static readonly type = '[$stateName] ${"$"}actionType${"$"}';
              ${
                if (withPayload)
                    """
              constructor(public ${"$"}payloadName${"$"}: ${"$"}payloadType${"$"}) {
              }
              """.trimStart()
                else ""
            }    
            }
            """.trimIndent()
        )

        val defaultActionName = ConstantNode(actionClassName)
        val defaultActionType = ConstantNode(actionClassName)

        template.addVariable("actionName", defaultActionName, defaultActionName, editingClassName)
        template.addVariable("actionType", defaultActionType, defaultActionName, editMode)

        if (withPayload) {
            val defaultPayloadName = ConstantNode("payload")
            template.addVariable("payloadName", defaultPayloadName, defaultPayloadName, editMode)
            val defaultPayloadType = ConstantNode("unknown")
            template.addVariable("payloadType", defaultPayloadType, defaultPayloadType, editMode)
        }
        return template
    }
}
