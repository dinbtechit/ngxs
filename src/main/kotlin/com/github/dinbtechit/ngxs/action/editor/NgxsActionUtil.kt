package com.github.dinbtechit.ngxs.action.editor

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.types.TypeScriptClassElementType
import com.intellij.lang.javascript.types.TypeScriptNewExpressionElementType
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.GlobalSearchScope
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

    fun findActionDeclaration(actionClassRef: PsiElement): PsiElement? {
        // Navigate through the PSI tree to find TypeScriptClass instances
        val typescriptClass = TypeScriptClassResolver.getInstance().findAnyClassByQName(
            actionClassRef.text,
            GlobalSearchScope.getScopeRestrictedByFileTypes(
                GlobalSearchScope.allScope(actionClassRef.project),
                TypeScriptFileType.INSTANCE
            )
        )
        if (typescriptClass != null) {
            return typescriptClass
        }
        return null
    }

    fun createActionDeclaration(
        editor: Editor? = null,
        actionFile: VirtualFile,
        actionClassRef: PsiElement,
        withPayload: Boolean = true,
        editMode: Boolean = true
    ) {
        val editorFactory = EditorFactory.getInstance()
        val document =
            FileDocumentManager.getInstance().getDocument(actionFile) ?: return
        document.insertString(document.textLength, "\n\n")

        val newEditor: Editor = if (editMode) {
            val editorManager = FileEditorManager.getInstance(actionClassRef.project)
            editorManager.openFile(actionFile, true)
            (editorManager.getSelectedEditor(actionFile) as TextEditor).editor
        } else {
            editorFactory.createEditor(document, actionClassRef.project)
        }

        newEditor.caretModel.moveToOffset(document.textLength)
        val stateName = actionFile.name.split(".")[0]
        val template = createActionDeclaration(actionClassRef, stateName, withPayload, editMode)
        val templateManager = TemplateManager
            .getInstance(actionClassRef.project)
        templateManager.startTemplate(newEditor, template)

        if(!editMode) {
            editorFactory.releaseEditor(newEditor)
        }
    }

    private fun createActionDeclaration(
        actionClassRef: PsiElement,
        stateName: String,
        withPayload: Boolean,
        editMode: Boolean,
    ): Template {
        val templateManager = TemplateManager.getInstance(actionClassRef.project)
        val template = templateManager.createTemplate(
            "ngxs-action-declaration", "Ngxs",
            """
            export class ${actionClassRef.text} {
              static readonly type = '[$stateName] ${"$"}actionType${"$"}';
              ${if (withPayload) 
              """
              constructor(public ${"$"}payloadName${"$"}: ${"$"}payloadType${"$"}) {
              }
              """.trimStart()
              else ""}    
            }
            """.trimIndent()
        )

        val defaultActionType = ConstantNode(actionClassRef.text)
        template.addVariable("actionType", defaultActionType, defaultActionType, editMode)
        val defaultPayloadName = ConstantNode("payload")
        template.addVariable("payloadName", defaultPayloadName, defaultPayloadName, editMode)
        val defaultPayloadType = ConstantNode("unknown")
        template.addVariable("payloadType", defaultPayloadType, defaultPayloadType, editMode)
        return template
    }

}
