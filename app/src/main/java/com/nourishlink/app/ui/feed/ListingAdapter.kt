package com.nourishlink.app.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nourishlink.app.data.local.ListingEntity
import com.nourishlink.app.databinding.ItemListingBinding

class ListingAdapter(
    private val onClick: (ListingEntity) -> Unit
) : ListAdapter<ListingEntity, ListingAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemListingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listing = getItem(position)
        with(holder.binding) {
            textTitle.text = listing.title
            textVendor.text = listing.vendorName ?: "Donor Vendor"
            val categoryStr = listing.category.replace('_', ' ').replaceFirstChar { it.uppercase() }
            val dietaryStr = if (!listing.dietaryFlags.isNullOrBlank()) " · ${listing.dietaryFlags}" else ""
            textMeta.text = "$categoryStr · ${listing.quantityServings} servings$dietaryStr"

            val dist = listing.distanceKm ?: 0.0
            textDistance.text = if (dist > 0) "${String.format("%.1f", dist)} km" else "Near you"

            textCollectBy.text = "Collect by: ${listing.collectBy.take(16).replace("T", " ")}"
            textAddress.text = listing.address ?: "Johannesburg, SA"
            root.setOnClickListener { onClick(listing) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ListingEntity>() {
            override fun areItemsTheSame(oldItem: ListingEntity, newItem: ListingEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ListingEntity, newItem: ListingEntity) = oldItem == newItem
        }
    }
}
