package com.pardeep.rtdbwithsbwithbundlepassing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAdp(var studentDataArray: ArrayList<StudentData>,
    var recyclerInterface: RecyclerInterface,
    var context: Context) : RecyclerView.Adapter<MyAdp.ViewHolder>() {
    class ViewHolder(var view : View) : RecyclerView.ViewHolder(view) {
        var cardView : CardView = view.findViewById(R.id.recyclerViewCardView)
        var studentName : TextView = view.findViewById(R.id.studentNameTv)
        var studentImage : ImageView = view.findViewById(R.id.imageView)
        var updateBtn : Button = view.findViewById(R.id.UpdateBtn)
        var deleteBtn : Button = view.findViewById(R.id.DeleteBtn)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_recycler_view,parent,false))   }

    override fun getItemCount(): Int {
        return studentDataArray.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.studentName.setText(studentDataArray[position].name)
        Glide.with(context)
            .load(studentDataArray[position].image)
            .into(holder.studentImage)

        holder.cardView.setOnClickListener {
            recyclerInterface.onItemClick(position)
        }
        holder.deleteBtn.setOnClickListener {
            recyclerInterface.operationType(position , "delete")
        }
        holder.updateBtn.setOnClickListener {
            recyclerInterface.operationType(position,"update")
        }
    }

}
