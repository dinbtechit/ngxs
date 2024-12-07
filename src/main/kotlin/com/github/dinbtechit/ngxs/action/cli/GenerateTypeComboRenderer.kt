package com.github.dinbtechit.ngxs.action.cli

import com.github.dinbtechit.ngxs.action.cli.store.CLIState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.jetbrains.rd.util.first
import javax.swing.JList

class GenerateTypeComboRenderer(val project: Project): ColoredListCellRenderer<String>() {
    private val cliService = project.service<CLIState>()
    override fun customizeCellRenderer(
        list: JList<out String>,
        value: String?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        if (value != null) {
            val desc = cliService.store.state.types.
            filter { it.key == value }.first().value
            append(value)
            append("    ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
            append(desc.description ?: "", SimpleTextAttributes.GRAY_ATTRIBUTES)
            setSize(list.width, Int.MAX_VALUE)
        }
    }
}
