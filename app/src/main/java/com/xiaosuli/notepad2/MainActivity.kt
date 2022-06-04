package com.xiaosuli.notepad2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: NoteAdapter
    private lateinit var mainViewModel: NoteViewModel
    var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.title = ""
        // 修改状态栏字体颜色，用AndroidX官方兼容API
        val wic = ViewCompat.getWindowInsetsController(window.decorView);
        // true表示Light Mode，状态栏字体呈黑色，反之呈白色
        wic?.isAppearanceLightStatusBars = application.resources.configuration.uiMode == 0x11
        addNote.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }
        mainViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(NoteViewModel::class.java)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        val noteLists = ArrayList<Note>()
        adapter = NoteAdapter(this, noteLists)
        recyclerView.adapter = adapter
        mainViewModel.queryAllNote().observe(this) {
            adapter.noteList = it
            adapter.notifyDataSetChanged()

        }
        search.setOnClickListener {
            searchNote(it)
        }
        swipeRefresh.setColorSchemeResources(R.color.yellow)
        swipeRefresh.setOnRefreshListener {
            clearFocus()
            refreshNote(150)
        }
        searchEdit.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            //如果actionId是搜索的id，则进行下一步的操作
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchNote(searchEdit)
            }
            true
        })
        clear.isVisible = false
        clear.setOnClickListener {
            clearFocus()
        }
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                clear.isVisible = !"".contentEquals(editable)
            }
        })
    }

    private fun searchNote(view: View) {
        val keyword = searchEdit.text.toString().trim()
        if (keyword != "") {
            thread {
                val notes = mainViewModel.queryNoteByKeyword(keyword)
                runOnUiThread {
                    if (notes.isEmpty()) {
                        Snackbar.make(view, "未查询到便签", Snackbar.LENGTH_SHORT).show()
                    } else {
                        adapter.noteList = notes
                        adapter.notifyDataSetChanged()
                    }
                }

            }
        } else {
            clearFocus()
        }
    }

    private fun refreshNote(min: Long) {
        thread {
            Thread.sleep(min)
            val notes = mainViewModel.queryAllNote2()
            runOnUiThread {
                adapter.noteList = notes
                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clearFocus()
        refreshNote(0)
    }

    private fun clearFocus() {
        // 关闭软键盘,并让输入框失去焦点
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEdit.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        searchEdit.setText("")
        searchEdit.clearFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_list_mode -> {
                if (flag) {
                    val layoutManager = LinearLayoutManager(this)
                    recyclerView.layoutManager = layoutManager
                    flag = false
                } else {
                    val layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    recyclerView.layoutManager = layoutManager
                    flag = true
                }
            }
        }
        return true
    }
}