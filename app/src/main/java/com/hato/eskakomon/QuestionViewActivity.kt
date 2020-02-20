package com.hato.eskakomon

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hato.eskakomon.model.Question
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.activity_question_view.*

class QuestionViewActivity : AppCompatActivity() {
    
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_view)

        val id = intent.getLongExtra("Q_ID", -1)
        val it = intent.getIntExtra("POS", -1)

        realm = Realm.getDefaultInstance()
        val result = realm.where(Question::class.java).equalTo("id", id).findFirst()
        bindQuestionToView(result, it)

        realm.close()
    }

    // 問題データをビューに反映
    private fun bindQuestionToView(result: Question?, it: Int) {

        if (result == null) return

        text_qview_q_num.text = "第" + (it + 1) + "問"          // 現在の問題番号
        text_qview_sentence.text = result.sentence       // 問題文

        // 正解情報
        text_qview_res_answer.text = "(正解：${result.answer})"

        // 選択肢ア
        if (result.choice_a.isNotEmpty()) {
            text_qview_choice_a.text = "ア. " + result.choice_a
        } else {
            text_qview_choice_a.text = ""
        }

        // 選択肢イ
        if (result.choice_i.isNotEmpty()) {
            text_qview_choice_i.text = "イ. " + result.choice_i
        } else {
            text_qview_choice_i.text = ""
        }

        // 選択肢ウ
        if (result.choice_u.isNotEmpty()) {
            text_qview_choice_u.text = "ウ. " + result.choice_u
        } else {
            text_qview_choice_u.text = ""
        }

        // 選択肢エ
        if (result.choice_e.isNotEmpty()) {
            text_qview_choice_e.text = "エ. " + result.choice_e
        } else {
            text_qview_choice_e.text = ""
        }

        // 問題出典
        text_qview_q_info.text = "(平成${result.year}年度 第${result.number}問)"

        // 画像表示
        if (result.image_path.isNotEmpty()) {
            val inputStream = assets.open(result.image_path + ".png")
            question_qview_image.setImageBitmap(BitmapFactory.decodeStream(inputStream))
        } else {
            question_qview_image.setImageDrawable(null)
        }
    }
}
