package com.example.budgetwiseapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            selectedPhotoUri = result.data!!.data
            Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI
        val amountEditText = findViewById<EditText>(R.id.editTextAmount)
        val descriptionEditText = findViewById<EditText>(R.id.editTextDescription)
        val dateEditText = findViewById<EditText>(R.id.etDate)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val addPhotoButton = findViewById<Button>(R.id.btnAddPhoto)
        val saveButton = findViewById<Button>(R.id.buttonSaveExpense)

        // Set spinner options
        val categories = listOf("Food", "Transport", "Entertainment", "Bills")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Photo picker
        addPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        // Save to Firestore
        saveButton.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountEditText.text.toString().toDoubleOrNull()
            val description = descriptionEditText.text.toString().trim()
            val rawDate = dateEditText.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()

            val formattedDate = try {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(rawDate)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate!!)
            } catch (e: Exception) {
                null
            }

            if (amount == null || description.isEmpty() || formattedDate == null) {
                Toast.makeText(this, "Please enter all required fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = hashMapOf(
                "userId" to userId,
                "amount" to amount,
                "description" to description,
                "date" to formattedDate,
                "category" to category,
                "photoUri" to (selectedPhotoUri?.toString() ?: ""),
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("expenses")
                .add(expense)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
        }
    }
    }
