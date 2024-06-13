package com.example.meatup.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(onScanComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                startCameraX(cameraProviderFuture, lifecycleOwner, context, coroutineScope, previewView, onScanComplete)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraX(cameraProviderFuture, lifecycleOwner, context, coroutineScope, previewView, onScanComplete)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).also { previewView = it }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .border(2.dp, Color.Red)
        )
    }
}

fun startCameraX(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    context: Context,
    coroutineScope: CoroutineScope,
    previewView: PreviewView?,
    onScanComplete: () -> Unit
) {
    if (previewView == null) {
        Log.e("BarcodeScanner", "PreviewView is not initialized")
        return
    }

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val barcodeAnalyzer = BarcodeAnalyzerX(coroutineScope, onScanComplete)

            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeAnalyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Error binding camera lifecycle: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e("BarcodeScanner", "Error initializing camera: ${e.message}", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

class BarcodeAnalyzerX(
    private val coroutineScope: CoroutineScope,
    private val onScanComplete: () -> Unit
) : ImageAnalysis.Analyzer {

    private var lastScannedCode: String? = null
    private var lastScanTime: Long = 0
    private val scanDelay = 2000

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            val currentTime = System.currentTimeMillis()
                            if (it != lastScannedCode || currentTime - lastScanTime > scanDelay) {
                                lastScannedCode = it
                                lastScanTime = currentTime
                                onBarcodeDetected(it)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("BarcodeAnalyzer", "Error processing barcode: ${it.message}", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun onBarcodeDetected(barcode: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
                url.httpGet().responseString { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.e("BarcodeScanner", "Error fetching product info: ${result.getException()}")
                        }
                        is Result.Success -> {
                            val data = result.get()
                            try {
                                val jsonObject = JSONObject(data)
                                val productName = jsonObject.getJSONObject("product").getString("product_name")
                                val productImage = jsonObject.getJSONObject("product").getString("image_url")

                                val db = FirebaseFirestore.getInstance()
                                val docRef = db.collection("products").document(barcode)
                                docRef.get().addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val currentCount = document.getLong("count") ?: 1
                                        docRef.update(
                                            mapOf(
                                                "count" to currentCount + 1,
                                                "barcode" to barcode
                                            )
                                        )
                                    } else {
                                        docRef.set(
                                            mapOf(
                                                "name" to productName,
                                                "image" to productImage,
                                                "count" to 1,
                                                "barcode" to barcode
                                            )
                                        )
                                    }
                                    onScanComplete()
                                }
                            } catch (e: Exception) {
                                Log.e("BarcodeScanner", "Error parsing product info: ${e.message}", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Error during HTTP request: ${e.message}", e)
            }
        }
    }
}
