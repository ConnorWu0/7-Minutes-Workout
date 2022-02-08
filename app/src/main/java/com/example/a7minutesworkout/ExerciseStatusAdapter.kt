package com.example.a7minutesworkout

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.a7minutesworkout.databinding.ItemExerciseStatusBinding

class ExerciseStatusAdapter(val items: ArrayList<ExerciseModel>):
RecyclerView.Adapter<ExerciseStatusAdapter.ViewHolder>(){

    class ViewHolder(binding: ItemExerciseStatusBinding):
    RecyclerView.ViewHolder(binding.root){
        val textViewItem = binding.textViewItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemExerciseStatusBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model : ExerciseModel = items[position]
        holder.textViewItem.text = model.getId().toString()

        when{
            model.getIsSelected() -> {
                holder.textViewItem.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.item_circular_thin_color_accent_border
                )
                holder.textViewItem.setTextColor(Color.parseColor("#212121"))
            }
            model.getIsCompleted() -> {
                holder.textViewItem.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.item_circular_color_accent_background
                )
                holder.textViewItem.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else -> {
                holder.textViewItem.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.item_circular_color_gray_background
                )
                holder.textViewItem.setTextColor(Color.parseColor("#212121"))
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}