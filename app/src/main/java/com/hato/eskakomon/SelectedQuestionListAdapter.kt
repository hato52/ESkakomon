package com.hato.eskakomon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.result_item_view.view.*
import kotlinx.android.synthetic.main.selected_item_view.view.*

class SelectedQuestionListAdapter(private val year: IntArray, private val num: IntArray, private val mode: Int) :
    RecyclerView.Adapter<SelectedQuestionListAdapter.SelectedQuestionViewHolder>() {

    // リスナー格納変数
    lateinit var listener: onItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : SelectedQuestionViewHolder {
        val myView = LayoutInflater.from(parent.context).inflate(R.layout.selected_item_view, parent, false)
        return SelectedQuestionViewHolder(myView)
    }

    override fun onBindViewHolder(holder: SelectedQuestionViewHolder, position: Int) {

        // 元号判定
        if (year[position] <= 31) {
            holder.yearText.text = "平成${year[position]}年 第${num[position]}問"
        } else {
            holder.yearText.text = "令和${year[position] - 30}年 第${num[position]}問"
        }

        // アイコンの変更
        when (mode) {
            0 -> holder.iconImage.setImageResource(R.drawable.miss)   // 間違えた問題
            1 -> holder.iconImage.setImageResource(R.drawable.check)   // チェックした問題
        }

        holder.linearLayout.setOnClickListener { view ->
            listener.onItemClick(view, position)
        }
    }

    override fun getItemCount() = year.size

    interface onItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    class SelectedQuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linearLayout: LinearLayout = view.selected_linear_layout
        val yearText: TextView = view.selected_q_text
        val iconImage: ImageView = view.selected_q_icon
    }
}