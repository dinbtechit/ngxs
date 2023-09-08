package com.github.dinbtechit.ngxs.action.editor

import com.github.dinbtechit.ngxs.NgxsIcons
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.types.TypeScriptNewExpressionElementType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import javax.swing.JComponent

class NgxsActionLineMarkerIconProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        /*val existingProviders = LineMarkerProviders.getInstance()
            .allForLanguage(Language.findLanguageByID("TypeScript")!!)
        var exist = false
        if (existingProviders.any { provider ->
                // Exclude this provider to prevent infinite recursion
                provider !is NgxsActionLineMarkerIconProvider && provider.getLineMarkerInfo(element) != null }) {
            // There exists a line marker from another provider
           exist = true
            return null
        }*/

        if (element.parent is JSReferenceExpression
            && element.parent.parent.elementType is TypeScriptNewExpressionElementType
            && element.parent.reference?.resolve()?.containingFile?.name?.contains(".actions.ts") == true
        ) {

            val lineNumber = getLineNumber(element)
            val elements = getAllPsiElementOnLine(element, lineNumber)

            val handlers = mutableSetOf<PsiElement>()
            if (elements.isNotEmpty()) {
                for (e in elements) {
                    handlers.add(e)
                }
            }

            if (elements.first() != element) return null

            val icon = if (handlers.size > 1) NgxsIcons.Gutter.MutipleActions else NgxsIcons.Gutter.Action
            //val icon = AllIcons.Gutter.OverridingMethod
            val tooltipText = if (handlers.size > 1) "NGXS Multiple Actions" else "NGXS Action \"${element.text}\""
            val clickAction = if (handlers.size > 1)
                createGroupGutterPopup(handlers)
            else createGutterNavigator(element)

            return LineMarkerInfo(
                element,
                element.textRange,
                icon, { tooltipText },
                clickAction,
                GutterIconRenderer.Alignment.RIGHT,
                { tooltipText }
            )
        }
        return null
    }

    private fun createGroupGutterPopup(handlers: MutableSet<PsiElement>): GutterIconNavigationHandler<PsiElement> {

        return GutterIconNavigationHandler<PsiElement> { e, element ->
            val group = DefaultActionGroup()
            for (handler in handlers) {
                val action = object : AnAction({ "NGXS Action \"${handler.text}\"" }, NgxsIcons.Gutter.Action) {
                    override fun actionPerformed(e: AnActionEvent) {
                        navigateToElement(handler)
                    }
                }
                group.add(action)
            }

            val editor: Editor? = e.source as? Editor

            val popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "",
                group,
                SimpleDataContext.builder().add(PlatformDataKeys.EDITOR, editor).build(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false
            )
            val component = e.component
            if (component is JComponent) {
                popup.showInScreenCoordinates(component, e.locationOnScreen)
            }
        }
    }

    private fun createGutterNavigator(element: PsiElement): GutterIconNavigationHandler<PsiElement> {
        return GutterIconNavigationHandler<PsiElement> { _, _ ->
            navigateToElement(element)
        }
    }

    private fun navigateToElement(element: PsiElement) {
        ApplicationManager.getApplication().runReadAction {
            val element2 = element.parent.reference?.resolve()?.navigationElement
            val refs = ReferencesSearch.search(element2!!).findAll()
            for (ref in refs.toList()) {
                val actionDecoratorElement = PsiTreeUtil.findFirstParent(ref.element) { it is ES6Decorator }
                val hasActionDecorator = actionDecoratorElement != null
                if (hasActionDecorator &&
                    ref.element.containingFile.name.contains(".state.ts")
                ) {
                    val fileEditorManager = FileEditorManager.getInstance(element.project)
                    val textEditor = fileEditorManager.openTextEditor(
                        OpenFileDescriptor(
                            element.project,
                            ref.element.containingFile.virtualFile
                        ), true
                    )
                    val start = ref.element.textRange.startOffset
                    textEditor?.caretModel?.moveToOffset(start)
                    textEditor?.scrollingModel?.scrollToCaret(ScrollType.MAKE_VISIBLE)
                }
            }
        }
    }

    private fun getLineNumber(element: PsiElement): Int {
        val psiFile: PsiFile = element.containingFile
        val document: Document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile)!!
        return document.getLineNumber(element.textRange.startOffset)
    }


    private fun getAllPsiElementOnLine(currentPsiElement: PsiElement, lineNumber: Int): List<PsiElement> {
        val psiFile: PsiFile = currentPsiElement.containingFile
        val document: Document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile)!!

        val res = mutableListOf<PsiElement>()
        val startOffset = document.getLineStartOffset(lineNumber)
        val endOffset = document.getLineEndOffset(lineNumber)

        var currentOffset = startOffset
        while (currentOffset <= endOffset) {
            val element = psiFile.findElementAt(currentOffset)

            if (element != null) {
                if (element.parent is JSReferenceExpression
                    && element.parent.parent.elementType is TypeScriptNewExpressionElementType
                    && element.parent.reference?.resolve()?.containingFile?.name?.contains(".actions.ts") == true
                ) {
                    res.add(element)
                }
                currentOffset = element.textRange.endOffset
            } else {
                break
            }
        }
        return res
    }


}
