package com.rakuten.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw IllegalArgumentException()
        setContentView(R.layout.activity_second)
    }
}