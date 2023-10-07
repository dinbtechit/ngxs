package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers

import com.github.dinbtechit.ngxs.NgxsIcons
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.NgxsLiveTemplates
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers.trimStartAtCurrentCaretPosition
import com.github.dinbtechit.ngxs.action.editor.psi.actions.NgxsActionsPsiFileFactory
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.util.ProcessingContext

class NgxsActionsFileCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val position = parameters.position
        val file = position.containingFile
        if (file.name.contains(".actions.ts")) {
            addCompletions(parameters, resultSet)
        }
    }

    private fun addCompletions(parameters: CompletionParameters, resultSet: CompletionResultSet) {
        getResultSets(parameters).forEach {
            resultSet.addElement(
                LookupElementBuilder.create(it.value.presentableText.replaceTextExist(parameters.editor))
                    .withIcon(NgxsIcons.Editor.Completion)
                    .withPresentableText(it.value.presentableText.replaceTextExist(parameters.editor))
                    .withTailText(it.value.tailText)
                    .withTypeText("NGXS")
                    .withLookupStrings(it.value.lookUpStrings)
                    .withCaseSensitivity(false)
                    .withInsertHandler { context, item ->
                        context.trimStartAtCurrentCaretPosition()
                        it.value.generateTemplate()
                    }
                    .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
            )
        }
    }

    private fun getResultSets(parameters: CompletionParameters): Map<String, NgxsLiveTemplates> {
        return mapOf(
            // 1. CreateAction
            "createAction" to NgxsLiveTemplates(
                presentableText = "export class NewAction",
                tailText = "(){...}",
                lookUpStrings = listOf(
                    "export class NewAction",
                    "action-definition",
                    "ngxs-action-definition"
                ),
                generateTemplate = {
                    NgxsActionsPsiFileFactory.createActionDeclarationFromActionFile(
                        parameters.originalFile, addNewLine = false
                    )
                }
            ),
            // 2. CreateActionWithPayload
            "createActionWithPayload" to NgxsLiveTemplates(
                presentableText = "export class NewAction-with-payload",
                tailText = "(){...}",
                lookUpStrings = listOf(
                    "export class NewAction",
                    "action-definition",
                    "ngxs-action-definition-with-payload"
                ),
                generateTemplate = {
                    NgxsActionsPsiFileFactory.createActionDeclarationFromActionFile(
                        parameters.originalFile, withPayload = true, addNewLine = false
                    )
                }
            ),
        )
    }

    private fun String.replaceTextExist(editor:Editor): String {
        val lineStartOffset = editor.document.getLineStartOffset(editor.caretModel.logicalPosition.line)
        val lineEndOffset = editor.document.getLineEndOffset(editor.caretModel.logicalPosition.line)
        val text = editor.document.getText(TextRange(lineStartOffset, lineEndOffset))
        val tokens = text.split(" ")
        var newText = this
        for(token in tokens) {
            newText = newText.replace("\\b$token\\b".toRegex(), "")
        }
        return newText.trim()
    }

}



