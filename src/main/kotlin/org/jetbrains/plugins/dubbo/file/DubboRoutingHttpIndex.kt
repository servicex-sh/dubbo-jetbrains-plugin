package org.jetbrains.plugins.dubbo.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.kotlin.utils.keysToMap

class DubboRoutingHttpIndex : ScalarIndexExtension<String>() {
    override fun getName() = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { fileContent ->
            fileContent.contentAsText.lines()
                .filter { it.startsWith("DUBBO ") }
                .map {
                    var routing = it.substring(it.indexOf(' ')).trim()
                    if (routing.contains(':')) {
                        val offset = routing.indexOf('/', routing.indexOf(':'))
                        routing = routing.substring(offset + 1)
                    }
                    if (routing.startsWith('/')) {
                        routing.substring(1)
                    }
                    if (routing.contains('(')) {
                        routing = routing.substring(0, routing.indexOf('('))
                    } else if (routing.contains('?')) {
                        routing = routing.substring(0, routing.indexOf('?'))
                    }
                    routing
                }.keysToMap { null }
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getVersion() = 0

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter {
            it.name.endsWith(".http") || it.name.endsWith(".rest")
        }
    }

    override fun dependsOnFileContent() = true

    companion object {
        val NAME = ID.create<String, Void?>("dubbo.routingHttpIndex")

        fun findAllDubboRouting(project: Project): Collection<String> {
            val fileBasedIndex = FileBasedIndex.getInstance()
            return fileBasedIndex.getAllKeys(NAME, project)
        }

        fun findHttpFiles(project: Project, routing: String): Collection<VirtualFile> {
            val fileBasedIndex = FileBasedIndex.getInstance()
            return fileBasedIndex.getContainingFiles(NAME, routing, GlobalSearchScope.allScope(project))
        }
    }

}