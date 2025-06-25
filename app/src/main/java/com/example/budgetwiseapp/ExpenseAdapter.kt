package com.example.budgetwiseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetwiseapp.data.entities.Expense

class ExpenseAdapter(private var expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInfo: TextView = itemView.findViewById(R.id.tvExpenseInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        val context = holder.itemView.context

        val amountStr = String.format("%.2f", expense.amount)
        val dateStr = expense.date

        holder.tvInfo.text = "Category: ${expense.categoryName.ifBlank { "Unknown" }}\nAmount: R $amountStr\nDate: $dateStr"
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    fun updateData(newList: List<Expense>) {
        expenseList = newList
        notifyDataSetChanged()
    }
}
