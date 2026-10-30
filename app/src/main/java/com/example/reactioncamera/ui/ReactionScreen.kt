package com.example.reactioncamera.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * 主界面：自适应分屏（支持 Galaxy Z Fold 8 等折叠屏设备）
 * - 外屏/竖屏模式：上下分屏
 * - 展开屏/横屏模式：左右分屏
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReactionScreen() {
    // 权限状态
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ),
    )

    // 参考视频 URI
    var referenceVideoUri by remember { mutableStateOf<Uri?>(null) }

    // 视频选择器
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        referenceVideoUri = uri
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = this.maxWidth > this.maxHeight

        if (isWideScreen) {
            // 折叠屏展开/横屏模式：左右分屏
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    ReferenceVideoPlayer(
                        videoUri = referenceVideoUri,
                    ) { videoPicker.launch("video/*") }
                }

                VerticalDivider(
                    thickness = 2.dp,
                    color = Color.Gray,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    if (permissionsState.allPermissionsGranted) {
                        CameraRecorder()
                    } else {
                        PermissionRequestUI(permissionsState)
                    }
                }
            }
        } else {
            // 折叠屏外屏/竖屏模式：上下分屏
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    ReferenceVideoPlayer(
                        videoUri = referenceVideoUri,
                    ) { videoPicker.launch("video/*") }
                }

                HorizontalDivider(
                    thickness = 2.dp,
                    color = Color.Gray,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    if (permissionsState.allPermissionsGranted) {
                        CameraRecorder()
                    } else {
                        PermissionRequestUI(permissionsState)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequestUI(permissionsState: MultiplePermissionsState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Camera and microphone permissions are required",
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
            Text("Grant Permission")
        }
    }
}
