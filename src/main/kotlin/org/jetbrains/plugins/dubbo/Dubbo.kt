package org.jetbrains.plugins.dubbo

import com.intellij.openapi.util.IconLoader
import org.jetbrains.plugins.dubbo.psi.DubboService

val dubboIcon = IconLoader.findIcon("dubbo-16x16.png", DubboService::class.java )!!
var dubboServiceAnnotationName = "org.apache.dubbo.config.annotation.DubboService"
