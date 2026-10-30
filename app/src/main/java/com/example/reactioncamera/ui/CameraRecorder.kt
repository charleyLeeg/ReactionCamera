package com.example.reactioncamera.ui

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds

/**
 * 下半部分：摄像头预览 + 录制
 * - 完全独立于参考视频
 * - 只保存摄像头录制的视频到相册
 * - 参考视频的播放/暂停不影响录制
 */
@Composable
fun CameraRecorder() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var isRecording by remember { mutableStateOf(value = false) }
    var recording: Recording? by remember { mutableStateOf(null) }
    var recordingStartTime by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // 录制计时器
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingStartTime = System.currentTimeMillis()
            while (isRecording) {
                elapsedSeconds = (System.currentTimeMillis() - recordingStartTime) / 1000
                delay(200.milliseconds)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    // 绑定相机
    LaunchedEffect(lensFacing, previewView) {
        val view = previewView ?: return@LaunchedEffect

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = withContext(Dispatchers.IO) {
            cameraProviderFuture.get()
        }

        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = view.surfaceProvider
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val capture = VideoCapture.withOutput(recorder)
        videoCapture = capture

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture,
            )
        } catch (e: Exception) {
            Log.e("CameraX", "相机绑定失败", e)
            Toast.makeText(context, "相机启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            recording?.stop()
            cameraExecutor.shutdown()
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 摄像头预览
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // 录制时间显示
        if (isRecording) {
            Text(
                text = formatDuration(elapsedSeconds),
                color = Color.Red,
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
            )
        }

        // 控制按钮区域
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 切换前后摄像头
            IconButton(
                onClick = {
                    if (!isRecording) {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                    } else {
                        Toast.makeText(context, "录制中无法切换摄像头", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "切换摄像头",
                    tint = Color.White,
                )
            }

            // 录制 / 停止按钮
            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        // 停止录制
                        recording?.stop()
                        recording = null
                        isRecording = false
                    } else {
                        // 开始录制
                        startRecording(
                            context = context,
                            videoCapture = videoCapture,
                            onRecordingStarted = { rec ->
                                recording = rec
                                isRecording = true
                            },
                        ) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            isRecording = false
                            recording = null
                        }
                    }
                },
                containerColor = if (isRecording) Color.Red else Color.White,
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = if (isRecording) "停止录制" else "开始录制",
                    tint = if (isRecording) Color.White else Color.Red,
                )
            }
        }
    }
}

/**
 * 开始录制并保存到相册 Movies/ReactionCamera 目录
 */
private fun startRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>?,
    onRecordingStarted: (Recording) -> Unit,
    onError: (String) -> Unit,
) {
    val capture = videoCapture ?: run {
        onError("摄像头未准备好")
        return
    }

    val name = "Reaction_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ReactionCamera")
        }
    }

    val mediaStoreOutput = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
    )
        .setContentValues(contentValues)
        .build()

    try {
        val recording = capture.output
            .prepareRecording(context, mediaStoreOutput)
            .withAudioEnabled() // 开启麦克风录音
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        Log.d("CameraX", "录制开始")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            Log.e("CameraX", "录制失败: ${event.cause}")
                            onError("录制失败: ${event.cause?.message}")
                        } else {
                            val uri = event.outputResults.outputUri
                            Log.d("CameraX", "视频已保存: $uri")
                            Toast.makeText(
                                context,
                                "视频已保存到相册 Movies/ReactionCamera",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                    else -> {}
                }
            }

        onRecordingStarted(recording)
    } catch (e: SecurityException) {
        onError("缺少录音权限: ${e.message}")
    } catch (e: Exception) {
        onError("启动录制失败: ${e.message}")
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
