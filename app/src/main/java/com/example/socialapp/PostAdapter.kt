package com.example.socialapp

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class PostAdapter (var posts : List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>()  {
    var holderList = ArrayList<PostViewHolder>()

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val postTitle : TextView = itemView.findViewById(R.id.post_title)
        val postDesciption : TextView = itemView.findViewById(R.id.post_description)
        val postImage : ImageView = itemView.findViewById(R.id.post_img)
        val progBar : ProgressBar = itemView.findViewById(R.id.post_progbar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view :View = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holderList.add(holder)
        holder.postTitle.text = post.postTitle
        holder.postDesciption.text = post.postDescription
        if (post.postUri != ""){
            Glide.with(holder.postImage.context).load(post.postUri).listener(object :
                RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    TODO("Not yet implemented")
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progBar.visibility = View.INVISIBLE
                    return false
                }

            }).fitCenter().into(holder.postImage)
        } else {
            holder.postImage.setImageResource(R.drawable.blank_image)
        }
    }

}