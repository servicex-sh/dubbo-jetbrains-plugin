package org.jetbrains.plugins.dubbo.navigation

import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.httpClient.http.request.psi.HttpRequestTarget
import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.parentOfType
import org.jetbrains.plugins.dubbo.completion.DubboRoutingCompletionContributor.Companion.dubboRoutingCapture
import org.jetbrains.plugins.dubbo.file.DubboServiceFileIndex
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt


@Suppress("UnstableApiUsage")
class DubboRoutingNavigation : DirectNavigationProvider {
    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (dubboRoutingCapture.accepts(element)) {
            val highLightText = element.text
            val httpRequestTarget = element.parentOfType<HttpRequestTarget>()!!
            val httpRequest = httpRequestTarget.parentOfType<HttpRequest>()
            if (httpRequest != null && httpRequest.httpMethod == "DUBBO") {
                val requestTarget = httpRequestTarget.text
                if (requestTarget.isNotEmpty()) {
                    val routing = extractServiceAndMethodSignature(requestTarget)
                    if (routing.contains(highLightText)) {
                        if (routing.isNotEmpty() && routing.contains('/')) {
                            val parts = routing.split('/')
                            val serviceName = parts[0];
                            val methodSignature = parts[1]
                            DubboServiceFileIndex.findDubboServiceFiles(element.project).forEach { psiFile ->
                                val psiJavaClass = extractFirstClassFromJavaOrKt(psiFile)
                                if (psiJavaClass != null) {
                                    val isDubboService = psiJavaClass.hasAnnotation("org.apache.dubbo.config.annotation.DubboService")
                                    if (isDubboService) {
                                        val dubboService = extractDubboService(psiJavaClass)
                                        val serviceFullName = dubboService.serviceName
                                        if (serviceName == serviceFullName) {
                                            if (highLightText == serviceFullName) { //navigator to class impl
                                                return psiJavaClass
                                            }
                                            psiJavaClass
                                                .methods
                                                .forEach { psiMethod ->
                                                    if (methodSignature.startsWith(psiMethod.name + "(")) {
                                                        val paramTypes = psiMethod.parameterList.parameters
                                                            .joinToString(",", "(", ")") { param ->
                                                                (param.type as PsiClassReferenceType).canonicalText
                                                            }
                                                        if (methodSignature == "${psiMethod.name}${paramTypes}") {
                                                            return psiMethod
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    fun extractServiceAndMethodSignature(requestTarget: String): String {
        var routing = requestTarget;
        if (routing.startsWith("dubbo://")) {
            val offset = routing.indexOf('/', 8)
            if (offset > 1) {
                routing = routing.substring(offset + 1)
            } else {
                routing = ""
            }
        } else if (routing.startsWith("/")) { // "/xxx"
            routing = routing.substring(1);
        } else if (routing.contains(":") && routing.contains('/')) { // "localhost:20880/xxx"
            routing = routing.substring(routing.indexOf('/') + 1)
        }
        if (routing.contains('?')) {
            routing = routing.substring(0, routing.indexOf('?'))
        }
        return routing
    }
}