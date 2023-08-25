package com.github.dinbtechit.ngxs.action.cli

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.jetbrains.rd.util.first

class CLIOptionsCompletionProvider(private val items: List<String>) : TextFieldWithAutoCompletionListProvider<String>(items) {

    companion object {
        val options = mapOf(
                "--name" to "Store name",
                "--directory" to " By default, the prompt is set to the current directory",
                "--folder-name" to " Use your own folder name, default: state.",
                "--spec" to " Creates a spec file for store, default: true",

        )
    }

    override fun getLookupString(item: String): String {
        return item
    }

    override fun getItems(prefix: String?, cached: Boolean, parameters: CompletionParameters?): List<String> {
        return if (prefix == null) emptyList() else items.filter { it.contains(prefix, ignoreCase = true) }
    }

    override fun acceptChar(c: Char): CharFilter.Result? {
        return if (c == '-') CharFilter.Result.ADD_TO_PREFIX else null
    }

    override fun createLookupBuilder(item: String): LookupElementBuilder {
        val description = options.filter {
            it.key == item }.first().value
        return super.createLookupBuilder(item)
                .withTailText("  $description", true)
    }
}

