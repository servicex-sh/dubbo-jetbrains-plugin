package org.jetbrains.plugins.dubbo.navigation

import com.intellij.httpClient.http.request.psi.HttpRequestTarget
import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.plugins.dubbo.completion.DubboRoutingCompletionContributor.Companion.dubboRoutingCapture
import org.jetbrains.plugins.dubbo.file.DubboServiceFileIndex
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt


@Suppress("UnstableApiUsage")
class DubboRoutingNavigation : DirectNavigationProvider {
    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (dubboRoutingCapture.accepts(element)) {
            val httpRequestTarget = element.parentOfType<HttpRequestTarget>()!!
            val host = httpRequestTarget.host
            val pathAbsolute = httpRequestTarget.pathAbsolute
            if (host != null || pathAbsolute != null) {
                val rsocketRouting = if (pathAbsolute != null) {
                    val path = pathAbsolute.text
                    path.substring(path.lastIndexOf('/') + 1)
                } else {
                    host!!.text
                }
                if (rsocketRouting.isNotEmpty()) {
                    DubboServiceFileIndex.findDubboServiceFiles(element.project).forEach { psiFile ->
                        val psiJavaClass = extractFirstClassFromJavaOrKt(psiFile)
                        if (psiJavaClass != null) {
                            val isDubboService = psiJavaClass.hasAnnotation("org.apache.dubbo.config.annotation.DubboService")
                            if (isDubboService) {
                                val dubboService = extractDubboService(psiJavaClass)
                                val serviceFullName = dubboService.serviceName
                                if (rsocketRouting.startsWith(serviceFullName)) {
                                    dubboService.serviceInterface
                                        .methods
                                        .forEach {
                                            val routingKey = "${serviceFullName}.${it.name}"
                                            if (routingKey == rsocketRouting) {
                                                return it
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
}