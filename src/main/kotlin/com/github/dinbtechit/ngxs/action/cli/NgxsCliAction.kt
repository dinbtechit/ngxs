package com.github.dinbtechit.ngxs.action.cli

import com.github.dinbtechit.ngxs.NgxsIcons
import com.github.dinbtechit.ngxs.action.cli.store.Action
import com.github.dinbtechit.ngxs.action.cli.store.CLIState
import com.github.dinbtechit.ngxs.action.cli.store.GenerateCLIState
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Files
import java.nio.file.Paths


class NgxsCliAction : DumbAwareAction(NgxsIcons.logo) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val ngxsStoreService = project?.service<CLIState>()
        if (ngxsStoreService != null) {
            val store = ngxsStoreService.store
            store.dispatch(Action.CheckIfNpmPackageInstalled(project))
            val dialog = GenerateCLIDialog(project, e)
            val clickedOk = dialog.showAndGet()
            if (clickedOk) {
                ApplicationManager.getApplication().executeOnPooledThread {
                    runGenerator(
                        store.state.project!!, store.state,
                        store.state.workingDir, store.state.module!!
                    )
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        // Display action only if it is a nest project.
        val project = e.project
        val isProjectOpen = project != null && !project.isDisposed
        var isFileExists = false

        if (isProjectOpen) {
            assert(project != null)
            val projectDirectory: VirtualFile = project!!.guessProjectDir()!!
            val filePath = Paths.get(projectDirectory.path, "angular.json")
            isFileExists = Files.exists(filePath)
        }
        e.presentation.isEnabledAndVisible = isProjectOpen && isFileExists
    }

    private fun runGenerator(
        project: Project,
        schematic: GenerateCLIState,
        workingDir: VirtualFile?,
        module: CompletionModuleInfo
    ) {
        val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: return

        //val modules: MutableList<CompletionModuleInfo> = mutableListOf()
        val cli: VirtualFile = project.guessProjectDir()!!
        // NodeModuleSearchUtil.findModulesWithName(modules, "@ngxs/cli", cli, interpreter)

        // val module = modules.firstOrNull() ?: return
        val parameters = schematic.parameter.trim().split(" ").toMutableList()
            .map { it.trim() }
            .filter { it != "" }
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
