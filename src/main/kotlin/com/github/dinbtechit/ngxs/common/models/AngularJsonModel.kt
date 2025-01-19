package com.github.dinbtechit.ngxs.common.models

import kotlinx.serialization.Serializable

@Serializable
data class AngularJson (
    val newProjectRoot: String,
    val projects: Map<String, AngularProject>
)

@Serializable
data class AngularProject (
    val projectType: String,
    val root: String,
    val sourceRoot: String,
)