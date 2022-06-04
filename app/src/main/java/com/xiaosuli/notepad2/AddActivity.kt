package com.xiaosuli.notepad2

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.note_edit.*
import kotlin.concurrent.thread
import kotlin.math.log

class AddActivity : AppCompatActivity() {

    private lateinit var noteViewModel:NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_edit)
        setSupportActionBar(toolbar)
        toolbar.title = ""
        // 修改状态栏字体颜色，用AndroidX官方兼容API
        val wic = ViewCompat.getWindowInsetsController(window.decorView);
        // true表示Light Mode，状态栏字体呈黑色，反之呈白色
        wic?.isAppearanceLightStatusBars = application.resources.configuration.uiMode == 0x11
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        noteViewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(application)).get(NoteViewModel::class.java)
        showSoftInputFromWindow(editContent)
    }

    //打开软键盘
    private fun showSoftInputFromWindow(editText: EditText) {
        editText.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        insertNote()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_toolbar, menu)
        // menu?.findItem(R.id.delete)?.setVisible(false)  // 隐藏菜单栏其中的某一项
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                editTitle.setText("")
                editContent.setText("")
                finish()
            }
            R.id.clear -> {
                editTitle.setText("")
                editContent.setText("")
            }
            R.id.save -> {
                insertNote()
                finish()
            }
            android.R.id.home -> {
                insertNote()
                finish()
            }
        }
        return true
    }

    private fun insertNote() {
        val title: String = editTitle.text.toString().trim { it <= ' ' }
        val content: String = editContent.text.toString().trim { it <= ' ' }
        if ("" != content || "" != title) {
            thread {
                noteViewModel.insertNote(title,content)
            }
        }
    }
}