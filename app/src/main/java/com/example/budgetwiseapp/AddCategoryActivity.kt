package com.example.budgetwiseapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val categoryNameEditText = findViewById<EditText>(R.id.editTextCategoryName)
        val saveButton = findViewById<Button>(R.id.buttonSaveCategory)

        saveButton.setOnClickListener {
            val name = categoryNameEditText.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = hashMapOf(
                "name" to name,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("categories")
                .add(category)
                .addOnSuccessListener {
                    Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save category", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

