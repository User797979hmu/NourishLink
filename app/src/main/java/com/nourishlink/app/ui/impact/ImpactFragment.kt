package com.nourishlink.app.ui.impact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.data.model.LeaderboardEntryDto
import com.nourishlink.app.databinding.FragmentImpactBinding
import com.nourishlink.app.databinding.ItemLeaderboardBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class ImpactFragment : Fragment() {

    private var _binding: FragmentImpactBinding? = null
    private val binding get() = _binding!!
    private val app by lazy { NourishLinkApp.from(requireActivity().application) }
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImpactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LeaderboardAdapter()
        binding.recyclerLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLeaderboard.adapter = adapter

        updateUserImpactUI()
        fetchLeaderboard()
    }

    private fun updateUserImpactUI() {
        val user = app.sessionManager.currentUser
        binding.textImpactPoints.text = "${user?.impactPoints ?: 0} pts"
        binding.textMealsCount.text = "${user?.mealsRedistributed ?: 0}"
        binding.textKgSaved.text = "${user?.kgSaved ?: 0} kg"

        binding.chipGroupBadges.removeAllViews()
        val badges = user?.badges?.ifEmpty { listOf("New Contributor") } ?: listOf("New Contributor")
        badges.forEach { badgeName ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle).apply {
                text = "🏆 $badgeName"
                isCheckable = false
            }
            binding.chipGroupBadges.addView(chip)
        }
    }

    private fun fetchLeaderboard() {
        lifecycleScope.launch {
            val list = app.authRepository.getLeaderboard()
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    private var items: List<LeaderboardEntryDto> = emptyList()

    fun submitList(list: List<LeaderboardEntryDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textRank.text = "#${item.rank}"
        holder.binding.textDisplayName.text = item.displayName
        holder.binding.textImpactStats.text = "${item.mealsRedistributed} meals · ${item.kgSaved}kg saved"
        holder.binding.textPoints.text = "${item.impactPoints} pts"
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)
}
