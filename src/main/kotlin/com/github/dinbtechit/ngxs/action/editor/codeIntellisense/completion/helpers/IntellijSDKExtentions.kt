package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers

import ai.grazie.utils.capitalize
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.LiveTemplateOptions
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.TextRange

fun InsertionContext.trimStartAtCurrentCaretPosition() {
    val lineStartOffset = document.getLineStartOffset(editor.caretModel.logicalPosition.line)
    val lineEndOffset = document.getLineEndOffset(editor.caretModel.logicalPosition.line)
    var text = document.getText(TextRange(lineStartOffset, lineEndOffset))
    text = text.replaceFirst(text.trim(), "")
    document.replaceString(
        lineStartOffset,
        lineEndOffset, text
    )
}

fun InsertionContext.getLiveTemplateOptions(lookupElement: LookupElement): LiveTemplateOptions? {
    val lineStartOffset = document.getLineStartOffset(editor.caretModel.logicalPosition.line)
    val lineEndOffset = document.getLineEndOffset(editor.caretModel.logicalPosition.line)
    var text = document.getText(TextRange(lineStartOffset, lineEndOffset))
    // getting rid of @
    text = text.replace("@", "")
    for (item in lookupElement.allLookupStrings) {
        text = text.replace(item, "")
    }
    val tokens = text.split("-").map { it.trim() }.filter { it.isNotBlank() }
    return when {
        tokens.size == 1 -> LiveTemplateOptions(
            tokens[0],
            editMode = false
        )

        tokens.size == 2 -> LiveTemplateOptions(
            tokens[0], tokens[1].capitalize(),
            editMode = false
        )

        tokens.size >= 3 -> LiveTemplateOptions(
            tokens[0], tokens[1].capitalize(), tokens[2],
            editMode = false
        )

        else -> null
    }
}


