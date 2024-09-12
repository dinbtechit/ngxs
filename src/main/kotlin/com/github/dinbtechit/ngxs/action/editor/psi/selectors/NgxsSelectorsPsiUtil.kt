package com.github.dinbtechit.ngxs.action.editor.psi.selectors

import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

object NgxsSelectorsPsiUtil {
    fun findAssociatedStateClass(selectorFile: PsiFile): VirtualFile? {
        val stateName = selectorFile.name.split(".")[0]
        val computedStateFileName = "$stateName.state.ts"
        val stateFile = selectorFile.containingDirectory?.files?.firstOrNull {
            it.name == computedStateFileName
        }?.virtualFile
        return if (stateFile != null && NgxsStatePsiUtil.isNgxsStateFile(
                selectorFile.project,
                stateFile
            )
        ) stateFile else null
    }

    private fun containsSelectorClass(selectorFile: PsiFile): Boolean {
        return PsiTreeUtil.findChildrenOfType(selectorFile, TypeScriptClass::class.java).any { isSelectorClass(it) }
    }

    private fun isSelectorClass(selectorClass: TypeScriptClass): Boolean {
        return PsiTreeUtil.findChildrenOfType(selectorClass, TypeScriptFunction::class.java).any {
            isSelectorFunction(it)
        }
    }

    fun getSelectorClassList(selectorFile: PsiFile): List<TypeScriptClass> {
        val selectorClassList = PsiTreeUtil.findChildrenOfType(
            selectorFile, TypeScriptClass::class.java
        ).filter { isSelectorClass(it) }
        return selectorClassList.ifEmpty { emptyList() }
    }

    private fun getSelectorClassFunctionList(selectorClass: TypeScriptClass): List<TypeScriptFunction> {
        return PsiTreeUtil.findChildrenOfType(selectorClass, TypeScriptFunction::class.java).filter {
            isSelectorFunction(it)
        }
    }

    private fun isSelectorFunction(selectorFunction: TypeScriptFunction): Boolean {
        return PsiTreeUtil.findChildOfType(
            selectorFunction,
            JSAttributeList::class.java
        )?.firstChild?.text?.contains("@Selector(") == true
    }

}