package com.github.dinbtechit.ngxs.action.cli

import com.github.dinbtechit.ngxs.NgxsIcons
import com.github.dinbtechit.ngxs.action.cli.models.ComputedCLIParameters
import com.github.dinbtechit.ngxs.action.cli.services.FileService
import com.github.dinbtechit.ngxs.action.cli.store.CLIActions
import com.github.dinbtechit.ngxs.action.cli.store.CLIState
import com.github.dinbtechit.ngxs.action.cli.store.GenerateCLIState
import com.github.dinbtechit.ngxs.action.cli.util.NgxsGeneratorFileUtil
import com.github.dinbtechit.ngxs.common.services.NgxsProject
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.ThreeState
import com.jetbrains.rd.util.first
import com.jetbrains.rd.util.firstOrNull
import org.reduxkotlin.Store
import java.nio.file.Files
import java.nio.file.Paths


class NgxsCliAction : DumbAwareAction(NgxsIcons.logo) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val ngxsStoreService = project.service<CLIState>()

        val store = ngxsStoreService.store
        store.dispatch(CLIActions.CheckIfNpmPackageInstalled(project))
        val ngxsProjectDetails = project.service<NgxsProject>().getNgxsProjectDetails(project)
        val dialog =
            if (ngxsProjectDetails.isValidNgxsProject && ngxsProjectDetails.isGreaterThanEqualTo18.isAtLeast(
                    ThreeState.YES
                )
            ) {
                GenerateCLIDialogV2(project, e)
            } else {
                GenerateCLIDialog(project, e)
            }
        val clickedOk = dialog.showAndGet()
        if (clickedOk) {
            ApplicationManager.getApplication().executeOnPooledThread {
                ApplicationManager.getApplication().runWriteAction {
                    runGenerator(
                        store.state.project!!, store.state,
                        store.state.workingDir, store.state.module!!
                    )
                    if (ngxsProjectDetails.isGreaterThanEqualTo18.isAtLeast(ThreeState.YES))
                        removeNBSPCharacter(project, store)
                }
            }
        }

    }

    override fun update(e: AnActionEvent) {
        // Display action only if it is a nest project.
        val project = e.project
        val isProjectOpen = project != null && !project.isDisposed
        var isFileExists = false
        var isValidPath = true

        if (isProjectOpen) {
            assert(project != null)
            val projectDirectory: VirtualFile = project!!.guessProjectDir()!!
            val filePath = Paths.get(projectDirectory.path, "angular.json")
            isFileExists = Files.exists(filePath)
        }

        val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val directory = when {
            virtualFile == null -> null
            virtualFile.isDirectory -> virtualFile // If it's directory, use it
            else -> virtualFile.parent // Otherwise, get its parent directory
        }
        if (directory != null) {
            isValidPath = NgxsGeneratorFileUtil.computeGeneratePath(e.project!!, directory) != null
        }

        e.presentation.isEnabledAndVisible = isProjectOpen && isFileExists && isValidPath
    }

    private fun runGenerator(
        project: Project,
        schematic: GenerateCLIState,
        workingDir: VirtualFile?,
        module: CompletionModuleInfo
    ) {
        val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
        if (interpreter == null || schematic.computedCLIParameters == null) {
            logger<NgxsCliAction>().error("A NPM Interpreter not found")
            return
        }

        //val modules: MutableList<CompletionModuleInfo> = mutableListOf()
        val cli: VirtualFile = project.guessProjectDir()!!
        // NodeModuleSearchUtil.findModulesWithName(modules, "@ngxs/cli", cli, interpreter)

        // val module = modules.firstOrNull() ?: return
        val parameters = schematic.parameter.trim().split(" ").toMutableList()
            .map { it.trim() }
            .filter { it != "" }.toMutableList()
        val ngxsProjectDetails = project.service<NgxsProject>().getNgxsProjectDetails(project)

        if (ngxsProjectDetails.isGreaterThanEqualTo18 == ThreeState.YES) {
            val modules: MutableList<CompletionModuleInfo> = mutableListOf()
            if (schematic.hasDefaultNameParameter) {
                parameters.add(0, "--name")
            }
            val hasPathParameter = parameters.filter { it.contains("path") }
            if (hasPathParameter.isEmpty()) {
                parameters.addAll(listOf("--path", schematic.computedCLIParameters.actualGeneratePath))
            }

            parameters.addAll(listOf("--project", schematic.computedCLIParameters.ngProjectName))

            NodeModuleSearchUtil.findModulesWithName(modules, "@angular/cli", cli, interpreter)
            NpmPackageProjectGenerator.generate(
                interpreter, NodePackage(modules.first().virtualFile!!.path),
                { pkg -> pkg.findBinFile("ng", null)?.absolutePath },
                cli, VfsUtilCore.virtualToIoFile(cli), project,
                null, JavaScriptBundle.message("generating.0", cli.name),
                arrayOf(), "generate", "@ngxs/store:${schematic.selectedSchematicType}",
                *parameters.toTypedArray()
            )
        } else {
            val npm = NodePackage(module.virtualFile?.path!!)
            NpmPackageProjectGenerator.generate(
                interpreter, npm,
                { pkg -> pkg.findBinFile("ngxs", null)?.absolutePath },
                cli, VfsUtilCore.virtualToIoFile(workingDir ?: cli), project,
                null, JavaScriptBundle.message("generating.0", cli.name),
                arrayOf(), *parameters.toTypedArray(),
            )
        }
    }

    private fun removeNBSPCharacter(project: Project, store: Store<GenerateCLIState>) {
        val connection = project.messageBus.connect()
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                val ngxsFileService = project.service<FileService>()
                for (event in events) {
                    if (event is VFileCreateEvent) {
                        if (store.store.state.workingDir != null) {
                            for (file in store.state.workingDir!!.children) {
                                if (file.isDirectory && store.state.folderName.isNotBlank()
                                    && file.name == store.state.folderName
                                ) {
                                    for (storeFiles in file.children) {
                                        ngxsFileService.replaceNbsp(storeFiles)
                                    }
                                }
                            }
                        }
                    }
                }
                connection.disconnect()
            }
        })
    }
}
