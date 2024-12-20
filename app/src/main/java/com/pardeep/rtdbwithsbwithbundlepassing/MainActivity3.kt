package com.pardeep.rtdbwithsbwithbundlepassing

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.pardeep.rtdbwithsbwithbundlepassing.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {
    var binding : ActivityMain3Binding?= null
    var name : String ?=""
    var image : String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bundle = intent.extras
        if (bundle!=null){
            name =bundle.getString("name")
           image = bundle.getString("image")
        }
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Glide.with(this)
            .load(image)
            .into(binding?.imageView!!)

        binding?.studentNameTv?.setText("$name")
    }
}