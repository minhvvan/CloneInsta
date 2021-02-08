package com.example.firstapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager: CallbackManager? =null


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //로그인 버튼
        var signInButton = findViewById<Button>(R.id.email_login_button)
        signInButton.setOnClickListener {
            signInAndSingUp()
        }

        //구글 로그인 버튼
        var googleSignInButton = findViewById<Button>(R.id.google_signIn_button)
        googleSignInButton.setOnClickListener {
            googleLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        //페이스북 로그인 버튼
        callbackManager = CallbackManager.Factory.create()
        var facebookSignInButton = findViewById<LoginButton>(R.id.facebook_signIn_button)
        auth = Firebase.auth

        callbackManager = CallbackManager.Factory.create()

        facebookSignInButton.setReadPermissions("email", "public_profile")
        facebookSignInButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("TAG", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("TAG", "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d("TAG", "facebook:onError", error)
                // ...
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth?.currentUser
//        moveMainPage(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if(result!!.isSuccess){
                var account = result.signInAccount
                Log.d("account", account.toString())
                firebaseAuthWithGoogle(account)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //login success
                    moveMainPage(task.result?.user)
                }else{
                    //login error
                    Toast.makeText(this, "signInEmail Error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }


    fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAG", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success")
                        val user = auth?.currentUser
                        moveMainPage(task.result?.user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun signInAndSingUp(){
        val email:String = findViewById<EditText>(R.id.email_edit_text).text.toString()
        val password:String = findViewById<EditText>(R.id.password_edit_text).text.toString()
        Log.d("email", email)
        Log.d("password", password)

        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //Create a user account
                    moveMainPage(task.result?.user)
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
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                //login success
                moveMainPage(task.result?.user)
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