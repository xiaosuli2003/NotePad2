package com.xiaosuli.notepad2

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_edit.*

class NoteAdapter(val context: Context, var noteList: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textTitle = itemView.findViewById<TextView>(R.id.text_title)
        var textContent = itemView.findViewById<TextView>(R.id.text_content)
        var textTime = itemView.findViewById<TextView>(R.id.text_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val note = noteList[position]
            val id = note.id.toInt()
            EditActivity.actionStart(context, id, note.title, note.content)
        }
        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            val note = noteList[position]
            AlertDialog.Builder(context).apply {
                setTitle("删除便签")
                setMessage("确认要删除所选的便签吗？")
                setCancelable(false)
                setNegativeButton("取消",null)
                setPositiveButton("确定") { _, _ ->
                    val id = note.id.toInt()
                    val repository = NoteRepository(context)
                    repository.deleteNote(id)
                }
                show()
            }
            true
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = noteList[position]
        /*if(holder.textTitle.text.isEmpty()){
            holder.textTitle.visibility = View.GONE
        }*/
        holder.textTitle.text = note.title
        holder.textContent.text = note.content
        holder.textTime.text = note.time
    }

    override fun getItemCount() = noteList.size
}