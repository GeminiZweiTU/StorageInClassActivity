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

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var numberEditText: EditText
    private lateinit var showButton: Button
    private lateinit var comicImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // TODO (3): Load previously saved comic automatically
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
                // Show it
                showComic(jsonObject)
                // TODO (2): Save it
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

        // Also update the EditText to match this comic (if num is present)
        val num = comicObject.optInt("num", 0)
        if (num != 0) {
            numberEditText.setText(num.toString())
        }
    }

    // TODO (2): Implement this function - save comic info when downloaded
    private fun saveComic(comicObject: JSONObject) {
        val prefs = getSharedPreferences("xkcd_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("lastComicJson", comicObject.toString())
            .apply()
    }

    // Helper for TODO (3): Load previously saved comic when app starts
    private fun loadSavedComic() {
        val prefs = getSharedPreferences("xkcd_prefs", MODE_PRIVATE)
        val lastComicJson = prefs.getString("lastComicJson", null) ?: return

        try {
            val jsonObject = JSONObject(lastComicJson)
            showComic(jsonObject)
        } catch (e: Exception) {
            // If parse fails, just ignore and start empty
            e.printStackTrace()
        }
    }
}