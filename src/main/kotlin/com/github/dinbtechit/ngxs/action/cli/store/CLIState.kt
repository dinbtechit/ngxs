package com.github.dinbtechit.ngxs.action.cli.store

import com.github.dinbtechit.ngxs.NgxsBundle
import com.github.dinbtechit.ngxs.action.cli.util.CliParameterUtil.convertToCli
import com.github.dinbtechit.ngxs.common.services.NgxsProject
import com.github.dinbtechit.ngxs.common.services.VersionCheck
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import org.reduxkotlin.Reducer
import org.reduxkotlin.threadsafe.createThreadSafeStore

@Service(Service.Level.PROJECT)
class CLIState(project: Project) {

    init {
        thisLogger().info(NgxsBundle.message("projectService", project.name))
    }

    val store = createThreadSafeStore(reducer(), GenerateCLIState())

    private fun reducer(): Reducer<GenerateCLIState> {
        return { state, action ->
            when (action) {
                is CLIActions.GenerateCLIAction -> {
                    thisLogger().info("Action.GenerateCLIAction - $action")
                    state.copy(
                        parameter = action.options,
                        folderName = action.options.convertToCli()["folder-name"] ?: "",
                        workingDir = action.workingDir,
                        project = action.project,
                    )
                }
                is CLIActions.LoadTypesAction -> {
                    thisLogger().info("Action.LoadTypesAction - $action")
                    state.copy(
                        types = action.cliTypeOptions,
                    )
                }
                is CLIActions.SelectSchematicType -> {
                    thisLogger().info("Action.SelectSchematicType - $action")
                    with(action) {
                        state.copy(
                            selectedSchematicType = selectedSchematicType,
                            hasDefaultNameParameter = hasDefaultNameParameter,
                            selectedSchematicParameters = selectedSchematicParameters
                        )
                    }
                }

                is CLIActions.UpdateParameter -> {
                    thisLogger().info("Action.UpdateParameter - $action")
                    state.copy(
                        name = action.name,
                        parameter = action.options
                    )
                }

                is CLIActions.CheckIfNpmPackageInstalled -> {
                    thisLogger().info("Action.CheckIfNpmPackageInstalled - $action")
                    val interpreter = NodeJsInterpreterManager.getInstance(action.project).interpreter
                    var patchState = state
                    if (interpreter != null) {
                        val modules: MutableList<CompletionModuleInfo> = mutableListOf()
                        val cli: VirtualFile = action.project.guessProjectDir()!!
                        val ngxsProjectDetails = action.project.service<NgxsProject>().getNgxsProjectDetails(action.project)
                        if (ngxsProjectDetails.isValidNgxsProject && ngxsProjectDetails.isGreaterThanEqualTo18.isAtLeast(VersionCheck.YES)) {
                            NodeModuleSearchUtil.findModulesWithName(modules, "@ngxs/store", cli, interpreter)
                        } else {
                            NodeModuleSearchUtil.findModulesWithName(modules, "@ngxs/cli", cli, interpreter)
                        }
                        val module = modules.firstOrNull()
                        patchState = state.copy(
                            module = module
                        )
                    }
                    patchState
                }

                else -> {
                    thisLogger().info("No Action found default state - $action")
                    state
                }
            }
        }
    }

}
