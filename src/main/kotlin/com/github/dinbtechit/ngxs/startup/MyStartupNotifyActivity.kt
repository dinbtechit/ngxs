package com.github.dinbtechit.ngxs.startup

import com.github.dinbtechit.ngxs.NgxsIcons
import com.github.dinbtechit.ngxs.action.notification.DonateAction
import com.github.dinbtechit.ngxs.action.notification.StarGithubRepoAction
import com.github.dinbtechit.ngxs.action.notification.WhatsNewAction
import com.github.dinbtechit.ngxs.settings.SettingsStore
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity


class MyStartupNotifyActivity : StartupActivity.DumbAware {

    private val updateContent: String by lazy {
        //language=HTML
        """
        """.trimIndent()
    }


    companion object {
        const val pluginId = "com.github.dinbtechit.ngxs"
        lateinit var notification: Notification
    }

    override fun runActivity(project: Project) {
        DumbService.getInstance(project).smartInvokeLater {
            val settings = SettingsStore.instance
            if (getPlugin()?.version != SettingsStore.instance.version) {
                settings.version = getPlugin()!!.version
                showNotificationPopup(project)
            }
            showNotificationPopup(project)
        }
    }

    private fun updateMsg(): String {
        val plugin = getPlugin()
        return if (plugin == null) {
            "NGXS (beta) installed."
        } else {
            "NGXS (beta) updated to ${plugin.version}"
        }
    }

    private fun showNotificationPopup(project: Project) {
        notification = createNotification(
            updateMsg(),
            notificationContent(),
            NotificationType.INFORMATION
        )
        showFullNotification(project, notification)
    }

    private fun notificationContent(): String {
        return updateContent
    }

    private fun getPlugin(): IdeaPluginDescriptor? = PluginManagerCore.getPlugin(PluginId.getId(pluginId))

    private fun createNotification(
        title: String, content: String, type: NotificationType
    ): Notification {
        return NotificationGroupManager.getInstance()
            .getNotificationGroup("NGXS Notification Group")
            .createNotification(content, type)
            .setTitle(title)
            .setIcon(NgxsIcons.logo)
            .addAction(DonateAction())
            .addAction(StarGithubRepoAction())
            .addAction(WhatsNewAction())
    }

    private fun showFullNotification(project: Project, notification: Notification) {
        try {
//            val frame = WindowManager.getInstance().getIdeFrame(project)
//            if (frame == null) {
//                notification.notify(project)
//                return
//            }
//            val bounds = frame.component.bounds
//            val target = RelativePoint(frame.component, Point(bounds.x + bounds.width, 20))
//            NotificationsManagerImpl.createBalloon(
//                frame,
//                notification,
//                true, // showCallout
//                false, // hideOnClickOutside
//                BalloonLayoutData.fullContent(),
//                ApplicationService.INSTANCE
//            ).show(target, Balloon.Position.atLeft)
            notification.notify(project)
        } catch(e: Exception) {
            notification.notify(project)
        }
    }
}
