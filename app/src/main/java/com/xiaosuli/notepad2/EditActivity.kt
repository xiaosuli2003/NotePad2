package com.xiaosuli.notepad2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.note_edit.*
import kotlin.concurrent.thread

class EditActivity : AppCompatActivity() {

    companion object {

        const val FIELD_ID = "id"
        const val FIELD_TITLE = "title"
        const val FIELD_CONTENT = "content"

        fun actionStart(context: Context, id: Int, title: String, content: String) {
            val intent = Intent(context, EditActivity::class.java).apply {
                putExtra(FIELD_ID, id)
                putExtra(FIELD_TITLE, title)
                putExtra(FIELD_CONTENT, content)
            }
            context.startActivity(intent)
        }
    }

    private var noteId = 0
    private var oldTitle = ""
    private var oldContent = ""
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_edit)
        // 修改状态栏字体颜色，用AndroidX官方兼容API
        val wic = ViewCompat.getWindowInsetsController(window.decorView);
        // true表示Light Mode，状态栏字体呈黑色，反之呈白色
        wic?.isAppearanceLightStatusBars = application.resources.configuration.uiMode == 0x11
        noteId = intent.getIntExtra(FIELD_ID, 0)
        oldTitle = intent.getStringExtra(FIELD_TITLE) ?: ""
        oldContent = intent.getStringExtra(FIELD_CONTENT) ?: ""
        setSupportActionBar(toolbar)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editTitle.setText(oldTitle)
        editContent.setText(oldContent)
        noteViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(NoteViewModel::class.java)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        queryAndUpdateNote()
    }

    private fun queryAndUpdateNote() {
        val newContent: String = editContent.getText().toString().trim { it <= ' ' }
        val newTitle: String = editTitle.getText().toString().trim { it <= ' ' }
        thread {
            val noteLists = noteViewModel.queryOneNoteByIdAndContent(noteId, newContent, newTitle)
            runOnUiThread {
                if (noteLists.isEmpty()) {
                    updateNote()
                }
            }
        }
    }

    private fun updateNote() {
        if (noteId != 0) {
            val newContent: String = editContent.getText().toString().trim { it <= ' ' }
            val newTitle: String = editTitle.getText().toString().trim { it <= ' ' }
            if ("" == newContent && "" == newTitle) {
                noteViewModel.deleteNote(noteId)
                finish()
                return
            }
            if (newContent != oldContent || oldTitle != newTitle) {
                noteViewModel.updateNote(noteId, newTitle, newContent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                noteViewModel.deleteNote(noteId)
                editTitle.setText("")
                editContent.setText("")
                finish()
            }
            R.id.clear -> {
                editContent.setText("")
            }
            R.id.save -> {
                queryAndUpdateNote()
                // 关闭软键盘,并让两个输入框失去焦点
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(
                    editContent.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                editContent.clearFocus()
                editTitle.clearFocus()
            }
            android.R.id.home -> {
                queryAndUpdateNote()
                finish()
            }
        }
        return true
    }
}