package com.nourishlink.app.ui.claims

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.databinding.FragmentClaimsBinding

/**
 * "My claims" — everything the signed-in user has reserved. Each claim shows a status chip.
 * Data comes straight from the on-device database, so it renders instantly.
 */
class ClaimsFragment : Fragment() {

    private var _binding: FragmentClaimsBinding? = null
    private val binding get() = _binding!!
    private val app by lazy { NourishLinkApp.from(requireActivity().application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClaimsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ClaimAdapter()
        binding.recyclerClaims.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerClaims.adapter = adapter

        app.claimRepository.observeMine().observe(viewLifecycleOwner) { claims ->
            adapter.submitList(claims)
            binding.textEmpty.visibility = if (claims.isEmpty()) View.VISIBLE else View.GONE
        }

        // Everything is stored on this device, so there's never an "offline" state to warn about.
        binding.bannerOffline.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
