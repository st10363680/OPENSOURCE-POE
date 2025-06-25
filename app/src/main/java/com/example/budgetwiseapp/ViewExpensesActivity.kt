package com.example.budgetwiseapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetwiseapp.data.entities.Expense
import com.google.firebase.firestore.FirebaseFirestore

class ViewExpensesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)

        recyclerView = findViewById(R.id.rvExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(expenseList)
        recyclerView.adapter = adapter

        fetchExpensesFromFirestore()
    }

    private fun fetchExpensesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("expenses")
            .get()
            .addOnSuccessListener { documents ->
                expenseList.clear()
                for (doc in documents) {
                    val expense = doc.toObject(Expense::class.java)
                    expenseList.add(expense)
                }
                adapter.updateData(expenseList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load expenses: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
