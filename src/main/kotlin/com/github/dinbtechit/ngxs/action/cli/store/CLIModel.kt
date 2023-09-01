package com.github.dinbtechit.ngxs.action.cli.store

import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class GenerateCLIState(
    val name: String = "",
    val folderName: String = "",
    val parameter: String = "",
    val project: Project? = null,
    val workingDir: VirtualFile? = null,
    val module: CompletionModuleInfo? = null
)
