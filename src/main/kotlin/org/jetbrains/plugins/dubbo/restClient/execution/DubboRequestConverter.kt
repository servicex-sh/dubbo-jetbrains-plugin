package org.jetbrains.plugins.dubbo.restClient.execution

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class DubboRequestConverter : RequestConverter<DubboRequest>() {

    override val requestType: Class<DubboRequest> get() = DubboRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): DubboRequest {
        var url = ""
        var requestType = ""
        var requestBody: String? = null
        var headers: Map<String, String>? = null
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            val requestTarget = httpRequest.requestTarget!!
            val requestTargetText = requestTarget.text
            url = httpRequest.getHttpUrl(substitutor)!!
            if (!url.contains('?') && requestTargetText.contains("?")) {
                url += requestTargetText.substring(requestTargetText.indexOf('?'))
            }
            if (url.startsWith("http://")) { //compatible mode
                url = url.replace("http://", "dubbo://")
            }
            if (!url.startsWith("dubbo://") && url.contains(":")) {
                url = "dubbo://${url}"
            }
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return DubboRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: DubboRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### dubbo request").append("\n")
        builder.append("RSOCKET ${request.dubboURI}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}