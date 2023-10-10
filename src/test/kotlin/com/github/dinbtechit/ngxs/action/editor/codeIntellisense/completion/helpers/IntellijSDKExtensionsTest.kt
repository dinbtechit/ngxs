package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers

import com.intellij.testFramework.fixtures.BasePlatformTestCase


class IntellijSDKExtensionsTest :BasePlatformTestCase(){
    fun `test live template options`() {
        val inputString = "id:string,name:string"
        val value = inputString.toMap()
        println(value)
    }
}