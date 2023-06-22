package com.example.vehicleclassfication

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class ApplicationActivity : AppCompatActivity() {
    private lateinit var ip: String
    private lateinit var port: String
    private lateinit var filePathCallback: ValueCallback<Array<Uri>>

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ip_side)

        val button: Button = findViewById(R.id.button)
        val txt1: EditText = findViewById(R.id.ip)
        val txt2: EditText = findViewById(R.id.port)

        button.setOnClickListener {
            ip = txt1.text.toString()
            port = txt2.text.toString()
            WwebView(it)
        }
    }

    private fun WwebView(v: View) {
        val url = "http://$ip:$port"

        setContentView(R.layout.webview)
        val wb: WebView = findViewById(R.id.wbView)
        wb.loadUrl(url)
        wb.settings.javaScriptEnabled = true
        wb.settings.allowContentAccess = true
        wb.settings.allowFileAccess = true
        wb.settings.allowFileAccessFromFileURLs = true
        wb.settings.allowUniversalAccessFromFileURLs = true

        wb.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val intent = fileChooserParams?.createIntent()
                intent?.type = "image/*"  // Set the intent type to select only image files
                intent?.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // Allow multiple image selection if needed

                try {
                    if (intent != null) {
                        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                    }
                } catch (e: ActivityNotFoundException) {
                    return false
                }

                filePathCallback?.let {
                    this@ApplicationActivity.filePathCallback = it
                }
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val uris = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                filePathCallback.onReceiveValue(uris)
            } else {
                filePathCallback.onReceiveValue(null)
            }
        }
    }
}