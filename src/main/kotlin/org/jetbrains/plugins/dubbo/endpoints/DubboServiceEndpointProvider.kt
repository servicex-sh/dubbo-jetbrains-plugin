package org.jetbrains.plugins.dubbo.endpoints

import com.intellij.lang.java.JavaLanguage
import com.intellij.microservices.endpoints.*
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.plugins.dubbo.dubboIcon
import org.jetbrains.plugins.dubbo.file.DubboServiceFileIndex

@Suppress("UnstableApiUsage")
class DubboServiceEndpointProvider : EndpointsProvider<DubboEndpointsGroup, DubboEndpoint> {

    companion object {
        var DUBBO_ENDPOINT_TYPE = EndpointType("Dubbo", dubboIcon) {
            "Dubbo"
        }
        var DUBBO_FRAMEWORK = FrameworkPresentation(
            "Dubbo",
            "Dubbo Server",
            dubboIcon
        )
    }

    override val endpointType: EndpointType
        get() = DUBBO_ENDPOINT_TYPE
    override val presentation: FrameworkPresentation
        get() = DUBBO_FRAMEWORK

    override fun getEndpointData(group: DubboEndpointsGroup, endpoint: DubboEndpoint, dataId: String): Any? {
        return endpoint.getData(dataId)
    }

    override fun getEndpointGroups(project: Project, filter: EndpointsFilter): List<DubboEndpointsGroup> {
        if (filter == ExternalEndpointsFilter) {
            return emptyList()
        }
        val groups = DubboServiceFileIndex.findDubboServiceFiles(project)
            .map { psiFile ->
                DubboEndpointsGroup(project, psiFile)
            }
            .toList()
        return groups
    }

    override fun getEndpointPresentation(group: DubboEndpointsGroup, endpoint: DubboEndpoint): ItemPresentation {
        return endpoint
    }

    override fun getEndpoints(group: DubboEndpointsGroup): Iterable<DubboEndpoint> {
        return group.endpoints()
    }

    override fun getModificationTracker(project: Project): ModificationTracker {
        return PsiModificationTracker.SERVICE.getInstance(project).forLanguage(JavaLanguage.INSTANCE)
    }

    override fun getStatus(project: Project): EndpointsProvider.Status = when {
        getEndpointGroups(project, object : EndpointsFilter {}).isNotEmpty() -> EndpointsProvider.Status.AVAILABLE
        else -> EndpointsProvider.Status.HAS_ENDPOINTS
    }

    override fun isValidEndpoint(group: DubboEndpointsGroup, endpoint: DubboEndpoint): Boolean {
        return true
    }
}