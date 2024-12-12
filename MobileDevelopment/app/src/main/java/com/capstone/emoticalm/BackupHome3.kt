//package com.capstone.emoticalm
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.Switch
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.content.ContextCompat
//import com.capstone.emoticalm.databinding.ActivityHomeBinding
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.OkHttpClient
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import retrofit2.Retrofit
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//class BackupHome3 : AppCompatActivity() {
//
//    private lateinit var binding: ActivityHomeBinding
//    private var imageCapture: ImageCapture? = null
//    private lateinit var outputDirectory: File
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityHomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            requestPermissions.launch(REQUIRED_PERMISSIONS)
//        }
//
//        outputDirectory = getOutputDirectory()
//
//        binding.captureButton.setOnClickListener {
//            takePhoto()
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(binding.previewView.surfaceProvider)
//            }
//
//            imageCapture = ImageCapture.Builder().build()
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
//            } catch (e: Exception) {
//                Log.e(TAG, "Use case binding failed", e)
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    private fun takePhoto() {
//        val imageCapture = imageCapture ?: return
//
//        val photoFile = File(
//            outputDirectory,
//            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
//        )
//
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    val savedUri = Uri.fromFile(photoFile)
//                    Toast.makeText(baseContext, "Photo saved: $savedUri", Toast.LENGTH_SHORT).show()
//                    sendImageToApi(photoFile)
//                }
//            }
//        )
//    }
//
//    private fun getOutputDirectory(): File {
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
//        }
//        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
//    }
//
//    private fun sendImageToApi(photoFile: File) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://yourapiurl.com/") // Replace with your API URL
//            .client(OkHttpClient())
//            .build()
//
//        val service = retrofit.create(UploadService::class.java)
//
//        val requestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val body = MultipartBody.Part.createFormData("file", photoFile.name, requestBody)
//
//        service.uploadImage(body).enqueue(object : retrofit2.Callback<Void> {
//            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
//                Toast.makeText(baseContext, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
//                Toast.makeText(baseContext, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private val requestPermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            if (permissions.values.all { it }) {
//                startCamera()
//            } else {
//                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(
//            baseContext, it
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    companion object {
//        private const val TAG = "HomeActivity"
//        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private val REQUIRED_PERMISSIONS =
//            mutableListOf(Manifest.permission.CAMERA).apply {
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
//    }
//}
//
//interface UploadService {
//    @Multipart
//    @POST("predict") // Replace with your API endpoint
//    fun uploadImage(@Part file: MultipartBody.Part): retrofit2.Call<Void>
//}
