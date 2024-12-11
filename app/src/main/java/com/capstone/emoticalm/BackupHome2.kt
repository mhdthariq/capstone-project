//package com.capstone.emoticalm
//
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.widget.Switch
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.ImageProxy
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.capstone.emoticalm.databinding.ActivityHomeBinding
//import com.google.common.util.concurrent.ListenableFuture
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class BackupHome2 : AppCompatActivity() {
//    private val PERMISSIONS_REQUEST_CODE = 10
//    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
//
//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
//    private lateinit var previewView: PreviewView
//    private lateinit var cameraSwitch: Switch
//    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//    private lateinit var binding: ActivityHomeBinding
//    private lateinit var imageCapture: ImageCapture
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//        binding = ActivityHomeBinding.inflate(layoutInflater)
//        previewView = findViewById(R.id.previewView)
//        cameraSwitch = findViewById(R.id.cameraSwitch)
//        if (!isCameraAvailable()) {
//            Toast.makeText(this, "Camera not available on this device.", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//        // Initialize CameraX
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            bindCameraUseCases(cameraProvider)
//        }, ContextCompat.getMainExecutor(this))
//
//        if (!allPermissionsGranted()) {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
//        } else {
//            initializeCamera()
//        }
//
//
//        // Handle Camera Switch
//        cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
//            cameraSelector = if (isChecked) {
//                CameraSelector.DEFAULT_FRONT_CAMERA
//            } else {
//                CameraSelector.DEFAULT_BACK_CAMERA
//            }
//
//            // Rebind the camera use cases with the new selector
//            cameraProviderFuture.addListener({
//                val cameraProvider = cameraProviderFuture.get()
//                bindCameraUseCases(cameraProvider)
//            }, ContextCompat.getMainExecutor(this))
//        }
//        binding.captureButton.setOnClickListener {
//            captureImage()
//        }
//
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//    private fun isCameraAvailable(): Boolean {
//        val pm = packageManager
//        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
//    }
//    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
//        // Set up the Preview use case
//        val preview = androidx.camera.core.Preview.Builder().build().apply {
//            setSurfaceProvider(previewView.surfaceProvider)
//        }
//        imageCapture = ImageCapture.Builder().build() // Initialize imageCapture here
//        try {
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Error binding camera use cases: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//    private fun checkCameraPermission() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.CAMERA),
//                CAMERA_REQUEST_CODE
//            )
//        } else {
//            initializeCamera()
//        }
//    }
//
//    private fun initializeCamera() {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            bindCameraUseCases(cameraProvider)
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSIONS_REQUEST_CODE) {
//            if (allPermissionsGranted()) {
//                initializeCamera()
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    companion object {
//        private const val CAMERA_REQUEST_CODE = 101
//        private fun initializeCamera(homeActivity: HomeActivity) {
//            homeActivity.cameraProviderFuture = ProcessCameraProvider.getInstance(homeActivity)
//            homeActivity.cameraProviderFuture.addListener({
//                val cameraProvider = homeActivity.cameraProviderFuture.get()
//                homeActivity.bindCameraUseCases(cameraProvider)
//            }, ContextCompat.getMainExecutor(homeActivity))
//        }
//    }
//
//    private fun captureImage() {
//        imageCapture.takePicture(
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageCapturedCallback() {
//                override fun onCaptureSuccess(imageProxy: ImageProxy) {
//                    val bitmap = convertYUVtoBitmap(imageProxy)
//                    if (bitmap != null) {
//                        uploadImage(bitmap)
//                    } else {
//                        Toast.makeText(applicationContext, "Failed to convert image", Toast.LENGTH_SHORT).show()
//                    }
//                    imageProxy.close()
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    exception.printStackTrace()
//                    Toast.makeText(applicationContext, "Image capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        )
//    }
//    private fun convertYUVtoBitmap(imageProxy: ImageProxy): Bitmap? {
//        val buffer = imageProxy.planes[0].buffer
//        val bytes = ByteArray(buffer.remaining())
//        buffer.get(bytes)
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//    }
//
//    private fun uploadImage(bitmap: Bitmap) {
//        // Convert bitmap to file
//        val file = createTempFile("image", ".jpg", cacheDir).apply {
//            outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
//        }
//
//        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
//
//        RetrofitClient.apiService.uploadImage(part).enqueue(object : Callback<ApiResponse> {
//            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful) {
//                    val apiResponse = response.body()
//                    Toast.makeText(this@HomeActivity, "Success: ${apiResponse?.data?.predicted_label}", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this@HomeActivity, "API Error: ${response.message()}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                Toast.makeText(this@HomeActivity, "Request Failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//
//}
