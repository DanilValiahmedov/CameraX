package com.example.camerax

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.camerax.databinding.ActivityMainBinding
import com.example.camerax.fragments.CameraFragment

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val fragment = CameraFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentСontainer, fragment)
            .commit()
    }
}