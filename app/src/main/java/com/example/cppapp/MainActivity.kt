package com.example.cppapp

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.cppapp.databinding.ActivityMainBinding
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.content.ContextCompat
import androidx.core.view.size
import android.widget.AdapterView
import androidx.loader.content.CursorLoader
import org.w3c.dom.Text
import java.lang.Exception
import android.content.ContentResolver
import java.net.URISyntaxException
import android.R.attr.data
import java.io.*


object PathUtil {
    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor =
                    context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var source: Uri

    private fun setSearch(enabled: Boolean) {
        findViewById<Button>(R.id.Main_SearchButton).isEnabled = enabled;
        findViewById<Button>(R.id.Main_AddLocationButton).isEnabled = enabled;
        findViewById<Spinner>(R.id.Main_LocationSpinner).isEnabled = enabled;
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
                result1 == PackageManager.PERMISSION_GRANTED
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
            var f = contentResolver.openFile(uri, "r", null)

            var res = importCSVFromFD("/proc/self/fd/" + f!!.fd.toString())

            if (res != 0)
            {
                findViewById<TextView>(R.id.Main_FileOutput).text = getImportError(res)
            }
            setSearch(res == 0)
            source = uri
        }
    }

    private val getFileForReuse = registerForActivityResult(ActivityResultContracts.GetContent())
    {
            uri: Uri? ->
        if (uri != null) {
            var f = contentResolver.openFile(uri, "r", null)
            var res = reimportCSVFromFD("/proc/self/fd/" + f!!.fd.toString())

            if (res != 0)
            {
                findViewById<TextView>(R.id.Main_FileOutput).text = getImportError(res)
            }
            else
            {
                spinnerAdapter.clear()
                for (i in 0 until getStockLocationCount())
                {
                    var temp = getStockLocationFromID(i)
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
            var f = contentResolver.openFile(uri, "w+", null)
            var res = exportCSVFromFD("/proc/self/fd/" + f!!.fd.toString())
        }
    }

    fun export(min: Boolean)
    {
        var path = this.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        var folder = File(path, "Stocker")
        var file = File(folder, "Out.csv")
        file.createNewFile()
        exportCSV(Environment.getExternalStorageDirectory().toString() + "/Stocker/out.csv", min)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSearch(false)

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
            export(true)
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
    fun sendMessage(view: View) {
        val editText = findViewById<EditText>(R.id.Main_SearchInput)
        val message = editText.text.toString()
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
    external fun getImportError(ec: Int): String
    private external fun importCSV(CSV: String): Int
    private external fun importCSVFromFD(FD: String): Int
    private external fun reimportCSV(CSV: String): Int
    private external fun reimportCSVFromFD(FD: String): Int
    external fun addLocation(): Void
    private external fun exportCSV(CSV: String, minimal: Boolean): Void
    private external fun exportCSVFromFD(FD: String, minimal: Boolean): Void
    private external fun setStockLocation(location: String): Void
    private external fun getStockLocation(): String
    private external fun hasStockLocation(): Boolean
    private external fun getStockLocationFromID(id: Int): String
    private external fun getStockLocationCount(): Int

    companion object {
        // Used to load the 'cppapp' library on application startup.
        init {
            System.loadLibrary("cppapp")
        }
    }

}