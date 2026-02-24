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

class HtmlInterface(private val onHtmlReceived: (String) -> Unit) {
    @JavascriptInterface
    fun processHTML(html: String) {
        onHtmlReceived(html)
    }
}

class LoggiScraper(private val context: Context) {
    suspend fun fetchHtml(trackingCode: String): String? {
        return suspendCancellableCoroutine { continuation ->

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
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    webView.addJavascriptInterface(HtmlInterface { html ->
                        if (continuation.isActive) continuation.resume(html)
                    }, "AndroidInterface")
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Handler(Looper.getMainLooper()).postDelayed({
                                view?.loadUrl("javascript:window.AndroidInterface.processHTML(document.documentElement.outerHTML);")
                            }, 6000)
                        }
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            Log.e("LOGGI", "erro webview")
                        }
                    }
                    val url = "https://app.loggi.com/rastreador/$trackingCode/historico"
                    webView.loadUrl(url)

                } catch (e: Exception) {
                    Log.e("LOGGI", "falha ${e.message}")
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }
}