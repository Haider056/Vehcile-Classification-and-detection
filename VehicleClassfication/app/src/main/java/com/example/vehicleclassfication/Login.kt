package com.example.vehicleclassfication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {
    private lateinit var btn_lregister: Button
    private lateinit var btn_llogin: Button
    private lateinit var et_lusername: EditText
    private lateinit var et_lpassword: EditText

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        databaseHelper = DatabaseHelper(this)

        et_lusername = findViewById(R.id.et_lusername)
        et_lpassword = findViewById(R.id.et_lpassword)

        btn_llogin = findViewById(R.id.btn_llogin)
        btn_lregister = findViewById(R.id.btn_lregister)

        btn_lregister.setOnClickListener {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
        }

        btn_llogin.setOnClickListener {
            val username = et_lusername.text.toString()
            val password = et_lpassword.text.toString()

            val checkLogin = databaseHelper.checkLogin(username, password)
            if (checkLogin) {
                val intent = Intent(this, ApplicationActivity2::class.java)

                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
