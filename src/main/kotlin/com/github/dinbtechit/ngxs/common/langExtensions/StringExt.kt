package com.github.dinbtechit.ngxs.common.langExtensions

import java.util.*

fun String.toCamelCase(): String = split(" ").joinToString("") {
    it.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }
}.replaceFirstChar { it.lowercase(Locale.getDefault()) }

fun String.convertKebabToTitleCase(): String {
    return this.split("-") // Split by hyphen
        .joinToString("") { it ->
            it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}

fun String.versionToInt(): List<Int> {
    return this.split(".").map{ it.toInt()}
}