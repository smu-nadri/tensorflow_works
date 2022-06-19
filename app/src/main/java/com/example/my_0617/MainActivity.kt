package com.example.my_0617

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.my_0617.ml.EfficientdetLite4Detection
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import java.lang.Exception

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
private val OPEN_GALLERY = 1

class MainActivity : AppCompatActivity() {
    var list = ArrayList<Uri>()

    companion object { private const val REQUEST_READ_EXTERNAL_STORAGE = 100 }
    private lateinit var btn_gallery: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request permission.
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_READ_EXTERNAL_STORAGE)

        btn_gallery = findViewById(R.id.btn_gallery)

        btn_gallery.setOnClickListener { openGallery() }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val readPermissionGrated = requestCode == REQUEST_READ_EXTERNAL_STORAGE &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!readPermissionGrated) {
            showPermissionDialog()
        }


    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setMessage("Read External Permission is needed.")
            .setPositiveButton("Allow.") {_, _ -> navigateToAppSetting() }
            .setNegativeButton("Nevermind.") {_, _ -> finish() }
            .show()
    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun openGallery() {
        val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(intent, OPEN_GALLERY)
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK) {
            if(requestCode == OPEN_GALLERY) {
                var currentImageUri: Uri? = data?.data
                try {
                    var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, currentImageUri)
                    imageView = findViewById(R.id.imageView)
                    imageView.setImageBitmap(bitmap)

                    bitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
                    classifyImage(bitmap)

                }
                catch (e: Exception) { e.printStackTrace() }
            }
        } else { Log.d("ActivityResult", "Something is wrong.") }
    }

    private fun classifyImage(bitmap: Bitmap) {
        textView = findViewById(R.id.textView)
        textView.text = ""

        val model = EfficientdetLite4Detection.newInstance(this)

        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)

        for (i in 0 until 10) {
            val detectionResult = outputs.detectionResultList.get(i)

            val category=detectionResult.categoryAsString
            val score=detectionResult.scoreAsFloat.toString()
            val location = detectionResult.locationAsRectF.toString()


            textView.append(category + "\t" + score + "\n")
        }

        model.close()
    }

}