package org.jetbrains.plugins.dubbo.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

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
    }
}