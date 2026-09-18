package com.spendora.data.sms

import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.ReviewStatus
import com.spendora.data.model.TransactionSource
import com.spendora.data.model.TransactionType
import java.util.Locale
import java.util.regex.Pattern

data class ParsedSmsResult(
    val amount: Double,
    val type: TransactionType,
    val merchant: String?,
    val accountRef: String?,
    val confidence: Float,
    val reviewStatus: ReviewStatus
)

object SmsEngine {

    private val OTP_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(otp|one time password|verification code|security code|secret code)\\b"),
        Pattern.compile("(?i)\\bdo not share\\b"),
        Pattern.compile("(?i)\\bvalid for\\s+\\d+\\s+min"),
        Pattern.compile("(?i)\\b(login|sign in|auth)\\s+code\\b")
    )

    private val PROMO_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(congratulations|pre-approved|apply for|loan offer|cashback offer|discount coupon)\\b"),
        Pattern.compile("(?i)\\bflat\\s+\\d+%\\s+off\\b"),
        Pattern.compile("(?i)\\b(win|cash prize|click here|download app)\\b")
    )

    private val BALANCE_INQUIRY_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(avail(able)?|clear)\\s+bal(ance)?\\s+(is|:)?\\s*(rs\\.?|inr|₹)?\\s*[\\d,]+(\\.\\d{2})?\\b"),
        Pattern.compile("(?i)\\benquiry\\b")
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        "(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)|([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:rs\\.?|inr|₹)"
    )

    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid", "sent", "deducted", "purchase", "txn of", "withdrawn"
    )

    private val CREDIT_KEYWORDS = listOf(
        "credited", "received", "refund", "deposited", "added to", "salary"
    )

    private val CASH_WITHDRAWAL_KEYWORDS = listOf(
        "atm wdl", "cash wdl", "atm withdrawal", "cash withdrawal", "withdrawn from atm"
    )

    private val KNOWN_MERCHANTS = listOf(
        "SWIGGY", "ZOMATO", "AMAZON", "FLIPKART", "BLINKIT", "ZEPTO", "MYNTRA",
        "UBER", "OLA", "INDIAN OIL", "BPCL", "HPCL", "SHELL", "MAKEMYTRIP",
        "NETFLIX", "SPOTIFY", "GOOGLE", "APPLE", "STARBUCKS", "TATACLIQ"
    )

    fun isFinancialSms(body: String): Boolean {
        for (pattern in OTP_PATTERNS) {
            if (pattern.matcher(body).find()) return false
        }
        for (pattern in PROMO_PATTERNS) {
            if (pattern.matcher(body).find()) return false
        }
        return true
    }

    fun parse(smsBody: String, sender: String = "", timestamp: Long = System.currentTimeMillis()): ParsedSmsResult? {
        if (!isFinancialSms(smsBody)) return null

        val lowerBody = smsBody.lowercase(Locale.ROOT)

        // Determine transaction type
        val isCashWdl = CASH_WITHDRAWAL_KEYWORDS.any { lowerBody.contains(it) }
        val isCredit = !isCashWdl && CREDIT_KEYWORDS.any { lowerBody.contains(it) }
        val isDebit = !isCashWdl && !isCredit && DEBIT_KEYWORDS.any { lowerBody.contains(it) }

        if (!isCashWdl && !isCredit && !isDebit) {
            // Not a clear financial debit/credit transaction
            return null
        }

        val type = when {
            isCashWdl -> TransactionType.CASH_WITHDRAWAL
            lowerBody.contains("refund") -> TransactionType.REFUND
            isCredit -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // Extract amount
        val matcher = AMOUNT_PATTERN.matcher(smsBody)
        var amount: Double? = null
        while (matcher.find()) {
            val group1 = matcher.group(1)
            val group2 = matcher.group(2)
            val rawAmt = group1 ?: group2
            if (rawAmt != null) {
                val cleaned = rawAmt.replace(",", "")
                val parsed = cleaned.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    amount = parsed
                    break
                }
            }
        }

        if (amount == null) return null

        // Extract merchant
        var merchant: String? = null
        val upperBody = smsBody.uppercase(Locale.ROOT)
        for (known in KNOWN_MERCHANTS) {
            if (upperBody.contains(known)) {
                merchant = known
                break
            }
        }

        // Extract account ref
        val accountPattern = Pattern.compile("(?i)(?:a/c|acct|card|xx|ending)\\s*([xX*]*\\d{3,4})")
        val acctMatcher = accountPattern.matcher(smsBody)
        val accountRef = if (acctMatcher.find()) acctMatcher.group(1) else null

        // Confidence calculation
        var confidence = 0.5f
        if (amount > 0) confidence += 0.2f
        if (merchant != null) confidence += 0.15f
        if (accountRef != null) confidence += 0.15f

        val reviewStatus = if (confidence >= 0.75f) ReviewStatus.CONFIRMED else ReviewStatus.PENDING_REVIEW

        return ParsedSmsResult(
            amount = amount,
            type = type,
            merchant = merchant,
            accountRef = accountRef,
            confidence = confidence,
            reviewStatus = reviewStatus
        )
    }

    fun toEntity(result: ParsedSmsResult, rawSms: String, timestamp: Long = System.currentTimeMillis()): TransactionEntity {
        return TransactionEntity(
            amount = result.amount,
            type = result.type,
            merchantName = result.merchant ?: "Unknown Merchant",
            note = if (result.merchant != null) "Via SMS (${result.merchant})" else "Bank Alert",
            rawSms = rawSms,
            reviewStatus = result.reviewStatus,
            source = TransactionSource.SMS_LIVE,
            timestamp = timestamp,
            confidenceScore = result.confidence
        )
    }
}
