package com.example.budgetwiseapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ViewPhotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_photo)

        val imageView = findViewById<ImageView>(R.id.ivExpensePhoto)
        val closeButton = findViewById<Button>(R.id.btnClose)

        // Get image path from intent extras
        val imagePath = intent.getStringExtra("imagePath")

        if (!imagePath.isNullOrEmpty()) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show()
        }

        closeButton.setOnClickListener {
            finish()
        }
    }
}
