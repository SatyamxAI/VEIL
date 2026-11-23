package com.veil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veil.core.crypto.Bip39Recovery
import com.veil.ui.components.VeilTopAppBar
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilBorder
import com.veil.ui.theme.VeilCard
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilSurfaceVariant
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary
import com.veil.ui.theme.VeilWarning

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultSetupScreen(
    onVaultCreated: (name: String, password: String, phrase: List<String>) -> Unit,
    onBack: () -> Unit
) {
    var vaultName by remember { mutableStateOf("Personal Vault") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmedBackup by remember { mutableStateOf(false) }

    val recoveryWords = remember { Bip39Recovery.generateRecoveryPhrase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeilBlack)
    ) {
        VeilTopAppBar(
            title = "Create New Vault",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VeilTextPrimary)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "End-to-End Encrypted Storage",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VeilTextPrimary
            )
            Text(
                text = "Your password never leaves this device. All files and filenames are encrypted before cloud sync.",
                fontSize = 14.sp,
                color = VeilTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            OutlinedTextField(
                value = vaultName,
                onValueChange = { vaultName = it },
                label = { Text("Vault Name") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilEmerald,
                    unfocusedBorderColor = VeilBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Master Password") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilEmerald,
                    unfocusedBorderColor = VeilBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Master Password") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilEmerald,
                    unfocusedBorderColor = VeilBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recovery Phrase Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = VeilWarning, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Emergency 24-Word Recovery Phrase",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VeilTextPrimary
                )
            }
            Text(
                text = "Write down these 24 words in exact order. This is the only way to recover your data if you forget your password.",
                fontSize = 13.sp,
                color = VeilTextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VeilCard, RoundedCornerShape(12.dp))
                    .border(1.dp, VeilBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recoveryWords.forEachIndexed { index, word ->
                        Text(
                            text = "${index + 1}. $word",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = VeilEmerald,
                            modifier = Modifier
                                .background(VeilSurfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = confirmedBackup,
                    onCheckedChange = { confirmedBackup = it },
                    colors = CheckboxDefaults.colors(checkedColor = VeilEmerald)
                )
                Text(
                    text = "I have safely written down my 24-word recovery phrase.",
                    fontSize = 13.sp,
                    color = VeilTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onVaultCreated(vaultName, password, recoveryWords) },
                enabled = vaultName.isNotBlank() && password.length >= 8 && password == confirmPassword && confirmedBackup,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilEmerald,
                    contentColor = VeilBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Initialize Encrypted Vault", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
