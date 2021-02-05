package com.example.firstapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        //로그인 버튼
        var signInButton = findViewById<Button>(R.id.email_login_button)

        signInButton.setOnClickListener {
            signInAndSingUp()
        }
    }

    fun signInAndSingUp(){
        val email:String = findViewById<EditText>(R.id.email_edit_text).text.toString()
        val password:String = findViewById<EditText>(R.id.password_edit_text).text.toString()
        Log.d("email", email)
        Log.d("password", password)

        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener {
            task ->
                if(task.isSuccessful){
                    //Create a user account
                    moveMainPage(task.result!!.user)
                }else if(task.exception?.message.isNullOrEmpty()){
                    //login error
//                    Toast.makeText(this, "signInAndSingUp Error", Toast.LENGTH_SHORT).show()
                }else{
                    //login
                    signInEmail()
                }
        }
    }


    fun signInEmail(){
        var email:String = findViewById<EditText>(R.id.email_edit_text).text.toString()
        var password:String = findViewById<EditText>(R.id.password_edit_text).text.toString()
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                //login success
                moveMainPage(task.result!!.user)
            }else{
                //login error
                Toast.makeText(this, "signInEmail Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}