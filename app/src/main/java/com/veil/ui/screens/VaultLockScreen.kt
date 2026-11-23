package com.veil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilBorder
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilError
import com.veil.ui.theme.VeilSurfaceVariant
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary

@Composable
fun VaultLockScreen(
    vaultName: String = "Personal Vault",
    hasBiometrics: Boolean = true,
    isUnlocking: Boolean = false,
    errorMessage: String? = null,
    onUnlockWithPassword: (String) -> Unit,
    onTriggerBiometrics: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToRecovery: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeilBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vault Logo / Shield Icon
        IconButton(
            onClick = onTriggerBiometrics,
            modifier = Modifier
                .size(80.dp)
                .background(VeilSurfaceVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = VeilEmerald,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "VEIL",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = VeilTextPrimary,
            letterSpacing = 2.sp
        )

        Text(
            text = "Private by design. Simple by nature.",
            fontSize = 14.sp,
            color = VeilTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = vaultName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = VeilTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Master Password", color = VeilTextSecondary) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password",
                        tint = VeilTextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeilEmerald,
                unfocusedBorderColor = VeilBorder,
                focusedTextColor = VeilTextPrimary,
                unfocusedTextColor = VeilTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = VeilError,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onUnlockWithPassword(password) },
            enabled = password.isNotBlank() && !isUnlocking,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VeilEmerald,
                contentColor = VeilBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isUnlocking) {
                CircularProgressIndicator(
                    color = VeilBlack,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Unlock Vault", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        if (hasBiometrics) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onTriggerBiometrics,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilSurfaceVariant,
                    contentColor = VeilTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Unlock",
                    tint = VeilEmerald,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Unlock with Biometrics")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onNavigateToRecovery) {
            Text("Lost password? Restore with recovery phrase", color = VeilTextSecondary, fontSize = 13.sp)
        }

        TextButton(onClick = onNavigateToSetup) {
            Text("Create another vault", color = VeilEmerald, fontSize = 14.sp)
        }
    }
}
