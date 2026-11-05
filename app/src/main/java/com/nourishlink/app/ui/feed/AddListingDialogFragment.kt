package com.nourishlink.app.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.R
import com.nourishlink.app.data.local.ListingEntity
import com.nourishlink.app.databinding.DialogAddListingBinding
import com.nourishlink.app.util.IsoTime
import kotlinx.coroutines.launch

class AddListingDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogAddListingBinding? = null
    private val binding get() = _binding!!
    private val app by lazy { NourishLinkApp.from(requireActivity().application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = app.sessionManager.currentUser
        if (user != null) {
            binding.inputAddress.setText(user.address?.takeIf { it.isNotBlank() } ?: user.organisation ?: "Sandton, Johannesburg")
        }

        binding.buttonPostListing.setOnClickListener { postListing() }
    }

    private fun postListing() {
        val user = app.sessionManager.currentUser ?: return
        val title = binding.inputTitle.text.toString().trim()
        val servingsText = binding.inputServings.text.toString().trim()
        val hoursText = binding.inputHours.text.toString().trim()
        val address = binding.inputAddress.text.toString().trim()
        val description = binding.inputDescription.text.toString().trim()

        if (title.isEmpty() || servingsText.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide a title and quantity of servings", Toast.LENGTH_SHORT).show()
            return
        }

        val servings = servingsText.toIntOrNull() ?: 10
        val hoursAhead = hoursText.toIntOrNull() ?: 6

        val category = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipCategoryProduce -> "produce"
            R.id.chipCategoryCooked -> "cooked_meals"
            R.id.chipCategoryBakery -> "bakery"
            R.id.chipCategoryDairy -> "dairy"
            R.id.chipCategoryPackaged -> "packaged"
            else -> "produce"
        }

        val dietaryFlags = mutableListOf<String>()
        if (binding.chipHalal.isChecked) dietaryFlags.add("halal")
        if (binding.chipVegetarian.isChecked) dietaryFlags.add("vegetarian")
        if (binding.chipVegan.isChecked) dietaryFlags.add("vegan")
        if (binding.chipGlutenFree.isChecked) dietaryFlags.add("gluten_free")
        if (binding.chipNutFree.isChecked) dietaryFlags.add("nut_free")

        val now = IsoTime.now()
        val listing = ListingEntity(
            donorId = user.id,
            title = title,
            category = category,
            description = description.takeIf { it.isNotBlank() },
            quantityServings = servings,
            latitude = user.latitude,
            longitude = user.longitude,
            address = address.ifBlank { "Sandton, Johannesburg" },
            collectBy = IsoTime.hoursFromNow(hoursAhead),
            status = "available",
            vendorName = user.organisation?.takeIf { it.isNotBlank() } ?: user.name,
            dietaryFlags = dietaryFlags.joinToString(", "),
            distanceKm = 0.0,
            createdAt = now,
            updatedAt = now
        )

        lifecycleScope.launch {
            app.listingRepository.createListing(listing)
            app.authRepository.recordListingCreated(servings)
            Toast.makeText(requireContext(), "Surplus listing posted successfully!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddListingDialogFragment"
        fun newInstance() = AddListingDialogFragment()
    }
}
