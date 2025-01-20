package io.qameta.allure.sample.junit4.android

import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.sample.junit4.android.util.AllureWrapper.step
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch

// This test runs on RobolectricTestRunner.
@Epic("SampleRobolectricTest")
@RunWith(AllureAndroidJUnit4::class)
class SampleRobolectricTest {
    @Test
    @Feature("test1")
    fun test1() {
        step("step1-1") {
            step("step1-2") {

            }
        }
    }

    @Test
    @Feature("test2")
    fun test2() {
        val latch = CountDownLatch(2)

        step("step2-1") {
            val innerLatch = CountDownLatch(1)

            sleep(500)

            step("step2-2") {
                innerLatch.countDown()
            }

            innerLatch.await()
            latch.countDown()
        }

        step("step2-3") {
            val innerLatch = CountDownLatch(1)

            step("step2-4") {
                innerLatch.countDown()
            }

            innerLatch.await()
            latch.countDown()
        }

        latch.await()
    }
}