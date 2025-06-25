package com.example.budgetwiseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvBalanceAmount: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvBalanceAmount = findViewById(R.id.tvBalanceAmount)

        findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        findViewById<Button>(R.id.btnSetGoals).setOnClickListener {
            startActivity(Intent(this, SetGoalsActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            startActivity(Intent(this, ViewExpensesActivity::class.java))
        }

        findViewById<Button>(R.id.btnSummary).setOnClickListener {
            startActivity(Intent(this, ViewSummaryActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewPhoto).setOnClickListener {
            startActivity(Intent(this, ViewPhotoActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            finish()
        }

        loadAvailableBudget()
    }

    private fun loadAvailableBudget() {
        val userId = auth.currentUser?.uid ?: return

        var totalExpenses = 0.0
        var monthlyGoal = 0.0

        // Step 1: Get total expenses
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    totalExpenses += amount
                }

                // Step 2: Get monthly goal
                db.collection("goals")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { goalDoc ->
                        monthlyGoal = goalDoc.getDouble("monthlyGoal") ?: 0.0
                        val available = monthlyGoal - totalExpenses

                        tvBalanceAmount.text = "R ${String.format("%.2f", available)}"
                    }
                    .addOnFailureListener {
                        tvBalanceAmount.text = "Goal Error"
                    }
            }
            .addOnFailureListener {
                tvBalanceAmount.text = "Expense Error"
            }
    }
}

// Optional: Logout button (if using Firebase)
        /*
        findViewById<Button>(R.id.btnLogout).apply {
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
            }
        }
        */
