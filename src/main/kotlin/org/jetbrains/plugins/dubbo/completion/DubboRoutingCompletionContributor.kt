package org.jetbrains.plugins.dubbo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.httpClient.http.request.psi.HttpRequestTarget
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.dubbo.dubboIcon
import org.jetbrains.plugins.dubbo.file.DubboServiceFileIndex
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt

class DubboRoutingCompletionContributor : CompletionContributor() {
    companion object {
        val dubboRoutingCapture: PsiElementPattern.Capture<LeafPsiElement> = PlatformPatterns.psiElement(LeafPsiElement::class.java)
            .withSuperParent(2, HttpRequestTarget::class.java)
    }

    init {
        extend(CompletionType.BASIC, dubboRoutingCapture, DubboRoutingProvider())
    }

    private class DubboRoutingProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val element = parameters.position
            val prefix = trimDummy(element.text)
            val httpRequest = element.parentOfType<HttpRequest>()
            if (httpRequest != null && "DUBBO" == httpRequest.httpMethod) {
                DubboServiceFileIndex.findDubboServiceFiles(httpRequest.project).forEach { psiFile ->
                    val psiJavaClass = extractFirstClassFromJavaOrKt(psiFile)
                    if (psiJavaClass != null) {
                        val rsocketService = psiJavaClass.hasAnnotation("org.apache.dubbo.config.annotation.DubboService")
                        if (rsocketService) {
                            val aliRSocketService = extractDubboService(psiJavaClass)
                            val serviceFullName = aliRSocketService.serviceName
                            if (prefix.isEmpty() || serviceFullName.contains(prefix)) { // class name completion
                                result.addElement(LookupElementBuilder.create(serviceFullName).withIcon(dubboIcon))
                            } else if (prefix.contains(serviceFullName)) {  // method completion
                                aliRSocketService.serviceInterface
                                    .methods
                                    .forEach {
                                        val routingKey = "${serviceFullName}.${it.name}"
                                        result.addElement(LookupElementBuilder.create(routingKey).withIcon(dubboIcon))
                                    }
                            }
                        }
                    }
                }
            }
        }

        private fun trimDummy(value: String): String {
            return StringUtil.trim(value.replace(CompletionUtil.DUMMY_IDENTIFIER, "").replace(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED, ""))
        }

    }

}