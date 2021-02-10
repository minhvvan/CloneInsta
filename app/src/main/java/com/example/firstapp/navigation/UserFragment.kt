package com.example.firstapp.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firstapp.LoginActivity
import com.example.firstapp.MainActivity
import com.example.firstapp.R
import com.example.firstapp.navigation.model.ContentDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class UserFragment: Fragment(){
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null
    var accountIvProfile: ImageView? = null
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        var accountBtnFollowSignout = fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)
        accountIvProfile = fragmentView?.findViewById<ImageView>(R.id.account_iv_profile)

        if(uid == currentUserUid){
            //My page
            accountBtnFollowSignout?.text = getString(R.string.signout)
            accountBtnFollowSignout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        }else{
            //other
            accountBtnFollowSignout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            var toolbarUsername = mainactivity.findViewById<TextView>(R.id.toolbar_user_name)
            var toolbarBtnBack = mainactivity.findViewById<ImageView>(R.id.toolbar_btn_back)
            var bottomNav = mainactivity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            var toolbarTitleImage = mainactivity.findViewById<ImageView>(R.id.toolbar_title_image)

            toolbarUsername?.text = arguments?.getString("userId")
            toolbarBtnBack?.setOnClickListener{
                bottomNav?.selectedItemId = R.id.action_home
            }
            toolbarTitleImage.visibility = View.GONE
            toolbarUsername.visibility = View.VISIBLE
            toolbarBtnBack.visibility = View.VISIBLE
        }

        accountIvProfile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        //RecyclerView setting
        var accountRecyclerView = fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)
        accountRecyclerView?.adapter = UserFragmentRecyclerViewAdapter()
        accountRecyclerView?.layoutManager = GridLayoutManager(activity, 3)

        getProfileImage()
        return fragmentView
    }

    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener{
            value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
            if(value == null){
                Log.i("image","ㄴ널널널")
                return@addSnapshotListener
            }
            if(value.data != null){
                var url = value?.data!!["image"]
                if (accountIvProfile != null) {
                    Log.i("url",url.toString())
                    Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(accountIvProfile!!)
                }

            }
        }
    }


    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var accountTvPostCount = fragmentView?.findViewById<TextView>(R.id.account_tv_post_count)
        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener{
                value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if(value == null){
                    return@addSnapshotListener
                }

                //get data
                for(snapshot in value.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                accountTvPostCount?.text = contentDTOs.size.toString()
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
