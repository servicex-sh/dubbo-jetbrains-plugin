package org.jetbrains.plugins.dubbo.restClient.execution

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.httpClient.execution.common.CommonClientRequest
import com.intellij.util.queryParameters
import java.net.URI


@Suppress("UnstableApiUsage")
class DubboRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, val headers: Map<String, String>?) : CommonClientRequest {
    val dubboURI: URI
    val serviceName: String
    var serviceVersion = "0.0.0"
    val methodName: String
    val paramsTypeArray: Array<String>
    val arguments: Array<Any>
    val port: Int

    init {
        var tempUri = URL!!
        if (headers != null && headers.contains("Host")) {
            val host = headers["Host"]
            if (URL.startsWith("/")) {
                tempUri = "${host}${URL}"
            }
            if (!tempUri.startsWith("dubbo://")) {
                tempUri = "dubbo://${tempUri}"
            }
        }
        dubboURI = URI.create(tempUri)
        port = if (dubboURI.port <= 0) {
            20880
        } else {
            dubboURI.port
        }
        serviceName = dubboURI.path.substring(1)
        val queryParameters = dubboURI.queryParameters
        if (queryParameters.contains("version")) {
            this.serviceVersion = queryParameters["version"]!!
        }
        val methodSignature = queryParameters["method"]!!
        if (methodSignature.contains("(")) {
            methodName = methodSignature.substring(0, methodSignature.indexOf('('))
            val paramTypes = methodSignature.substring(methodSignature.indexOf('(') + 1, methodSignature.indexOf(')'))
            if (paramTypes.isNotEmpty()) {
                paramsTypeArray = paramTypes.split(",").map { it.trim() }.toTypedArray()
            } else {
                paramsTypeArray = arrayOf()
            }
        } else {
            methodName = methodSignature
            paramsTypeArray = arrayOf()
        }
        if (textToSend != null && textToSend.isNotEmpty()) {
            val body = textToSend.trim();
            if (body.startsWith("[")) {
                val objectMapper = ObjectMapper()
                val jsonArray = objectMapper.readValue<List<Any>>(body)
                arguments = jsonArray.map {
                    if (it is String) {
                        it
                    } else {
                        objectMapper.writeValueAsString(it)
                    }
                }.toTypedArray()
            } else {
                arguments = arrayOf(body.trim('"'))
            }
        } else {
            arguments = arrayOf()
        }
    }

}