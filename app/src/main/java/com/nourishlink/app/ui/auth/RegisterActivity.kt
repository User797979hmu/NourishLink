package com.nourishlink.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.R
import com.nourishlink.app.data.repository.AuthResult
import com.nourishlink.app.databinding.ActivityRegisterBinding
import com.nourishlink.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val app by lazy { NourishLinkApp.from(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleRole.check(R.id.buttonRoleBeneficiary)
        binding.buttonRegister.setOnClickListener { attemptRegister() }
        binding.textBackToLogin.setOnClickListener { finish() }
        binding.buttonGuestMode.setOnClickListener {
            val role = if (binding.toggleRole.checkedButtonId == R.id.buttonRoleDonor) "donor" else "beneficiary"
            lifecycleScope.launch {
                app.authRepository.loginAsGuest(role)
                startActivity(android.content.Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun attemptRegister() {
        val name = binding.inputName.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        val organisation = binding.inputOrganisation.text.toString().trim()
        val role = if (binding.toggleRole.checkedButtonId == R.id.buttonRoleDonor) "donor" else "beneficiary"

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_fill_all_fields))
            return
        }
        if (password.length < 8) {
            showError(getString(R.string.error_password_too_short))
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            when (val result = app.authRepository.register(name, email, password, role, organisation.ifEmpty { null })) {
                is AuthResult.Success -> {
                    startActivity(android.content.Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> showError(result.message)
            }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.buttonRegister.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.textError.text = message
        binding.textError.visibility = android.view.View.VISIBLE
    }
}
