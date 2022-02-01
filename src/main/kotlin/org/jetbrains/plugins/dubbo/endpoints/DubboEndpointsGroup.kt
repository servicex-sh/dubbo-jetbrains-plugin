package org.jetbrains.plugins.dubbo.endpoints

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.dubbo.psi.extractDubboService
import org.jetbrains.plugins.dubbo.psi.extractFirstClassFromJavaOrKt

class DubboEndpointsGroup(private val project: Project, private val psiFile: PsiFile) {

    fun endpoints(): List<DubboEndpoint> {
        val endpoints = mutableListOf<DubboEndpoint>()
        val psiClass = extractFirstClassFromJavaOrKt(psiFile)
        if (psiClass != null) {
            val isDubboService = psiClass.hasAnnotation("org.apache.dubbo.config.annotation.DubboService")
            if (isDubboService) {
                val dubboService = extractDubboService(psiClass)
                val serviceInterfaceMethods = dubboService.serviceInterface.methods.map { it.name }.toList()
                psiClass.methods
                    .filter {
                        serviceInterfaceMethods.contains(it.name)
                    }
                    .map {
                        DubboEndpoint("${dubboService.serviceName}.${it.name}", it)
                    }.forEach {
                        endpoints.add(it)
                    }
            }
        }
        return endpoints
    }


}