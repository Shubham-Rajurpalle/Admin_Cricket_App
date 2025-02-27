package com.cricketapp.admin_cricket_app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cricketapp.admin_cricket_app.databinding.ActivityUploadShotBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.net.URLDecoder

class UploadShot : AppCompatActivity() {
    private lateinit var binding: ActivityUploadShotBinding
    private lateinit var videoStorageRef: StorageReference
    private lateinit var thumbnailStorageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private var videoUri: Uri? = null
    private var thumbnailUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadShotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        // Initialize Firebase
        videoStorageRef = FirebaseStorage.getInstance().reference.child("videos")
        thumbnailStorageRef = FirebaseStorage.getInstance().reference.child("thumbnails")
        firestore = FirebaseFirestore.getInstance()

        // Select Video
        binding.btnSelectVideo.setOnClickListener { selectVideo.launch("video/*") }

        // Select Thumbnail
        binding.btnSelectThumbnail.setOnClickListener { selectThumbnail.launch("image/*") }

        // Upload Video
        binding.btnUploadVideo.setOnClickListener {
            val title = binding.etVideoTitle.text.toString().trim()
            if (title.isEmpty()) {
                showToast("Enter a video title!")
                return@setOnClickListener
            }
            if (videoUri == null || thumbnailUri == null) {
                showToast("Select both video and thumbnail!")
                return@setOnClickListener
            }
            uploadThumbnail()
        }
    }

    private val selectVideo =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                videoUri = uri
                binding.tvSelectedVideo.text = "Selected: ${uri.lastPathSegment}"
            }
        }

    private val selectThumbnail =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                thumbnailUri = uri
                binding.ivThumbnail.apply {
                    setImageURI(thumbnailUri)
                    visibility = View.VISIBLE
                }
            }
        }

    private fun uploadThumbnail() {
        binding.progressBar.visibility = View.VISIBLE
        val thumbRef = thumbnailStorageRef.child("${System.currentTimeMillis()}.jpg")
        val uploadTask = thumbRef.putFile(thumbnailUri!!)

        uploadTask.addOnSuccessListener {
            thumbRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("UploadShot", "Thumbnail Uploaded: $uri")
                thumbnailUri = uri
                uploadVideo()
            }
        }.addOnFailureListener {
            Log.e("UploadShot", "Thumbnail upload failed!", it)
            showToast("Thumbnail upload failed!")
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun uploadVideo() {
        val videoRef = videoStorageRef.child("${System.currentTimeMillis()}.mp4")
        val uploadTask = videoRef.putFile(videoUri!!)

        uploadTask.addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { videoUrl ->
                Log.d("UploadShot", "Video Uploaded: $videoUrl")  // Debugging log
                saveDataToFirestore(videoUrl.toString(), thumbnailUri.toString())
            }
        }.addOnFailureListener {
            Log.e("UploadShot", "Video upload failed!", it)
            showToast("Video upload failed!")
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun saveDataToFirestore(videoUrl: String, thumbUrl: String) {
        val title = binding.etVideoTitle.text.toString().trim()
        val docRef = firestore.collection("videos").document() // Generate a Firestore document ID
        val videoData = Video(
            id = docRef.id, // Store document ID in the object
            title = title,
            videoUrl = videoUrl,
            thumbnailUrl = thumbUrl,
            views = 0,
            timestamp = System.currentTimeMillis()
        )

        docRef.set(videoData)
            .addOnSuccessListener {
                Log.d("UploadShot", "Video saved to Firestore successfully!")
                binding.progressBar.visibility = View.GONE
                showToast("Video uploaded successfully!")
                finish()
            }
            .addOnFailureListener {
                Log.e("UploadShot", "Failed to save video in Firestore!", it)
                showToast("Failed to upload video!")
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
