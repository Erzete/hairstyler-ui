package com.dicoding.shavemax.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.dicoding.shavemax.data.remote.response.RecommendationsItem
import com.dicoding.shavemax.databinding.CardviewResultBinding

class ResultAdapter : ListAdapter<RecommendationsItem, ResultAdapter.MyViewHolder>(DIFF_CALLBACK) {

    inner class MyViewHolder(val binding: CardviewResultBinding) : ViewHolder(binding.root) {
        fun bind(item: RecommendationsItem) {
            binding.tvHaircutName.text = item.name
            binding.tvHaircutDesc.text = item.description
            Glide.with(binding.root)
                .load(item.photoUrl)
                .into(binding.ivResult)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            CardviewResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val recommendationsItem = getItem(position)
        holder.bind(recommendationsItem)
        holder.itemView.setOnClickListener {
            val toDetailFragment = ResultFragmentDirections.actionResultFragmentToDetailFragment(
                recommendationsItem.name ?: "Tes Name",
                recommendationsItem.description ?: "Tes Desc",
                recommendationsItem.photoUrl ?: "https://cdn.britannica.com/61/137461-050-BB6C5D80/Brad-Pitt-2008.jpg"
            )
            holder.itemView.findNavController().navigate(toDetailFragment)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecommendationsItem>() {
            override fun areItemsTheSame(
                oldItem: RecommendationsItem,
                newItem: RecommendationsItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: RecommendationsItem,
                newItem: RecommendationsItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}