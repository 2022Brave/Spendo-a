package com.spendora.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendoraPurplePrimary
import com.spendora.data.entity.TransactionEntity
import com.spendora.ui.SpendoraViewModel
import com.spendora.ui.components.SpendoraPillNavBar
import com.spendora.ui.components.TransactionDetailSheet
import com.spendora.ui.screens.AccountsScreen
import com.spendora.ui.screens.AddTransactionScreen
import com.spendora.ui.screens.AnalyticsScreen
import com.spendora.ui.screens.DashboardScreen
import com.spendora.ui.screens.FinancialCycleScreen
import com.spendora.ui.screens.HistoricalImportScreen
import com.spendora.ui.screens.MoreScreen
import com.spendora.ui.screens.PendingReviewScreen
import com.spendora.ui.screens.SplashScreen
import com.spendora.ui.screens.TransactionsScreen
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun SpendoraNavigationShell(
    viewModel: SpendoraViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val confirmedTransactions by viewModel.confirmedTransactions.collectAsState()
    val summary by viewModel.financialCycleSummary.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val pendingTransactions by viewModel.pendingReviewTransactions.collectAsState()
    val currentStartDay by viewModel.currentCycleStartDay.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()

    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    // Determine if bottom pill nav bar should be shown
    val isBottomBarVisible = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Transactions.route,
        Screen.Analytics.route,
        Screen.More.route
    )

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() }
        ) {
            // 1. Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Dashboard Screen
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    summary = summary,
                    allTransactions = confirmedTransactions,
                    onCycleClick = {
                        navController.navigate(Screen.FinancialCycleSettings.route)
                    },
                    onSeeAllTransactions = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTransactionClick = { tx ->
                        selectedTransactionForDetail = tx
                    }
                )
            }

            // 3. Transactions Screen
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    transactions = confirmedTransactions,
                    onTransactionClick = { tx ->
                        selectedTransactionForDetail = tx
                    }
                )
            }

            // 4. Analytics Screen
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    summary = summary,
                    transactions = confirmedTransactions,
                    categories = categories
                )
            }

            // 5. More / Settings Screen
            composable(Screen.More.route) {
                MoreScreen(
                    cycleLabel = summary.cycleLabel,
                    pendingReviewCount = pendingTransactions.size,
                    isBiometricEnabled = isBiometricEnabled,
                    onToggleBiometric = { enabled ->
                        viewModel.toggleBiometricLock(enabled)
                    },
                    onNavigateCycle = {
                        navController.navigate(Screen.FinancialCycleSettings.route)
                    },
                    onNavigateAccounts = {
                        navController.navigate(Screen.Accounts.route)
                    },
                    onNavigatePendingReview = {
                        navController.navigate(Screen.PendingReview.route)
                    },
                    onNavigateImportSms = {
                        navController.navigate(Screen.HistoricalImport.route)
                    }
                )
            }

            // 6. Add Transaction Screen (Modal full sheet)
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    categories = categories,
                    accounts = accounts,
                    onBack = { navController.popBackStack() },
                    onSaveTransaction = { newTx ->
                        viewModel.addTransaction(newTx)
                        navController.popBackStack()
                    }
                )
            }

            // 7. Financial Cycle Settings
            composable(Screen.FinancialCycleSettings.route) {
                FinancialCycleScreen(
                    currentStartDay = currentStartDay,
                    onBack = { navController.popBackStack() },
                    onSaveStartDay = { newDay ->
                        viewModel.updateCycleStartDay(newDay)
                        navController.popBackStack()
                    }
                )
            }

            // 8. Accounts Screen
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    accounts = accounts,
                    onBack = { navController.popBackStack() },
                    onAddAccount = { newAcc ->
                        viewModel.addAccount(newAcc)
                    }
                )
            }

            // 9. Pending Review Screen
            composable(Screen.PendingReview.route) {
                PendingReviewScreen(
                    pendingTransactions = pendingTransactions,
                    onBack = { navController.popBackStack() },
                    onConfirm = { tx ->
                        viewModel.confirmPendingTransaction(tx)
                    },
                    onReject = { tx ->
                        viewModel.rejectPendingTransaction(tx)
                    }
                )
            }

            // 10. Historical SMS Import Screen
            composable(Screen.HistoricalImport.route) {
                HistoricalImportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Floating Pill Navigation Bar
        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SpendoraPillNavBar(
                currentRoute = currentRoute,
                onNavigate = { targetRoute ->
                    if (targetRoute != currentRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onAddClick = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            )
        }

        // Transaction Detail Bottom Sheet
        selectedTransactionForDetail?.let { tx ->
            val catName = categories.find { it.id == tx.categoryId }?.name ?: "General"
            TransactionDetailSheet(
                transaction = tx,
                categoryName = catName,
                onDismiss = { selectedTransactionForDetail = null },
                onDelete = { toDelete ->
                    viewModel.deleteTransaction(toDelete.id)
                }
            )
        }

        // Biometric Lock Screen Overlay
        if (isBiometricEnabled && !isAppUnlocked && currentRoute != Screen.Splash.route) {
            BiometricLockOverlay(
                onUnlock = {
                    viewModel.setAppUnlocked(true)
                }
            )
        }
    }
}

@Composable
fun BiometricLockOverlay(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    fun triggerBiometricPrompt() {
        if (activity == null) {
            Toast.makeText(context, "Biometric authentication not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock SPENDORA")
            .setSubtitle("Authenticate using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onUnlock()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(activity, errString, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(activity, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(SpendoraPurplePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "App Locked",
                    tint = SpendoraPurplePrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SPENDORA Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Biometric authentication is required to access your financial records.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = { triggerBiometricPrompt() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpendoraPurplePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Unlock with Biometrics",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
