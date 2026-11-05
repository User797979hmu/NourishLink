package com.nourishlink.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.R
import com.nourishlink.app.data.repository.AuthResult
import com.nourishlink.app.databinding.FragmentSettingsBinding
import com.nourishlink.app.ui.auth.LoginActivity
import com.nourishlink.app.util.LocaleHelper
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val app by lazy { NourishLinkApp.from(requireActivity().application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()

        when (LocaleHelper.getSavedLanguage(requireContext())) {
            LocaleHelper.ZULU -> binding.toggleLanguage.check(R.id.buttonLangZulu)
            LocaleHelper.XITSONGA -> binding.toggleLanguage.check(R.id.buttonLangXitsonga)
            else -> binding.toggleLanguage.check(R.id.buttonLangEnglish)
        }

        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val code = when (checkedId) {
                R.id.buttonLangZulu -> LocaleHelper.ZULU
                R.id.buttonLangXitsonga -> LocaleHelper.XITSONGA
                else -> LocaleHelper.ENGLISH
            }
            LocaleHelper.setLocale(requireContext(), code)
            lifecycleScope.launch { app.authRepository.updateProfile(language = code) }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { app.authRepository.updateProfile(notificationsEnabled = isChecked) }
        }

        binding.switchLeaderboardOptIn.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { app.authRepository.updateProfile(leaderboardOptIn = isChecked) }
        }

        binding.buttonSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        binding.buttonLogout.setOnClickListener {
            app.authRepository.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    private fun loadUserProfile() {
        val user = app.sessionManager.currentUser
        binding.textName.text = user?.name ?: ""
        binding.textRole.text = "${user?.role?.replaceFirstChar { it.uppercase() }} · ${user?.organisation ?: "Verified Account"}"
        binding.textEmail.text = user?.email ?: ""

        binding.inputEditName.setText(user?.name ?: "")
        binding.inputEditPhone.setText(user?.phone ?: "")
        binding.inputEditAddress.setText(user?.address ?: "")
        binding.inputEditProfilePicture.setText(user?.profilePicture ?: "")

        binding.switchNotifications.isChecked = user?.notificationsEnabled ?: true
        binding.switchLeaderboardOptIn.isChecked = user?.leaderboardOptIn ?: true
    }

    private fun saveProfileChanges() {
        val name = binding.inputEditName.text.toString().trim()
        val phone = binding.inputEditPhone.text.toString().trim()
        val address = binding.inputEditAddress.text.toString().trim()
        val profilePicture = binding.inputEditProfilePicture.text.toString().trim()

        lifecycleScope.launch {
            val result = app.authRepository.updateProfile(
                name = name,
                phone = phone,
                address = address,
                profilePicture = profilePicture
            )
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.profile_updated_success), Toast.LENGTH_SHORT).show()
                    loadUserProfile()
                }
                is AuthResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
