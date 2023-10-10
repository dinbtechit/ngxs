package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers

import com.github.dinbtechit.ngxs.NgxsIcons
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.NgxsLiveTemplates
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers.getLiveTemplateOptions
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.helpers.trimStartAtCurrentCaretPosition
import com.github.dinbtechit.ngxs.action.editor.psi.actions.NgxsActionsPsiFileFactory
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsActionType
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiFileFactory
import com.github.dinbtechit.ngxs.action.editor.psi.state.NgxsStatePsiUtil
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext


class NgxsStateFileCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {

        val file = parameters.originalFile
        val editor = parameters.editor
        if (file.name.contains(".state.ts")
            && NgxsStatePsiUtil.isCursorWithinStateClass(editor, file)
        ) {
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
                    .withTypeText("NGXS")
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
        return mapOf(
            // 1. createMetaSelector
            "createMetaSelector" to NgxsLiveTemplates(
                presentableText = "selector-meta",
                tailText = "(){...}",
                lookUpStrings = listOf(
                    "@Selector-meta",
                    "ngxs-selector-meta"
                ),
                generateTemplate = {
                    if (it is LiveTemplateOptions) {
                        NgxsStatePsiFileFactory.createSelectorMethodLiveTemplates(
                            parameters.editor,
                            parameters.originalFile,
                            it
                        )
                    } else {
                        NgxsStatePsiFileFactory.createSelectorMethodLiveTemplates(
                            parameters.editor,
                            parameters.originalFile
                        )
                    }
                }),

            // 2. createSelector
            "createSelector" to NgxsLiveTemplates(
                presentableText = "@Selector",
                tailText = "(){...}",
                lookUpStrings = listOf(
                    "selector",
                    "ngxs-selector"
                ),
                generateTemplate = {
                    if (it is LiveTemplateOptions) {
                        NgxsStatePsiFileFactory.createSelectorsMethodLiveTemplates(
                            parameters.editor,
                            parameters.originalFile,
                            it
                        )
                    } else {
                        NgxsStatePsiFileFactory.createSelectorsMethodLiveTemplates(
                            parameters.editor, parameters.originalFile
                        )
                    }
                }),

            // 3. action
            "createAction" to NgxsLiveTemplates(
                presentableText = "@Action",
                tailText = "(){...}",
                lookUpStrings = listOf(
                    "action",
                    "ngxs-action"
                ),
                generateTemplate = {
                    if (it is LiveTemplateOptions) {
                        NgxsStatePsiFileFactory.createActionMethodLiveTemplates(
                            parameters.editor, parameters.originalFile,
                            NgxsActionType.WITHOUT_PAYLOAD, it
                        )
                        NgxsActionsPsiFileFactory.createActionDeclaration(it.className, parameters, withPayload = false)
                    } else {
                        NgxsStatePsiFileFactory.createActionMethodLiveTemplates(
                            parameters.editor, parameters.originalFile,
                            NgxsActionType.WITHOUT_PAYLOAD, null
                        )
                    }
                }),

            // 4. action with payload
            "createActionPayload" to NgxsLiveTemplates(
                presentableText = "@Action-payload",
                tailText = "(){...}",
                lookUpStrings = listOf(
                    "action-payload",
                    "ngxs-action-payload"
                ),
                generateTemplate = {
                    if (it is LiveTemplateOptions) {
                        NgxsStatePsiFileFactory.createActionMethodLiveTemplates(
                            parameters.editor, parameters.originalFile,
                            NgxsActionType.WITH_PAYLOAD, it
                        )
                        NgxsActionsPsiFileFactory.createActionDeclaration(it.className, parameters, withPayload = true)
                    } else {
                        NgxsStatePsiFileFactory.createActionMethodLiveTemplates(
                            parameters.editor, parameters.originalFile,
                            NgxsActionType.WITH_PAYLOAD, null
                        )
                    }
                }),
        )
    }

}

data class LiveTemplateOptions(
    val methodName: String? = null,
    val className: String? = null,
    val constructorParameters: String? = null,
    val editMode: Boolean = false
)



