package com.example.cppapp

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.cppapp.databinding.ActivityMainBinding
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.content.ContextCompat
import androidx.core.view.size
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import java.lang.StringBuilder

var table: StockTable = StockTable()
var lookups: LookupTable = LookupTable(table)


@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var source: Uri

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
                val item = spinnerAdapter.getItem(0)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSearch(false)

        requestPermission()

        val importButton = findViewById<Button>(R.id.Main_ImportButton)
        importButton.setOnClickListener{
            getFileForImport.launch("text/*")
        }

        val reimportButton = findViewById<Button>(R.id.Main_ReimportButton)
        reimportButton.setOnClickListener{
            getFileForReuse.launch("text/*")
        }

        val exportButton = findViewById<Button>(R.id.Main_ExportButton)
        exportButton.setOnClickListener{
            getFileForExport.launch("out.csv")
        }

        val exportMinButton = findViewById<Button>(R.id.Main_MinexportButton)
        exportMinButton.setOnClickListener{
            getFileForMinExport.launch("out.csv")
        }

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

    fun broadcastMin(view: View)
    {
        broadcastCount(true)
    }

    fun broadcastAll(view: View)
    {
        broadcastCount(false)
    }

    companion object {
        // Used to load the 'cppapp' library on application startup.
        init {
            System.loadLibrary("cppapp")
        }
    }

}