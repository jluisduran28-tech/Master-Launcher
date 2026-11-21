package com.example.tvlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.setBackgroundColor(Color.BLACK)
        setContentView(web)
        hideSystemUi()

        // Sirve ../web como https://appassets.androidplatform.net/assets/  (localStorage e IndexedDB con origen propio)
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                loader.shouldInterceptRequest(request.url)
        }
        web.webChromeClient = object : WebChromeClient() {
            // Selector de imágenes (memoria USB o almacenamiento) para el modo ambiente
            override fun onShowFileChooser(
                w: WebView, cb: ValueCallback<Array<Uri>>, p: FileChooserParams
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = cb
                return try {
                    startActivityForResult(p.createIntent(), REQ_FILES); true
                } catch (e: Exception) {
                    fileCallback = null; false
                }
            }
        }
        web.addJavascriptInterface(TVBridge(this), "TVBridge")
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        web.requestFocus()
        warnOldWebView()
    }

    /** Avisa si el Android System WebView es demasiado antiguo (< 69) para la interfaz. */
    private fun warnOldWebView() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val v = WebView.getCurrentWebViewPackage()?.versionName?.substringBefore('.')?.toIntOrNull()
            if (v != null && v < 69) {
                android.widget.Toast.makeText(
                    this, "WebView $v desactualizado: actualiza Android System WebView en Play Store",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_FILES) {
            fileCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            fileCallback = null
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    /** Botones del control que la web no recibe como teclas normales. */
    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        val key = when (e.keyCode) {
            KeyEvent.KEYCODE_BACK -> "Back"
            KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_PAGE_UP -> "CH+"
            KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_PAGE_DOWN -> "CH-"
            KeyEvent.KEYCODE_MENU -> "Menu"
            219, 231 -> "Assist"   // KEYCODE_ASSIST / KEYCODE_VOICE_ASSIST (botón del Asistente)
            else -> null
        }
        if (key != null) {
            if (e.action == KeyEvent.ACTION_UP) web.evaluateJavascript("press('$key')", null)
            return true
        }
        return super.dispatchKeyEvent(e)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* un launcher no se cierra con Atrás */ }

    /** Botón Home pulsado con el launcher ya abierto. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) web.evaluateJavascript("window.onHome&&onHome()", null)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        web.evaluateJavascript("window.onNativeResume&&onNativeResume()", null)
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    companion object { private const val REQ_FILES = 1001 }
}
