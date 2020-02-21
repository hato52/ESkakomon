package com.hato.eskakomon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hato.eskakomon.model.Question
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_checked_question.*
import kotlinx.android.synthetic.main.activity_missed_question.*

class CheckedQuestionActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checked_question)

        // チェックした問題を取得
        realm = Realm.getDefaultInstance()
        val results = realm.where(Question::class.java)
            .equalTo("check", 1.toInt())
            .findAll()

        if (results.size != 0) {    // 問題が存在するかどうかの表示切り替え
            check_is_exist.visibility = View.INVISIBLE
        }

        val yearArray = IntArray(results.size)
        val numArray = IntArray(results.size)
        for (i in 0..(results.size - 1)) {
            yearArray[i] = results[i]!!.year
            numArray[i] = results[i]!!.number
        }

        // RecyclerView
        val adapter = SelectedQuestionListAdapter(yearArray, numArray, 1)
        val recyclerView: RecyclerView = checked_list
        recyclerView.layoutManager = LinearLayoutManager(this)  // LayoutManagerをセット
        recyclerView.adapter = adapter

        // リストのクリックインターフェースの実装
        adapter.setOnItemClickListener(object:SelectedQuestionListAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                // 問題表示画面に移動
                val intent = Intent(this@CheckedQuestionActivity, QuestionActivity::class.java)
                val param: IntArray = arrayOf(2,4,1, 1,0,0,0,0,0,0,0,0,0,0,0).toIntArray()
                intent.putExtra("Q_PARAM", param)   // チェックした問題　全問　番号順
                intent.putExtra("Q_IT", position)
                startActivity(intent)
            }
        })

        realm.close()
    }
}
