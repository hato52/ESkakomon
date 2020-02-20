package com.hato.eskakomon

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.result_item_view.view.*

class ResultListAdapter
    (private val year : IntArray, private val num: IntArray, private val correct: IntArray) :
    RecyclerView.Adapter<ResultListAdapter.ResultViewHolder>() {

    // リスナー格納変数
    lateinit var listener: onItemClickListener

    // ViewHolderが作成された際に実行
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ResultViewHolder {
        val myView = LayoutInflater.from(parent.context).inflate(R.layout.result_item_view, parent, false)
        return ResultViewHolder(myView)
    }

    // ViewHolderがリストに設定された際に実行
    // テキストビューに渡されたテキストを設定
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.numText.text = (position + 1).toString()
        holder.yearText.text = "平成${year[position]}年 第${num[position]}問"
        when (correct[position]) {
            -1 -> holder.correctText.text = "-"
             0 -> {
                 holder.correctText.text = "×"
                 holder.correctText.setTextColor(Color.BLUE)
             }
             1 -> {
                 holder.correctText.text = "◯"
                 holder.correctText.setTextColor(Color.RED)
             }
        }

        holder.linearLayout.setOnClickListener { view ->
            listener.onItemClick(view, position)
        }
    }

    // データセットの数を返す
    override fun getItemCount() = year.size

    // クリックイベント用のインタフェースの作成
    interface onItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    // リスナー
    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    // RecyclerViewに渡すためのViewHolderを定義
    class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linearLayout: LinearLayout = view.list_linear_layout
        val numText: TextView = view.text_list_no
        val yearText: TextView = view.text_list_year
        val correctText: TextView = view.text_list_r_or_w
    }
}