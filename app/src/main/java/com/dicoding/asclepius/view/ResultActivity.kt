package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.model.ImageData

class ResultActivity : AppCompatActivity() {

    private val binding by lazy { ActivityResultBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getData()
    }

    private fun getData() {
        val imageData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_IMAGE_DATA, ImageData::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_IMAGE_DATA)
        }
        val imageUri = Uri.parse(imageData?.currentImageUri.orEmpty())
        val label = imageData?.label.orEmpty()
        val score = imageData?.percentageScore.orEmpty()
        val inferenceTime = imageData?.inferenceTimeInMillis.toString()

        binding.apply {
            imageUri?.let {
                binding.resultImage.setImageURI(it)
            }
            resultText.text = getString(R.string.result, label, score, inferenceTime)
        }
    }

    companion object {
        const val EXTRA_IMAGE_DATA = "extra_image_data"
    }
}