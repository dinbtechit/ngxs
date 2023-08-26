package com.github.dinbtechit.ngxs.action.notification


import com.github.dinbtechit.ngxs.NgxsIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class StarGithubRepoAction: DumbAwareAction("Star Repo", "", NgxsIcons.GitHub) {

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open("https://github.com/dinbtechit/ngxs")
    }

}
