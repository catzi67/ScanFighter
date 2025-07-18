package com.catto.scanfighter.ui.screens

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.components.GameDialog
import com.catto.scanfighter.ui.components.GameTextField
import com.catto.scanfighter.utils.viewmodels.FighterViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * A dedicated ImageAnalysis.Analyzer class to handle barcode scanning.
 * This isolates the use of the experimental `imageProxy.image` API.
 */
private class BarcodeAnalyzer(private val onBarcodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {

    private var isScanning = true

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        // If a barcode has already been successfully scanned, do nothing.
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder().build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { barcodeValue ->
                        // Ensure we only process the first valid barcode found.
                        if (isScanning) {
                            isScanning = false
                            onBarcodeScanned(barcodeValue)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeAnalyzer", "Barcode scanning failed", e)
                }
                .addOnCompleteListener {
                    // It's crucial to close the imageProxy to allow the next frame to be processed.
                    imageProxy.close()
                }
        } else {
            imageProxy.close() // Also close if the image is null
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreateFighterScreen(navController: NavController, viewModel: FighterViewModel) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var showNameDialog by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var fighterName by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        cameraPermissionState.launchPermissionRequest()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        if (cameraPermissionState.status.isGranted) {
            // We now pass a lambda that will be called by our analyzer.
            CameraPreview { barcode ->
                scannedBarcode = barcode
                showNameDialog = true
            }
        } else {
            Text("Camera permission is required to scan for new fighters.", modifier = Modifier.padding(16.dp))
        }
    }

    if (showNameDialog && scannedBarcode != null) {
        GameDialog(
            title = "Name Your Fighter",
            onDismissRequest = {
                showNameDialog = false
                scannedBarcode = null
                fighterName = ""
            },
            content = {
                GameTextField(
                    value = fighterName,
                    onValueChange = { fighterName = it },
                    label = "Fighter Name"
                )
            },
            confirmButton = {
                GameButton(
                    text = "Create",
                    onClick = {
                        if (fighterName.isNotBlank()) {
                            viewModel.createFighter(fighterName, scannedBarcode!!)
                            showNameDialog = false
                            scannedBarcode = null
                            fighterName = ""
                            navController.popBackStack()
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun CameraPreview(onBarcodeScanned: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    // We remember the analyzer instance to avoid creating a new one on every recomposition.
    val barcodeAnalyzer = remember { BarcodeAnalyzer(onBarcodeScanned) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        // Set the analyzer instance.
                        it.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeAnalyzer)
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
