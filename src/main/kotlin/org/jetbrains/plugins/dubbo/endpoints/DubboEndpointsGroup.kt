package org.jetbrains.plugins.dubbo.endpoints

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.jetbrains.plugins.dubbo.dubboServiceAnnotationName
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt

class DubboEndpointsGroup(private val project: Project, private val psiFile: PsiFile) {

    fun endpoints(): List<DubboEndpoint> {
        val endpoints = mutableListOf<DubboEndpoint>()
        val psiClass = extractFirstClassFromJavaOrKt(psiFile)
        if (psiClass != null) {
            val isDubboService = psiClass.hasAnnotation(dubboServiceAnnotationName)
            if (isDubboService) {
                val dubboService = extractDubboService(psiClass)
                val serviceInterfaceMethods = dubboService.serviceInterface.methods.map { it.name }.toList()
                psiClass.methods
                    .filter {
                        serviceInterfaceMethods.contains(it.name)
                    }
                    .map { psiMethod ->
                        val paramTypes = psiMethod.parameterList.parameters
                            .joinToString(",", "(", ")") { param ->
                                (param.type as PsiClassReferenceType).canonicalText
                            }
                        DubboEndpoint("${dubboService.serviceName}/${psiMethod}${paramTypes}", psiMethod)
                    }.forEach {
                        endpoints.add(it)
                    }
            }
        }
        return endpoints
    }


}