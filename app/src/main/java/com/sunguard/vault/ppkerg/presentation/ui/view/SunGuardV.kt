package com.sunguard.vault.ppkerg.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication
import com.sunguard.vault.ppkerg.presentation.ui.load.SunGuardLoadFragment
import org.koin.android.ext.android.inject

class SunGuardV : Fragment(){

    private lateinit var sunGuardPhoto: Uri
    private var sunGuardFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val sunGuardTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        sunGuardFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        sunGuardFilePathFromChrome = null
    }

    private val sunGuardTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            sunGuardFilePathFromChrome?.onReceiveValue(arrayOf(sunGuardPhoto))
            sunGuardFilePathFromChrome = null
        } else {
            sunGuardFilePathFromChrome?.onReceiveValue(null)
            sunGuardFilePathFromChrome = null
        }
    }

    private val sunGuardDataStore by activityViewModels<SunGuardDataStore>()


    private val sunGuardViFun by inject<SunGuardViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (sunGuardDataStore.sunGuardView.canGoBack()) {
                        sunGuardDataStore.sunGuardView.goBack()
                        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "WebView can go back")
                    } else if (sunGuardDataStore.sunGuardViList.size > 1) {
                        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "WebView can`t go back")
                        sunGuardDataStore.sunGuardViList.removeAt(sunGuardDataStore.sunGuardViList.lastIndex)
                        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "WebView list size ${sunGuardDataStore.sunGuardViList.size}")
                        sunGuardDataStore.sunGuardView.destroy()
                        val previousWebView = sunGuardDataStore.sunGuardViList.last()
                        sunGuardAttachWebViewToContainer(previousWebView)
                        sunGuardDataStore.sunGuardView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (sunGuardDataStore.sunGuardIsFirstCreate) {
            sunGuardDataStore.sunGuardIsFirstCreate = false
            sunGuardDataStore.sunGuardContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return sunGuardDataStore.sunGuardContainerView
        } else {
            return sunGuardDataStore.sunGuardContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "onViewCreated")
        if (sunGuardDataStore.sunGuardViList.isEmpty()) {
            sunGuardDataStore.sunGuardView = SunGuardVi(requireContext(), object :
                SunGuardCallBack {
                override fun sunGuardHandleCreateWebWindowRequest(sunGuardVi: SunGuardVi) {
                    sunGuardDataStore.sunGuardViList.add(sunGuardVi)
                    Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "WebView list size = ${sunGuardDataStore.sunGuardViList.size}")
                    Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "CreateWebWindowRequest")
                    sunGuardDataStore.sunGuardView = sunGuardVi
                    sunGuardVi.sunGuardSetFileChooserHandler { callback ->
                        sunGuardHandleFileChooser(callback)
                    }
                    sunGuardAttachWebViewToContainer(sunGuardVi)
                }

            }, sunGuardWindow = requireActivity().window).apply {
                sunGuardSetFileChooserHandler { callback ->
                    sunGuardHandleFileChooser(callback)
                }
            }
            sunGuardDataStore.sunGuardView.sunGuardFLoad(arguments?.getString(
                SunGuardLoadFragment.SUN_GUARD_D) ?: "")
//            ejvview.fLoad("www.google.com")
            sunGuardDataStore.sunGuardViList.add(sunGuardDataStore.sunGuardView)
            sunGuardAttachWebViewToContainer(sunGuardDataStore.sunGuardView)
        } else {
            sunGuardDataStore.sunGuardViList.forEach { webView ->
                webView.sunGuardSetFileChooserHandler { callback ->
                    sunGuardHandleFileChooser(callback)
                }
            }
            sunGuardDataStore.sunGuardView = sunGuardDataStore.sunGuardViList.last()

            sunGuardAttachWebViewToContainer(sunGuardDataStore.sunGuardView)
        }
        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "WebView list size = ${sunGuardDataStore.sunGuardViList.size}")
    }

    private fun sunGuardHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        sunGuardFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Launching file picker")
                    sunGuardTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Launching camera")
                    sunGuardPhoto = sunGuardViFun.sunGuardSavePhoto()
                    sunGuardTakePhoto.launch(sunGuardPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                sunGuardFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun sunGuardAttachWebViewToContainer(w: SunGuardVi) {
        sunGuardDataStore.sunGuardContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            sunGuardDataStore.sunGuardContainerView.removeAllViews()
            sunGuardDataStore.sunGuardContainerView.addView(w)
        }
    }


}