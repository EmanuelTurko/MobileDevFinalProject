package com.example.recipehub.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipehub.ui.theme.Purple40
import com.example.recipehub.ui.theme.PurpleGrey40
import kotlinx.coroutines.delay

@Composable
fun LoadingSpinner(
    isLoading: Boolean,
    progress: Float,
    onComplete: () -> Unit,
    showCompletion: Boolean // Add showCompletion parameter
) {
    var hideAfterDelay by remember { mutableStateOf(false) }

    LaunchedEffect(showCompletion) {
        if (showCompletion) {
            delay(1000)
            hideAfterDelay = true
            onComplete()
        }
    }

    if (hideAfterDelay) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            progress = { if (isLoading) progress else 1f },
            modifier = Modifier.size(80.dp),
            color = PurpleGrey40,
            strokeWidth = 8.dp,
            trackColor = Purple40,
        )
        if (showCompletion) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Task Completed",
                tint = PurpleGrey40,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}
@Composable
fun SimulateLoading(onLoadingComplete: () -> Unit) {
    var showSpinner by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var showCompletion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Simulate loading progress
        for (i in 1..100) {
            delay(30)
            progress = i / 100f
        }
        isLoading = false
        showCompletion = true
        delay(1000)
        onLoadingComplete()
    }

    if (showSpinner) {
        LoadingSpinner(
            isLoading = isLoading,
            progress = progress,
            onComplete = { showSpinner = false },
            showCompletion = showCompletion
        )
    }
}