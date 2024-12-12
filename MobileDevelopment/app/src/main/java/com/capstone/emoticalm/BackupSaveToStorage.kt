package com.capstone.emoticalm

import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.emoticalm.databinding.ActivityHomeBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.File
import java.io.OutputStream
import java.nio.ByteBuffer


class BackupSaveToStorage : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 10
    private val SAF_REQUEST_CODE = 1001

    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var cameraSwitch: Switch
    private lateinit var binding: ActivityHomeBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var imageCapture: ImageCapture
    private var imageUri: Uri? = null

    private var bitmapCaptured: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)

        previewView = binding.previewView
        cameraSwitch = binding.cameraSwitch

        if (!isCameraAvailable()) {
            showToast("Camera not available on this device.")
            finish()
            return
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        } else {
            initializeCamera()
        }

        cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            cameraSelector = if (isChecked) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            rebindCamera()
        }

        binding.captureButton.setOnClickListener { captureImage() }
    }

    private fun isCameraAvailable(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                showToast("Failed to initialize camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun rebindCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (e: Exception) {
            showToast("Error binding use cases: ${e.message}")
        }
    }

    private fun captureImage() {
        val executor = ContextCompat.getMainExecutor(this)
        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val format = imageProxy.format
                Log.d("ImageFormat", "Captured image format: $format")

                val bitmap = imageProxyToBitmap(imageProxy)
                if (bitmap != null) {
                    bitmapCaptured = bitmap
                    requestStorageAccess(bitmap)
                } else {
                    showToast("Failed to process image.")
                }
                imageProxy.close()
            }

            fun onError(exception: ImageCaptureException, imageProxy: ImageProxy) {
                val format = imageProxy.format
                Log.d("ImageFormat", "Captured image format: $format")
                showToast("Capture failed: ${exception.message}")
                imageProxy.close()
            }
        })
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val format = imageProxy.format
        Log.d("ImageProxy", "Image format: $format")

        return when (format) {
            ImageFormat.YUV_420_888 -> {
                val plane = imageProxy.planes[0]
                val buffer = plane.buffer
                val byteArray = ByteArray(buffer.remaining())
                buffer.get(byteArray)

                YuvToRgbConverter.convertToBitmap(byteArray, imageProxy.width, imageProxy.height)
            }
            ImageFormat.JPEG -> {
                val buffer = imageProxy.planes[0].buffer
                val byteArray = ByteArray(buffer.remaining())
                buffer.get(byteArray)
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
            else -> {
                Log.e("ImageProxy", "Unsupported image format: $format")
                showToast("Unsupported image format: $format")
                imageProxy.close()
                null
            }
        }
    }

    private fun requestStorageAccess(bitmap: Bitmap) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_TITLE, "captured_image.jpg")
        }
        storageActivityResultLauncher.launch(intent)
    }

    // Register the ActivityResultLauncher to handle the result from the storage intent
    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let { uri ->
                bitmapCaptured?.let { saveImageToUri(it, uri) }
            }
        }
    }

    private fun saveImageToUri(bitmap: Bitmap, uri: Uri) {
        try {
            val resolver: ContentResolver = contentResolver
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            showToast("Image saved successfully!")
        } catch (e: Exception) {
            showToast("Failed to save image: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
