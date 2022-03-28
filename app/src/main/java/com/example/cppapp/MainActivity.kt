package com.example.cppapp

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import androidx.core.app.ActivityCompat
import com.example.cppapp.databinding.ActivityMainBinding
import android.net.Uri
import android.os.Build
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.view.size
import android.widget.AdapterView
import androidx.annotation.RequiresApi

import android.widget.TextView
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.widget.Toast
import android.view.inputmethod.EditorInfo

import android.widget.TextView.OnEditorActionListener
import android.widget.EditText











var table: StockTable = StockTable()
var lookups: LookupTable = LookupTable(table)

class bufferData{
    public var ID: Int
    public var  delta: Float

    constructor(id: Int, delta: Float)
    {
        this.ID = id
        this.delta = delta
    }
}

var history: MutableList<bufferData> = mutableListOf<bufferData>()

fun addToHistory(ID: Int, delta: Float)
{
    history.add(0, bufferData(ID, delta))
    while (history.size > 5)
        history.removeAt(5)
}

@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var source: Uri

    class CustomAdapter(private val parent: MainActivity) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        // create new views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // inflates the card_view_design view
            // that is used to hold list item
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.stock_entry_layout, parent, false)

            return ViewHolder(view)
        }

        // binds the list items to a view
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = history[position]

            // sets the text to the textview from our itemHolder class
            holder.product.text = table.getStockName(item.ID)
            holder.delta.text = item.delta.toString()

            holder.itemView.setOnClickListener {
                onClick(position)
            }
        }

        private fun onClick(index: Int)
        {
            val intent = Intent(parent, AlterStockActivity::class.java)
            intent.putExtra("com.example.cppapp.STOCKID", history[index].ID.toString())

            parent.startActivity(intent)
        }

        // return the number of the items in the list
        override fun getItemCount(): Int {
            return history.size
        }

        // Holds the views for adding it to image and text
        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
            val product: TextView = itemView.findViewById(R.id.StockName)
            val delta: TextView = itemView.findViewById(R.id.StockSize)
        }
    }

    private fun toast(message: String)
    {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setSearch(enabled: Boolean) {
        findViewById<Button>(R.id.Main_SearchButton).isEnabled = enabled
        findViewById<Button>(R.id.Main_AddLocationButton).isEnabled = enabled
        findViewById<Spinner>(R.id.Main_LocationSpinner).isEnabled = enabled
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                //Manifest.permission.MANAGE_EXTERNAL_STORAGE
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            ), 100
        )
    }

    private val getFileForImport = registerForActivityResult(ActivityResultContracts.GetContent())
    {
            uri: Uri? ->
        if (uri != null)
        {
            val f = contentResolver.openFile(uri, "r", null)!!.fd.toString()
            val res = table.importFromFile("/proc/self/fd/$f")

            if (res != 0)
            {
                toast("Unable to open file: " + table.getImportError(res))
            }
            else
            {
                lookups = LookupTable(table)
                toast("File loaded successfully")
            }
            setSearch(res == 0)
            source = uri
        }
    }

    private val getFileForReuse = registerForActivityResult(ActivityResultContracts.GetContent())
    {
            uri: Uri? ->
        if (uri != null) {
            val f = contentResolver.openFile(uri, "r", null)!!.fd.toString()
            val res = table.reuseFromFile("/proc/self/fd/$f")

            if (res != 0)
            {
                toast("Unable to open file: " + table.getImportError(res))
            }
            else
            {
                lookups = LookupTable(table)
                toast("File loaded successfully")

                spinnerAdapter.clear()
                if (table.getLocationCount() == 0)
                    spinnerAdapter.add(("Global"))
                else {
                    for (i in 0 until table.getLocationCount()) {
                        spinnerAdapter.add(table.getLocationName(i))
                    }
                }
                spinnerAdapter.notifyDataSetChanged()
                val item = spinnerAdapter.getItem(spinnerAdapter.count - 1)
                val spinner = findViewById<Spinner>(R.id.Main_LocationSpinner)
                spinner.setSelection(spinner.count - 1)
                table.setLocation(item.toString())
            }
            setSearch(res == 0)
            source = uri
        }
    }

    private fun export(uri: Uri, min: Boolean)
    {
        val f = contentResolver.openFile(uri, "wt", null)
        val path : String = "/proc/self/fd/" + f!!.fd.toString()
        table.exportToFile(path, min)
        f.close()
        toast("File saved successfully")
    }

    private val getFileForExport = registerForActivityResult(ActivityResultContracts.CreateDocument())
    {
            uri: Uri? ->
        if (uri != null)
        {
            export(uri, false)
        }
    }

    private val getFileForMinExport = registerForActivityResult(ActivityResultContracts.CreateDocument())
    {
            uri: Uri? ->
        if (uri != null)
        {
            export(uri, true)
        }
    }

    private fun quickSave()
    {
        val file = File(filesDir, "quickdata.csv")
        table.exportToFile(file.path, false)
        toast("File saved successfully")
    }

    private fun quickLoad()
    {
        val file = File(filesDir, "quickdata.csv")
        val res = table.reuseFromFile(file.path)

        if (res != 0) {
            toast("Unable to open file: " + table.getImportError(res))
        } else {
            lookups = LookupTable(table)
            toast("File loaded successfully")

            spinnerAdapter.clear()
            if (table.getLocationCount() == 0)
                spinnerAdapter.add(("Global"))
            else {
                for (i in 0 until table.getLocationCount()) {
                    spinnerAdapter.add(table.getLocationName(i))
                }
            }
            spinnerAdapter.notifyDataSetChanged()
            val item = spinnerAdapter.getItem(spinnerAdapter.count - 1)
            val spinner = findViewById<Spinner>(R.id.Main_LocationSpinner)
            spinner.setSelection(spinner.count - 1)
            table.setLocation(item.toString())
        }
        setSearch(res == 0)
    }

    private fun quickLoadQuery() {
        if (findViewById<Button>(R.id.Main_SearchButton).isEnabled == false)
        {
            quickLoad()
            return
        }
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirm")
        builder.setMessage("Any unsaved data will be lost.")

        builder.setPositiveButton("YES") { dialog, which ->
            quickLoad()
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "NO"
        ) { dialog, which ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_quicksave -> {
            quickSave()
            true
        }

        R.id.action_quickload -> {
            quickLoadQuery()
            true
        }

        R.id.action_save -> {
            getFileForExport.launch("out.csv")
            true
        }

        R.id.action_save_min -> {
            getFileForMinExport.launch("out.csv")
            true
        }

        R.id.action_import -> {
            getFileForImport.launch("text/*")
            true
        }

        R.id.action_reuse -> {
            getFileForReuse.launch("text/*")
            true
        }


        R.id.action_broadcast -> {
            broadcastCount(false)
            true
        }


        R.id.action_broadcast_min -> {
            broadcastCount(true)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun displayHistory()
    {
        val hist = findViewById<RecyclerView>(R.id.Main_History)

        // this creates a vertical layout Manager
        hist.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(this)

        // Setting the Adapter with the recyclerview
        hist.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_toolbar, menu)
        return true
    }

    override fun onResume()
    {
        super.onResume()
        displayHistory()
        findViewById<EditText>(R.id.Main_SearchInput).requestFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<EditText>(R.id.Main_SearchInput)
            .setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // If the event is a key-down event on the "enter" button
                if (event.action === KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    // Perform action on key press
                    search(v!!)
                    return true
                }
                return false
            }
        })

        setSearch(false)

        requestPermission()

        val spinner = findViewById<Spinner>(R.id.Main_LocationSpinner)

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(pos)
                if (table.getLocationCount() != 0)
                    table.setLocation(item.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        displayHistory()

        spinnerAdapter=
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinnerAdapter.add("Global")
        spinner.setSelection(spinner.size - 1)
        spinnerAdapter.notifyDataSetChanged()
    }

    fun search(view: View) {
        val editText = findViewById<EditText>(R.id.Main_SearchInput)
        val message = editText.text.toString()
        editText.text.clear()
        val intent = Intent(this, SearchResultActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    fun addLocation(view: View){
        val locationSrc = findViewById<EditText>(R.id.Main_LocationInput)
        val location = locationSrc.text.toString()
        if (location.isEmpty())
        {
            toast("All locations must have names")
            return
        }

        //By default "Global" is the only option, remove this as soon as it is not needed
        if (table.getLocationCount() == 0) {
            spinnerAdapter.remove(
                spinnerAdapter.getItem(0)
            )
        }

        if (table.setLocation(location)) {
            spinnerAdapter.add(location)
            findViewById<Spinner>(R.id.Main_LocationSpinner).setSelection(spinnerAdapter.count)
        }
        else
        {
            findViewById<Spinner>(R.id.Main_LocationSpinner).setSelection(table.getCurrentLocation())
        }

        spinnerAdapter.notifyDataSetChanged()
    }

    private fun broadcastCount(min: Boolean)
    {
        var UDP = UDPSender(Char(40404))
        UDP.use {
            UDP.broadcastAddressRequest()
            var res = UDP.awaitMessage(Char(1000))
            if (!res.valid()) {
                toast("Did not get any response")
                return
            }
            val maxTimeouts = 10
            var timeouts = 0

            do {
                UDP.sendLinkRequest(res.address(), Char(40405))
                val nextRes = UDP.awaitMessage(Char(3000))
                if (!nextRes.valid())
                {
                    timeouts++
                    continue
                }
                if (nextRes.address() != res.address())
                    continue
                val m = nextRes.request()
                if (nextRes.request() == nextRes.requestConfirmLink())
                    break
                if (nextRes.request() == nextRes.requestDenyLink())
                {
                    toast("Request was denied by client")
                    return
                }
            } while (timeouts < maxTimeouts)

            if (timeouts == maxTimeouts) {
                toast("Link request timed out")
                return
            }

            timeouts = 0


            var TCP = TCPSender()
            TCP.use {
                while (!TCP.connect(res.address(), Char(40405), Char(300)))
                {
                    if (timeouts == maxTimeouts)
                    {
                        toast("Link accept timed out")
                        return
                    }
                    timeouts++
                }

                TCP.send(table.exportToString(min))

                toast("Content sent")
            }
        }
    }

    companion object {
        // Used to load the 'cppapp' library on application startup.
        init {
            System.loadLibrary("cppapp")
        }
    }

}