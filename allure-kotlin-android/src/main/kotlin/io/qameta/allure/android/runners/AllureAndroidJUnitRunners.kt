package io.qameta.allure.android.runners

import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import io.qameta.allure.android.AllureAndroidLifecycle
import io.qameta.allure.android.internal.isDeviceTest
import io.qameta.allure.android.listeners.ExternalStoragePermissionsListener
import io.qameta.allure.android.writer.TestStorageResultsWriter
import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.junit4.AllureJunit4
import io.qameta.allure.kotlin.util.PropertiesUtils
import org.junit.runner.*
import org.junit.runner.manipulation.*
import org.junit.runner.notification.*
import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.internal.bytecode.InstrumentationConfiguration

/**
 * Wrapper that attaches the [AllureJunit4] listener.
 *
 * For device tests, delegates to [AndroidJUnit4].
 * For Robolectric tests, delegates to [AllureRobolectricRunner] which excludes allure
 * packages from Robolectric's SandboxClassLoader so that the test code and the listener
 * share the same [Allure] singleton.
 */
open class AllureAndroidJUnit4(clazz: Class<*>) : Runner(), Filterable, Sortable {

    private val delegate: Runner = if (isDeviceTest()) {
        AndroidJUnit4(clazz)
    } else {
        AllureRobolectricRunner(clazz)
    }

    override fun run(notifier: RunNotifier?) {
        createListener()?.let {
            notifier?.addListener(it)
        }
        delegate.run(notifier)
    }

    private fun createListener(): RunListener? =
        if (isDeviceTest()) {
            createDeviceListener()
        } else {
            createRobolectricListener()
        }

    /**
     * Creates listener for the tests running on a device.
     *
     * In instrumentation tests the listeners are shared between the class runners,
     * hence extra logic has to be put in place, to avoid attaching the listener more than once.
     *
     * Check is made whether [AllureAndroidLifecycle] has been specified as [Allure.lifecycle],
     * if so it means that in one way or another the listener has already been attached.
     */
    private fun createDeviceListener(): RunListener? {
        if (Allure.lifecycle is AllureAndroidLifecycle) return null

        val androidLifecycle = createAllureAndroidLifecycle()
        Allure.lifecycle = androidLifecycle
        return AllureJunit4(androidLifecycle)
    }

    protected open fun createAllureAndroidLifecycle() : AllureAndroidLifecycle =
        createDefaultAllureAndroidLifecycle()

    /**
     * Creates listener for tests running in an emulated Robolectric environment.
     *
     * The listeners are not shared between class runners, hence they have to be added to each class runner separately.
     */
    private fun createRobolectricListener(): RunListener? = AllureJunit4()

    override fun getDescription(): Description = delegate.description

    override fun filter(filter: Filter?) = (delegate as Filterable).filter(filter)

    override fun sort(sorter: Sorter?) = (delegate as Sortable).sort(sorter)
}

/**
 * Custom [AndroidJUnitRunner] that setups [AllureAndroidLifecycle] and attaches [AllureJunit4] listener.
 * It also automatically grants the external storage permission (required for the test results to be saved).
 */
open class AllureAndroidJUnitRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        Allure.lifecycle = createAllureAndroidLifecycle()
        val listenerArg = listOfNotNull(
            arguments.getCharSequence("listener"),
            AllureJunit4::class.java.name,
            ExternalStoragePermissionsListener::class.java.name.takeIf { useTestStorage }
        ).joinToString(separator = ",")
        arguments.putCharSequence("listener", listenerArg)
        super.onCreate(arguments)
    }

    protected open fun createAllureAndroidLifecycle() : AllureAndroidLifecycle =
        createDefaultAllureAndroidLifecycle()
}

/**
 * [AllureAndroidJUnitRunner] that additionally patches the instrumentation context using [MultiDex]
 */
open class MultiDexAllureAndroidJUnitRunner : AllureAndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        MultiDex.installInstrumentation(context, targetContext)
        super.onCreate(arguments)
    }
}

private fun createDefaultAllureAndroidLifecycle() : AllureAndroidLifecycle {
    if (useTestStorage) {
        return AllureAndroidLifecycle(TestStorageResultsWriter())
    }

    return AllureAndroidLifecycle()
}

private val useTestStorage: Boolean
    get() = PropertiesUtils.loadAllureProperties()
        .getProperty("allure.results.useTestStorage", "false")
        .toBoolean()

/**
 * Custom [RobolectricTestRunner] that excludes allure packages from Robolectric's
 * SandboxClassLoader.
 *
 * By default, Robolectric loads all non-system classes via its SandboxClassLoader,
 * creating separate class instances from the system ClassLoader. This causes the
 * [Allure] singleton in the test code to be a different instance from the one used
 * by the JUnit listener, so steps and attachments are not recorded.
 *
 * [doNotAcquirePackage][InstrumentationConfiguration.Builder] tells the
 * SandboxClassLoader to delegate allure classes to the parent (system) ClassLoader,
 * ensuring a single shared [Allure] instance.
 */
private open class AllureRobolectricRunner(clazz: Class<*>) : RobolectricTestRunner(clazz) {

    override fun createClassLoaderConfig(method: FrameworkMethod): InstrumentationConfiguration {
        return InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
            .doNotAcquirePackage("io.qameta.allure")
            .build()
    }
}

