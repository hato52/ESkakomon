package com.hato.eskakomon

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.hato.eskakomon.model.Question
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_question.*

class QuestionActivity : AppCompatActivity() {

    private val NUM_OF_YEARS = 13   /* 問題を追加した際にはここの値も増やす */

    private lateinit var realm: Realm
    private var qIt: Int = 0                            // 出題リスト参照用イテレータ
    private lateinit var correctArray: IntArray         // 回答の正誤リスト(-1:回答なし, 0:不正解, 1:正解)
    private lateinit var locCheckArray: IntArray        // チェック状態をローカルで管理するリスト

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val param: IntArray = intent.getIntArrayExtra("Q_PARAM")
        qIt = intent.getIntExtra("Q_IT", 0)

        // Adの表示
        MobileAds.initialize(this) {}
        ad_view_question.loadAd(AdRequest.Builder().build())

        // 出題リストの作成
        val results: Array<Question?> = makeQuestionList(param)
        if (results.isEmpty()) {
            Toast.makeText(this, "条件に当てはまる問題がありません", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // ビューに問題データを反映(elseのなかで処理しないと画面遷移がうまくいかない？)
            bindQuestionToView(results, qIt)
        }

        // 選択ボタン ア
        button_a.setOnClickListener {
            showAnswerDialog("ア", results, qIt)
        }

        // 選択ボタン イ
        button_i.setOnClickListener {
            showAnswerDialog("イ", results, qIt)
        }

        // 選択ボタン ウ
        button_u.setOnClickListener {
            showAnswerDialog("ウ", results, qIt)
        }

        // 選択ボタン エ
        button_e.setOnClickListener {
            showAnswerDialog("エ", results, qIt)
        }

        // 問題バック
        button_back.setOnClickListener {
            if (qIt > 0) {
                bindQuestionToView(results, --qIt)
            }
        }

        // 問題ネクスト
        button_next.setOnClickListener {
            if (qIt < (results.size - 1)) {             // 通常は次の問題へ移動
                bindQuestionToView(results, ++qIt)
            }
        }

        // 中断
        button_intr.setOnClickListener {
            if (qIt < (results.size - 1)) {
                showResultDialog(1, results)
            } else {
                showResultDialog(0, results)
            }
        }

        // チェックボックス
        question_check.setOnClickListener {
            realm = Realm.getDefaultInstance()

            val q = realm.where(Question::class.java)
                .equalTo("id", results[qIt]?.id)
                .findFirst()

            try {
                realm.executeTransaction {
                    if (question_check.isChecked) {
                        q?.check = 1
                        locCheckArray[qIt] = 1
                    } else {
                        q?.check = 0
                        locCheckArray[qIt] = 0
                    }
                }
                realm.close()
            } catch (e: Exception) {
                realm.close()
            }
        }
    }

    // 出題リストの作成
    private fun makeQuestionList(param: IntArray): Array<Question?> {
        realm = Realm.getDefaultInstance()

        var query = realm.where(Question::class.java)

        // 出題年度
        if (param[3] == 0) {    // 全年度出題にチェックが入っていなければ実行
            val yearsList: MutableList<Int> = mutableListOf()
            for (i in 4..NUM_OF_YEARS + 2) {
                if (param[i] == 1) {
                    yearsList.add(i + 17)
                }
            }
            // 出題年度で絞り込み
            val yearsArray: Array<Int> = yearsList.toTypedArray()
            query = query.`in`("year", yearsArray)
        }

        // 出題範囲
        when (param[0]) {
            1 -> query = query.and().equalTo("miss", 1.toInt())      // 間違えた問題から
            2 -> query = query.and().equalTo("check", 1.toInt())     // チェックした問題から
        }

        // 検索結果をリストに変換
        var results = realm.copyFromRealm(query.findAll())

        // 出題方法
        when (param[2]) {
            0 -> results.shuffle() // ランダム出題
        }

        // 出題数
        when (param[1]) {
            0 -> results = results.take(5)
            1 -> results = results.take(10)
            2 -> results = results.take(15)
            3 -> results = results.take(20)
        }

        // 最終的な出題リストを配列に変換
        val resultsArray: Array<Question?> = results.toTypedArray()

        // 回答の正誤リストを初期化
        correctArray = IntArray(results.size) { -1 }
        // チェック状態管理用リストを初期化
        locCheckArray = IntArray(results.size) { -1 }
        for (i in 0..results.size-1) {
            locCheckArray[i] = resultsArray[i]!!.check
        }

        realm.close()
        return resultsArray
    }

