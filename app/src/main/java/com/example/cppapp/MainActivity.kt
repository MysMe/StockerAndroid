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

@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var source: Uri

    private fun setSearch(enabled: Boolean) {
        findViewById<Button>(R.id.Main_SearchButton).isEnabled = enabled
        findViewById<Button>(R.id.Main_AddLocationButton).isEnabled = enabled
        findViewById<Spinner>(R.id.Main_LocationSpinner).isEnabled = enabled
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val result1 = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val result2 = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                //Manifest.permission.MANAGE_EXTERNAL_STORAGE
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 100
        )
    }

    private val getFileForImport = registerForActivityResult(ActivityResultContracts.GetContent())
    {
            uri: Uri? ->
        if (uri != null)
        {
            val f = contentResolver.openFile(uri, "r", null)!!.fd.toString()
            val res = importCSVFromFD("/proc/self/fd/$f")

            if (res != 0)
            {
                findViewById<TextView>(R.id.Main_FileOutput).text = getImportError(res)
            }
            else
            {
                findViewById<TextView>(R.id.Main_FileOutput).text = "File loaded successfully!"
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
            val res = reimportCSVFromFD("/proc/self/fd/$f")

            if (res != 0)
            {
                findViewById<TextView>(R.id.Main_FileOutput).text = getImportError(res)
            }
            else
            {
                findViewById<TextView>(R.id.Main_FileOutput).text = "File loaded successfully!"

                spinnerAdapter.clear()
                for (i in 0 until getStockLocationCount())
                {
                    spinnerAdapter.add(getStockLocationFromID(i))
                }
                spinnerAdapter.notifyDataSetChanged()
                val item = spinnerAdapter.getItem(0)
                setStockLocation(item.toString())
            }
            setSearch(res == 0)
            source = uri
        }
    }

    private val getFileForExport = registerForActivityResult(ActivityResultContracts.CreateDocument())
    {
            uri: Uri? ->
        if (uri != null)
        {
            val f = contentResolver.openFile(uri, "wt", null)
            val path : String = "/proc/self/fd/" + f!!.fd.toString()
            exportCSVFromFD(path, false)
            f.close()
            findViewById<TextView>(R.id.Main_FileOutput).text = "File saved successfully!"
        }
    }

    private val getFileForMinExport = registerForActivityResult(ActivityResultContracts.CreateDocument())
    {
            uri: Uri? ->
        if (uri != null)
        {
            val f = contentResolver.openFile(uri, "wt", null)
            val path : String = "/proc/self/fd/" + f!!.fd.toString()
            exportCSVFromFD(path, true)
            f.close()
            findViewById<TextView>(R.id.Main_FileOutput).text = "File saved successfully!"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSearch(tableHasContent())

        if (!checkPermission())
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

        var spinner = findViewById<Spinner>(R.id.Main_LocationSpinner)

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(pos)
                if (hasStockLocation())
                    setStockLocation(item.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        spinnerAdapter=
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinnerAdapter.add(getStockLocation())
        spinner.setSelection(spinner.size - 1)
        spinnerAdapter.notifyDataSetChanged()
    }

    /** Called when the user taps the Send button */
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
        findViewById<TextView>(R.id.Main_LocationDescriptor).text = location
        if (!hasStockLocation()) {
            spinnerAdapter.remove(
                spinnerAdapter.getItem(0)
            )
        }

        setStockLocation(location)

        spinnerAdapter.add(location)
        findViewById<Spinner>(R.id.Main_LocationSpinner).setSelection(spinnerAdapter.count)
        spinnerAdapter.notifyDataSetChanged()
    }

    /**
     * A native method that is implemented by the 'cppapp' native library,
     * which is packaged with this application.
     */
    private external fun getImportError(ec: Int): String
    private external fun importCSV(CSV: String): Int
    private external fun importCSVFromFD(FD: String): Int
    private external fun reimportCSV(CSV: String): Int
    private external fun reimportCSVFromFD(FD: String): Int
    external fun addLocation(): Void
    private external fun exportCSV(CSV: String, minimal: Boolean): Void
    private external fun exportCSVFromFD(FD: String, minimal: Boolean): Int
    private external fun setStockLocation(location: String): Void
    private external fun getStockLocation(): String
    private external fun hasStockLocation(): Boolean
    private external fun getStockLocationFromID(id: Int): String
    private external fun getStockLocationCount(): Int
    private external fun tableHasContent() : Boolean

    companion object {
        // Used to load the 'cppapp' library on application startup.
        init {
            System.loadLibrary("cppapp")
        }
    }

}