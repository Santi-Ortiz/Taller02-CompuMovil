package com.example.taller2

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taller2.databinding.ActivityCameraBinding
import java.io.IOException
import java.io.OutputStream

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var imageButtonCamera: ImageView
    private var imageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageButtonCamera = binding.imageButtonCamera

        binding.buttonGallery.setOnClickListener {
            openGallery()
        }

        /*binding.buttonCamera.setOnClickListener {
            openCamera()
        }

         */

        binding.buttonCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                captureImageFromCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara no concedido", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),0)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)

    }

    /*private fun openCamera() {
        captureImageFromCamera()
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Log.e("PERMISSION_APP", "Error al iniciar la cámara", e)
        }
    }

     */

    private fun saveImageToGallery(imageUri: Uri) {
        try {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
            val outputStream: OutputStream? = contentResolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream?.flush()
            outputStream?.close()
            Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .override(500, 500)
                    .into(binding.imageButtonCamera)
                saveImageToGallery(uri)
            }
        } else {
            Toast.makeText(this, "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureImageFromCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode,
        data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                /*REQUEST_IMAGE_CAPTURE -> {
                    if (data != null) {
                        imageUri = data.data
                        imageButtonCamera.setImageURI(imageUri)
                    }
                }*/
                REQUEST_IMAGE_GALLERY -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        Glide.with(this)
                            .load(it)
                            .override(500, 500)
                            .into(binding.imageButtonCamera)
                    }
                }
            }
        }
    }

    private fun getResizedBitmap(bm: Bitmap): Bitmap {
        val newWidth = 200
        val newHeight = 200
        val scaledBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true)
        return scaledBitmap
    }

}
