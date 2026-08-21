package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LanguageOption
import com.example.network.GeminiConstants
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenBrandProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearFavoritesDialog by remember { mutableStateOf(false) }
    var showApiKeySheet by remember { mutableStateOf(false) }

    var enteredApiKey by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Settings & AI Engine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure Gemini 3.6 Flash, API keys, brand profiles, and preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Gemini AI Engine & API Key Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.isApiKeyConfigured) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("gemini_settings_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini Engine",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Gemini 3.6 Flash",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Native Multi-Platform Generation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.isApiKeyConfigured) Color(0xFF1B5E20) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isApiKeyConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.isApiKeyConfigured) Color(0xFF81C784) else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (uiState.isApiKeyConfigured) "Connected" else "Key Required",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.isApiKeyConfigured) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Current Stored Key info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Gemini API Key",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.maskedApiKey,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                enteredApiKey = ""
                                viewModel.clearApiKeyTestResult()
                                showApiKeySheet = true
                            },
                            modifier = Modifier.testTag("manage_api_key_btn"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isApiKeyConfigured) "Manage Key" else "Add API Key")
                        }
                    }

                    if (!uiState.isApiKeyConfigured) {
                        Text(
                            text = "💡 Add your free Gemini API key from Google AI Studio to unlock real-time AI content generation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // Appearance Group
        item {
            SettingsGroup(title = "Appearance") {
                SettingsRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Theme",
                    description = "Switch between Dark, Light, or System default",
                    value = uiState.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )
            }
        }

        // Generation & Language Preferences
        item {
            SettingsGroup(title = "AI Preferences") {
                SettingsRow(
                    icon = Icons.Outlined.Translate,
                    title = "Default Language Mode",
                    description = "Sinhala, English, or Mixed language support",
                    value = uiState.language.displayName,
                    onClick = { showLanguageDialog = true }
                )
            }
        }

        // Brand Profile Group
        item {
            SettingsGroup(title = "Brand & Persona") {
                SettingsRow(
                    icon = Icons.Outlined.Business,
                    title = "Brand Profiles",
                    description = if (uiState.activeBrandProfile != null) "Active: ${uiState.activeBrandProfile?.name}" else "Set default CTA, brand tone and tags",
                    value = if (uiState.activeBrandProfile != null) "Active" else "None",
                    onClick = onOpenBrandProfiles
                )
            }
        }

        // Data & Privacy Group
        item {
            SettingsGroup(title = "Data Management") {
                SettingsRow(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Clear Project History",
                    description = "Remove all saved generation history",
                    value = null,
                    isDestructive = true,
                    onClick = { showClearHistoryDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    icon = Icons.Outlined.StarBorder,
                    title = "Clear Favorites",
                    description = "Remove all starred snippets and packages",
                    value = null,
                    isDestructive = true,
                    onClick = { showClearFavoritesDialog = true }
                )
            }
        }

        // About Group
        item {
            SettingsGroup(title = "About") {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "Manula Social AI",
                    description = "Model: ${GeminiConstants.DEFAULT_MODEL} • minSdk 27 (Android 8.1+)",
                    value = "v1.0",
                    onClick = { viewModel.showToast("Manula Social AI • Powered by Gemini 3.6 Flash") }
                )
            }
        }
    }

    // Gemini API Key Bottom Sheet / Dialog
    if (showApiKeySheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showApiKeySheet = false
                viewModel.clearApiKeyTestResult()
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gemini API Configuration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Default Model: ${GeminiConstants.DEFAULT_MODEL}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showApiKeySheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Masked stored key reminder
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Stored Key:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.maskedApiKey,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Key Input Field
                OutlinedTextField(
                    value = enteredApiKey,
                    onValueChange = {
                        enteredApiKey = it
                        viewModel.clearApiKeyTestResult()
                    },
                    label = { Text("Enter New Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide Key" else "Show Key"
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                // Test Connection live feedback banner
                AnimatedVisibility(visible = uiState.apiKeyTestResult != null || uiState.isTestingApiKey) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (uiState.apiKeyTestSuccess) {
                            true -> Color(0xFF1B5E20).copy(alpha = 0.15f)
                            false -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (uiState.apiKeyTestSuccess) {
                                true -> Color(0xFF81C784)
                                false -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.isTestingApiKey) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = if (uiState.apiKeyTestSuccess == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (uiState.apiKeyTestSuccess == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = uiState.apiKeyTestResult ?: "Testing connection...",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.apiKeyTestSuccess == true) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test Button
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.testApiKey(enteredApiKey.takeIf { it.isNotBlank() })
                        },
                        modifier = Modifier.weight(1f).testTag("test_api_key_btn"),
                        enabled = !uiState.isTestingApiKey && (enteredApiKey.isNotBlank() || uiState.isApiKeyConfigured)
                    ) {
                        Text("Test Connection")
                    }

                    // Save Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (enteredApiKey.isNotBlank()) {
                                viewModel.saveApiKey(enteredApiKey)
                                enteredApiKey = ""
                                showApiKeySheet = false
                            } else {
                                viewModel.showToast("Please enter an API key first.")
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("save_api_key_btn"),
                        enabled = enteredApiKey.isNotBlank()
                    ) {
                        Text("Save Key")
                    }
                }

                // Remove Key button if currently configured
                if (uiState.isCustomApiKeySet) {
                    TextButton(
                        onClick = {
                            viewModel.removeApiKey()
                            showApiKeySheet = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Custom Key")
                    }
                }

                Text(
                    text = "🔒 Your key is securely stored in Android Keystore with AES-GCM encryption and is never logged or exposed.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Default Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    LanguageOption.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.language == lang,
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All Projects?", fontWeight = FontWeight.Bold) },
            text = { Text("This will delete all saved content packages from local storage. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllProjects()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Favorites Dialog
    if (showClearFavoritesDialog) {
        AlertDialog(
            onDismissRequest = { showClearFavoritesDialog = false },
            title = { Text("Clear All Favorites?", fontWeight = FontWeight.Bold) },
            text = { Text("This will delete all starred content snippets from your library.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFavorites()
                        showClearFavoritesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearFavoritesDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    value: String?,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
