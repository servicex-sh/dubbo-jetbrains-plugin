package org.jetbrains.plugins.dubbo.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.plugins.dubbo.dubboServiceAnnotationName
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt

class DubboServiceFileIndex : ScalarIndexExtension<String>() {
    override fun getName() = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer {
            if (it.contentAsText.contains("@DubboService")) {
                mapOf("dubbo" to null)
            } else {
                emptyMap()
            }
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getVersion() = 0

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter {
            it.name.endsWith(".java") || it.name.endsWith(".kt")
        }
    }

    override fun dependsOnFileContent() = true

    companion object {
        val NAME = ID.create<String, Void?>("dubbo.DubboServiceFileIndex")

        fun findDubboServiceFiles(project: Project): List<PsiFile> {
            val fileBasedIndex = FileBasedIndex.getInstance()
            val psiManager = PsiManager.getInstance(project)
            return ReadAction.compute<List<PsiFile>, Throwable> {
                fileBasedIndex.getContainingFiles(NAME, "dubbo", GlobalSearchScope.projectScope(project))
                    .map { psiManager.findFile(it)!! }
                    .toList()
            }
        }

        fun findRelatedElement(project: Project, serviceName: String, methodSignature: String): PsiElement? {
            findDubboServiceFiles(project).forEach { psiFile ->
                val psiJavaClass = extractFirstClassFromJavaOrKt(psiFile)
                if (psiJavaClass != null) {
                    val isDubboService = psiJavaClass.hasAnnotation(dubboServiceAnnotationName)
                    if (isDubboService) {
                        val dubboService = extractDubboService(psiJavaClass)
                        val serviceFullName = dubboService.serviceName
                        if (serviceName == serviceFullName) {
                            psiJavaClass
                                .methods
                                .forEach { psiMethod ->
                                    if (methodSignature.startsWith(psiMethod.name + "(")) {
                                        val paramTypes = psiMethod.parameterList.parameters
                                            .joinToString(",", "(", ")") { param ->
                                                (param.type as PsiClassReferenceType).canonicalText
                                            }
                                        if (methodSignature == "${psiMethod.name}${paramTypes}") {
                                            return psiMethod
                                        }
                                    }
                                }
                        }
                    }
                }
            }
            return null
        }
    }
}