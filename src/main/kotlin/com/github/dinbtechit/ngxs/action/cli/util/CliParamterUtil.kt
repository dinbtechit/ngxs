package com.github.dinbtechit.ngxs.action.cli.util

object CliParameterUtil {
    fun String.convertToCli (): Map<String, String> {
        return this.split("--")
            .filter { it.isNotBlank() }
            .map { it.trim().split(" ", "=", limit = 2) }
            .associate { if (it.size > 1) Pair(it[0], it[1]) else Pair(it[0], "") }
    }

    fun <K, V> MutableMap<K, V>.update(key: K, value: V): MutableMap<K, V> {
        this[key] = value
        return this
    }

    fun <K, V> Map<K, V>.convertToString(): String {
        return entries.joinToString(separator = " ") { "--${it.key} ${it.value}" }
    }


}
