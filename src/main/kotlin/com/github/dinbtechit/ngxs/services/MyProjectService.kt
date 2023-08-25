package com.github.dinbtechit.ngxs.services

import com.github.dinbtechit.ngxs.NgxsBundle
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {

    init {
        thisLogger().info(NgxsBundle.message("projectService", project.name))
    }
}
