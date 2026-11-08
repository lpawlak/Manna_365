package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FeedbackCategory
import com.example.service.feedback.FeedbackService
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackDialog(
    preferredBibleSourceTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val feedbackService = remember(context) { FeedbackService.create(context) }

    val diagnostics = remember(context, preferredBibleSourceTitle) {
        FeedbackService.collectDeviceDiagnostics(context, preferredBibleSourceTitle)
    }

    var selectedCategory by remember { mutableStateOf(FeedbackCategory.BUG) }
    var messageText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var showDiagnosticsDetails by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (submissionSuccess) "Wiadomość wysłana" else "Kontakt i zgłoszenia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (submissionSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Dziękujemy za kontakt!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Twoje zgłoszenie zostało bezpiecznie zapisane w bazie danych. Pomaga to w rozwoju i usuwaniu usterek aplikacji Manna 365.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Wybierz temat wiadomości:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Categories chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeedbackCategory.entries.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(category.title, fontSize = 12.sp)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (category) {
                                            FeedbackCategory.BUG -> Icons.Default.BugReport
                                            FeedbackCategory.SUGGESTION -> Icons.Default.Lightbulb
                                            FeedbackCategory.QUESTION -> Icons.Default.HelpOutline
                                            FeedbackCategory.OTHER -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Message input
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = {
                            if (it.length <= 4000) messageText = it
                            errorMessage = null
                        },
                        label = { Text("Treść wiadomości *") },
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    FeedbackCategory.BUG -> "Opisz, co poszło nie tak (np. brak tekstu lektury, błąd w powiadomieniach)..."
                                    FeedbackCategory.SUGGESTION -> "Opisz swój pomysł na nową funkcję lub usprawnienie..."
                                    FeedbackCategory.QUESTION -> "Zadaj pytanie dotyczące działania aplikacji..."
                                    FeedbackCategory.OTHER -> "Wpisz wiadomość..."
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("feedback_message_input"),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text("${messageText.length} / 4000")
                        }
                    )

                    // Optional Email
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = {
                            emailText = it
                            errorMessage = null
                        },
                        label = { Text("Twój adres e-mail (opcjonalnie)") },
                        placeholder = { Text("kontakt@przyklad.pl") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text("Podaj, jeśli chcesz otrzymać odpowiedź od autora")
                        }
                    )

                    // Diagnostic info preview (expandable)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDiagnosticsDetails = !showDiagnosticsDetails },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Smartphone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dołączone dane techniczne (bez danych prywatnych)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = if (showDiagnosticsDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = showDiagnosticsDetails) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("• System: ${diagnostics.osVersion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("• Urządzenie: ${diagnostics.manufacturer} ${diagnostics.model}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("• Połączenie: ${diagnostics.networkType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("• Wersja aplikacji: ${diagnostics.appVersion} (${diagnostics.appVersionCode})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("• Ekran: ${diagnostics.screenDensity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("• Domyślne źródło: ${diagnostics.preferredBibleSource}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (submissionSuccess) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("feedback_success_ok_button")
                ) {
                    Text("Gotowe")
                }
            } else {
                Button(
                    onClick = {
                        if (messageText.trim().length < 3) {
                            errorMessage = "Wpisz co najmniej kilka słów treści wiadomości."
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = feedbackService.submitFeedback(
                                category = selectedCategory,
                                message = messageText,
                                contactEmail = emailText,
                                deviceInfo = diagnostics
                            )
                            isSubmitting = false
                            if (result.isSuccess) {
                                submissionSuccess = true
                            } else {
                                errorMessage = "Błąd wysyłania: ${result.exceptionOrNull()?.localizedMessage ?: "brak połączenia"}"
                            }
                        }
                    },
                    enabled = !isSubmitting && messageText.isNotBlank(),
                    modifier = Modifier.testTag("feedback_submit_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wysyłanie...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wyślij")
                    }
                }
            }
        },
        dismissButton = {
            if (!submissionSuccess) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    modifier = Modifier.testTag("feedback_cancel_button")
                ) {
                    Text("Anuluj")
                }
            }
        }
    )
}
