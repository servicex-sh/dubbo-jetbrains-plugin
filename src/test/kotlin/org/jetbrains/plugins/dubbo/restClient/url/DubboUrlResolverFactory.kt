@file:Suppress("UnstableApiUsage")

package org.jetbrains.plugins.dubbo.restClient.url

import com.intellij.microservices.url.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.dubbo.file.DubboServiceFileIndex

/**
 *  dubbo:// schema resolve support
 * <microservices.urlResolverFactory implementation="org.jetbrains.plugins.dubbo.restClient.url.DubboUrlResolverFactory"/>
 */
class DubboUrlResolverFactory : UrlResolverFactory {
    override fun forProject(project: Project): UrlResolver {
        return DubboUrlResolver(project)
    }
}

class DubboUrlResolver(private val project: Project) : UrlResolver {
    override val supportedSchemes: List<String>
        get() {
            return listOf("dubbo://")
        }

    override fun getVariants(): Iterable<UrlTargetInfo> {
        return listOf()
    }

    override fun resolve(request: UrlResolveRequest): Iterable<UrlTargetInfo> {
        return listOf(RSocketUrlTargetInfo(project, request.path))
    }

    override fun getAuthorityHints(schema: String?): List<Authority.Exact> {
        return listOf()
    }
}

class RSocketUrlTargetInfo(private val project: Project, private val urlPath: UrlPath) : UrlTargetInfo {
    override val authorities: List<Authority> = listOf()
    override val path: UrlPath = urlPath
    override val schemes: List<String> = listOf("dubbo")

    override val methods: Set<String> = setOf("DUBBO")

    override fun resolveToPsiElement(): PsiElement? {
        var dubboRouting = urlPath.getPresentation()
        if (dubboRouting.indexOf('/') > 8) {
            dubboRouting = dubboRouting.substring(dubboRouting.indexOf('/', 8) + 1)
        }
        if (dubboRouting.contains('?')) {
            dubboRouting = dubboRouting.substring(0, dubboRouting.indexOf('?'))
        }
        val parts = dubboRouting.split('/')
        return DubboServiceFileIndex.findRelatedElement(project, parts[0], parts[1])
    }

}

