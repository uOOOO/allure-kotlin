package io.qameta.allure.sample.junit4.android

import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.Allure.parameter
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.junit4.DisplayName
import io.qameta.allure.kotlin.junit4.Tag
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("Samples")
@DisplayName("SampleRobolectric tests")
@Tag("Robolectric test")
class SampleRobolectricTest {

    @Test
    @DisplayName("addition test")
    @Feature("Addition")
    @Description("Checks if addition is implemented correctly")
    fun additionTest() {
        val x = 2
        val y = 4
        parameter("x", x)
        parameter("y", y)

        step("Add values") {
            val actual = SampleCalculator().add(x = x, y = y)

            step("Verify correctness") {
                assertThat(actual, `is`(6))
            }
        }
    }

    @Test
    @Feature("Subtraction")
    @DisplayName("subtraction test")
    @Description("Checks if subtractions is implemented correctly")
    fun subtractionTest() {
        val x = 2
        val y = 4
        parameter("x", x)
        parameter("y", y)

        step("Subtract values") {
            val actual = SampleCalculator().subtract(x = x, y = y)

            step("Verify correctness") {
                assertThat(actual, `is`(-2))
            }
        }
    }
}