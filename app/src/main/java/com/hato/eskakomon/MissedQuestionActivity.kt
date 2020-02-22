package com.hato.eskakomon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.hato.eskakomon.model.Question
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_missed_question.*
import kotlinx.android.synthetic.main.activity_question.*

class MissedQuestionActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missed_question)

        // Adの表示
        MobileAds.initialize(this) {}
        ad_view_miss.loadAd(AdRequest.Builder().build())

        // 間違えた問題を取得
        realm = Realm.getDefaultInstance()
        val results = realm.where(Question::class.java)
            .equalTo("miss", 1.toInt())
            .findAll()

        if (results.size != 0) {    // 問題が存在するかどうかの表示切り替え
            miss_is_exist.visibility = View.INVISIBLE
        }

        val yearArray = IntArray(results.size)
        val numArray = IntArray(results.size)
        for (i in 0..(results.size - 1)) {
            yearArray[i] = results[i]!!.year
            numArray[i] = results[i]!!.number
        }

        // RecyclerView
        val adapter = SelectedQuestionListAdapter(yearArray, numArray, 0)
        val recyclerView: RecyclerView = missed_list
        recyclerView.layoutManager = LinearLayoutManager(this)  // LayoutManagerをセット
        recyclerView.adapter = adapter

        // リストのクリックインターフェースの実装
        adapter.setOnItemClickListener(object:SelectedQuestionListAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                // 問題表示画面に移動
                val intent = Intent(this@MissedQuestionActivity, QuestionActivity::class.java)
                val param: IntArray = arrayOf(1,4,1, 1,0,0,0,0,0,0,0,0,0,0,0).toIntArray()
                intent.putExtra("Q_PARAM", param)   // 間違えた問題　全問　番号順
                intent.putExtra("Q_IT", position)
                startActivity(intent)
            }
        })

        realm.close()
    }
}
