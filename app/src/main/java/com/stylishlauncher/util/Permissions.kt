package com.stylishlauncher.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object Permissions {
    const val CONTACTS = android.Manifest.permission.READ_CONTACTS
    const val CALL_PHONE = android.Manifest.permission.CALL_PHONE
    const val ACCESS_FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
    const val POST_NOTIFICATIONS = android.Manifest.permission.POST_NOTIFICATIONS

    val ALL_LAUNCHER_PERMISSIONS = arrayOf(CONTACTS, CALL_PHONE)

    @Composable
    fun rememberPermissionState(permission: String): Boolean {
        var granted by remember { mutableStateOf(false) }
        val context = androidx.compose.ui.platform.LocalContext.current

        androidx.compose.runtime.LaunchedEffect(context) {
            granted = hasPermission(context, permission)
        }

        return granted
    }

    @Composable
    fun rememberMultiplePermissionsState(permissions: Array<String>): Map<String, Boolean> {
        var states by remember { mutableStateOf<Map<String, Boolean>>(permissions.associateWith { false }) }
        val context = androidx.compose.ui.platform.LocalContext.current

        androidx.compose.runtime.LaunchedEffect(context) {
            states = permissions.associateWith { hasPermission(context, it) }
        }

        return states
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }

    @Composable
    fun LauncherForPermission(permission: String): androidx.activity.result.ActivityResultLauncher<String> {
        return rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Result handled by caller via LaunchedEffect
        }
    }

    @Composable
    fun LauncherForMultiplePermissions(): androidx.activity.result.ActivityResultLauncher<Array<String>> {
        return rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // Result handled by caller via LaunchedEffect
        }
    }

    @Composable
    fun requestPermissionIfNeeded(
        activity: Activity,
        permission: String,
        rationale: String? = null,
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {},
    ) {
        val launcher = LauncherForPermission(permission)
        val granted = rememberPermissionState(permission)

        androidx.compose.runtime.LaunchedEffect(granted) {
            if (!granted) {
                val shouldShowRationale = activity.shouldShowRequestPermissionRationale(permission)
                if (shouldShowRationale && rationale != null) {
                    // Could show a dialog here explaining why permission is needed
                    // For now, just request directly
                }
                launcher.launch(permission)
            }
        }

        // Observe result
        androidx.compose.runtime.LaunchedEffect(launcher) {
            // This is a bit tricky with compose - in practice you'd use a callback
            // For simplicity, we'll rely on the state update
        }
    }

    @Composable
    fun requestPermissionsIfNeeded(
        activity: Activity,
        permissions: Array<String>,
        onAllGranted: () -> Unit = {},
        onAnyDenied: (Array<String>) -> Unit = {},
    ) {
        val launcher = LauncherForMultiplePermissions()
        val states = rememberMultiplePermissionsState(permissions)

        val allGranted = states.values.all { it }

        androidx.compose.runtime.LaunchedEffect(allGranted) {
            if (!allGranted) {
                val denied = permissions.filter { !hasPermission(activity, it) }
                if (denied.isNotEmpty()) {
                    launcher.launch(denied.toTypedArray())
                }
            } else {
                onAllGranted()
            }
        }
    }
}