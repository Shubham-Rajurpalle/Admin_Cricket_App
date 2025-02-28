package com.cricketapp.admin_cricket_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cricketapp.admin_cricket_app.databinding.ActivityNewsUploadBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NewsUploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsUploadBinding
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val storageRef = FirebaseStorage.getInstance().reference.child("news_images")
    private val firestore = FirebaseFirestore.getInstance().collection("NewsPosts")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectImageBtn.setOnClickListener { selectImage() }
        binding.uploadBtn.setOnClickListener { uploadNews() }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            binding.imagePreview.visibility = View.VISIBLE
            Glide.with(this).load(imageUri).into(binding.imagePreview)
        }
    }

    private fun uploadNews() {
        val title = binding.NewsTitle.text.toString().trim()
        val content = binding.NewsContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields & select an image!", Toast.LENGTH_SHORT).show()
            return
        }

        val docRef = firestore.document()
        val newsId = docRef.id

        val fileRef = storageRef.child("$newsId.jpg")
        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveNewsToFirestore(newsId, title, content, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveNewsToFirestore(id: String, title: String, content: String, imageUrl: String) {
        val news = News(
            id = id,
            title = title,
            imageUrl = imageUrl,
            newsContent = content,
            views = 0,
            timestamp = System.currentTimeMillis()
        )

        firestore.document(id).set(news)
            .addOnSuccessListener {
                Toast.makeText(this, "News Uploaded!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
