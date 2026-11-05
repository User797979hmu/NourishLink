package com.nourishlink.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.data.repository.AuthResult
import com.nourishlink.app.databinding.ActivityLoginBinding
import com.nourishlink.app.ui.main.MainActivity
import kotlinx.coroutines.launch

/**
 * Sign-in screen: email/password login checked against the accounts stored on this device
 * (passwords are salted + hashed), or a guest demo.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val app by lazy { NourishLinkApp.from(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (app.sessionManager.isLoggedIn) {
            goToMain()
            return
        }

        binding.buttonLogin.setOnClickListener { attemptLogin() }

        // Google Sign-In needs an online service to verify the token, so it isn't offered in this
        // self-contained build (it was already hidden unless a Google client ID was configured).
        binding.buttonGoogleSignIn.visibility = android.view.View.GONE

        binding.textCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.buttonGuestMode.setOnClickListener { showGuestRoleChooser() }
    }

    /** Login has no role toggle (only Register does), so ask which role to demo before going offline-guest. */
    private fun showGuestRoleChooser() {
        val roles = arrayOf(
            getString(com.nourishlink.app.R.string.role_beneficiary),
            getString(com.nourishlink.app.R.string.role_donor)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(com.nourishlink.app.R.string.action_guest_mode))
            .setItems(roles) { _, which ->
                val role = if (which == 1) "donor" else "beneficiary"
                lifecycleScope.launch {
                    app.authRepository.loginAsGuest(role)
                    goToMain()
                }
            }
            .show()
    }

    private fun attemptLogin() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            binding.textError.text = getString(com.nourishlink.app.R.string.error_fill_all_fields)
            binding.textError.visibility = android.view.View.VISIBLE
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            when (val result = app.authRepository.login(email, password)) {
                is AuthResult.Success -> goToMain()
                is AuthResult.Error -> showError(result.message)
            }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.buttonLogin.isEnabled = !loading
        binding.buttonGoogleSignIn.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.textError.text = message
        binding.textError.visibility = android.view.View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
