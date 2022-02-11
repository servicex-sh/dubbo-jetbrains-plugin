package org.jetbrains.plugins.dubbo.psi

import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.plugins.dubbo.dubboServiceAnnotationName

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
    val dubboServiceAnnotation = serviceImpPsiClass.getAnnotation(dubboServiceAnnotationName)!!
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
    val interfaceName = dubboServiceAnnotation.findAttributeValue("interfaceName")
    if (interfaceName != null) {
        val temp = interfaceName.text.trim('"')
        if (temp.isNotEmpty() && temp != "void") {
            serviceFullName = temp;
        }
    }
    return DubboService(serviceFullName, serviceInterfacePsiClass)
}


fun extractPsiMethod(element: PsiElement): PsiMethod? {
    if (element is PsiMethod) {
        return element
    } else if (element is KtNamedFunction) {
        val ktNamedFunction: KtNamedFunction = element
        val lightMethods = ktNamedFunction.toLightMethods()
        if (lightMethods.isNotEmpty()) {
            return lightMethods[0]
        }
    }
    return null
}