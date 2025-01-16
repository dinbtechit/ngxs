package com.github.dinbtechit.ngxs.action.cli.store

import com.github.dinbtechit.ngxs.NgxsBundle
import com.github.dinbtechit.ngxs.action.cli.models.ComputedCLIParameters
import com.github.dinbtechit.ngxs.action.cli.util.CliParameterUtil.convertToCli
import com.github.dinbtechit.ngxs.action.cli.util.NgxsGeneratorFileUtil
import com.github.dinbtechit.ngxs.common.services.NgxsProject
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ThreeState
import com.jetbrains.rd.util.firstOrNull
import org.reduxkotlin.Reducer
import org.reduxkotlin.threadsafe.createThreadSafeStore

@Service(Service.Level.PROJECT)
class CLIState(project: Project) {


    init {
        thisLogger().info(NgxsBundle.message("projectService", project.name))
    }

    val project = project
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
                    val angularJson = project.service<NgxsProject>().getNgProject()
                    var angularProjectName = ""
                    var generatePath = NgxsGeneratorFileUtil.getRelativePath(project, action.workingDir!!)
                    var angularProjectType = ""
                    if (angularJson != null) {
                        val angularRootProject = angularJson.projects
                            .filter { it.value.root == "" && it.value.sourceRoot == "src" }.firstOrNull()
                        val angularProject = angularJson.projects
                            .filter { it.value.root != "" && it.value.sourceRoot != "src" }
                            .filter {
                                NgxsGeneratorFileUtil.getRelativePath(project, action.workingDir)
                                    .contains(it.value.root)
                            }.firstOrNull()
                        if (angularProject != null) {
                            angularProjectName = angularProject.key
                            angularProjectType = angularProject.value.projectType.substring(
                                0,
                                3
                            )
                            generatePath = generatePath.replace(angularProject.value.root, "")
                                .replace(
                                    "src/${angularProjectType}", ""
                                ).replace("//", "/")
                            if (generatePath.isNotBlank() && generatePath.startsWith("/")) {
                                generatePath = generatePath.substring(1, generatePath.length)
                            }
                            thisLogger().info(
                                "Action.LoadTypesAction - $action - $angularProjectName - $angularProjectType - $generatePath"
                            )
                        } else if (angularRootProject != null) {
                            angularProjectName = angularRootProject.key
                            angularProjectType = angularRootProject.value.projectType.substring(
                                0,
                                3
                            )
                            generatePath = generatePath.replace(angularRootProject.value.root, "")
                                .replace(
                                    "src/${angularProjectType}", ""
                                )
                                .replace("//", "/")
                        } else {

                        }
                    }
                    state.copy(
                        types = action.cliTypeOptions,
                        angularProjectName = angularProjectName,
                        angularProjectType = angularProjectType,
                        generatePath = generatePath
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

                is CLIActions.UpdateComputedCLIParameters -> {
                    thisLogger().info("Action.UpdateComputedCLIParameters - $action")
                    state.copy(
                        computedCLIParameters = action.computedCLIParameters
                    )
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
                        val ngxsProjectDetails =
                            action.project.service<NgxsProject>().getNgxsProjectDetails(action.project)
                        if (ngxsProjectDetails.isValidNgxsProject && ngxsProjectDetails.isGreaterThanEqualTo18.isAtLeast(
                                ThreeState.YES
                            )
                        ) {
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
