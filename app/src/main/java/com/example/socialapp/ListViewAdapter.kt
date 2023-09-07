package com.example.socialapp

import android.app.Activity
import android.graphics.Movie
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes


class ListViewAdapter(val nicknames : List<String> = listOf()) : BaseAdapter() {
    override fun getCount(): Int {
        return nicknames.size
    }

    override fun getItem(position: Int): Any {
        return nicknames[position]
    }

    override fun getItemId(position : Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.admincp_item, parent, false)
        val nickname: TextView = view.findViewById(R.id.txtviw_cp_item)
        nickname.text = nicknames[position]
        return view
    }


}