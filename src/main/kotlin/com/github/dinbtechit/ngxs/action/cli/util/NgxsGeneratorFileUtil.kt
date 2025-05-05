package com.github.dinbtechit.ngxs.action.cli.util

import com.github.dinbtechit.ngxs.action.cli.models.ComputedCLIParameters
import com.github.dinbtechit.ngxs.common.models.AngularProject
import com.github.dinbtechit.ngxs.common.services.NgxsProject
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.firstOrNull
import java.nio.file.Paths

object NgxsGeneratorFileUtil {

    private val logger = logger<NgxsGeneratorFileUtil>()

    fun getRelativePathExcludeSrcApp(project: Project, virtualFile: VirtualFile): String {
        val basePath = project.basePath
        val filePath = virtualFile.path

        val projectPath = Paths.get(basePath!!)
        val relativePath = projectPath.relativize(Paths.get(filePath))

        return relativePath.toString().replace("src/app", "")
    }

    fun getRelativePath(project: Project, virtualFile: VirtualFile): String {
        val basePath = project.basePath
        val filePath = virtualFile.path

        val projectPath = Paths.get(basePath!!)
        val relativePath = projectPath.relativize(Paths.get(filePath))

        return relativePath.toString()
    }

    fun getFilePath(project: Project, e: AnActionEvent, workingDir: VirtualFile): String {
        return getPathDifference(
            findClosestModuleFileDir(project, e, workingDir).path, workingDir.path
        )
    }

    fun findClosestModuleFileDir(project: Project, e: AnActionEvent, workingDir: VirtualFile): VirtualFile {
        val moduleFile = findClosestModuleFile(project, e, workingDir)
        return when {
            moduleFile.isDirectory -> moduleFile
            else -> moduleFile.parent
        }
    }

    fun findClosestModuleFile(project: Project, e: AnActionEvent, workingDir: VirtualFile): VirtualFile {
        val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        return recursivelyFindModuleInParentFolders(virtualFile) ?: workingDir
    }

    private fun getPathDifference(moduleFilePath: String, workingDirPath: String) = workingDirPath.replace(
        moduleFilePath, ""
    )

    private fun recursivelyFindModuleInParentFolders(virtualFile: VirtualFile?): VirtualFile? {
        val regex = Regex(".*module\\.ts$")
        var parentDirectory: VirtualFile? = virtualFile


        while (parentDirectory != null) {
            val childrenFiles = parentDirectory.children

            val moduleFile = childrenFiles.firstOrNull {
                it.isInLocalFileSystem && it.fileType.defaultExtension == "ts"
                        && regex.matches(it.name)
            }

            if (moduleFile != null) {
                return moduleFile
            }

            parentDirectory = parentDirectory.parent
        }

        return null
    }

    fun computeGeneratePath(type: String, project: Project, virtualFile: VirtualFile): String {
        if (type == "app" ||
            type == "sub-app" ||
            type == "library"
        ) {
            return project.guessProjectDir()?.path ?: ""
        }
        return getRelativePath(project, virtualFile)
    }

    fun computeGeneratePath(project: Project, workingDirPath: VirtualFile): ComputedCLIParameters? {
        val angularProject = findAngularProjectName(project, workingDirPath) ?: return null
        val angularProjectName = angularProject.key
        val generatePath = getRelativePath(project, workingDirPath)
        val angularProjectType = angularProject.value.projectType
        var displayPath = generatePath
        println("project: $angularProjectName")
        println("generatepath: $generatePath")
        var actualGeneratePath = generatePath.replace(angularProject.value.root, "")
            .replace("src/${angularProjectType.substring(0, 3)}", "")
            .replace("src", "")
            .replace("//", "/")
        if (actualGeneratePath.isNotBlank() && actualGeneratePath == "src") {
            actualGeneratePath = ""
            displayPath = "/${angularProjectType.substring(0, 3)}"
        }
        if (actualGeneratePath.isNotBlank() && actualGeneratePath.contains("src")) {
            displayPath = "/${angularProjectType.substring(0, 3)}"
        }
        if (!generatePath.contains("src/${angularProjectType.substring(0, 3)}")) {
            displayPath = generatePath + if (generatePath.contains("src")) "" else "/src"
            displayPath += "/${angularProjectType.substring(0, 3)}"
        }
        if (actualGeneratePath.isNotBlank() && actualGeneratePath.startsWith("/")) {
            actualGeneratePath = actualGeneratePath.replaceFirst("/", "")
        }
        if (actualGeneratePath.isNotBlank() && actualGeneratePath.endsWith("/")) {
            actualGeneratePath = actualGeneratePath.substring(0, actualGeneratePath.length - 1)
        }
        if (displayPath.isNotBlank() && displayPath.startsWith("/")) {
            displayPath = displayPath.replaceFirst("/", "")
        }
        if (displayPath.isNotBlank() && displayPath.endsWith("/")) {
            displayPath = displayPath.substring(0, displayPath.length - 1)
        }

        logger.debug("actualGeneratePath: $actualGeneratePath")
        logger.debug("displayPath: $displayPath")
        logger.debug("---------------------------------")

        return ComputedCLIParameters(
            ngProjectName = angularProjectName,
            actualGeneratePath = actualGeneratePath,
            displayGeneratePath = displayPath
        )
    }


    private fun findAngularProjectName(
        project: Project,
        workingDirPath: VirtualFile
    ): Map.Entry<String, AngularProject>? {
        val angularJson = project.service<NgxsProject>().getNgProject() ?: return null
        val angularRootProject = angularJson.projects
            .filter { it.value.root == "" && it.value.sourceRoot == "src" }
            .filter { getRelativePath(project, workingDirPath).startsWith("src") }
            .firstOrNull()
        val angularProject = angularJson.projects
            .filter { it.value.root != "" && it.value.sourceRoot != "src" }
            .filter {
                getRelativePath(project, workingDirPath).contains(it.value.root)
            }.firstOrNull()
        if (angularProject != null) {
            return angularProject
        } else if (angularRootProject != null) {
            return angularRootProject
        }
        return null
    }


}
