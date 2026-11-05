package com.nourishlink.app.ui.claims

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nourishlink.app.data.local.ClaimEntity
import com.nourishlink.app.databinding.ItemClaimBinding

class ClaimAdapter : ListAdapter<ClaimEntity, ClaimAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemClaimBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClaimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val claim = getItem(position)
        with(holder.binding) {
            textListingTitle.text = claim.listingTitle
            textMeta.text = "${claim.quantityServings} servings · collect by ${claim.collectBy}"
            when (claim.status) {
                "cancelled" -> {
                    chipStatus.text = "Cancelled"
                    chipStatus.setChipBackgroundColorResource(com.nourishlink.app.R.color.gold_200)
                }
                "collected" -> {
                    chipStatus.text = "Collected"
                    chipStatus.setChipBackgroundColorResource(com.nourishlink.app.R.color.green_200)
                }
                else -> {
                    chipStatus.text = "Reserved"
                    chipStatus.setChipBackgroundColorResource(com.nourishlink.app.R.color.green_200)
                }
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ClaimEntity>() {
            override fun areItemsTheSame(oldItem: ClaimEntity, newItem: ClaimEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ClaimEntity, newItem: ClaimEntity) = oldItem == newItem
        }
    }
}
