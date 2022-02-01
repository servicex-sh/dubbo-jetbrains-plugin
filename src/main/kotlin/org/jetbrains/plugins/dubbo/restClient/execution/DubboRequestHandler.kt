package org.jetbrains.plugins.dubbo.restClient.execution

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext
import org.jetbrains.plugins.dubbo.requests.DubboRequestManager

@Suppress("UnstableApiUsage")
class DubboRequestHandler : RequestHandler<DubboRequest> {

    override fun execute(request: DubboRequest, runContext: RunContext): CommonClientResponse {
        val rsocketRequestManager = runContext.project.getService(DubboRequestManager::class.java)
        return if (request.httpMethod == "DUBBO") {
            rsocketRequestManager.requestResponse(request)
        } else {
            object : CommonClientResponse {
                override val body: CommonClientResponseBody
                    get() = CommonClientResponseBody.Empty()
                override var executionTime: Long?
                    get() = 0
                    set(value) {}
            }
        }
    }

    override fun prepareExecutionEnvironment(request: DubboRequest, runContext: RunContext) {

    }
}