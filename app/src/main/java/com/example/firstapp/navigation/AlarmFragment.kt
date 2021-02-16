package com.example.firstapp.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firstapp.R
import com.example.firstapp.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class AlarmFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)
        var alarmRecyclerView = view.findViewById<RecyclerView>(R.id.alarmFragment_recyclerview)

        alarmRecyclerView.adapter = AlarmRecyclerviewAdapter()
        alarmRecyclerView.layoutManager = LinearLayoutManager(activity)

        return view
    }
    inner class AlarmRecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()
        init{
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener{ value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                alarmDTOList.clear()
                if(value == null) return@addSnapshotListener

                for(snapshot in value.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            var commentViewItemTextProfile = view.findViewById<TextView>(R.id.commentViewItem_textView_profile)
            var commentViewItemTextcomment= view.findViewById<TextView>(R.id.commentViewItem_textView_comment)
            var commentViewItemImageProfile = view.findViewById<ImageView>(R.id.commentViewItem_imageView_profile)

            //profile image get
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val url = task.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(commentViewItemImageProfile)
                }
            }

            when(alarmDTOList[position].kind){
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    commentViewItemTextProfile.text = str_0
                }
                1 -> {
                    val str_1 = alarmDTOList[position].userId + getString(R.string.alarm_comment)
                    commentViewItemTextProfile.text = str_1
                }
                2 -> {
                    val str_2 = alarmDTOList[position].userId + " " +  getString(R.string.alarm_follow)
                    commentViewItemTextProfile.text = str_2
                }
            }
            commentViewItemTextcomment.visibility = View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

    }
}
