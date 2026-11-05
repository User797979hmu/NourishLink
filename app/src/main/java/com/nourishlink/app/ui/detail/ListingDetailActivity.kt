package com.nourishlink.app.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.data.model.MessageDto
import com.nourishlink.app.databinding.ActivityListingDetailBinding
import com.nourishlink.app.databinding.ItemMessageBinding
import kotlinx.coroutines.launch

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingDetailBinding
    private val app by lazy { NourishLinkApp.from(application) }
    private lateinit var messageAdapter: MessageAdapter
    private var currentListingId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentListingId = intent.getIntExtra(EXTRA_LISTING_ID, -1)
        if (currentListingId == -1) { finish(); return }

        binding.buttonBack.setOnClickListener { finish() }

        messageAdapter = MessageAdapter()
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerMessages.adapter = messageAdapter

        loadListingDetails()
        loadLogisticsMessages()

        binding.buttonSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadListingDetails() {
        lifecycleScope.launch {
            val listing = app.listingRepository.getById(currentListingId)
            if (listing == null) { finish(); return@launch }

            with(binding) {
                textTitle.text = listing.title
                textCategory.text = listing.category.replace('_', ' ').replaceFirstChar { it.uppercase() }
                textDescription.text = listing.description ?: ""
                textServings.text = "${listing.quantityServings} servings"
                textCollectBy.text = listing.collectBy
                textAddress.text = listing.address ?: "Address not provided"

                val alreadyReserved = listing.status != "available"
                buttonReserve.isEnabled = !alreadyReserved
                buttonReserve.text = if (alreadyReserved) "Already reserved" else "Reserve now"

                buttonReserve.setOnClickListener {
                    buttonReserve.isEnabled = false
                    buttonReserve.text = "Reserving…"
                    lifecycleScope.launch {
                        val claim = app.claimRepository.reserve(listing)
                        if (claim != null) {
                            buttonReserve.text = "Reserved"
                            textOfflineNote.visibility = android.view.View.VISIBLE
                        } else {
                            // Someone else got there first (or nobody is signed in).
                            buttonReserve.text = "Already reserved"
                        }
                    }
                }
            }
        }
    }

    private fun loadLogisticsMessages() {
        lifecycleScope.launch {
            messageAdapter.submitList(app.messageRepository.getMessages(currentListingId))
        }
    }

    private fun sendMessage() {
        val text = binding.inputMessageText.text.toString().trim()
        if (text.isBlank()) return

        lifecycleScope.launch {
            if (app.messageRepository.send(currentListingId, text)) {
                binding.inputMessageText.setText("")
                loadLogisticsMessages()
            } else {
                Toast.makeText(this@ListingDetailActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
    }
}

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var items: List<MessageDto> = emptyList()

    fun submitList(list: List<MessageDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textSender.text = item.senderName
        holder.binding.textMessage.text = item.text
        holder.binding.textTime.text = item.createdAt.take(16).replace("T", " ")
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)
}
