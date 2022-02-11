package org.jetbrains.plugins.dubbo.codeInsight

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.parentOfType
import org.jetbrains.plugins.dubbo.dubboServiceAnnotationName
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractPsiMethod


class GenerateDubboRequestIntention : BaseElementAtCaretIntentionAction() {
    override fun getFamilyName(): String {
        return "Generate Dubbo Request in index.http file"
    }

    override fun getText(): String {
        return this.familyName
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val psiMethod: PsiMethod? = extractPsiMethod(element.parent)
        if (psiMethod != null) {
            val psiClass = psiMethod.parentOfType<PsiClass>()
            return psiClass != null && psiClass.hasAnnotation(dubboServiceAnnotationName)
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val psiMethod: PsiMethod? = extractPsiMethod(element.parent)
        if (psiMethod != null) {
            val psiClass = psiMethod.parentOfType<PsiClass>()
            if (psiClass != null && psiClass.hasAnnotation(dubboServiceAnnotationName)) {
                val serviceName = extractDubboService(psiClass).serviceName
                val params = psiMethod.parameterList.parameters
                val paramTypes = params.joinToString(",", "(", ")") { param ->
                    (param.type as PsiClassReferenceType).canonicalText
                }
                var body = ""
                if (params.size == 1) {
                    body = getDefaultValue(params[0])
                } else {
                    body = params.joinToString(",\n  ", "[\n  ", "\n]") { param ->
                        getDefaultValue(param)
                    }
                }
                val routing = "${serviceName}/${psiMethod.name}${paramTypes}"
                // construct http request code
                val builder = StringBuilder()
                builder.append("\n")
                builder.append("### dubbo request for ${psiClass.name}/${psiMethod.name}").append("\n")
                builder.append("DUBBO localhost:20880/${routing}").append("\n")
                builder.append("\n")
                builder.append(body)
                val projectDir = project.guessProjectDir()!!
                var indexHttpVirtualFile = projectDir.findChild("index.http")
                if (indexHttpVirtualFile == null) {  // create index.http
                    indexHttpVirtualFile = projectDir.createChildData(psiMethod, "index.http");
                }
                //insert code to index.http file
                val indexHttpFile = PsiManager.getInstance(project).findFile(indexHttpVirtualFile)!!
                val document = PsiDocumentManager.getInstance(project).getDocument(indexHttpFile)!!
                val offset = if(document.lineCount > 0)  {
                    document.getLineEndOffset(document.lineCount - 1)
                } else {
                    0
                }  
                document.insertString(offset, builder.toString())
            }
        }

    }

    override fun startInWriteAction(): Boolean {
        return true
    }

    private fun getDefaultValue(param: PsiParameter): String {
        val paramType = param.type.canonicalText.lowercase()
        if (paramType.endsWith("integer")
            || paramType.endsWith("int")
            || paramType.endsWith("short")
            || paramType.endsWith("byte")
            || paramType.endsWith("long")
        ) {
            return "1"
        } else if (paramType.endsWith("double") || paramType.endsWith("float")) {
            return "1.0"
        } else if (paramType.endsWith("boolean") || paramType.endsWith("bool")) {
            return "true"
        } else if (paramType.contains("date") || paramType.contains("time")) {
            return "\"2022-02-22\""
        } else if (paramType.contains("map")) {
            return "{\"key\", \"\"}"
        } else if (paramType.contains("list")) {
            return "[]"
        } else if (paramType.contains("string")) {
            return "\"\""
        } else {
            return "{}"
        }
    }

}