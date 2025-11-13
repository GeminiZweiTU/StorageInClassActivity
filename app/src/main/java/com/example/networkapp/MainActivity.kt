package com.example.networkapp

import android.content.SharedPreferences
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

private const val LAST_COMIC_KEY = "last_comic_json"

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var numberEditText: EditText
    private lateinit var showButton: Button
    private lateinit var comicImageView: ImageView

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get preferences for this activity
        preferences = getPreferences(MODE_PRIVATE)

        requestQueue = Volley.newRequestQueue(this)

        titleTextView = findViewById(R.id.comicTitleTextView)
        descriptionTextView = findViewById(R.id.comicDescriptionTextView)
        numberEditText = findViewById(R.id.comicNumberEditText)
        showButton = findViewById(R.id.showComicButton)
        comicImageView = findViewById(R.id.comicImageView)

        showButton.setOnClickListener {
            val idText = numberEditText.text.toString().trim()

            if (idText.isEmpty()) {
                Toast.makeText(this, "Please enter a comic number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idNum = idText.toIntOrNull()
            if (idNum == null || idNum <= 0) {
                Toast.makeText(this, "Please enter a valid positive number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            downloadComic(idNum.toString())
        }

        // Load previously saved comic when app starts
        loadSavedComic()
    }

    // Fetches comic from web as JSONObject
    private fun downloadComic(comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { jsonObject ->
                // Show the comic
                showComic(jsonObject)
                // Save the comic when downloaded
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

        // Also update the EditText to match this comic number (if present)
        val num = comicObject.optInt("num", 0)
        if (num != 0) {
            numberEditText.setText(num.toString())
        }
    }

    // Save comic info when downloaded
    private fun saveComic(comicObject: JSONObject) {
        val editor = preferences.edit()
        editor.putString(LAST_COMIC_KEY, comicObject.toString())
        editor.apply()
    }

    // Load previously saved comic when app starts
    private fun loadSavedComic() {
        val lastComicJson = preferences.getString(LAST_COMIC_KEY, null) ?: return

        try {
            val comicObject = JSONObject(lastComicJson)
            showComic(comicObject)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}