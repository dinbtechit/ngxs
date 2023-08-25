package com.github.dinbtechit.ngxs.action.cli.store

import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import org.reduxkotlin.Reducer
import org.reduxkotlin.createThreadSafeStore

data class GenerateCLIState(
    val name: String = "",
    val parameter: String = "",
    val project: Project? = null,
    val workingDir: VirtualFile? = null,
    val module: CompletionModuleInfo? = null
)

object CLIStore {
    val store = createThreadSafeStore(reducer, GenerateCLIState())
}

val reducer: Reducer<GenerateCLIState> = { state, action ->
    when(action) {
        is Action.GenerateCLIAction -> {
            state.copy (
               parameter = action.options,
               workingDir = action.workingDir,
               project = action.project,
           )
        }
        is Action.UpdateParameter -> {
            state.copy(
                name = action.name,
                parameter = action.options
            )
        }
        is Action.CheckIfNpmPackageInstalled -> {
            val interpreter = NodeJsInterpreterManager.getInstance(action.project).interpreter
            var patchState = state
            if (interpreter != null) {
                val modules: MutableList<CompletionModuleInfo> = mutableListOf()
                val cli: VirtualFile = action.project.guessProjectDir()!!
                NodeModuleSearchUtil.findModulesWithName(modules, "@ngxs/cli", cli, interpreter)
                val module = modules.firstOrNull()
                patchState = state.copy(
                    module = module
                )
            }
            patchState
        }
        else -> state
    }
}
