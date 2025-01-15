package com.github.dinbtechit.ngxs.action.cli.store

import com.github.dinbtechit.ngxs.common.models.SchematicInfo
import com.github.dinbtechit.ngxs.common.models.SchematicParameters
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class CLIActions {
    data class LoadTypesAction(
        val type: String = "[CLI] load type",
        val cliTypeOptions: Map<String, SchematicInfo>,
        val workingDir: VirtualFile?,
    )
    data class GenerateCLIAction(
        val options: String,
        val filePath: String,
        val project: Project,
        val workingDir: VirtualFile,
        val module: CompletionModuleInfo
    )
    data class SelectSchematicType (
        val type: String = "[CLI] select schematic type",
        val selectedSchematicType: String,
        val hasDefaultNameParameter: Boolean,
        val selectedSchematicParameters: Map<String, SchematicParameters>
    )

    data class UpdateParameter(
        val name: String,
        val options: String
    )
    data class CheckIfNpmPackageInstalled(
        val project: Project
    )
}
