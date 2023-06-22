package com.example.vehicleclassfication

import android.content.Intent
import android.os.Bundle
import android.os.Handler

import android.widget.Button
import android.widget.EditText

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private var backPressedOnce = false
    private lateinit var et_username: EditText
    private lateinit var et_password: EditText
    private lateinit var et_cpassword: EditText
    private lateinit var btn_register: Button
    private lateinit var btn_login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        databaseHelper = DatabaseHelper(this)
        et_username = findViewById(R.id.et_username)
        et_password = findViewById(R.id.et_password)
        et_cpassword = findViewById(R.id.et_cpassword)
        btn_register = findViewById(R.id.btn_register)
        btn_login = findViewById(R.id.btn_login)

        btn_login.setOnClickListener {
            val intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
        }

        btn_register.setOnClickListener {
            val username = et_username.text.toString()
            val password = et_password.text.toString()
            val confirmPassword = et_cpassword.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(applicationContext, "Fields Required", Toast.LENGTH_SHORT).show()
            } else {
                if (password == confirmPassword) {
                    val checkUsername = databaseHelper.checkUsername(username)
                    if (checkUsername) {
                        val insert = databaseHelper.insert(username, password)
                        if (insert) {
                            Toast.makeText(applicationContext, "Registered", Toast.LENGTH_SHORT).show()
                            et_username.setText("")
                            et_password.setText("")
                            et_cpassword.setText("")
                        }
                    } else {
                        Toast.makeText(applicationContext, "Username already taken", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed()
            return
        }

        this.backPressedOnce = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()

        // Reset the flag after a delay of 2 seconds
        Handler().postDelayed({ backPressedOnce = false }, 2000)
    }
}
