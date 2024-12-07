package com.github.dinbtechit.ngxs.action.cli

import com.github.dinbtechit.ngxs.action.cli.store.CLIState
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.jetbrains.rd.util.first

/**
 * This completion Provider would be used for version 18 and Above
 */
class CLIOptionsCompletionProviderV2(private val project: Project, private val items: List<String>) : TextFieldWithAutoCompletionListProvider<String>(items) {

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
        val state = project.service<CLIState>().store.state
        val schematic = state.selectedSchematicParameters.filter {
            it.key == item
        }.first().value
        val typeText = if (schematic.default.toString().isBlank() || "${schematic.default}" == "null")
            "[${schematic.type}]"
        else "[${schematic.type}][default: ${schematic.default}]"
        val schematicDescription = if ("${schematic.description}" != "null") "${schematic.description}"
        else ""
        return super.createLookupBuilder(item)
            .withTailText("  $schematicDescription", true)
            .withTypeText(typeText)
    }
}

