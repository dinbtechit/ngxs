package com.github.dinbtechit.ngxs.action.editor

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.types.TypeScriptClassElementType
import com.intellij.lang.javascript.types.TypeScriptNewExpressionElementType
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeafs

object NgxsActionUtil {

    fun isActionDispatched(element: PsiElement): Boolean {
        return (element.parent is JSReferenceExpression
                && element.parent.parent.elementType is TypeScriptNewExpressionElementType
                && element.parent.reference?.resolve() !== null) &&
                element.parent.reference?.resolve()!!.children.any {
                    it is ES6FieldStatementImpl && it.text.contains("^static(.*)type".toRegex())
                }
    }

    fun isActionClass(element: PsiElement): Boolean {
        return element.elementType is TypeScriptClassElementType && element.children.any {
            it is ES6FieldStatementImpl && it.text.contains("^static(.*)type".toRegex())
        }
    }

    fun hasPayload(element: PsiElement): Boolean {
        return TypeScriptPsiUtil.getParentClass(element)?.constructor
            ?.parameterList?.children?.isNotEmpty() == true
    }

    fun isActionImplExist(psiElement: PsiElement): Boolean {
        return when {
            isActionDispatched(psiElement) -> {
                val element2 = psiElement.parent.reference?.resolve()?.navigationElement
                this.findActionUsages(element2)
            }

            isActionClass(psiElement) -> {
                this.findActionUsages(psiElement)
            }

            else -> false
        }
    }

    fun getActionClassPsiElement(element: PsiElement): PsiElement? {
        val endIndex = element.firstChild.nextLeafs.indexOfFirst { it.text == "{" }
        return element.firstChild.nextLeafs.toList()
            .subList(0, if (endIndex < 0) 0 else endIndex)
            .firstOrNull { it !is PsiWhiteSpace && it.text != "class" }
    }

    fun findActionUsages(element: PsiElement?): Boolean {

        if (element == null) return false

        val refs = ReferencesSearch.search(element).findAll()
        for (ref in refs.toList()) {
            val actionDecoratorElement = PsiTreeUtil.findFirstParent(ref.element) { it is ES6Decorator }
            val hasActionDecorator = actionDecoratorElement != null
            if (hasActionDecorator
                && ref.element.containingFile.name.contains(".state.ts")
            ) {
                return true
            }
        }
        return false
    }
}
