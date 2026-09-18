package com.spendora.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.data.database.CategorySeedData
import com.spendora.data.database.SpendoraDatabase
import com.spendora.data.engine.FinancialCycleCalculator
import com.spendora.data.entity.AccountEntity
import com.spendora.data.entity.CategoryEntity
import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.FinancialCycle
import com.spendora.data.model.FinancialCycleSummary
import com.spendora.data.model.ReviewStatus
import com.spendora.data.repository.AccountRepository
import com.spendora.data.repository.CategoryRepository
import com.spendora.data.repository.FinancialCycleRepository
import com.spendora.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpendoraViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SpendoraDatabase.getDatabase(application, viewModelScope)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val financialCycleRepository = FinancialCycleRepository(database.financialCycleDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val accountRepository = AccountRepository(database.accountDao())

    val confirmedTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.getConfirmedTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> =
        categoryRepository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> =
        accountRepository.getAllAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReviewTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.getPendingReviewTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentCycleStartDay = MutableStateFlow(1)
    val currentCycleStartDay: StateFlow<Int> = _currentCycleStartDay.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.ensureDefaultCategories(CategorySeedData.defaultCategories)
            accountRepository.ensureDefaultAccounts(CategorySeedData.defaultAccounts)
        }
        viewModelScope.launch {
            financialCycleRepository.getPrimaryCycle().collect { cycleEntity ->
                if (cycleEntity != null) {
                    _currentCycleStartDay.value = cycleEntity.startDay
                }
            }
        }
    }

    val financialCycleSummary: StateFlow<FinancialCycleSummary> =
        combine(confirmedTransactions, _currentCycleStartDay) { transactions, startDay ->
            val cycle = FinancialCycleCalculator.calculateCycle(startDay)
            FinancialCycleCalculator.calculateSummary(transactions, cycle)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FinancialCycleSummary()
        )

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }

    fun updateCycleStartDay(startDay: Int) {
        viewModelScope.launch {
            _currentCycleStartDay.value = startDay
            financialCycleRepository.updateStartDay(startDay)
        }
    }

    fun confirmPendingTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(
                transaction.copy(reviewStatus = ReviewStatus.CONFIRMED)
            )
        }
    }

    fun rejectPendingTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(
                transaction.copy(reviewStatus = ReviewStatus.REJECTED)
            )
        }
    }

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(true)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    fun toggleBiometricLock(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    fun setAppUnlocked(unlocked: Boolean) {
        _isAppUnlocked.value = unlocked
    }

    fun addAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.insertAccount(account)
        }
    }
}
