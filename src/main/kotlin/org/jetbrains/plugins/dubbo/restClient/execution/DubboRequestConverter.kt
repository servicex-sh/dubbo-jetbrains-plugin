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
            url = substitutor.getValue(httpRequest.requestTarget!!)
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        val host = headers?.getOrDefault("Host", "localhost")!!
        url = convertToDubboUrl(url, host)
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

    private fun convertToDubboUrl(url: String, host: String): String {
        var tempUri = url
        if (!tempUri.contains("://")) { //without schema
            if (tempUri.contains(":")) { //contains host
                tempUri = "dubbo://${tempUri}"
            } else { //path only
                if (host.contains("://")) { //host with schema
                    tempUri = "${host.trim('/')}/${url.trim('/')}"
                } else {
                    tempUri = "dubbo://$host/${url.trim('/')}"
                }
            }
        }
        if (!tempUri.startsWith("dubbo")) {
            tempUri = tempUri.replace("http://", "dubbo://")
        }
        return tempUri
    }

}