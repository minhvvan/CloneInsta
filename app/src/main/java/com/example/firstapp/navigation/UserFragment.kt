package com.example.firstapp.navigation

import android.content.Intent
import android.graphics.PorterDuff
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firstapp.LoginActivity
import com.example.firstapp.MainActivity
import com.example.firstapp.R
import com.example.firstapp.navigation.model.ContentDTO
import com.example.firstapp.navigation.model.FollowDTO
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

            accountBtnFollowSignout?.setOnClickListener {
                requestFollow()
            }
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
        getFollowerAndFollowing()
        return fragmentView
    }
    fun requestFollow(){
        //save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if(followDTO.follwings.containsKey(uid)){
                //remove following
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers.remove(uid)
            }else{
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        //save data to third person
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transition ->
            var followDTO = transition.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transition.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }else{
                if(followDTO!!.followers.containsKey(currentUserUid!!)){
                    followDTO!!.followerCount = followDTO!!.followerCount - 1
                    followDTO!!.followers.remove(currentUserUid!!)
                }else{
                    followDTO!!.followerCount = followDTO!!.followerCount + 1
                    followDTO!!.followers[currentUserUid!!] = true
                }

                transition.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
        }
    }
    fun getFollowerAndFollowing(){
        var accountTvFollowingCount = fragmentView?.findViewById<TextView>(R.id.account_tv_following_count)
        var accountTvFollowerCount = fragmentView?.findViewById<TextView>(R.id.account_tv_follower_count)
        var accountBtnFollowSingout = fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener{ value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
            if(value == null){
                return@addSnapshotListener
            }
            var followDTO = value.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                accountTvFollowingCount?.text = followDTO?.followingCount.toString()
            }
            if(followDTO?.followerCount != null){
                accountTvFollowerCount?.text = followDTO?.followerCount.toString()
                if(followDTO?.followers.containsKey(currentUserUid!!)){
                    accountBtnFollowSingout?.text = getString(R.string.follow_cancel)
                    accountBtnFollowSingout?.background?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ContextCompat.getColor(activity!!, R.color.colorLightGray), BlendModeCompat.MODULATE)
                    accountBtnFollowSingout?.invalidate()
                }else{
                    if(uid != currentUserUid){
                        accountBtnFollowSingout?.text = getString(R.string.follow)
                        accountBtnFollowSingout?.background?.colorFilter = null
                    }
                }
            }
        }
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
