package com.example.networkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var numberEditText: EditText
    private lateinit var showButton: Button
    private lateinit var comicImageView: ImageView
    private lateinit var cacheFile: File
    private val cacheFileName = "last_comic.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        titleTextView = findViewById(R.id.comicTitleTextView)
        descriptionTextView = findViewById(R.id.comicDescriptionTextView)
        numberEditText = findViewById(R.id.comicNumberEditText)
        showButton = findViewById(R.id.showComicButton)
        comicImageView = findViewById(R.id.comicImageView)

        // create reference to cache file in internal storage
        cacheFile = File(filesDir, cacheFileName)

        // 🔹 On startup: if cache file exists, load and display cached comic
        if (cacheFile.exists()) {
            loadCachedComic()
        }

        showButton.setOnClickListener {
            val idText = numberEditText.text.toString().trim()
            val idNum = idText.toIntOrNull()

            if (idNum == null || idNum <= 0) {
                Toast.makeText(this, "Please enter a valid comic number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            downloadComic(idNum.toString())
        }
    }

    // Fetches comic from web as JSONObject
    private fun downloadComic(comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { jsonObject ->
                showComic(jsonObject)
                saveComic(jsonObject)
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error loading comic: ${error.message ?: "unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        requestQueue.add(request)
    }

    // Display a comic for a given comic JSON object
    private fun showComic(comicObject: JSONObject) {
        titleTextView.text = comicObject.optString("title", "No title")
        descriptionTextView.text = comicObject.optString("alt", "No description")

        val imgUrl = comicObject.optString("img", null)
        if (imgUrl != null) {
            Picasso.get().load(imgUrl).into(comicImageView)
        } else {
            comicImageView.setImageDrawable(null)
        }

        // Also update the EditText to match this comic number
        val num = comicObject.optInt("num", 0)
        if (num != 0) {
            numberEditText.setText(num.toString())
        }
    }

    // Save comic JSON to cache file
    private fun saveComic(comicObject: JSONObject) {
        try {
            val outputStream = FileOutputStream(cacheFile)
            outputStream.write(comicObject.toString().toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save comic.", Toast.LENGTH_SHORT).show()
        }
    }

    // Load comic JSON from cache file and display it
    private fun loadCachedComic() {
        try {
            val br = BufferedReader(FileReader(cacheFile))
            val text = StringBuilder()
            var line: String?

            while (br.readLine().also { line = it } != null) {
                text.append(line)
            }
            br.close()

            val json = JSONObject(text.toString())
            showComic(json)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load cached comic.", Toast.LENGTH_SHORT).show()
        }
    }
}