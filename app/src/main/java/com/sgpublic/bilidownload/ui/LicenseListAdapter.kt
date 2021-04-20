package com.sgpublic.bilidownload.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.sgpublic.bilidownload.R

class LicenseListAdapter(context: Context, private val resource: Int, objects: List<LicenseListItem>) : ArrayAdapter<LicenseListItem>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val view: View = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        view.findViewById<TextView>(R.id.item_license_title).text = item?.projectTitle
        view.findViewById<TextView>(R.id.item_license_author).text = item?.projectAuthor
        view.findViewById<TextView>(R.id.item_license_about).text = item?.projectAbout
        view.findViewById<View>(R.id.item_license_base).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(item?.projectUrl)
            context.startActivity(intent)
        }
        return view
    }
}