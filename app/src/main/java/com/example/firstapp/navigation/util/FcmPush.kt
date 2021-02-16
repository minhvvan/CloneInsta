package com.example.firstapp.navigation.util

import android.util.Log
import com.example.firstapp.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush{
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    val serverKey = "AAAAugm3DnE:APA91bFXDyKl_uU--GZOhAZeD0CtCW49TVLze26pfpD6k9IwsBmVIbb2BgrbIKn9gp0MhItutiMl7jqKupS_UNGOLbGhFP8QH3SFY1q19H8s_mrxXO0RRAuXSTE54eZqHbCguQpedlmn"
    var gson: Gson? = null
    var okHttpClient: OkHttpClient? = null
    companion object{
        var instance = FcmPush()
    }
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid: String, title: String, message: String){
        Log.d("콜","실행")
        FirebaseFirestore.getInstance().collection("pushTokens").document(destinationUid).get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                Log.d("콜","성공")
                var token = task.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                var request = Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object: Callback{
                    override fun onFailure(call: Call?, e: IOException?) {
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        Log.d("콜","보냄")
                        println(response?.body()?.string())
                    }

                })

            }
        }
    }
}