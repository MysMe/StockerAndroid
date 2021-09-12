package com.example.cppapp

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cppapp.databinding.ActivitySearchResultBinding
import androidx.core.content.ContextCompat.startActivity

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding


    private external fun searchStock(searchTerm: String): IntArray
    private external fun getStockName(index: Int): String
    private external fun getStockSize(index: Int): String

    data class ItemsViewModel(val name: String, val size: String, val idx: Int) {
    }

    class CustomAdapter(private val mList: List<ItemsViewModel>,
                        private val container: RecyclerView,
                        private val parent: SearchResultActivity) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        // create new views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // inflates the card_view_design view
            // that is used to hold list item
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.stock_entry_layout, parent, false)

            return ViewHolder(view)
        }

        private fun onClick(item: ItemsViewModel)
        {
            val intent = Intent(parent, AlterStockActivity::class.java)
            intent.putExtra("com.example.cppapp.STOCKNAME", item.name)
            intent.putExtra("com.example.cppapp.STOCKSIZE", item.size)
            intent.putExtra("com.example.cppapp.STOCKID", item.idx.toString())

            parent.startActivity(intent)
        }

        // binds the list items to a view
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val itemsViewModel = mList[position]

            // sets the text to the textview from our itemHolder class
            holder.name.text = itemsViewModel.name
            holder.size.text = itemsViewModel.size

            holder.itemView.setOnClickListener {
                onClick(mList[position])
            }
        }

        // return the number of the items in the list
        override fun getItemCount(): Int {
            return mList.size
        }

        // Holds the views for adding it to image and text
        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
            val name: TextView = itemView.findViewById(R.id.StockName)
            val size: TextView = itemView.findViewById(R.id.StockSize)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.StockEntryList)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        val searchTerm: String = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)!!

        val searchResult = searchStock(searchTerm)

        for (i in searchResult) {
            data.add(ItemsViewModel(getStockName(i), getStockSize(i), i))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data, recyclerview, this)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
    }
}