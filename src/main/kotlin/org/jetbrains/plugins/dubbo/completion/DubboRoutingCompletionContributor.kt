package org.jetbrains.plugins.dubbo.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.httpClient.http.request.psi.HttpPathAbsolute
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.httpClient.http.request.psi.HttpRequestTarget
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.dubbo.dubboIcon
import org.jetbrains.plugins.dubbo.dubboServiceAnnotationName
import org.jetbrains.plugins.dubbo.file.DubboServiceFileIndex
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt

class DubboRoutingCompletionContributor : CompletionContributor() {
    companion object {
        val dubboRoutingCapture: PsiElementPattern.Capture<LeafPsiElement> = PlatformPatterns.psiElement(LeafPsiElement::class.java)
            .withParent(object : ElementPattern<PsiElement?> {
                override fun accepts(o: Any?): Boolean {
                    return o is HttpPathAbsolute || o is HttpRequestTarget
                }

                override fun accepts(o: Any?, context: ProcessingContext): Boolean {
                    return accepts(o)
                }

                override fun getCondition(): ElementPatternCondition<PsiElement?>? {
                    return null
                }
            })
    }

    init {
        extend(CompletionType.BASIC, dubboRoutingCapture, DubboRoutingProvider())
    }

    private class DubboRoutingProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val element = parameters.position
            val parent = element.parent
            var prefix = trimDummy(parent.text)
            if (prefix == "/") {
                prefix = ""
            } else if (prefix.startsWith("dubbo://")) {
                val offset = prefix.indexOf('/', 8)
                if (offset > 8) {
                    prefix = prefix.substring(offset + 1)
                } else {
                    prefix = ""
                }
            }
            val httpRequest = element.parentOfType<HttpRequest>()
            if (httpRequest != null && "DUBBO" == httpRequest.httpMethod) {
                DubboServiceFileIndex.findDubboServiceFiles(httpRequest.project).forEach { psiFile ->
                    val psiJavaClass = extractFirstClassFromJavaOrKt(psiFile)
                    if (psiJavaClass != null) {
                        val dubboServiceAnnotation = psiJavaClass.hasAnnotation(dubboServiceAnnotationName)
                        if (dubboServiceAnnotation) {
                            val dubboServiceInterface = extractDubboService(psiJavaClass)
                            val serviceFullName = dubboServiceInterface.serviceName
                            if (prefix.isEmpty() || serviceFullName.contains(prefix)) { // class name completion
                                result.addElement(LookupElementBuilder.create(serviceFullName).withIcon(dubboIcon))
                            } else if (prefix.contains(serviceFullName)) {  // method completion
                                dubboServiceInterface.serviceInterface
                                    .methods
                                    .forEach { psiMethod ->
                                        val paramTypes = psiMethod.parameterList.parameters
                                            .joinToString(",", "(", ")") { param ->
                                                (param.type as PsiClassReferenceType).canonicalText
                                            }
                                        val methodSignature = "${psiMethod.name}${paramTypes}"
                                        result.addElement(LookupElementBuilder.create(methodSignature).withIcon(dubboIcon))
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