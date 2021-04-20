package com.sgpublic.bilidownload.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.sgpublic.bilidownload.R

class ExploreFolderAdapter(context: Context, private val resource: Int, objects: List<ExploreFolderItem?>) : ArrayAdapter<ExploreFolderItem?>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val view: View = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        view.findViewById<TextView>(R.id.item_explore_title).text = item?.foldName
        view.findViewById<TextView>(R.id.item_explore_content).text = item?.foldDescription
        return view
    }
}