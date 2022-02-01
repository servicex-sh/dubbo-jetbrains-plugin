package org.jetbrains.plugins.dubbo.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile

data class DubboService(val serviceName: String, val serviceInterface: PsiClass)


fun extractFirstClassFromJavaOrKt(psiFile: PsiFile): PsiClass? {
    return when (psiFile) {
        is KtFile -> {
            psiFile.classes.firstOrNull()
        }
        is PsiJavaFile -> {
            psiFile.classes.firstOrNull()
        }
        else -> {
            null
        }
    }
}

fun extractDubboService(serviceImpPsiClass: PsiClass): DubboService {
    var serviceFullName = serviceImpPsiClass.name!!
    var serviceInterfacePsiClass: PsiClass = serviceImpPsiClass
    val dubboServiceAnnotation = serviceImpPsiClass.getAnnotation("org.apache.dubbo.config.annotation.DubboService")!!
    val serviceInterface = dubboServiceAnnotation.findAttributeValue("interfaceClass")
    if (serviceInterface != null && serviceInterface.text.trim('"').isNotEmpty()) {
        val serviceInterfaceClassName = serviceInterface.text.trim('"')
            .replace(".class", "")
            .replace("::class", "")
        val serviceInterfaceClassType = serviceImpPsiClass.superTypes.firstOrNull {
            it.className == serviceInterfaceClassName
        }
        if (serviceInterfaceClassType != null) {
            serviceFullName = serviceInterfaceClassType.canonicalText
            serviceInterfacePsiClass = serviceInterfaceClassType.resolve()!!
        } else {
            serviceFullName = serviceInterfaceClassName
        }
    }
    val serviceName = dubboServiceAnnotation.findAttributeValue("interfaceName")
    if (serviceName != null && serviceName.text.trim('"').isNotEmpty()) {
        serviceFullName = serviceName.text.trim('"')
    }
    return DubboService(serviceFullName, serviceInterfacePsiClass)
}
