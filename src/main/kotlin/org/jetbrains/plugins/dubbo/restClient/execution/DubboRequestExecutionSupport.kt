package org.jetbrains.plugins.dubbo.restClient.execution

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class DubboRequestExecutionSupport : RequestExecutionSupport<DubboRequest> {

    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method == "DUBBO"
    }

    override fun getRequestConverter(): RequestConverter<DubboRequest> {
        return DubboRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<DubboRequest> {
        return DubboRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("DUBBO")
    }

    override val needsScheme: Boolean
        get() = false
}