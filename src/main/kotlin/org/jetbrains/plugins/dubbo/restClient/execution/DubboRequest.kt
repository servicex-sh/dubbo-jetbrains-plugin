package org.jetbrains.plugins.dubbo.restClient.execution

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.httpClient.execution.common.CommonClientRequest
import com.intellij.util.queryParameters
import java.net.URI


@Suppress("UnstableApiUsage")
class DubboRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, val headers: Map<String, String>?) : CommonClientRequest {
    val dubboURI: URI
    var serviceName: String
    var serviceVersion = "0.0.0"
    val methodName: String
    val paramsTypeArray: Array<String>
    val arguments: Array<Any>

    companion object {
        private val shortTypeMapping = HashMap<String, String>().apply {
            put("boolean", "java.lang.Boolean")
            put("Boolean", "java.lang.Boolean")
            put("Boolean[]", "java.lang.Boolean[]")
            put("byte", "java.lang.Byte")
            put("Byte", "java.lang.Byte")
            put("Byte[]", "java.lang.Byte[]")
            put("char", "java.lang.Char")
            put("Char", "java.lang.Char")
            put("Char[]", "java.lang.Char[]")
            put("short", "java.lang.Short")
            put("Short", "java.lang.Short")
            put("Short[]", "java.lang.Short[]")
            put("int", "java.lang.Integer")
            put("Integer", "java.lang.Integer")
            put("Integer[]", "java.lang.Integer[]")
            put("long", "java.lang.Long")
            put("Long", "java.lang.Long")
            put("Long[]", "java.lang.Long[]")
            put("float", "java.lang.Float")
            put("Float", "java.lang.Float")
            put("Float[]", "java.lang.Float[]")
            put("double", "java.lang.Double")
            put("Double", "java.lang.Double")
            put("Double[]", "java.lang.Double[]")
            put("String", "java.lang.String")
            put("String[]", "java.lang.String[]")
        }
    }

    init {
        dubboURI = URI.create(URL!!)
        val queryParameters = dubboURI.queryParameters
        if (queryParameters.contains("version")) {
            this.serviceVersion = queryParameters["version"]!!
        }
        var methodSignature = queryParameters["method"]
        serviceName = dubboURI.path.substring(1)
        if (serviceName.contains('/')) {
            val parts = serviceName.split('/')
            serviceName = parts[0]
            methodSignature = parts[1]
        }
        if (methodSignature!!.contains("(")) {
            methodName = methodSignature.substring(0, methodSignature.indexOf('('))
            val paramTypes = methodSignature.substring(methodSignature.indexOf('(') + 1, methodSignature.indexOf(')'))
            if (paramTypes.isNotEmpty()) {
                paramsTypeArray = paramTypes.split(",").map { shortTypeMapping.getOrDefault(it, it) }.toTypedArray()
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