package com.github.dinbtechit.ngxs.action.cli.store

import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class Action {
    data class GenerateCLIAction(
        val options: String,
        val filePath: String,
        val project: Project,
        val workingDir: VirtualFile,
        val module: CompletionModuleInfo
    )
    data class UpdateParameter(
        val name: String,
        val options: String
    )
    data class CheckIfNpmPackageInstalled(
        val project: Project
    )
}
