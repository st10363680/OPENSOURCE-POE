package com.example.budgetwiseapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SetGoalsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_goals)

        val edtMin = findViewById<EditText>(R.id.edtMinGoal)
        val edtMax = findViewById<EditText>(R.id.edtMaxGoal)
        val btnSave = findViewById<Button>(R.id.btnSaveGoals)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        btnSave.setOnClickListener {
            val minText = edtMin.text.toString()
            val maxText = edtMax.text.toString()

            if (minText.isBlank() || maxText.isBlank()) {
                Toast.makeText(this, "Please enter both goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minGoal = minText.toDouble()
            val maxGoal = maxText.toDouble()

            if (minGoal > maxGoal) {
                Toast.makeText(this, "Min goal can't be greater than Max goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monthlyGoal = maxGoal  // We'll use maxGoal as the monthly budget

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val goalsMap = hashMapOf(
                    "minGoal" to minGoal,
                    "maxGoal" to maxGoal,
                    "monthlyGoal" to monthlyGoal  // Needed by Dashboard
                )
                firestore.collection("goals")
                    .document(currentUser.uid)
                    .set(goalsMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Goals saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save goals", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
