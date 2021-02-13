package com.example.firstapp.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firstapp.R
import com.example.firstapp.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class GridFragment: Fragment(){
    var firestore: FirebaseFirestore? = null
    var fragmentView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid,container,false)
        firestore = FirebaseFirestore.getInstance()
        var gridFragmentRecyclerView = fragmentView?.findViewById<RecyclerView>(R.id.gridFragment_recyclerView)
        gridFragmentRecyclerView?.adapter = UserFragmentRecyclerViewAdapter()
        gridFragmentRecyclerView?.layoutManager = GridLayoutManager(activity, 3)

        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        init{
            firestore?.collection("images")?.addSnapshotListener{
                value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if(value == null){
                    return@addSnapshotListener
                }

                //get data
                for(snapshot in value.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}
