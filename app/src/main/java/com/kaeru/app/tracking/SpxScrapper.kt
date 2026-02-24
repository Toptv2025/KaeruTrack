package com.kaeru.app.tracking

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private class SpxHtmlInterface(private val onHtmlReceived: (String?) -> Unit) {
    @JavascriptInterface
    fun success(html: String) {
        Log.d("SPX", "html pego")
        onHtmlReceived(html)
    }
}

class SpxScraper(private val context: Context) {

    suspend fun fetchHtml(trackingCode: String): String? {
        return suspendCancellableCoroutine { continuation ->
            var hasResumed = false
            fun safeResume(result: String?) {
                if (!hasResumed && continuation.isActive) {
                    hasResumed = true
                    continuation.resume(result)
                }
            }

            Handler(Looper.getMainLooper()).post {
                try {
                    val webView = WebView(context)
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(webView, true)
                    webView.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = null
                    }
                    webView.addJavascriptInterface(SpxHtmlInterface { safeResume(it) }, "SpxInterface")
                    val jsCode = """
                        (function() {
                            if (window.hasSentData) return;
                            var items = document.getElementsByClassName('nss-comp-tracking-item');
                            if (items.length > 0) {
                                window.hasSentData = true;
                                window.SpxInterface.success(document.documentElement.outerHTML);
                            }
                        })();
                    """.trimIndent()

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            Log.d("SPX", "página ok")
                        }
                    }
                    val url = "https://spx.com.br/track?$trackingCode"
                    val headers = mapOf("Referer" to "https://shopee.com.br/")
                    webView.loadUrl(url, headers)
                    val handler = Handler(Looper.getMainLooper())
                    val runnable = object : Runnable {
                        var attempts = 0
                        override fun run() {
                            if (hasResumed || attempts > 25) return

                            if (attempts % 5 == 0) Log.d("SPX", "tentativa js")
                            webView.evaluateJavascript(jsCode, null)

                            attempts++
                            handler.postDelayed(this, 1000)
                        }
                    }
                    handler.postDelayed(runnable, 2000)

                    handler.postDelayed({
                        if (!hasResumed) {
                            Log.e("SPX", "timeout")
                            safeResume(null)
                        }
                    }, 30000)

                } catch (e: Exception) {
                    safeResume(null)
                }
            }
        }
    }
}