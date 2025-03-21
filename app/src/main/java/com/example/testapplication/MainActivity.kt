package com.example.testapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import com.example.testapplication.ui.theme.TestApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //Initialize app usage manager
        val appUsageManager = AppUsageManager(this)

        setContent {
            val appUsageStats = remember {
                mutableStateOf<List<AppUsageInfo>>(emptyList())
            }

            TestApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("App Access Tracker") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF2196F3),
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        // Buttons Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White, contentColor = Color(0xFF2196F3)),
                                onClick = {
                                    if (!appUsageManager.hasUsageStatsPermission()) {
                                        appUsageManager.requestUsageStatsPermission()
                                    }
                                    appUsageStats.value = appUsageManager.getAppUsageStats()
                                }
                            ) { Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                                Text("Refresh")
                            }
                            }

//                            Button(
//                                onClick = {
//                                    appUsageStats.value = emptyList()
//                                }
//                            ) {
//                                Text("Clear Data")
//                            }
                        }
                        // List apps & their info
                        if (appUsageStats.value.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No data available. Click 'Get Data'",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(appUsageStats.value) {
                                    ViewAppAsCard(it, appUsageManager)
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViewAppAsCard(appUsageStats: AppUsageInfo, _appUsageManager: AppUsageManager) {
    // Track if permission are expanded or not
    val expandedPermissions = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Display app name
            Text(
                text = appUsageStats.appName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display manage permissions button
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        _appUsageManager.OpenAppSettings(appUsageStats.packageName)
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Manage")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Display usage data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: " + appUsageStats.getFormattedTime(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f) // Make the space consistent
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Last usage was " + appUsageStats.getFormattedLastTimeUsed(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f) // Make the space consistent
                )
            }

            // Separate app info and permissions sections
            if (appUsageStats.grantedPermissions.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Display permissions
                Text(
                    text = "Permissions:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                val permissionsToShow = if (expandedPermissions.value || appUsageStats.grantedPermissions.size <= 3) {
                    appUsageStats.grantedPermissions
                } else {
                    appUsageStats.grantedPermissions.take(3)
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    permissionsToShow.forEach { permission ->
                        val formattedPermissionName = FormatPermissionName(permission)
                        PermissionWrapper(formattedPermissionName)
                    }
                }

                if (appUsageStats.grantedPermissions.size > 3) {
                    Text(
                        text = if (expandedPermissions.value) "Show less" else "See more...",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable {
                                expandedPermissions.value = !expandedPermissions.value
                            }
                    )
                }
            } else {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "No permissions granted",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FormatPermissionName(permission: String): String {
    // Remove the common prefix
    val withoutPrefix = when {
        permission.startsWith("android.permission.") ->
            permission.removePrefix("android.permission.")
        permission.contains("routines.ROUTINES_ACTIONS") ->
            "Access to Routines"
        permission.contains("settings") ->
            permission.substringAfterLast(".")
        else -> permission.substringAfterLast(".")
    }

    // Lowercase some characters
    return withoutPrefix
        .split("_")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}
@Composable
fun PermissionWrapper(permission: String){
    Surface(
        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF0F0F0) // Light gray background for rounded wrapper
    ) {
        Text(
            text = permission,
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
