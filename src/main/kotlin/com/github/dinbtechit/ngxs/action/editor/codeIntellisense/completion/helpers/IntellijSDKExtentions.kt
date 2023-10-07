package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers

import com.intellij.codeInsight.completion.InsertionContext
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
