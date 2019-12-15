package com.don.myapplication

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import pub.devrel.easypermissions.EasyPermissions
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class InstaActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    val MYTAG = "MY_MESSAGE"
    //    lateinit var  mProgressDialog: ProgressBar
    lateinit var mImageView: ImageView
    lateinit var mImageViewInternal: ImageView

    private var mMyTask: AsyncTask<*, *, *>? = null

    private var absolutePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insta)

        val button = findViewById<Button>(R.id.btn_do)
        val share = findViewById<Button>(R.id.btn_share)
        mImageView = findViewById(R.id.iv)
        mImageViewInternal = findViewById(R.id.iv_internal)
//        mProgressDialog = findViewById(R.id.progress)
        button.setOnClickListener {
            mMyTask = DownloadTask()
                .execute(
                    stringToURL(
                        "https://image.tmdb.org/t/p/w200/kqjL17yufvn9OVLyXYpvtyrFfak.jpg"
                    )
                )
        }

        share.setOnClickListener {
            shareIG()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this@InstaActivity
        )

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        Log.d(MYTAG, "Permission has been denied")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        //Download the file once permission is granted
        mMyTask = DownloadTask()
            .execute(
                stringToURL(
                    "https://image.tmdb.org/t/p/w200/kqjL17yufvn9OVLyXYpvtyrFfak.jpg"
                )
            )
    }

    private inner class DownloadTask : AsyncTask<URL, Void, Bitmap>() {
        // Before the tasks execution
        override fun onPreExecute() {
            // Display the progress dialog on async task start
//            mProgressDialog.visibility
        }

        // Do the task in background/non UI thread
        override fun doInBackground(vararg urls: URL): Bitmap? {
            val url = urls[0]
            var connection: HttpURLConnection? = null

            try {
                // Initialize a new http url connection
                connection = url.openConnection() as HttpURLConnection

                // Connect the http url connection
                connection!!.connect()

                // Get the input stream from http url connection
                val inputStream = connection!!.getInputStream()

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                val bufferedInputStream = BufferedInputStream(inputStream)

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object

                // Return the downloaded bitmap
                return BitmapFactory.decodeStream(bufferedInputStream)

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                // Disconnect the http url connection
                connection!!.disconnect()
            }
            return null
        }

        // When all async task done
        override fun onPostExecute(result: Bitmap?) {
            // Hide the progress dialog
//            mProgressDialog.dismiss()

            if (result != null) {
                // Display the downloaded image into ImageView
                mImageView.setImageBitmap(result)

                // Save bitmap to internal storage
                val imageInternalUri = saveImageToInternalStorage(result)
                // Set the ImageView image from internal storage
                mImageViewInternal.setImageURI(imageInternalUri)
            } else {
                // Notify user that an error occurred while downloading image

                Toast.makeText(this@InstaActivity, "ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Custom method to convert string to url
    private fun stringToURL(urlString: String): URL? {
        try {
            return URL(urlString)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        return null
    }

    // Custom method to save a bitmap into internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        // Initialize ContextWrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
//        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
//        var file = wrapper.getDir(getFilesDir())
        var file = applicationContext.filesDir
        // Create a file to save the image
        file = File(file, "UniqueFileName" + ".jpg")

        try {
            // Initialize a new OutputStream
            var stream: OutputStream? = null

            // If the output file exists, it can be replaced or appended to it
            stream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flushes the stream
            stream!!.flush()

            // Closes the stream
            stream!!.close()

        } catch (e: IOException) // Catch the exception
        {
            e.printStackTrace()
        }

        // Parse the gallery image url to uri

        // Return the saved image Uri
        absolutePath = file.absolutePath
        return Uri.parse(file.absolutePath)
    }

    private fun shareIG() {

//        val uri = FileProvider.getUriForFile(this@InstaActivity,"com.don.myapplication.provider", File(absolutePath))
//        val uri = Uri.fromFile(File(absolutePath))


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val uri = FileProvider.getUriForFile(
                this@InstaActivity,
                "com.don.myapplication.provider",
                File(absolutePath)
            )
            shareMan(uri)

        } else {
            val uri = Uri.fromFile(File(absolutePath))
            shareMan(uri)
        }


    }

    private fun shareMan(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Ini ada textnya loch")
        shareIntent.setType("image/*")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        shareIntent.setPackage("com.instagram.android")
        startActivity(Intent.createChooser(shareIntent, "Share.."))
    }

}


