package com.github.dinbtechit.ngxs.common.services

import com.github.dinbtechit.ngxs.common.models.AngularJson
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ThreeState
import kotlinx.serialization.json.Json
import java.io.File

data class NgxsVersion(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}


data class NgxsProjectDetails(
    val isValidNgxsProject: Boolean = true,
    val isGreaterThanEqualTo18: ThreeState = ThreeState.NO,
    val version: NgxsVersion = NgxsVersion(-1, -1, -1)
)

@Service(Service.Level.PROJECT)
class NgxsProject(val project: Project) {
    fun getNgxsProjectDetails(project: Project): NgxsProjectDetails {
        val sd = FilenameIndex.getVirtualFilesByName("package.json", GlobalSearchScope.projectScope(project))
        if (sd.isEmpty()) return NgxsProjectDetails(isValidNgxsProject = false)

        for (packageJson in sd) {
            val psiManager = PsiManager.getInstance(project)
            val packageJsonFile =
                PackageJsonUtil.asPackageJsonFile(psiManager.findFile(packageJson))
                    ?: return NgxsProjectDetails(
                        isValidNgxsProject = false
                    )
            val ngxs =
                PackageJsonUtil.findDependencyByName(packageJsonFile, "@ngxs/store") ?: continue
            val ngxsVersion = (ngxs.value!!.text).replace(
                "[^\\d.]".toRegex(),   // Filter numbers and dots only for example - "^3.7.8"  will be "3.7.8"
                ""
            )
            val (major, minor, patch) = ngxsVersion.split(".").map{ it.toInt()}
            return NgxsProjectDetails(
                isGreaterThanEqualTo18 = PackageJsonUtil.isVersionGreaterOrEqualMajor(
                    ngxsVersion, 18
                ),
                version = NgxsVersion(major, minor, patch)
            )
        }
        return NgxsProjectDetails(
            isValidNgxsProject = false
        )
    }
    fun getNgProject(): AngularJson? {
        val sd = FilenameIndex.getVirtualFilesByName("angular.json", GlobalSearchScope.projectScope(project))
        if (sd.isEmpty()) return null
        val angularJsonVirtualFile = sd.first() ?: return null
        return deserializeJsonFile(angularJsonVirtualFile)
    }

    private fun deserializeJsonFile(schemaJsonFile: VirtualFile): AngularJson? {
        val jsonFile = File(schemaJsonFile.path)
        if (jsonFile.exists()) {
            try {
                val content = jsonFile.readText()
                if (content.isNotBlank()) {
                    val json = Json {
                        ignoreUnknownKeys = true
                    }
                    return json.decodeFromString<AngularJson>(content)
                }
            } catch (e: Exception) {
                thisLogger().error("unable to serialize - ${jsonFile.path}", e)
                return null
            }
        }
        thisLogger().error("File does not exist - $jsonFile")
        return null
    }
}