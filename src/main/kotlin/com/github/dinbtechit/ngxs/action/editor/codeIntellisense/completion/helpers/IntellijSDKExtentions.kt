package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers

import ai.grazie.utils.capitalize
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.LiveTemplateOptions
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.diagnostic.thisLogger
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
    val text = document.getText(TextRange(lineStartOffset, lineEndOffset))
    return getLiveTemplateOptions(text, lookupElement.allLookupStrings)
}

fun getLiveTemplateOptions(text: String, lookupElements: MutableSet<String>): LiveTemplateOptions? {

    fun String.containsParameter(): Boolean {
       return this.contains(":") && (!this.contains(",") || this.contains(","))
    }

    // getting rid of @
    var newText = text.replace("@", "")
    for (item in lookupElements) {
        newText = newText.replace(item, "")
    }
    val tokens = newText.split("-").map { it.trim() }.filter { it.isNotBlank() }
    return when {
        tokens.size == 1 -> LiveTemplateOptions(
            tokens[0],
            tokens[0].capitalize(),
            editMode = false
        )

        tokens.size == 2 -> LiveTemplateOptions(
            tokens[0],
            if (tokens[1].containsParameter()) tokens[0].capitalize() else tokens[1].capitalize(),
            if (tokens[1].containsParameter()) tokens[1].toMap() else null,
            editMode = false
        )

        tokens.size >= 3 -> LiveTemplateOptions(
            tokens[0],
            tokens[1].capitalize(),
            tokens[2].toMap(),
            editMode = false
        )

        else -> null
    }
}



fun String.toMap(): MutableMap<String, String>? {
    val parameterList = this.split(",")
    val parameterMap = mutableMapOf<String, String>()

    for (parameter in parameterList) {
        val parts = parameter.split(":")
        if (parts.size == 2) {
            val paramName = parts[0].trim()
            val paramType = parts[1].trim()
            parameterMap[paramName] = paramType
        } else {
            thisLogger().warn("Invalid parameter format: $parameter")
            continue
        }
    }
    return parameterMap.ifEmpty { null }

}
