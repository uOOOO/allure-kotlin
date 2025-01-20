package io.qameta.allure.sample.junit4.android.util

import io.qameta.allure.kotlin.Allure
import io.qameta.allure.sample.junit4.android.util.Reflection.getClass
import io.qameta.allure.sample.junit4.android.util.Reflection.getMethod
import java.lang.reflect.Method

object AllureWrapper {
    private val allureClass = Allure.getClass(ClassLoader.getSystemClassLoader())

    private fun getStepMethod(): Method {
        return allureClass.getMethod("step", "String", "Function1")
    }

    fun <T> step(
        name: String = "step",
        block: StepContextWrapper.() -> T
    ): T {
        val method = getStepMethod()

        val blockWrapper: Any.() -> T = {
            block(StepContextWrapper(this))
        }

        @Suppress("UNCHECKED_CAST")
        return method.invoke(null, name, blockWrapper) as T
    }
}

class StepContextWrapper(@PublishedApi internal val stepContext: Any) {
    fun name(name: String) {
        stepContext.getMethod("name", String::class.java)
            .invoke(stepContext, name)
    }

    inline fun <reified T> parameter(name: String, value: T): T {
        return stepContext.getMethod("parameter", String::class.java, Object::class.java)
            .invoke(stepContext, name, value) as T
    }
}

object Reflection {
    fun Any.getClass(classLoader: ClassLoader): Class<*> {
        return classLoader.loadClass(this::class.java.name)
    }

    fun Any.getMethod(name: String, vararg parameterTypes: Class<*>): Method {
        return this::class.java.getDeclaredMethod(name, *parameterTypes)
            .apply { isAccessible = true }
    }

    fun Class<*>.getMethod(name: String, vararg parameterTypes: String): Method {
        return this.declaredMethods
            .filter { it.name == name }
            .filter { it.parameterCount == parameterTypes.size }
            .first {
                it.parameters.filterIndexed { index, parameter ->
                    parameter.type.toString().endsWith(parameterTypes[index])
                }.size == it.parameters.size
            }
            .apply { isAccessible = true }
    }
}
