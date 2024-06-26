package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.model.ImageData
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            showToast(getString(R.string.no_media_selected))
        } else {
            currentImageUri = uri
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        val imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast(error)
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    results?.let { classifications ->
                        classifications.forEach { classification ->
                            val categoriesList = classification.categories
                            val label = categoriesList[0].label
                            val score = categoriesList[0].score
                            val percentageScore = (score * 100).toInt()
                            val percentageScoreStr = "$percentageScore%"
                            moveToResult(currentImageUri, label, percentageScoreStr, inferenceTime)
                        }
                    }
                }
            })

        imageClassifierHelper.classifyStaticImage(imageUri)
    }

    private fun moveToResult(
        currentImageUri: Uri?,
        label: String,
        percentageScore: String,
        inferenceTimeInMillis: Long
    ) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(
                ResultActivity.EXTRA_IMAGE_DATA, ImageData(
                    currentImageUri = currentImageUri.toString(),
                    label = label,
                    percentageScore = percentageScore,
                    inferenceTimeInMillis = inferenceTimeInMillis
                )
            )
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}