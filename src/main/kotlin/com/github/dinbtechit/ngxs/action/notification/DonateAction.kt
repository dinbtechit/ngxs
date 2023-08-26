package com.github.dinbtechit.ngxs.action.notification

import com.github.dinbtechit.ngxs.NgxsIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class DonateAction: DumbAwareAction("Donate ($2)", "", NgxsIcons.Donate) {

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open("https://www.buymeacoffee.com/dinbtechit")
    }

}
