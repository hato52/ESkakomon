package com.hato.eskakomon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.AppLaunchChecker
import com.hato.eskakomon.model.Config
import com.hato.eskakomon.model.Question
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.InputStream
import java.lang.Exception
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private var years: Array<Boolean> = Array(12, {false})      // チェックボックス管理用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初回起動確認(開発時は処理を行わない)
        if (!AppLaunchChecker.hasStartedFromLauncher(applicationContext)) {
            // ダイアログの表示
            val dialog = AlertDialog.Builder(this).setView(R.layout.load_dialog).create()
            dialog.setCancelable(false)     // バックボタンを無効化?
            dialog.show()

            // 各種データの初期化
            GlobalScope.launch(Dispatchers.Main) {
                async(Dispatchers.Default) { initData() }.await().let {
                    dialog.dismiss()
                }
            }
            // 初回以降この処理が行われないようにする
            AppLaunchChecker.onActivityCreate(this)
        }

        // チェックボックスの初期設定
        years[0] = true

        // FloatingActionButton
        start_question.setOnClickListener {
            // パラメータの取得
            val param = getSelectItem()

            // 現在の選択パラメータを保存
//            realm = Realm.getDefaultInstance()
//            try {
//                realm.executeTransaction {
//                    val config = realm.where(Config::class.java).equalTo("id", "config").findFirst()
//                    config?.range = param[0]
//                    config?.num = param[1]
//                    config?.random = param[2]
//                }
//            } catch (e: Exception) {}
//            realm.close()

            // 画面遷移
            if (param.lastIndexOf(1) > 2) { // 出題年度が選択されているかチェック
                val intent = Intent(this, QuestionActivity::class.java)
                intent.putExtra("Q_PARAM", param)
                startActivity(intent)
            } else {
                Toast.makeText(this, "出題年度が1つも選択されていません", Toast.LENGTH_SHORT).show()
            }
        }

        // ToolBar
        setSupportActionBar(toolbar)

        // NavigationDrawer
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigation_view.setNavigationItemSelectedListener {
            when(it.itemId) {
//                R.id.drawer_item1 -> {      // 記録
//
//                }
                R.id.drawer_item2 -> {      // 間違えた問題
                    val intent = Intent(this, MissedQuestionActivity::class.java)
                    startActivity(intent)
                }
                R.id.drawer_item3 -> {      // チェックした問題
                    val intent = Intent(this, CheckedQuestionActivity::class.java)
                    startActivity(intent)
                }
                R.id.drawer_item4 -> {      // ライセンス
                    val intent = Intent(this, OssLicensesMenuActivity::class.java)
                    intent.putExtra("title", "ライセンス")
                    startActivity(intent)
                }
            }

            drawer_layout.closeDrawer(navigation_view)  // ドロワーを閉じる
            true
        }
    }

    // 出題年度チェックボックスのクリック処理(xmlから参照)
    fun onYearClicked(view: View) {
        if (view is CheckBox) {
            var it = 0
            val checked: Boolean = view.isChecked
            when (view.id) {
                R.id.check_all  -> it = 0
                R.id.check_31   -> it = 11
                R.id.check_30   -> it = 10
                R.id.check_29   -> it = 9
                R.id.check_28   -> it = 8
                R.id.check_27   -> it = 7
                R.id.check_26   -> it = 6
                R.id.check_25   -> it = 5
                R.id.check_24   -> it = 4
                R.id.check_23   -> it = 3
                R.id.check_22   -> it = 2
                R.id.check_21   -> it = 1
            }
            years[it] = checked
        }
    }

    // 選択されたアイテムを渡す
    private fun getSelectItem(): IntArray {
        val selected = IntArray(15, {-1})
        selected[0] = spinner_range.selectedItemPosition    // 出題範囲
        selected[1] = spinner_num.selectedItemPosition      // 出題数
        selected[2] = spinner_random.selectedItemPosition   // ランダム設定

        var i = 3
        years.forEach {         // 出題年度
            if (it) {
                selected[i] = 1 // true
            } else {
                selected[i] = 0 // false
            }
            i++
        }
        return selected
    }

    // 非同期でCSVデータをDBに取り込む
    private fun initData() {
        realm = Realm.getDefaultInstance()

        // 設定データの初期化
//        try {
//            realm.executeTransaction {
//                val config = realm.createObject(Config::class.java, "config")
//                config.range = 0
//                config.num = 0
//                config.random = 0
//            }
//        } catch(e: Exception) {}

        // CSVファイルから問題データを読み込み
        val inputStream: InputStream = assets.open("es_kakomon.csv")
        inputStream.bufferedReader().use {
            val csv: String = it.readText()
            val questions = csv.split(";\r\n")

            questions.forEach { question ->
                val data: List<String> = question.split(",")
                try {
                    realm.executeTransaction {
                        val question = realm.createObject(Question::class.java, data[0])
                        question.year = Integer.parseInt(data[1])
                        question.number = Integer.parseInt(data[2])
                        question.sentence = data[3]
                        question.choice_a = data[4]
                        question.choice_i = data[5]
                        question.choice_u = data[6]
                        question.choice_e = data[7]
                        question.answer = data[8]
                        question.image_path = data[9]
                        question.miss = 0
                        question.check = 0
                    }
                } catch (e: Exception) {
                }
            }
        }

        realm.close()
    }
}
