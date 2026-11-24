package com.stylishlauncher

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.stylishlauncher.data.AppRepository
import com.stylishlauncher.data.SearchRepository
import com.stylishlauncher.ui.home.HomeScreen
import com.stylishlauncher.ui.theme.StylishTheme
import com.stylishlauncher.ui.theme.brandColors
import com.stylishlauncher.util.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var searchRepository: SearchRepository

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val denied = permissions.entries.filter { !it.value }.keys.toList()
        if (denied.isNotEmpty()) {
            // Permissions denied - could show rationale
        }
    }

    private val voiceSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.firstOrNull()?.let { query ->
                // Handle voice search result
                onSearchSubmit(query)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set theme based on system preference
        val nightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        // Check if we're the default launcher
        checkDefaultLauncher()

        // Request permissions
        requestLauncherPermissions()

        setContent {
            StylishTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = brandColors().background,
                ) {
                    LauncherContent(
                        appRepository = appRepository,
                        searchRepository = searchRepository,
                        onVoiceSearch = { startVoiceSearch() },
                        onOpenSettings = { openSettings() },
                        onOpenWidgetPicker = { openWidgetPicker() },
                        onLockScreen = { lockScreen() },
                        onSearchSubmit = { query -> handleSearchSubmit(query) },
                    )
                }
            }
        }
    }

    private fun checkDefaultLauncher() {
        val packageManager = packageManager
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentLauncher = resolveInfo?.activityInfo?.packageName

        if (currentLauncher != packageName) {
            // Not default launcher - could prompt user
            // We'll show a subtle hint in the UI instead
        }
    }

    private fun requestLauncherPermissions() {
        val permissionsToRequest = Permissions.ALL_LAUNCHER_PERMISSIONS.filter { permission ->
            checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            // Voice recognition not available
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
    }

    private fun openWidgetPicker() {
        // On Android 12+, we can use the app widget picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICKER).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, ComponentName(this@MainActivity, WidgetReceiver::class.java))
            }
            startActivity(intent)
        } else {
            // Fallback - show widget picker via long press on home
            // This is typically handled by the system
        }
    }

    private fun lockScreen() {
        // Use DevicePolicyManager to lock screen
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as? android.app.admin.DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        if (devicePolicyManager?.isAdminActive(componentName) == true) {
            devicePolicyManager.lockNow()
        } else {
            // Request admin permission
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Allow Stylish Launcher to lock the screen")
            }
            startActivity(intent)
        }
    }

    private fun handleSearchSubmit(query: String) {
        // Handle search - the UI will handle navigation
        // This could open web search, launch app, open settings, etc.
        lifecycleScope.launch {
            val results = searchRepository.search(query)
            // Results are observed by UI via state
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            when (it.action) {
                Intent.ACTION_SEARCH -> {
                    it.getStringExtra(android.app.SearchManager.QUERY)?.let { query ->
                        handleSearchSubmit(query)
                    }
                }
                Intent.ACTION_ASSIST -> {
                    startVoiceSearch()
                }
            }
        }
    }
}

@Composable
fun LauncherContent(
    appRepository: AppRepository,
    searchRepository: SearchRepository,
    onVoiceSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onLockScreen: () -> Unit,
    onSearchSubmit: (String) -> Unit,
) {
    HomeScreen(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        appRepository = appRepository,
        searchRepository = searchRepository,
        onAppClick = { packageName ->
            launchApp(packageName)
        },
        onSearchQueryChange = { _ -> },
        onSearchSubmit = onSearchSubmit,
        onVoiceSearch = onVoiceSearch,
        onOpenSettings = onOpenSettings,
        onOpenWidgetPicker = onOpenWidgetPicker,
        onLockScreen = onLockScreen,
    )
}

fun launchApp(packageName: String) {
    // This will be called from composable, need context
    // We'll handle this via the repository in the actual implementation
}

// Device Admin Receiver for lock screen
class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}

// Widget Receiver placeholder
class WidgetReceiver : android.appwidget.AppWidgetProvider()