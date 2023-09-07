package com.github.dinbtechit.ngxs.action.editor

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.types.TypeScriptNewExpressionElementType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class NgxsActionLineMarkerIconProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {


        if (element.parent is JSReferenceExpression
            && element.parent.parent.elementType is TypeScriptNewExpressionElementType
            && element.parent.reference?.resolve()?.containingFile?.name?.contains(".actions.ts") == true
        ) {

            //val icon = NgxsIcons.Gutter.state
            val icon = AllIcons.Gutter.OverridingMethod
            val tooltipText = "NGXS Action \"${element.text}\""
            val clickAction = GutterIconNavigationHandler<PsiElement> { _, _ ->
                ApplicationManager.getApplication().runReadAction {
                    val element2 = element.parent.reference?.resolve()?.navigationElement
                    val refs = ReferencesSearch.search(element2!!).findAll()
                    for (ref in refs.toList()) {
                        val hasActionDecorator = PsiTreeUtil.findFirstParent(ref.element) { it is ES6Decorator } != null
                        if (hasActionDecorator &&
                            ref.element.containingFile.name.contains(".state.ts")) {
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
}
