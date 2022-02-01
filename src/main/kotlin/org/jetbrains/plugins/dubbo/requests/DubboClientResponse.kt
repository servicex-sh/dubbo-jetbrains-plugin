package org.jetbrains.plugins.dubbo.requests

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.UserBinaryFileType

@Suppress("UnstableApiUsage")
class DubboClientResponse(
    private val responseBody: CommonClientResponseBody = CommonClientResponseBody.Empty(),
    private val dataMimeType: String = "application/json",
    private val status: String = "OK",
    private val error: String? = null,
    override var executionTime: Long? = 0
) : CommonClientResponse {
    override val body: CommonClientResponseBody
        get() = responseBody

    override fun suggestFileTypeForPresentation(): FileType? {
        if (body is CommonClientResponseBody.Text) {
            if ((body as CommonClientResponseBody.Text).content[0] == '{') {
                return JsonFileType.INSTANCE
            }
        }
        return if (dataMimeType == "application/json") {
            PlainTextFileType.INSTANCE
        } else if (dataMimeType.contains("text")) {
            PlainTextFileType.INSTANCE
        } else if (dataMimeType.contains("xml")) {
            XmlFileType.INSTANCE
        } else {
            UserBinaryFileType.INSTANCE
        }
    }

    override val statusPresentation: String
        get() = status

    override val presentationHeader: String
        get() {
            return if (status == "OK") {
                "Dubbo/2.0.2 200 OK\n"
            } else {
                "Dubbo/2.0.2 ERROR\n${(error ?: "")}\n"
            }
        }

    override val presentationFooter: String
        get() {
            return "Execution time: $executionTime ms"
        }
}
