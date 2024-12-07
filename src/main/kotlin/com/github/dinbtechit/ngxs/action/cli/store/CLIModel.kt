package com.github.dinbtechit.ngxs.action.cli.store

import com.github.dinbtechit.ngxs.common.models.SchematicInfo
import com.github.dinbtechit.ngxs.common.models.SchematicParameters
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class GenerateCLIState(
    val name: String = "",
    val folderName: String = "",
    val types: Map<String, SchematicInfo> = mapOf(),
    val selectedSchematicType: String = "",
    val selectedSchematicParameters: Map<String, SchematicParameters> = mapOf(),
    val hasDefaultNameParameter: Boolean = true,
    val parameter: String = "",
    val project: Project? = null,
    val workingDir: VirtualFile? = null,
    val module: CompletionModuleInfo? = null
)
