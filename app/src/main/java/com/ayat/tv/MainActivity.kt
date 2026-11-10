package com.ayat.tv

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Hosts the AYAT web app (assets/index.html) in a WebView.
 * Assets are served from https://appassets.androidplatform.net so the page has a real https origin.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var lastBack = 0L

    private val picker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileCallback?.onReceiveValue(uris)
        fileCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        web = WebView(this)
        web.setBackgroundColor(Color.BLACK)
        web.overScrollMode = android.view.View.OVER_SCROLL_NEVER
        web.importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        setContentView(web)
        hideSystemBars()

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val uri = request?.url ?: return null
                val host = uri.host ?: ""
                // Page-initiated XHR (has an Origin header) to the audio hosts: fetch natively and add CORS,
                // so the page can prefetch the next verses into memory for gapless playback.
                if ((host.endsWith("everyayah.com") || host.endsWith("mp3quran.net")) &&
                    request.requestHeaders.containsKey("Origin")) {
                    val cors = mapOf("Access-Control-Allow-Origin" to "*")
                    return try {
                        val c = URL(uri.toString()).openConnection() as HttpURLConnection
                        c.connectTimeout = 10000
                        c.readTimeout = 20000
                        val code = c.responseCode
                        if (code == 200) {
                            WebResourceResponse(c.contentType ?: "audio/mpeg", null, 200, "OK", cors, c.inputStream)
                        } else {
                            WebResourceResponse("text/plain", "utf-8", code, "Error", cors, ByteArrayInputStream(ByteArray(0)))
                        }
                    } catch (e: Exception) {
                        WebResourceResponse("text/plain", "utf-8", 504, "Gateway Timeout", cors, ByteArrayInputStream(ByteArray(0)))
                    }
                }
                return loader.shouldInterceptRequest(uri)
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                if (callback == null || params == null) return false
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    picker.launch(params.createIntent())
                    true
                } catch (e: Exception) {
                    fileCallback = null
                    callback.onReceiveValue(null)
                    false
                }
            }
        }

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }

        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        web.requestFocus()
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    // Remote media keys control playback inside the page.
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { web.evaluateJavascript("toggle()", null); return true }
            KeyEvent.KEYCODE_MEDIA_PLAY -> { web.evaluateJavascript("if(!P)toggle()", null); return true }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> { web.evaluateJavascript("if(P)toggle()", null); return true }
            KeyEvent.KEYCODE_MEDIA_NEXT -> { web.evaluateJavascript("nextS(1)", null); return true }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> { web.evaluateJavascript("nextS(-1)", null); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    // Back closes an open list/dialog first; otherwise press twice to exit.
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        web.evaluateJavascript("(SH.open||DG.open||DR.open)?1:0") { value ->
            if (value == "1") {
                web.evaluateJavascript("window.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape'}))", null)
            } else {
                val now = System.currentTimeMillis()
                if (now - lastBack < 2000) {
                    finish()
                } else {
                    lastBack = now
                    Toast.makeText(this, "اضغط رجوع مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }
}
