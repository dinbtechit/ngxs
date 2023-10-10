package com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion

import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.LiveTemplateOptions
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.NgxsActionsFileCompletionProvider
import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.completion.providers.NgxsStateFileCompletionProvider
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiFile

data class NgxsLiveTemplates(
    val presentableText: String,
    val tailText: String? = null,
    val lookUpStrings: List<String>,
    val generateTemplate: (param: LiveTemplateOptions?) -> Unit,
)

class NgxsCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
            psiElement().inside(psiElement(PsiFile::class.java)),
            NgxsActionsFileCompletionProvider()
        )
        extend(CompletionType.BASIC,
            psiElement().inside(psiElement(PsiFile::class.java)),
            NgxsStateFileCompletionProvider()
        )
    }

}
