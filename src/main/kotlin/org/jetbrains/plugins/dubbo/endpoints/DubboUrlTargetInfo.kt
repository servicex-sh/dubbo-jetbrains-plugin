package org.jetbrains.plugins.dubbo.endpoints

import com.intellij.microservices.url.Authority
import com.intellij.microservices.url.UrlPath
import com.intellij.microservices.url.UrlTargetInfo
import com.intellij.psi.PsiElement


@Suppress("UnstableApiUsage")
class DubboUrlTargetInfo(private val element: PsiElement, private val routing: String) : UrlTargetInfo {
    override val authorities: List<Authority>
        get() = emptyList()
    override val path: UrlPath
        get() = UrlPath.fromExactString(routing)
    override val schemes: List<String>
        get() = listOf()

    override fun resolveToPsiElement() = element

    override val methods: Set<String>
        get() = setOf("DUBBO")

    override val documentationPsiElement: PsiElement
        get() = element
}