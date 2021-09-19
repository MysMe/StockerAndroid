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

class AlterStockActivity : AppCompatActivity() {

    private external fun getStockCount(index: Int): Float
    private external fun alterStock(index: Int, delta: Float): Void
    private external fun getStockLocation(): String

    private var stockID : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alter_stock)

        val stockName = intent.getStringExtra("com.example.cppapp.STOCKNAME")
        val stockSize = intent.getStringExtra("com.example.cppapp.STOCKSIZE")
        stockID = intent.getStringExtra("com.example.cppapp.STOCKID")!!.toInt()

        findViewById<TextView>(R.id.Alter_StockName).text = "Product Name: $stockName"
        findViewById<TextView>(R.id.Alter_StockSize).text = "Product Size: $stockSize"
        findViewById<TextView>(R.id.Alter_CurrentStock).text = "Current Count: " + getStockCount(stockID).toString()
        findViewById<TextView>(R.id.Alter_Location).text = "Current Location: " + getStockLocation()
    }

    fun confirmAlter(view: View) {
        val input = findViewById<EditText>(R.id.Alter_Value)
        val delta = input.text.toString().toFloatOrNull()
        if (delta == null)
        {
            input.setBackgroundColor(Color.RED)
            return
        }

        alterStock(stockID, delta)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
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