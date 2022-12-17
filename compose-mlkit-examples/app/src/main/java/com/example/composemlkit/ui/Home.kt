package com.example.composemlkit.ui

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.composemlkit.common.enums.Examples
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Home(
    onExampleSelected: (Examples) -> Unit = {}
) {
    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(true) {
        cameraPermissionState.launchPermissionRequest()
    }

    Scaffold { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Button(onClick = {
                    onClickWithCameraGranted(cameraPermissionState) {
                        onExampleSelected(Examples.FACE_DETECTION)
                    }
                }) {
                    Text(text = "Face Detection")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun onClickWithCameraGranted(permissionState: PermissionState, callback: () -> Unit) {
    if (permissionState.status.isGranted) callback() else permissionState.launchPermissionRequest()
}

@Preview
@Composable
fun HomePreview() {
    Home()
}
