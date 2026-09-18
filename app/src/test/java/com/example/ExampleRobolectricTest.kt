package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.spendora.data.engine.FinancialCycleCalculator
import com.spendora.data.sms.SmsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies SPENDORA branding`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SPENDORA", appName)
    }

    @Test
    fun `financial cycle calculator calculates monthly window accurately`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 15)
        }
        val cycle = FinancialCycleCalculator.calculateCycle(startDay = 1, referenceTimeMillis = cal.timeInMillis)
        assertEquals(1, cycle.startDay)
        assertTrue(cycle.label.contains("Sep"))
    }

    @Test
    fun `sms engine parses bank transaction and ignores otp`() {
        val bankSms = "Rs. 450.00 spent on your HDFC Bank Card ending 1234 at SWIGGY on 15-SEP-26. Bal: INR 12,000"
        val parsed = SmsEngine.parse(bankSms)
        assertNotNull(parsed)
        assertEquals(450.0, parsed!!.amount, 0.01)
        assertEquals("SWIGGY", parsed.merchant)

        val otpSms = "Your OTP for login is 948210. Valid for 10 min. Do not share with anyone."
        val parsedOtp = SmsEngine.parse(otpSms)
        assertEquals(null, parsedOtp)
    }
}
