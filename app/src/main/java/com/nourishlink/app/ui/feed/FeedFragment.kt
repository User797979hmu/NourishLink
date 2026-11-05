package com.nourishlink.app.ui.feed

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.R
import com.nourishlink.app.data.local.ListingEntity
import com.nourishlink.app.databinding.FragmentFeedBinding
import com.nourishlink.app.ui.auth.LoginActivity
import com.nourishlink.app.ui.detail.ListingDetailActivity

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val app by lazy { NourishLinkApp.from(requireActivity().application) }
    private lateinit var adapter: ListingAdapter

    private var selectedCategory: String? = null
    private var selectedRadius: Int? = null
    private var searchQuery: String? = null
    private var isMapViewMode = false

    /** Every listing still available, as last emitted by the database. Filters are applied on top of this. */
    private var allListings: List<ListingEntity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ListingAdapter { listing ->
            startActivity(Intent(requireContext(), ListingDetailActivity::class.java).apply {
                putExtra(ListingDetailActivity.EXTRA_LISTING_ID, listing.id)
            })
        }
        binding.recyclerFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFeed.adapter = adapter

        app.listingRepository.observeAvailable().observe(viewLifecycleOwner) { listings ->
            allListings = listings
            render()
        }

        setupFilters()

        binding.swipeRefresh.setOnRefreshListener { refresh() }

        binding.buttonLogoutTop.setOnClickListener {
            app.authRepository.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }

        binding.buttonToggleView.setOnClickListener {
            isMapViewMode = !isMapViewMode
            binding.buttonToggleView.text = if (isMapViewMode) "List View 📋" else "Map View 🗺️"
            refresh()
        }

        binding.buttonAddListing.setOnClickListener {
            AddListingDialogFragment.newInstance().show(childFragmentManager, AddListingDialogFragment.TAG)
        }
    }

    private fun setupFilters() {
        binding.inputSearchVendor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.takeIf { it.isNotBlank() }
            }
            override fun afterTextChanged(s: Editable?) {
                refresh()
            }
        })

        binding.inputSearchVendor.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                refresh()
                true
            } else false
        }

        binding.chipGroupRadius.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull()
            selectedRadius = when (id) {
                R.id.chipRadius2km -> 2
                R.id.chipRadius5km -> 5
                R.id.chipRadius10km -> 10
                R.id.chipRadius20km -> 20
                else -> null
            }
            refresh()
        }

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull()
            selectedCategory = when (id) {
                R.id.chipCategoryCooked -> "cooked_meals"
                R.id.chipCategoryProduce -> "produce"
                R.id.chipCategoryBakery -> "bakery"
                R.id.chipCategoryDairy -> "dairy"
                R.id.chipCategoryPackaged -> "packaged"
                else -> null
            }
            refresh()
        }
    }

    /** Re-applies the current category / radius / search filters to the latest listings. */
    private fun render() {
        val user = app.sessionManager.currentUser
        val visible = app.listingRepository.filterAndRank(
            listings = allListings,
            category = selectedCategory,
            maxDistanceKm = selectedRadius,
            query = searchQuery,
            lat = user?.latitude ?: -26.1076,
            lng = user?.longitude ?: 28.0567
        )
        adapter.submitList(visible)
        binding.textEmpty.visibility = if (visible.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun refresh() {
        render()
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
