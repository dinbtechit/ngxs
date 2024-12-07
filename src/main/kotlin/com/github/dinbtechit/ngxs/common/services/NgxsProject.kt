package com.github.dinbtechit.ngxs.common.services


import com.github.dinbtechit.ngxs.common.langExtensions.versionToInt
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ThreeState

data class NgxsVersion(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}
typealias VersionCheck = ThreeState

data class NgxsProjectDetails(
    val isValidNgxsProject: Boolean = true,
    val isGreaterThanEqualTo18: VersionCheck = VersionCheck.NO,
    val version: NgxsVersion = NgxsVersion(-1, -1, -1)
)

@Service(Service.Level.PROJECT)
class NgxsProject {
    fun getNgxsProjectDetails(project: Project): NgxsProjectDetails {
        val sd = FilenameIndex.getVirtualFilesByName("package.json", GlobalSearchScope.projectScope(project))
        if (sd.isEmpty()) return NgxsProjectDetails(isValidNgxsProject = false)

        val packageJsonVirtualFile = sd.first()
        val psiManager = PsiManager.getInstance(project)
        val packageJsonFile =
            PackageJsonUtil.asPackageJsonFile(psiManager.findFile(packageJsonVirtualFile)) ?: return NgxsProjectDetails(
                isValidNgxsProject = false
            )
        val ngxs = PackageJsonUtil.findDependencyByName(packageJsonFile, "@ngxs/store") ?: return NgxsProjectDetails(
            isValidNgxsProject = false
        )
        val ngxsVersion = (ngxs.value!!.text).replace(
            "[^\\d.]".toRegex(),   // Filter numbers and dots only for example - "^3.7.8"  will be "3.7.8"
            ""
        )
        val (major, minor, patch) = ngxsVersion.versionToInt()
        return NgxsProjectDetails(
            isGreaterThanEqualTo18 = PackageJsonUtil.isVersionGreaterOrEqualMajor(
                ngxsVersion, 18
            ),
            version = NgxsVersion(major, minor, patch)
        )
    }
}