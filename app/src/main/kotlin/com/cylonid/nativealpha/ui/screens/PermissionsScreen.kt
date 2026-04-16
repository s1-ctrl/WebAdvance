package com.cylonid.nativealpha.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.cylonid.nativealpha.ui.theme.*
import com.cylonid.nativealpha.util.PermissionsManager

@Composable
fun PermissionsScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var permissionsStatus by remember {
        mutableStateOf(PermissionsManager.getAllPermissionsStatus(context))
    }

    // Refresh permissions when screen is composed
    LaunchedEffect(Unit) {
        permissionsStatus = PermissionsManager.getAllPermissionsStatus(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BgDark, BgDeep)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBackPressed, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { 
                    permissionsStatus = PermissionsManager.getAllPermissionsStatus(context)
                }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = TextPrimary, modifier = Modifier.size(24.dp))
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.radialGradient(listOf(GradVioletStart.copy(0.4f), Color.Transparent)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, null, tint = GradVioletEnd, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Permissions", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Manage app permissions", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = GradVioletEnd,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Grant permissions to enable features like downloads, screenshots, and file access.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Permissions List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PermissionsManager.Permission.values().toList()) { permission ->
                val isGranted = permissionsStatus[permission] ?: false

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFf44336),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    permission.id.replace("_", " "),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                permission.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (isGranted) "✓ Granted" else "✗ Not Granted",
                                color = if (isGranted) Color(0xFF4CAF50) else Color(0xFFf44336),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (!isGranted) {
                            Button(
                                onClick = {
                                    activity?.let { act ->
                                        when (permission) {
                                            PermissionsManager.Permission.SYSTEM_ALERT_WINDOW -> {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.fromParts("package", context.packageName, null)
                                                )
                                                context.startActivity(intent)
                                            }
                                            PermissionsManager.Permission.MANAGE_STORAGE -> {
                                                val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                        data = Uri.fromParts("package", context.packageName, null)
                                                    }
                                                } else {
                                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                        data = Uri.fromParts("package", context.packageName, null)
                                                    }
                                                }
                                                context.startActivity(intent)
                                            }
                                            else -> {
                                                // Request all runtime permissions at once
                                                PermissionsManager.requestPermissions(act, PermissionsManager.Permission.values().filter { 
                                                    it != PermissionsManager.Permission.SYSTEM_ALERT_WINDOW && 
                                                    it != PermissionsManager.Permission.MANAGE_STORAGE &&
                                                    it.androidPermission.isNotEmpty()
                                                }, 1001)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GradVioletEnd),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    when (permission) {
                                        PermissionsManager.Permission.SYSTEM_ALERT_WINDOW,
                                        PermissionsManager.Permission.MANAGE_STORAGE -> "Open Settings"
                                        else -> "Request All Permissions"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Settings Button
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GradVioletEnd),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open App Settings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
    }
}
