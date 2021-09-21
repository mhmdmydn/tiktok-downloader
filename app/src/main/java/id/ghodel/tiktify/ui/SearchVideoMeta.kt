package id.ghodel.tiktify.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import id.ghodel.tiktify.databinding.ActivitySearchVideoMetaBinding

class SearchVideoMeta : AppCompatActivity() {

    private lateinit var binding: ActivitySearchVideoMetaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchVideoMetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButton.setOnClickListener {
            val url = binding.etSearchUrl.text.toString()
            val isValidURL = Patterns.WEB_URL.matcher(url).matches()

            if (url.isNotEmpty() && isValidURL){

                Log.d("URL", url)
                val intent = Intent(this, MainActivity::class.java)
                intent.action = Intent.ACTION_VIEW
                intent.putExtra("URL", url)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Harap masukan URL yang sesuai.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}