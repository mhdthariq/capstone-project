package com.capstone.emoticalm

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.emoticalm.databinding.ActivityHomeBinding
import androidx.camera.view.PreviewView
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID
//class BackupHome:ComponentActivity{
class BackupHome{
//    private lateinit var binding: ActivityHomeBinding
//    private lateinit var previewView: PreviewView
//    private val REQUEST_CODE_PERMISSIONS = 10
//    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
//    private lateinit var imageCapture: ImageCapture
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityHomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        previewView = binding.previewView
//
//        if (allPermissionsGranted()) {
//            startCamera(CameraSelector.LENS_FACING_BACK)
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//        binding.cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
//            val lensFacing = if (isChecked) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
//            startCamera(lensFacing)
//        }
//
//        binding.captureButton.setOnClickListener {
//            captureImage()
//        }
//    }
//
//    private fun allPermissionsGranted(): Boolean {
//        return REQUIRED_PERMISSIONS.all {
//            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    private fun startCamera(lensFacing: Int = CameraSelector.LENS_FACING_BACK) {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            cameraProvider.unbindAll()
//
//            val preview = Preview.Builder().build()
//            preview.setSurfaceProvider(previewView.surfaceProvider)
//
//            imageCapture = ImageCapture.Builder()
//                .setTargetRotation(windowManager.defaultDisplay.rotation)
//                .build()
//
//            val cameraSelector = CameraSelector.Builder()
//                .requireLensFacing(lensFacing)
//                .build()
//
//            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
//        }, ContextCompat.getMainExecutor(this))
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
//
//    private fun convertYUVtoBitmap(imageProxy: ImageProxy): Bitmap? {
//        val yPlane = imageProxy.planes[0].buffer
//        val uPlane = imageProxy.planes.getOrNull(1)?.buffer
//        val vPlane = imageProxy.planes.getOrNull(2)?.buffer
//
//        if (yPlane == null || (uPlane == null && vPlane != null) || (vPlane == null && uPlane != null)) {
//            Toast.makeText(applicationContext, "Invalid YUV format", Toast.LENGTH_SHORT).show()
//            imageProxy.close()
//            return null
//        }
//
//        val yuvByteArray = ByteArray(yPlane.remaining() + (uPlane?.remaining() ?: 0) + (vPlane?.remaining() ?: 0))
//
//        yPlane.get(yuvByteArray, 0, yPlane.remaining())
//        uPlane?.get(yuvByteArray, yPlane.remaining(), uPlane.remaining())
//        vPlane?.get(yuvByteArray, yPlane.remaining() + (uPlane?.remaining() ?: 0), vPlane.remaining())
//
//        val yuvImage = YuvImage(yuvByteArray, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
//        val outStream = ByteArrayOutputStream()
//        val rect = Rect(0, 0, imageProxy.width, imageProxy.height)
//        yuvImage.compressToJpeg(rect, 100, outStream)
//
//        val byteArray = outStream.toByteArray()
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//    }
//    `
//    private fun uploadImage(bitmap: Bitmap) {
//        val randomFileName = "captured_image_${UUID.randomUUID()}.jpg"
//        val imageFile = File(filesDir, randomFileName)
//
//        val outputStream = FileOutputStream(imageFile)
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//        outputStream.flush()
//        outputStream.close()
//
//        val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
//
//        RetrofitClient.apiService.uploadImage(multipartBody).enqueue(object : Callback<ApiResponse> {
//            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        val intent = Intent(this@HomeActivity, SuccessActivity::class.java).apply {
//                            putExtra("imageUrl", it.data.image_url)
//                            putExtra("predictedLabel", it.data.predicted_label)
//                            putExtra("predictions", it.data.predictions.toString())
//                        }
//                        startActivity(intent)
//                    }
//                } else {
//                    Toast.makeText(applicationContext, "Error uploading image", Toast.LENGTH_SHORT).show()
//                    Log.e("HomeActivity", "API Error: ${response.code()} - ${response.message()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                Toast.makeText(applicationContext, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//
//    private fun uploadImage(bitmap: Bitmap) {
//        // Determine the file extension (JPEG or PNG)
//        val format = if (isJPEG(bitmap)) {
//            Bitmap.CompressFormat.JPEG
//        } else {
//            Bitmap.CompressFormat.PNG
//        }
//
//        // Generate a random file name using UUID
//        val randomFileName = "captured_image_${UUID.randomUUID()}.${if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"}"
//        val imageFile = File(filesDir, randomFileName)
//
//        // Write bitmap to file with the chosen format
//        val outputStream = FileOutputStream(imageFile)
//        bitmap.compress(format, 100, outputStream)
//        outputStream.flush()
//        outputStream.close()
//
//        // Now, create the requestBody with the randomized file name
//        val requestBody = imageFile.asRequestBody("image/${if (format == Bitmap.CompressFormat.JPEG) "jpeg" else "png"}".toMediaTypeOrNull())
//        val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
//
//        RetrofitClient.apiService.uploadImage(multipartBody).enqueue(object : Callback<ApiResponse> {
//            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        val intent = Intent(this@HomeActivity, SuccessActivity::class.java).apply {
//                            putExtra("imageUrl", it.data.image_url)
//                            putExtra("predictedLabel", it.data.predicted_label)
//                            putExtra("predictions", it.data.predictions.toString())
//                        }
//                        startActivity(intent)
//                    }
//                } else {
//                    Toast.makeText(applicationContext, "Error uploading image", Toast.LENGTH_SHORT).show()
//                    Log.e("HomeActivity", "API Error: ${response.code()} - ${response.message()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                Toast.makeText(applicationContext, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    // Optionally define a method to check if the bitmap is suitable for JPEG compression
//    private fun isJPEG(bitmap: Bitmap): Boolean {
//        // Add custom logic to check if the bitmap is of the required format for JPEG, if needed
//        return true  // For simplicity, assuming it's JPEG, can be customized
//    }
//
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
//            startCamera(CameraSelector.LENS_FACING_BACK)
//        } else {
//            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//    }
}