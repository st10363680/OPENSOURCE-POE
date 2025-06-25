package com.example.budgetwiseapp.data.entities
import com.google.firebase.firestore.PropertyName

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",

    @get:PropertyName("category")
    @set:PropertyName("category")
    var categoryName: String = "",

    val amount: Double = 0.0,
    val date: Timestamp? = null, // FIXED: was String
    val userId: String = ""
)
