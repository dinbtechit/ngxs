package com.github.dinbtechit.ngxs.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nullable

@State(name = "NgxsPluginSettings", storages = [(Storage("ngxs_plugin_settings.xml"))])
class SettingsStore: PersistentStateComponent<SettingsStore> {

    companion object {
        val instance: SettingsStore
            get() = ApplicationManager.getApplication().getService(SettingsStore::class.java)
    }

    var showNotificationOnUpdate = true
    var version = "unknown"

    @Nullable
    override fun getState() = this

    override fun loadState(state: SettingsStore) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
