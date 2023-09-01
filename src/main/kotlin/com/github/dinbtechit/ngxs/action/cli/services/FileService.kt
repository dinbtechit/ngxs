package com.github.dinbtechit.ngxs.action.cli.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager

@Service(Service.Level.PROJECT)
class FileService(val project: Project) {
    fun replaceNbsp(file: VirtualFile) {
        ApplicationManager.getApplication().invokeAndWait {
            val psiFile = PsiManager.getInstance(project).findFile(file)
            if (psiFile != null) {
                val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                if (document != null) {
                    val fileText = document.text
                    // Identify non-breaking space
                    val nbsp = "\u00A0" // Unicode for non-breaking space
                    val newText = fileText.replace(nbsp, " ")
                    document.setText(newText)
                }
            }
        }
    }
}
