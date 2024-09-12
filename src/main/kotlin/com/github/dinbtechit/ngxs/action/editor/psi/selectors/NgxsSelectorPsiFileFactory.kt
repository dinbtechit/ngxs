package com.github.dinbtechit.ngxs.action.editor.psi.selectors

import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.LiveTemplateOptions
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
import com.github.dinbtechit.ngxs.common.langExtensions.toCamelCase
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile


object NgxsSelectorPsiFileFactory {

    fun createSelectorsMethodLiveTemplates(editor: Editor, file: PsiFile,
                                           methodNameAndClassName: LiveTemplateOptions? = null) {
        if (file.virtualFile == null || editor.project == null) return
        val stateClass = NgxsSelectorsPsiUtil.findAssociatedStateClass(file)
        if (stateClass != null) {
            val metaSelector = NgxsStatePsiUtil.getMetaSelector(editor.project!!, stateClass.canonicalFile!!)
            val stateModel = NgxsStatePsiUtil.getTypeFromStateAnnotation(editor.project!!, stateClass.canonicalFile!!)
            if (stateModel != null) {
                val templateManager = TemplateManager.getInstance(editor.project)
                val stateClassName = (NgxsStatePsiUtil.getStateClassElement(editor.project!!, stateClass) as TypeScriptClass).name
                val metaSelectorName = (metaSelector as TypeScriptFunction).name
                val template = createSelectorMethod(templateManager, stateModel, "$stateClassName.$metaSelectorName", methodNameAndClassName)
                templateManager.startTemplate(editor, template)
            }
        }
    }


    private fun createSelectorMethod(templateManager: TemplateManager, stateModel: String,
                                     selectorName: String,
                                     liveTemplateOptions: LiveTemplateOptions? = null): Template {
        var methodName = stateModel.toCamelCase().replace("Model", "")
        var editMode = true
        if (liveTemplateOptions?.methodName != null) {
            methodName = liveTemplateOptions.methodName
            editMode = liveTemplateOptions.editMode
        }

        val template = templateManager.createTemplate(
            "ngxs-meta-selector", "Ngxs",
            """
            @Selector([$selectorName])
            static ${"$"}methodName${"$"}(state: $stateModel) {
               // TODO - return a slice of the State
            }
            """.trimIndent()
        )
        val defaultName = ConstantNode(methodName)
        template.addVariable("methodName", defaultName, defaultName, editMode)
        return template
    }


}
