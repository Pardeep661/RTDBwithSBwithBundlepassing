package com.pardeep.rtdbwithsbwithbundlepassing

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.pardeep.rtdbwithsbwithbundlepassing.databinding.ActivityMain2Binding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity2 : AppCompatActivity() ,RecyclerInterface {

    lateinit var supabaseClient: SupabaseClient
    var binding : ActivityMain2Binding?=null
    var studentDataArray = arrayListOf<StudentData>()
    var myAdp = MyAdp(studentDataArray, this,this)
    var addImageView : ImageView?=null
    var updateImageView : ImageView?=null
    private val TAG = "MainActivity2"
    val firebase = FirebaseDatabase.getInstance()
    val myRef = firebase.getReference("StudentData")
    lateinit var linearLayoutManager: LinearLayoutManager
    var imageUri : Uri?=null
    var state = false
    var imageUrl : String?=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        supabaseClient = (applicationContext as MyApplication).supabaseClient
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // -------------------------- firestore functionality------------------
        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val studentDataModel : StudentData ?=snapshot.getValue(StudentData::class.java)
                studentDataModel?.id = snapshot.key
                if (studentDataModel!=null) {
                    studentDataArray.add(studentDataModel)
                    myAdp.notifyDataSetChanged()
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val studentDataModel : StudentData ?=snapshot.getValue(StudentData::class.java)
                studentDataModel?.id = snapshot.key

                if (studentDataModel!=null){
                    studentDataArray.forEachIndexed { index, studentData ->
                        if (studentData.id == studentDataModel.id){
                            studentDataArray[index] = studentDataModel
                            myAdp.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val studentDataModel : StudentData ?=snapshot.getValue(StudentData::class.java)
                studentDataModel?.id = snapshot.key
                studentDataArray.remove(studentDataModel)
                myAdp.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        // -------------------------- firestore functionality------------------


        binding?.fab?.setOnClickListener {
            Dialog(this).apply {
                setContentView(R.layout.custom_dialog_layout)
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val studentNameEt = findViewById<EditText>(R.id.studentEt)
                addImageView = findViewById<ImageView>(R.id.dialogImageview)
                val addBtn = findViewById<Button>(R.id.addBtn)

                addImageView?.setOnClickListener {
                    selectImage()
                    checkAndRequestPermission()
                }
                //---------------------- on add button click ----------------------
                addBtn.setOnClickListener {
                    if (studentNameEt.text.trim().isNullOrEmpty()) {
                        studentNameEt.error = "Please enter student name"
                    }else {
                        dismiss()
                        state = true
                        Toast.makeText(this@MainActivity2, "$imageUri", Toast.LENGTH_SHORT).show()
                        stateChecker(imageUri!!)
                        Handler(Looper.getMainLooper()).postDelayed({
                            val key = myRef.push().key!!
                            Toast.makeText(this@MainActivity2,"${imageUrl}", Toast.LENGTH_SHORT).show()
                            val newData = StudentData(id = key, name = studentNameEt.text.toString() , image = imageUrl )
                            myRef.child(key).setValue(newData)
                        },3000)
                        state = false
                    }
                }
                //---------------------- on add button click ----------------------
            }.show()
        }

        binding?.recyclerView?.adapter = myAdp
        linearLayoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding?.recyclerView?.layoutManager = linearLayoutManager


    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // permission granted
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestMangeExternalStoragePermission()
                    }

                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMangeExternalStoragePermission()
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
            }
        }
    }

    private fun requestMangeExternalStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent , 100)
            }catch (e: ActivityNotFoundException) {
                Log.e(
                    TAG,
                    "requestMangeExternalStoragePermission: Activity not found for the intent",
                )
            }
        } else {
            Log.e(
                TAG,
                "requestMangeExternalStoragePermission: This is only applicable on android 11",
            )
        }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onRequestPermissionsResult: Permission granted")
                }
            }

            101 -> {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "full storage access granted", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, " storage access denied", Toast.LENGTH_SHORT).show()

                }

            }

        }
    }


    private fun selectImage() {
        startActivityForResult(Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.data.let { uri ->
            addImageView?.setImageURI(uri)
            updateImageView?.setImageURI(uri)
            imageUri = uri
            if (uri != null) {
                stateChecker(uri)
            }
        }
    }

    private fun stateChecker(uri: Uri) {
        if (state){
            uploadtoSupaBase(uri)
        }
    }

    private fun uploadtoSupaBase(uri: Uri){
        val bucket = supabaseClient.storage.from("RTDB_with_BundlePassing")
        val byteArray = convertToByteArray(uri ,this)
        val fileName = "upload/${System.currentTimeMillis()}.jpg"

        lifecycleScope.launch {
            try {
                bucket?.uploadAsFlow(fileName,byteArray)?.collect{status ->
                    when(status){
                        is UploadStatus.Progress ->{
                            Log.d(TAG, "uploadToSupaBase: progress%")
                        }

                        is UploadStatus.Success ->{
                            imageUrl = bucket.publicUrl(fileName)
                            Toast.makeText(this@MainActivity2, "${imageUrl}", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "uploadToSupaBase: ${imageUrl}")

                        }
                    }
                }
            }
            catch (e : Exception){
                withContext(Dispatchers.Main){
                    Log.e(TAG, "uploadToSupaBase:Error uploading image : ${e.message}" )
                    Toast.makeText(this@MainActivity2, "Error uploading image : ${e.message}", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun convertToByteArray(uri: Uri, context: Context): ByteArray {
        val inputStram = context.contentResolver.openInputStream(uri)
        return inputStram?.readBytes()?: ByteArray(0)
    }

    override fun onItemClick(positon: Int) {
        val intent = Intent(this,MainActivity3::class.java)
        val bundle = Bundle()
        bundle.putString("name", studentDataArray[positon].name)
        bundle.putString("image" , studentDataArray[positon].image)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun operationType(positon: Int, callFor: String) {
        when(callFor){
            "delete"->{
                deleteData(positon)
            }
            "update" ->{
                updateData(positon)
            }
        }
    }

    private fun updateData(positon: Int) {
        Dialog(this).apply {
            setContentView(R.layout.custom_dialog_layout)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val studentNameEt = findViewById<EditText>(R.id.studentEt)
            updateImageView = findViewById<ImageView>(R.id.dialogImageview)
            val addBtn = findViewById<Button>(R.id.addBtn)
            addBtn.setText("Update")
            studentNameEt.setText(studentDataArray[positon].name)
            Glide.with(this@MainActivity2)
                .load(studentDataArray[positon].image)
                .into(updateImageView!!)

            updateImageView?.setOnClickListener {
                selectImage()
                checkAndRequestPermission()
            }
            //---------------------- on add button click ----------------------
            addBtn.setOnClickListener {
                if (studentNameEt.text.trim().isNullOrEmpty()) {
                    studentNameEt.error = "Please enter student name"
                }else {
                    val studentName = studentNameEt.text.toString()
                    var key = studentDataArray[positon].id
                    var updatedata = StudentData(id = key, name = studentName)
                    var hashData = updatedata.toMap()
                    myRef.child(key.toString()).updateChildren(hashData)
                    myAdp.notifyDataSetChanged()
                    dismiss()
                }
            }
            //---------------------- on add button click ----------------------
        }.show()
    }

    private fun deleteData(positon: Int) {
//        val bucket = supabaseClient?.storage?.from("assignment")
        myRef.child(studentDataArray[positon].id.toString()).removeValue()
        myAdp.notifyDataSetChanged()
    }
}