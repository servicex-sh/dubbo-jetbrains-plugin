package org.jetbrains.plugins.dubbo.requests

import com.caucho.hessian.io.Hessian2Input
import com.caucho.hessian.io.HessianSerializerInput
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.dubbo.restClient.execution.DubboBodyFileHint
import org.jetbrains.plugins.dubbo.restClient.execution.DubboRequest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

@Suppress("UnstableApiUsage")
class DubboRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(dubboRequest: DubboRequest): CommonClientResponse {
        val dubboUri = dubboRequest.dubboURI
        var responseJsonBody = ""
        try {
            Socket(dubboUri.host, dubboRequest.port).use { clientSocket ->
                val invocation = DubboRpcInvocation(
                    dubboRequest.serviceName, dubboRequest.methodName,
                    dubboRequest.paramsTypeArray, dubboRequest.arguments
                )
                val contentBytes = invocation.toBytes()
                val headerBytes = invocation.frameHeaderBytes(0L, contentBytes.size)
                val output: OutputStream = clientSocket.getOutputStream()
                output.write(headerBytes)
                output.write(contentBytes)
                val inputStream: InputStream = clientSocket.getInputStream()
                val data: ByteArray = extractData(inputStream)
                val input: Hessian2Input = HessianSerializerInput(ByteArrayInputStream(data))
                val responseMark = input.readObject() as Int
                if (responseMark == 5 || responseMark == 2) { // null return
                    responseJsonBody = ""
                } else {
                    val result = input.readObject()
                    if (responseMark == 3 || responseMark == 0) { //exception return
                        return DubboClientResponse(
                            CommonClientResponseBody.Text(responseJsonBody, DubboBodyFileHint.jsonBodyFileHint("dubbo-result.json")),
                            "application/json", "Error", result.toString()
                        )
                    } else {
                        if (result is String || result is Number) {
                            responseJsonBody = result.toString()
                        } else {
                            val objectMapper = ObjectMapper()
                            responseJsonBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return DubboClientResponse(
                CommonClientResponseBody.Text(responseJsonBody, DubboBodyFileHint.jsonBodyFileHint("dubbo-result.json")),
                "application/json", "Error", e.message
            )
        }
        return DubboClientResponse(CommonClientResponseBody.Text(responseJsonBody, DubboBodyFileHint.jsonBodyFileHint("dubbo-result.json")))
    }

    private fun extractData(inputStream: InputStream): ByteArray {
        val bos = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var readCount: Int
        var counter = 0
        do {
            readCount = inputStream.read(buf)
            var startOffset = 0
            var length = readCount
            if (counter == 0) {
                startOffset = 16
                length = readCount - 16
            }
            bos.write(buf, startOffset, length)
            counter++
        } while (readCount == 1024)
        return bos.toByteArray()
    }

}