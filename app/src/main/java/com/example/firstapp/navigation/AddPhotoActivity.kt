package com.example.firstapp.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.firstapp.R
import com.example.firstapp.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        var addPhotoBtnUpload = findViewById<Button>(R.id.addPhoto_btn_upload)

        //Initiate storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        addPhotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var addPhotoImage = findViewById<ImageView>(R.id.addPhoto_image)
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //path to the selected image
                photoUri = data?.data
                addPhotoImage.setImageURI(photoUri)
            }else{
                //cancel
                finish()
            }
        }
    }


    fun contentUpload() {
        //make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var addPhotoEditExplain = findViewById<EditText>(R.id.addPhoto_edit_explain)

        var storageRef = storage?.reference?.child("image")?.child(imageFileName)

        //promise
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener{ uri ->
                var contetnDTO = ContentDTO()

                //Insert downloadUrl of image
                contetnDTO.imageUrl = uri.toString()

                //Insert uid of user
                contetnDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contetnDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contetnDTO.explain = addPhotoEditExplain.text.toString()

                //Insert timestamp
                contetnDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contetnDTO)

                setResult(Activity.RESULT_OK)

                finish()

        }
    }

}