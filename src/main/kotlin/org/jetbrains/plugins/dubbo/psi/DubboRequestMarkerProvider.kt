package org.jetbrains.plugins.dubbo.psi

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.httpClient.http.request.HttpRequestFileType
import com.intellij.httpClient.http.request.psi.HttpRequestBlock
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.plugins.dubbo.dubboServiceAnnotationName
import org.jetbrains.plugins.dubbo.file.DubboRoutingHttpIndex


class DubboRequestMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        var psiMethod: PsiMethod? = null;
        if (element is PsiMethod) {
            psiMethod = element
        } else if (element is KtNamedFunction) {
            val ktNamedFunction: KtNamedFunction = element
            val lightMethods = ktNamedFunction.toLightMethods()
            if (lightMethods.isNotEmpty()) {
                psiMethod = lightMethods[0]
            }
        }
        if (psiMethod != null) {
            val psiClass = psiMethod.parentOfType<PsiClass>()
            if (psiClass != null && psiClass.hasAnnotation(dubboServiceAnnotationName)) {
                val serviceName = extractDubboService(psiClass).serviceName
                val routing = "${serviceName}/${psiMethod.name}"
                val project = element.project
                if (DubboRoutingHttpIndex.findAllDubboRouting(project).contains(routing)) {
                    val navigationBuilder = dubboRequestsNavigationBuilder(project, routing)
                    if (navigationBuilder != null) {
                        result.add(navigationBuilder.createLineMarkerInfo(psiMethod.nameIdentifier!!))
                    }
                }
            }
        }
    }

    private fun dubboRequestsNavigationBuilder(project: Project, routing: String): NavigationGutterIconBuilder<PsiElement>? {
        val httpFiles = DubboRoutingHttpIndex.findHttpFiles(project, routing)
        if (httpFiles.isNotEmpty()) {
            val targets = mutableListOf<PsiElement>()
            httpFiles.forEach { virtualFile ->
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                psiFile?.getChildrenOfType<HttpRequestBlock>()?.forEach {
                    val requestTarget = it.request.requestTarget!!.text
                    if (requestTarget.contains(routing)) {
                        targets.add(it.request.requestTarget!!)
                    }
                }
            }
            return NavigationGutterIconBuilder.create(HttpRequestFileType.INSTANCE.icon)
                .setTargets(targets)
                .setTooltipText("Navigate to Dubbo request")
        }
        return null
    }

}

