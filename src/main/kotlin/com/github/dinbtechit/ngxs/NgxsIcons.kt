package com.github.dinbtechit.ngxs

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader

object NgxsIcons {
    @JvmField
    val logo = IconLoader.getIcon("icons/ngxs.svg", javaClass)
    @JvmField
    val Donate = IconLoader.getIcon("icons/donate.svg", javaClass)
    @JvmField
    val GitHub = AllIcons.Vcs.Vendors.Github

    object Gutter {
        @JvmField
        val Action = IconLoader.getIcon("icons/ngxs-action.svg", javaClass)
        val MultipleActions = IconLoader.getIcon("icons/ngxs-multiple-action.svg", javaClass)
    }
    object Editor {
        @JvmField
        //val Completion = IconLoader.getIcon("icons/ngxs.svg", javaClass)
        val Completion = IconLoader.getIcon("icons/ngxs-completion-icon.svg", javaClass)
    }
}