    // 問題データをビューに反映
    private fun bindQuestionToView(results: Array<Question?>, it: Int) {
        text_q_num.text = "第" + (it + 1) + "問"          // 現在の問題番号
        text_sentence.text = results[it]?.sentence       // 問題文

        // 正解情報
        var ans: String = ""
        when (correctArray[it]) {
            1 -> ans = "◯️ (正解：${results[it]?.answer})"
            0 -> ans = "× (正解：${results[it]?.answer})"
        }
        text_res_answer.text = ans

        // 選択肢ア
        if (results[it]?.choice_a != null && results[it]?.choice_a!!.isNotEmpty()) {
            text_choice_a.text = "ア. " + results[it]?.choice_a
        } else {
            text_choice_a.text = ""
        }

        // 選択肢イ
        if (results[it]?.choice_i != null && results[it]?.choice_i!!.isNotEmpty()) {
            text_choice_i.text = "イ. " + results[it]?.choice_i
        } else {
            text_choice_i.text = ""
        }

        // 選択肢ウ
        if (results[it]?.choice_u != null && results[it]?.choice_u!!.isNotEmpty()) {
            text_choice_u.text = "ウ. " + results[it]?.choice_u
        } else {
            text_choice_u.text = ""
        }

        // 選択肢エ
        if (results[it]?.choice_e != null && results[it]?.choice_e!!.isNotEmpty()) {
            text_choice_e.text = "エ. " + results[it]?.choice_e
        } else {
            text_choice_e.text = ""
        }

        // 問題出典
        // 元号判定
        if (results[it]?.year != null && results[it]?.year!!.toInt() <= 31) {
            text_q_info.text = "(平成${results[it]?.year}年度 第${results[it]?.number}問)"
        } else {
            text_q_info.text = "(令和${(results[it]?.year!!.toInt()) - 30}年度 第${results[it]?.number}問)"
        }

        // チェック
        if (locCheckArray[it] == 1) {
            question_check.isChecked = true
        } else if (locCheckArray[it] == 0) {
            question_check.isChecked = false
        }

        // 画像表示
        if (results[it]?.image_path != null && results[it]?.image_path!!.isNotEmpty()) {
            val inputStream = assets.open(results[it]?.image_path + ".png")
            question_image.setImageBitmap(BitmapFactory.decodeStream(inputStream))
        } else {
            question_image.setImageDrawable(null)
        }

        // 問題バックボタン
        if (it == 0) {
            button_back.text = ""
        } else {
            button_back.text = "<<"
        }

        // 問題ネクストボタンと中断ボタン
        if (it == (results.size - 1)) {
            button_next.text = ""
            button_intr.text = "結果を見る"
        } else {
            button_next.text = ">>"
            button_intr.text = "中断"
        }
    }

    // 正解のダイアログを表示
    private fun showAnswerDialog(ans: String, results: Array<Question?>, it: Int) {

        realm = Realm.getDefaultInstance()

        val titleView = TextView(this)
        titleView.textSize = 24f
        titleView.setPadding(40, 40, 20, 20)

        val builder = AlertDialog.Builder(this)
        if (ans == results[it]?.answer) {   // 正解
            titleView.text = "正解！"
            titleView.setTextColor(Color.RED)
            if (correctArray[it] == -1) {       // 初回回答時のみ処理
                correctArray[it] = 1
                text_res_answer.text = "◯️ (正解：${results[it]?.answer})"
                // 現在の問題の間違いフラグをオフに(0)
                val q = realm.where(Question::class.java)
                    .equalTo("id", results[it]?.id)
                    .findFirst()
                try {
                    realm.executeTransaction {
                        q?.miss = 0
                    }
                    realm.close()
                } catch (e: Exception) {
                    realm.close()
                }
            }
        } else {                            // 不正解
            titleView.text = "不正解"
            titleView.setTextColor(Color.BLUE)
            if (correctArray[it] == -1) {       // 初回回答時のみ処理
                correctArray[it] = 0
                text_res_answer.text = "× (正解：${results[it]?.answer})"
                // 間違いフラグをオンに(1)
                val q = realm.where(Question::class.java)
                    .equalTo("id", results[it]?.id)
                    .findFirst()
                try {
                    realm.executeTransaction {
                        q?.miss = 1
                    }
                    realm.close()
                } catch (e: Exception) {
                    realm.close()
                }
            }
        }

        builder.setCustomTitle(titleView)
        builder.setMessage("答え：" + results[it]?.answer + "    (回答：" + ans + ")")

        if (it == (results.size - 1)) {
            builder.setPositiveButton("閉じる", null)
        } else {
            builder.setPositiveButton("次の問題") { dialog, which ->
                if (it < (results.size - 1)) {
                    bindQuestionToView(results, ++qIt)
                }
            }
            builder.setNegativeButton("閉じる", null)
        }

        builder.show()
    }

    // 結果画面移動用のダイアログを表示
    private fun showResultDialog(mode: Int, results: Array<Question?>) {
        // ダイアログ用のテキスト設定
        var title = ""
        var message = ""
        when (mode) {
            0 -> {
                title = "終了"
                message = "結果画面へ移動しますか？"
            }
            1 -> {
                title = "中断"
                message = "問題を途中で終了しますか？"
            }
        }

        // 結果画面へ渡すためのパラメータ生成
        val idArray = LongArray(results.size)
        val yearArray = IntArray(results.size)
        val numArray = IntArray(results.size)
        for (i in 0..(results.size - 1)) {
            idArray[i] = results[i]!!.id
            yearArray[i] = results[i]!!.year
            numArray[i] = results[i]!!.number
        }

        // ダイアログの表示
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("はい") { dialog, which ->
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("IS_CORRECT", correctArray)
                intent.putExtra("Q_ID", idArray)
                intent.putExtra("Q_YEAR", yearArray)
                intent.putExtra("Q_NUM", numArray)
                startActivity(intent)
            }
            .setNegativeButton("いいえ", null)
            .show()
    }
}
