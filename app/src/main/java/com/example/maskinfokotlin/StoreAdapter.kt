package com.example.maskinfokotlin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.maskinfokotlin.databinding.ItemStoreBinding
import com.example.maskinfokotlin.model.Store
import java.util.*

// 아이템 뷰 정보를 가지고 있는 클래스
class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var binding = ItemStoreBinding.bind(itemView)
}

class StoreAdapter : RecyclerView.Adapter<StoreViewHolder>() {
    private var mItems: List<Store> = ArrayList()

    fun updateItems(items: List<Store>) {
        mItems = items
        notifyDataSetChanged() // UI 갱신
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        //holder.binding.store = mItems[position]
        val store: Store = mItems[position]
        holder.binding.nameTextView.text = store.name
        holder.binding.addrTextView.text = store.addr
        holder.binding.distanceTextView.text = "1km"

        var remainStat = "충분"
        var count = "10개 이상"
        var color = Color.GREEN
        when (store.remain_stat) {
            "plenty" -> {
                remainStat = "충분"
                count = "100개 이상"
                color = Color.GREEN
            }
            "some" -> {
                remainStat = "여유"
                count = "30개 이상"
                color = Color.YELLOW
            }
            "few" -> {
                remainStat = "매진 임박"
                count = "2개 이상"
                color = Color.RED
            }
            "empty" -> {
                remainStat = "재고 없음"
                count = "1개 이하"
                color = Color.GRAY
            }
            else -> {
            }
        }
        holder.binding.remainTextView.text = remainStat
        holder.binding.countTextView.text = count
        holder.binding.remainTextView.setTextColor(color)
        holder.binding.countTextView.setTextColor(color)
    }

    override fun getItemCount() = mItems.size
}