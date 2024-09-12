package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers

import com.github.dinbtechit.ngxs.NgxsIcons
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.NgxsLiveTemplates
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers.getLiveTemplateOptions
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers.trimStartAtCurrentCaretPosition
import com.github.dinbtechit.ngxs.action.editor.psi.selectors.NgxsSelectorPsiFileFactory
import com.github.dinbtechit.ngxs.action.editor.psi.selectors.NgxsSelectorsPsiUtil
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.util.ProcessingContext


class NgxsSelectorFileCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {

        val file = parameters.originalFile
        val editor = parameters.editor
        if (file.name.contains(".selectors.ts")) {
            addCompletions(parameters, resultSet)
        }
    }

    private fun addCompletions(parameters: CompletionParameters, resultSet: CompletionResultSet) {
            getResultSets(parameters).forEach {
                resultSet.addElement(
                    LookupElementBuilder.create(it.value.presentableText)
                        .withIcon(NgxsIcons.Editor.Completion)
                        .withPresentableText(it.value.presentableText)
                        .withTailText(it.value.tailText)
                        .withTypeText(it.value.typeText ?: "NGXS Selectors")
                        .withLookupStrings(it.value.lookUpStrings)
                        .withCaseSensitivity(false)
                        .withInsertHandler { context, item ->
                            val methodNameAndClassName = context.getLiveTemplateOptions(item)
                            context.trimStartAtCurrentCaretPosition()
                            it.value.generateTemplate(methodNameAndClassName)
                        }
                        .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
                )
        }
    }

    private fun getResultSets(parameters: CompletionParameters): Map<String, NgxsLiveTemplates> {
        val selectorFile = parameters.originalFile
        val stateFile = NgxsSelectorsPsiUtil.findAssociatedStateClass(selectorFile)
        val metaSelectorFunction = if (parameters.editor.project != null && stateFile != null) {
            NgxsStatePsiUtil.getMetaSelector(parameters.editor.project!!, stateFile) as TypeScriptFunction
        } else null
        val stateClass = if (stateFile != null) {
            NgxsStatePsiUtil.getStateClassElement(parameters.editor.project!!, stateFile) as TypeScriptClass
        } else null
        val stateClassName = stateClass?.name ?: "STATE"
        val metaSelectorText = metaSelectorFunction?.name ?: "META_SELECTOR"
        val selectorClassList = NgxsSelectorsPsiUtil.getSelectorClassList(selectorFile)


        return mapOf(

            // 1. createSelector
            "createSelector" to NgxsLiveTemplates(
                presentableText = "@Selector([$stateClassName.$metaSelectorText])",
                tailText = " static name(){...}",
                typeText = "NGXS Selector",
                lookUpStrings = listOf(
                    "selector",
                    "ngxs-selector"
                ),
                generateTemplate = {
                    if (it is LiveTemplateOptions) {
                        NgxsSelectorPsiFileFactory.createSelectorsMethodLiveTemplates(
                            parameters.editor,
                            parameters.originalFile,
                            it
                        )
                    } else {
                        NgxsSelectorPsiFileFactory.createSelectorsMethodLiveTemplates(
                            parameters.editor, parameters.originalFile
                        )
                    }
                }),

            // 2. createSelector
            "createSelector-selectors" to NgxsLiveTemplates(
                presentableText = "@Selector([Selectors])",
                tailText = " static name(){...}",
                lookUpStrings = listOf(
                    "selector",
                    "ngxs-selector"
                ),
                generateTemplate = {
                    if (it is LiveTemplateOptions) {
                        NgxsSelectorPsiFileFactory.createSelectorsMethodLiveTemplates(
                            parameters.editor,
                            parameters.originalFile,
                            it
                        )
                    } else {
                        NgxsSelectorPsiFileFactory.createSelectorsMethodLiveTemplates(
                            parameters.editor, parameters.originalFile
                        )
                    }
                }),

            )
    }

}
