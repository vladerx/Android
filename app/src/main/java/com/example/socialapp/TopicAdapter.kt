package com.example.socialapp

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
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

class TopicAdapter(var topics : List<Topic>) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>()  {
    var holderList = ArrayList<TopicViewHolder>()

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val topicTitle : TextView = itemView.findViewById(R.id.topic_title)
        val topicDesciption : TextView = itemView.findViewById(R.id.topic_description)
        val topicImage : ImageView = itemView.findViewById(R.id.topic_image)
        val progBar : ProgressBar = itemView.findViewById(R.id.topic_progbar)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view :View = LayoutInflater.from(parent.context).inflate(R.layout.topic_item, parent, false)
        return TopicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return topics.size
    }



    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holderList.add(holder)
        holder.topicTitle.text = topic.topicTitle
        holder.topicDesciption.text = topic.topicDescription

        if (topic.topicUri != ""){
            Glide.with(holder.topicImage.context).load(topic.topicUri).listener(object :
                RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.w("TAGGY", e?.message, e)
                    return false
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

            }).fitCenter()
                .into(holder.topicImage)
        } else {
            holder.topicImage.setImageResource(R.drawable.blank_image)
        }

    }

}