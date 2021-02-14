package com.example.firstapp.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ContentView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.navigation.model.AlarmDTO
import com.example.firstapp.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment: Fragment(){
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        var detailViewFragment_rectclerview = view.findViewById<RecyclerView>(R.id.detailViewItem_recyclerView)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        detailViewFragment_rectclerview.adapter = DetailViewRecyclerViewAdapter()
        detailViewFragment_rectclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if(querySnapshot == null){
                    return@addSnapshotListener
                }
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView
            var detailViewItem_profile_textView = viewHolder.findViewById<TextView>(R.id.detailViewItem_profile_textView)
            var detailViewItem_profile_image = viewHolder.findViewById<ImageView>(R.id.detailViewItem_profile_image)
            var detailViewItem_imageView_content = viewHolder.findViewById<ImageView>(R.id.detailViewItem_imageView_content)
            var detailViewItem_explain_textView = viewHolder.findViewById<TextView>(R.id.detailViewItem_explain_textView)
            var detailViewItem_favoriteCounter_textView = viewHolder.findViewById<TextView>(R.id.detailViewItem_favoriteCounter_textView)
            var detailViewItem_favorite_imageView = viewHolder.findViewById<ImageView>(R.id.detailViewItem_favorite_imageView)
            var detailViewItem_comment_imageView = viewHolder.findViewById<ImageView>(R.id.detailViewItem_comment_imageView)

            //favorite button click event
            detailViewItem_favorite_imageView.setOnClickListener {
                favoriteEvent(position)
            }
            if(contentDTOs!![position].favorite.containsKey(uid)){
                //like status
                detailViewItem_favorite_imageView.setImageResource(R.drawable.ic_favorite)
            }else{
                //not yet like
                detailViewItem_favorite_imageView.setImageResource(R.drawable.ic_favorite_border)
            }

            //UserId
            detailViewItem_profile_textView.text = contentDTOs!![position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(detailViewItem_imageView_content)

            //Explain
            detailViewItem_explain_textView.text = contentDTOs!![position].explain

            //likes
            detailViewItem_favoriteCounter_textView.text = "Likes "+ contentDTOs!![position].favoriteCount

            //ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(detailViewItem_profile_image)

            //comment
            detailViewItem_comment_imageView.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[position])
                intent.putExtra("destinationUid",contentDTOs[position].uid)
                startActivity(intent)
            }
            //go to other's profile
            detailViewItem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }
        }


        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorite.containsKey(uid)){
                    //clicked
                    contentDTO?.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO?.favorite.remove(uid)
                }else{
                    //unclicked
                    contentDTO?.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO?.favorite[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}
