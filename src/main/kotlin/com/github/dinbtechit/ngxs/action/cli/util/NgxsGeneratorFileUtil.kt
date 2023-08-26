package com.github.dinbtechit.ngxs.action.cli.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Paths

object NgxsGeneratorFileUtil {
    fun getRelativePath(project: Project, virtualFile: VirtualFile): String {
        val basePath = project.basePath
        val filePath = virtualFile.path

        val projectPath = Paths.get(basePath!!)
        val relativePath = projectPath.relativize(Paths.get(filePath))

        return relativePath.toString()
    }
}
