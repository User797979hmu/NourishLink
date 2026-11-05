package com.nourishlink.app.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.data.model.NotificationDto
import com.nourishlink.app.databinding.FragmentNotificationsBinding
import com.nourishlink.app.databinding.ItemNotificationBinding
import com.nourishlink.app.ui.detail.ListingDetailActivity
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val app by lazy { NourishLinkApp.from(requireActivity().application) }
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter { notification ->
            if (notification.listingId != null && notification.listingId > 0) {
                startActivity(Intent(requireContext(), ListingDetailActivity::class.java).apply {
                    putExtra(ListingDetailActivity.EXTRA_LISTING_ID, notification.listingId)
                })
            }
        }

        binding.recyclerNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotifications.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { fetchNotifications() }
        fetchNotifications()
    }

    private fun fetchNotifications() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val list: List<NotificationDto> = try {
                app.notificationRepository.getMine()
            } catch (e: Exception) {
                emptyList()
            }
            binding.swipeRefresh.isRefreshing = false
            adapter.submitList(list)
            binding.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class NotificationAdapter(
    private val onClick: (NotificationDto) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var items: List<NotificationDto> = emptyList()

    fun submitList(list: List<NotificationDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textNotificationTitle.text = item.title
        holder.binding.textNotificationBody.text = item.body
        holder.binding.textNotificationTime.text = item.createdAt.take(16).replace("T", " ")
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
}
