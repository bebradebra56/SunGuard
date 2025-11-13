package com.sunguard.vault.ppkerg.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sunguard.vault.MainActivity
import com.sunguard.vault.R
import com.sunguard.vault.databinding.FragmentLoadSunGuardBinding
import com.sunguard.vault.ppkerg.data.shar.SunGuardSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class SunGuardLoadFragment : Fragment(R.layout.fragment_load_sun_guard) {
    private lateinit var sunGuardLoadBinding: FragmentLoadSunGuardBinding

    private val sunGuardLoadViewModel by viewModel<SunGuardLoadViewModel>()

    private val sunGuardSharedPreference by inject<SunGuardSharedPreference>()

    private var sunGuardUrl = ""

    private val sunGuardRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sunGuardNavigateToSuccess(sunGuardUrl)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                sunGuardSharedPreference.sunGuardNotificationRequest =
                    (System.currentTimeMillis() / 1000) + 259200
                sunGuardNavigateToSuccess(sunGuardUrl)
            } else {
                sunGuardNavigateToSuccess(sunGuardUrl)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sunGuardLoadBinding = FragmentLoadSunGuardBinding.bind(view)

        sunGuardLoadBinding.sunGuardGrandButton.setOnClickListener {
            val sunGuardPermission = Manifest.permission.POST_NOTIFICATIONS
            sunGuardRequestNotificationPermission.launch(sunGuardPermission)
            sunGuardSharedPreference.sunGuardNotificationRequestedBefore = true
        }

        sunGuardLoadBinding.sunGuardSkipButton.setOnClickListener {
            sunGuardSharedPreference.sunGuardNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            sunGuardNavigateToSuccess(sunGuardUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sunGuardLoadViewModel.sunGuardHomeScreenState.collect {
                    when (it) {
                        is SunGuardLoadViewModel.SunGuardHomeScreenState.SunGuardLoading -> {

                        }

                        is SunGuardLoadViewModel.SunGuardHomeScreenState.SunGuardError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is SunGuardLoadViewModel.SunGuardHomeScreenState.SunGuardSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val sunGuardPermission = Manifest.permission.POST_NOTIFICATIONS
                                val sunGuardPermissionRequestedBefore = sunGuardSharedPreference.sunGuardNotificationRequestedBefore

                                if (ContextCompat.checkSelfPermission(requireContext(), sunGuardPermission) == PackageManager.PERMISSION_GRANTED) {
                                    sunGuardNavigateToSuccess(it.data)
                                } else if (!sunGuardPermissionRequestedBefore && (System.currentTimeMillis() / 1000 > sunGuardSharedPreference.sunGuardNotificationRequest)) {
                                    // первый раз — показываем UI для запроса
                                    sunGuardLoadBinding.sunGuardNotiGroup.visibility = View.VISIBLE
                                    sunGuardLoadBinding.sunGuardLoadingGroup.visibility = View.GONE
                                    sunGuardUrl = it.data
                                } else if (shouldShowRequestPermissionRationale(sunGuardPermission)) {
                                    // временный отказ — через 3 дня можно показать
                                    if (System.currentTimeMillis() / 1000 > sunGuardSharedPreference.sunGuardNotificationRequest) {
                                        sunGuardLoadBinding.sunGuardNotiGroup.visibility = View.VISIBLE
                                        sunGuardLoadBinding.sunGuardLoadingGroup.visibility = View.GONE
                                        sunGuardUrl = it.data
                                    } else {
                                        sunGuardNavigateToSuccess(it.data)
                                    }
                                } else {
                                    // навсегда отклонено — просто пропускаем
                                    sunGuardNavigateToSuccess(it.data)
                                }
                            } else {
                                sunGuardNavigateToSuccess(it.data)
                            }
                        }

                        SunGuardLoadViewModel.SunGuardHomeScreenState.SunGuardNotInternet -> {
                            sunGuardLoadBinding.sunGuardStateGroup.visibility = View.VISIBLE
                            sunGuardLoadBinding.sunGuardLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun sunGuardNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_sunGuardLoadFragment_to_sunGuardV,
            bundleOf(SUN_GUARD_D to data)
        )
    }

    companion object {
        const val SUN_GUARD_D = "sunGuardData"
    }
}