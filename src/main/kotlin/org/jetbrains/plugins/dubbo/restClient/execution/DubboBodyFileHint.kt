package org.jetbrains.plugins.dubbo.restClient.execution

import com.intellij.httpClient.execution.common.CommonClientBodyFileHint
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType

@Suppress("UnstableApiUsage")
class DubboBodyFileHint(override val fileExtensionHint: String?, override val fileNameHint: String?, override val fileTypeHint: FileType?) : CommonClientBodyFileHint {

    companion object {
        
        fun jsonBodyFileHint(fileName: String): DubboBodyFileHint {
            return if (fileName.endsWith(".json")) {
                DubboBodyFileHint("json", fileName, JsonFileType.INSTANCE)
            } else {
                DubboBodyFileHint("json", "${fileName}.json", JsonFileType.INSTANCE)
            }
        }
    }
}