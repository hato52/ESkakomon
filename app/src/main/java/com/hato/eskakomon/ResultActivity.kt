package com.hato.eskakomon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.activity_result.*
import kotlin.math.round

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // インテントのデータ受け取り
        val correctArray = intent.getIntArrayExtra("IS_CORRECT")
        val idArray = intent.getLongArrayExtra("Q_ID")
        val yearArray = intent.getIntArrayExtra("Q_YEAR")
        val numArray = intent.getIntArrayExtra("Q_NUM")

        // Adの表示
        MobileAds.initialize(this) {}
        ad_view_result.loadAd(AdRequest.Builder().build())

        // ビューの設定
        val correctNum = correctArray.count{ it == 1 }
        text_res_correct_rate.text = "正解率：${ round((correctNum.toDouble() / correctArray.size.toDouble()) * 100).toInt() }%"
        text_res_correct_num.text = "(${correctNum}問 / ${correctArray.size}問中)"

        // RecyclerView
        val adapter = ResultListAdapter(yearArray!!, numArray!!, correctArray!!)
        val recyclerView: RecyclerView = results_list
        recyclerView.layoutManager = LinearLayoutManager(this)  // LayoutManagerをセット
        recyclerView.adapter = adapter                                  // RecyclerViewにAdapterをセット

        // リストのクリックインターフェースの実装
        adapter.setOnItemClickListener(object:ResultListAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                // 問題表示画面に移動
                val intent = Intent(this@ResultActivity, QuestionViewActivity::class.java)
                intent.putExtra("Q_ID", idArray[position])
                intent.putExtra("POS", position)
                startActivity(intent)
            }
        })

        button_res_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
