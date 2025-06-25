package com.example.budgetwiseapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetwiseapp.data.entities.Expense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewSummaryActivity : AppCompatActivity() {

    private lateinit var totalSpentText: TextView
    private lateinit var totalIncomeText: TextView
    private lateinit var categorySummaryTitle: TextView
    private lateinit var rvCategorySummary: RecyclerView
    private lateinit var btnClose: Button
    private lateinit var barChart: BarChart
    private lateinit var tvGoalStatus: TextView
    private lateinit var imgBadge: ImageView
    private lateinit var imgConsistency: ImageView

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_summary)

        // Bind UI elements
        totalSpentText = findViewById(R.id.tvTotalSpent)
        totalIncomeText = findViewById(R.id.tvTotalIncome)
        categorySummaryTitle = findViewById(R.id.tvCategorySummary)
        rvCategorySummary = findViewById(R.id.rvCategorySummary)
        btnClose = findViewById(R.id.btnCloseSummary)
        barChart = findViewById(R.id.barChart)
        tvGoalStatus = findViewById(R.id.tvGoalStatus)
        imgBadge = findViewById(R.id.imgBadge)
        imgConsistency = findViewById(R.id.imgConsistency)

        rvCategorySummary.layoutManager = LinearLayoutManager(this)

        btnClose.setOnClickListener { finish() }

        fetchSummaryData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchSummaryData() {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                var totalSpent = 0.0
                var totalIncome = 0.0
                val categoryTotals = mutableMapOf<String, Double>()

                for (doc in documents) {
                    val expense = doc.toObject(Expense::class.java)
                    if (expense.amount < 0) {
                        totalIncome += -expense.amount
                    } else {
                        totalSpent += expense.amount
                        val category = expense.categoryName.ifBlank { "Other" }
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + expense.amount
                    }
                }

                totalSpentText.text = "Total Spent: R${"%.2f".format(totalSpent)}"
                totalIncomeText.text = "Total Income: R${"%.2f".format(totalIncome)}"

                val categorySummaryList = categoryTotals.map { "${it.key}: R${"%.2f".format(it.value)}" }
                rvCategorySummary.adapter = SimpleTextAdapter(categorySummaryList)

                setupBarChart(categoryTotals)

                checkGoalStatus(categoryTotals)

                handleRewardsAndBadges()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load summary: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupBarChart(categoryTotals: Map<String, Double>) {
        val minGoal = 300f
        val maxGoal = 800f
        val barEntries = mutableListOf<BarEntry>()
        val categoryLabels = mutableListOf<String>()
        val barColors = mutableListOf<Int>()
        var index = 0f

        for ((category, total) in categoryTotals) {
            barEntries.add(BarEntry(index, total.toFloat()))
            categoryLabels.add(category)
            barColors.add(if (total in minGoal..maxGoal) Color.GREEN else Color.RED)
            index++
        }

        val barDataSet = BarDataSet(barEntries, "Spending by Category").apply {
            colors = barColors
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.9f
        }

        barChart.apply {
            data = barData
            setFitBars(true)
            axisRight.isEnabled = false
            invalidate()

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(categoryLabels)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = categoryLabels.size
            }

            axisLeft.apply {
                removeAllLimitLines()
                addLimitLine(LimitLine(minGoal, "Min Goal").apply {
                    lineColor = Color.GREEN
                    lineWidth = 2f
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                })
                addLimitLine(LimitLine(maxGoal, "Max Goal").apply {
                    lineColor = Color.RED
                    lineWidth = 2f
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                })
            }
        }
    }

    private fun checkGoalStatus(categoryTotals: Map<String, Double>) {
        val minGoal = 300.0
        val maxGoal = 800.0
        val withinGoals = categoryTotals.values.all { it in minGoal..maxGoal }

        tvGoalStatus.text = if (withinGoals) {
            "✅ You're within your goals! Great job!"
        } else {
            "⚠️ You're over/under some goals. Adjust your spending."
        }

        tvGoalStatus.setTextColor(if (withinGoals) Color.GREEN else Color.parseColor("#FFA000"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleRewardsAndBadges() {
        val rewardsRef = firestore.collection("users").document(userId!!).collection("rewards")

        // Show badge if earned
        rewardsRef.document("monthlyGoalMet").get().addOnSuccessListener {
            if (it.getBoolean("earned") == true) {
                imgBadge.visibility = View.VISIBLE
                imgBadge.setImageResource(R.drawable.badge)
            }
        }

        // Check for consistency: 5+ unique dates
        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { docs ->
                val uniqueDates = docs.mapNotNull {
                    (it.get("date") as? Timestamp)?.toDate()?.toInstant()?.toString()?.substring(0, 10)
                }.toSet()

                if (uniqueDates.size >= 5) {
                    rewardsRef.document("consistentLogger").set(mapOf("earned" to true))
                    rewardsRef.document("consistentLogger").get().addOnSuccessListener { badge ->
                        if (badge.getBoolean("earned") == true) {
                            imgConsistency.visibility = View.VISIBLE
                            imgConsistency.setImageResource(R.drawable.consistency)
                        }
                    }
                }
            }
    }
}

