package com.dskochilov.cosmos.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dskochilov.cosmos.core.domain.settings.ThemeMode
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val cacheClearedMessage = stringResource(R.string.feature_settings_cache_cleared)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onIntent(SettingsIntent.SetDailyNotification(granted))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                SettingsEffect.CacheCleared -> snackbarHostState.showSnackbar(cacheClearedMessage)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SettingsContent(
            state = uiState,
            onIntent = viewModel::onIntent,
            onDailyNotificationToggle = { enabled ->
                if (enabled && needsNotificationPermission(context)) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onIntent(SettingsIntent.SetDailyNotification(enabled))
                }
            }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun needsNotificationPermission(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

@Composable
internal fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onDailyNotificationToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.feature_settings_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        SectionHeader(text = stringResource(R.string.feature_settings_theme))
        ThemeModeOption(ThemeMode.SYSTEM, R.string.feature_settings_theme_system, state, onIntent)
        ThemeModeOption(ThemeMode.LIGHT, R.string.feature_settings_theme_light, state, onIntent)
        ThemeModeOption(ThemeMode.DARK, R.string.feature_settings_theme_dark, state, onIntent)

        SwitchRow(
            title = stringResource(R.string.feature_settings_dynamic_color),
            subtitle = stringResource(R.string.feature_settings_dynamic_color_hint),
            checked = state.preferences.useDynamicColor,
            onCheckedChange = { onIntent(SettingsIntent.SetDynamicColor(it)) }
        )

        SectionHeader(text = stringResource(R.string.feature_settings_notifications))
        SwitchRow(
            title = stringResource(R.string.feature_settings_daily_apod),
            subtitle = stringResource(R.string.feature_settings_daily_apod_hint),
            checked = state.preferences.dailyApodNotificationEnabled,
            onCheckedChange = onDailyNotificationToggle
        )

        SectionHeader(text = stringResource(R.string.feature_settings_storage))
        Text(
            text = stringResource(R.string.feature_settings_clear_cache_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Button(
            onClick = { onIntent(SettingsIntent.ClearCache) },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = stringResource(R.string.feature_settings_clear_cache))
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    labelRes: Int,
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = state.preferences.themeMode == mode
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = { onIntent(SettingsIntent.SetThemeMode(mode)) })
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
