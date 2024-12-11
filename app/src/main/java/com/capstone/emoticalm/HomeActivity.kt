package com.capstone.emoticalm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class HomeActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 10
    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var cameraSwitch: Switch
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var imageCapture: ImageCapture
    private var bitmapCaptured: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewView = binding.previewView
        cameraSwitch = binding.cameraSwitch
        if (!isCameraAvailable()) {
            showToast("Camera not available on this device.")
            finish()
            return
        }
        // Check permissions and initialize the camera
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
            cameraProvider.unbindAll()  // Unbind previous use cases
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,  // Use the current cameraSelector (front or back)
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            showToast("Error binding use cases: ${e.message}")
        }
    }

    private fun captureImage() {
        val executor = ContextCompat.getMainExecutor(this)
        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val bitmap = imageProxyToBitmap(imageProxy)
                if (bitmap != null) {
                    bitmapCaptured = bitmap
                    val compressedBitmap = compressImage(bitmap)  // Compress the image
                    saveImageToFile(compressedBitmap)
                } else {
                    showToast("Failed to process image.")
                }
                imageProxy.close()
            }

            override fun onError(exception: ImageCaptureException) {
                showToast("Capture failed: ${exception.message}")
            }
        })
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val buffer = imageProxy.planes[0].buffer
        val byteArray = ByteArray(buffer.remaining())
        buffer.get(byteArray)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun compressImage(bitmap: Bitmap): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)  // Compress to 80% quality
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun saveImageToFile(bitmap: Bitmap) {
        try {
            val tempFile = File(applicationContext.cacheDir, "captured_image.jpg")
            val outputStream: OutputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            uploadImage(tempFile)  // Upload the image after saving
        } catch (e: Exception) {
            showToast("Failed to save image: ${e.message}")
        }
    }

    private fun uploadImage(file: File) {
        if (!isNetworkAvailable()) {
            showToast("No network available.")
            return
        }

        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

        RetrofitClient.apiService.uploadImage(part).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    apiResponse?.let {
                        Log.d("API Success", "Image URL: ${it.data.image_url}")
                        Log.d("API Response", "Predicted label: ${it.data.predicted_label}")
                        showToast("Predicted label: ${it.data.predicted_label}")

                        // Send the data to SuccessActivity
                        val intent = Intent(this@HomeActivity, SuccessActivity::class.java).apply {
                            putExtra("imageUrl", it.data.image_url)
                            putExtra("predictedLabel", it.data.predicted_label)
                            putExtra("predictions", it.data.predictions.toString()) // You can format it as needed
                        }
                        startActivity(intent)
                    }
                } else {
                    showToast("Failed to upload image: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("Upload Error", "Error uploading image: ${t.message}")
                showToast("Error uploading image. Retrying...")

                // Retry the upload after 3 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    uploadImage(file)  // Retry uploading the image
                }, 3000)  // Delay 3 seconds before retry
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
