package com.example.cppapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class AlterStockActivity : AppCompatActivity() {

    private var stockID : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alter_stock)

        stockID = intent.getStringExtra("com.example.cppapp.STOCKID")!!.toInt()
        val stockName = table.getStockName(stockID)
        val stockSize = table.getStockSize(stockID)

        findViewById<TextView>(R.id.Alter_StockName).text = "Product Name: $stockName"
        findViewById<TextView>(R.id.Alter_StockSize).text = "Product Size: $stockSize"
        findViewById<TextView>(R.id.Alter_CurrentStock).text = "Current Count Total: " + table.getStockCount(stockID).toString()
        if (table.getLocationCount() == 0)
            findViewById<TextView>(R.id.Alter_Location).text = ""
        else
            findViewById<TextView>(R.id.Alter_Location).text = "Current Count For " + table.getLocationName(table.getCurrentLocation()) +
                ": " + table.getStockCountAt(stockID, table.getCurrentLocation())
    }

    private fun applyAlter(): Boolean
    {
        val input = findViewById<EditText>(R.id.Alter_Value)
        val delta = input.text.toString().toFloatOrNull()
        if (delta == null)
        {
            input.setBackgroundColor(Color.RED)
            Toast.makeText(applicationContext, "Number required", Toast.LENGTH_LONG).show()
            return false
        }

        table.alter(stockID, delta)
        addToHistory(stockID, delta)
        return true
    }

    fun confirmAlter(view: View) {
        if (!applyAlter())
            return

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    fun confirmAndBackToResult(view: View) {
        if (!applyAlter())
            return

        finish()
    }

    fun backToSearchResult(view: View) {
        finish()
    }

    fun backToSearch(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}