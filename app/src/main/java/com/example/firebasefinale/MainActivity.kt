package com.example.firebasefinale

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.ktx.Firebase

import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    var currfile: Uri?=null
    val REQUEST_CODE_IMAGE_PICK:Int=0

    //it is necessary to create a reference for our firebase storage to access/upload/delete files
    val imageRef = Firebase.storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("brooo","waht")
        ivImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                launchActivity.launch(it)
            }
        }
        btnUploadImage.setOnClickListener {
            uploadImageToStorage("myImage")
        }
        btnDownloadImage.setOnClickListener {
            downloadImage("myImage")
        }
    }
    private fun downloadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 10L * 1024 * 1024//maximum download size--->10MB is here...
            //when we download our file it is stored as a bytes file...there are multiple ways to download our file
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            //here we convert our image from the bytes we got from above
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main) {
                ivImage.setImageBitmap(bmp)
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    //but we also need to create a image folder with same name as pathstring name we gave here:"images"
    //also check whether the rules are only for author or everyone.....replace "auth!=null" to "true" in rules
    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            currfile?.let {
                //the "images" here is the file name we want to have in firebase
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Successfully uploaded image",
                        Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    //here we pass the image when the new activity is started and we pass the result of that activity as uri for image
    var  launchActivity= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result:ActivityResult->
        Log.d("brooo","waht")
        if(result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            data?.data?.let {
                currfile=it
                ivImage.setImageURI(it)
            }
        }
    }
}