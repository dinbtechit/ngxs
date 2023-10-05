package com.github.dinbtechit.ngxs.quickfix

import com.github.dinbtechit.ngxs.action.editor.codeIntellisense.inspection.quickfix.NgxsActionDeclarationQuickFix
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NgxsActionDeclarationQuickFixTest : BasePlatformTestCase() {

    fun `test QuickFix Intention`() {
      val psiFile = myFixture.configureByFiles("fan.actions.ts", "fan.state.ts")
        val actionPsiElement = TypeScriptPsiUtil.getPsiElementByRange(psiFile[1], TextRange(663,672))
        val actionPsiElement2 = TypeScriptPsiUtil.getPsiElementByRange(psiFile[1], TextRange(782,799))
        WriteCommandAction.runWriteCommandAction(actionPsiElement!!.project) {
            NgxsActionDeclarationQuickFix("fan.actions.ts", actionPsiElement).applyFix()
            NgxsActionDeclarationQuickFix("fan.actions.ts", actionPsiElement2!!, true).applyFix()
            // Formatting
            val codeStyleManager = CodeStyleManager.getInstance(project)
            codeStyleManager.reformat(psiFile[0])
            val psiDocumentManager = PsiDocumentManager.getInstance(project)
            // Adding a new Line to the document
            val document = psiDocumentManager.getDocument(psiFile[0])
            document?.let {
                it.insertString(it.textLength, "\n")
            }
        }
        myFixture.checkResultByFile("fan.actions_after.ts")
    }


    override fun getTestDataPath(): String {
        return "src/test/testData/store"
    }
}
