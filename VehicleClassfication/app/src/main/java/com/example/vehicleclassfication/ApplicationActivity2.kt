package com.example.vehicleclassfication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ApplicationActivity2 : AppCompatActivity() {

    private val TAG = "ApplicationActivity2"
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val IMAGE_SIZE = 224
    private val MODEL_FILE_NAME = "images.tflite"
    private val LABELS_FILE_NAME = "labels.txt"
    private var backPressedTime: Long = 0
    private lateinit var imageView: ImageView
    private lateinit var classifiedTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var confidenceTextView: TextView
    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application2)

        imageView = findViewById(R.id.imageView)
        classifiedTextView = findViewById(R.id.classified)
        resultTextView = findViewById(R.id.result)
        confidenceTextView = findViewById(R.id.confidence)
        val button3 = findViewById<View>(R.id.button3)
        val takePictureButton: Button = findViewById(R.id.button1)
        takePictureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val launchGalleryButton: Button = findViewById(R.id.button2)
        launchGalleryButton.setOnClickListener {
            openImagePicker()
        }

        // Initialize TensorFlow Lite classifier
        classifier = Classifier(assets, MODEL_FILE_NAME, LABELS_FILE_NAME)

        // Check camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
        }
        button3.setOnClickListener {
            val intent = Intent(this, ApplicationActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000) {
            Toast.makeText(this, "Press back again to Logout", Toast.LENGTH_SHORT).show()
            backPressedTime = currentTime
        } else {
            super.onBackPressed()
            finish()
        }
    }
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    processImage(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let {
                        val imageBitmap = BitmapFactory.decodeStream(
                            contentResolver.openInputStream(it)
                        )
                        processImage(imageBitmap)
                    }
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
        imageView.setImageBitmap(resizedBitmap)

        val inputByteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Run image classification
        val result = classifier.classifyImage(inputByteBuffer)
        classifiedTextView.text = "Classified as:"
        resultTextView.text = result.className
        confidenceTextView.text = "Confidence: ${result.confidence}"
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            byteBuffer.putFloat((r - 128) / 128.0f)
            byteBuffer.putFloat((g - 128) / 128.0f)
            byteBuffer.putFloat((b - 128) / 128.0f)
        }

        return byteBuffer
    }

    private inner class Classifier(
        private val assetManager: AssetManager,
        private val modelFileName: String,
        private val labelsFileName: String
    ) {
        private lateinit var interpreter: Interpreter
        private val labelList = ArrayList<String>()

        init {
            loadLabels()
            loadModel()
        }

        private fun loadLabels() {
            try {
                val labels = assetManager.open(labelsFileName)
                val bufferedReader = labels.bufferedReader()
                bufferedReader.useLines { lines ->
                    lines.forEach { labelList.add(it) }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading label file: $e")
            }
        }

        private fun loadModel() {
            try {
                val modelFileDescriptor = assetManager.openFd(modelFileName)
                val fileInputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
                val fileChannel = fileInputStream.channel
                val startOffset = modelFileDescriptor.startOffset
                val declaredLength = modelFileDescriptor.declaredLength

                val mappedByteBuffer: MappedByteBuffer =
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
                interpreter = Interpreter(mappedByteBuffer)
            } catch (e: IOException) {
                Log.e(TAG, "Error reading model file: $e")
            }
        }

        fun classifyImage(inputByteBuffer: ByteBuffer): Result {
            // Perform classification using TensorFlow Lite interpreter
            // Modify this method according to your model's input and output tensor requirements
            val outputClasses = labelList.size
            val outputByteBuffer = ByteBuffer.allocateDirect(outputClasses * 4)
            outputByteBuffer.order(ByteOrder.nativeOrder())
            interpreter.run(inputByteBuffer, outputByteBuffer)

            // Process the output tensor and return the classification result
            // Modify this method according to your model's output tensor format
            outputByteBuffer.rewind()
            val outputArray = FloatArray(outputClasses)
            outputByteBuffer.asFloatBuffer().get(outputArray)

            var maxConfidence = 0.0f
            var maxIndex = -1

            for (i in outputArray.indices) {
                if (outputArray[i] > maxConfidence) {
                    maxConfidence = outputArray[i]
                    maxIndex = i
                }
            }

            val className = labelList[maxIndex]
            val confidence = maxConfidence

            return Result(className, confidence)
        }
    }

    data class Result(val className: String, val confidence: Float)
}
