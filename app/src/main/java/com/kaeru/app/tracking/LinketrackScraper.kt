package com.kaeru.app.data.scraper

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private class LinketrackHtmlInterface(private val onHtmlReceived: (String?) -> Unit) {
    @JavascriptInterface
    fun success(html: String) {
        onHtmlReceived(html)
    }

    @JavascriptInterface
    fun notFound() {
        onHtmlReceived(null)
    }

    @JavascriptInterface
    fun log(msg: String) {
        Log.d("LINKETRACK", msg)
    }
}

class LinketrackWebViewScraper(private val context: Context) {

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
                    val activity = context as? Activity
                    val rootView = activity?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)

                    val webView = WebView(context)

                    val params = ViewGroup.LayoutParams(1, 1)
                    webView.layoutParams = params
                    webView.alpha = 0.01f
                    rootView?.addView(webView)

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
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
                    }
                    webView.addJavascriptInterface(LinketrackHtmlInterface {
                        Handler(Looper.getMainLooper()).post {
                            rootView?.removeView(webView)
                            webView.destroy()
                        }
                        safeResume(it)
                    }, "LinketrackInterface")

                    val jsCode = """
                        (function() {
                            if (window.hasSentData) return;
                            
                            var items = document.getElementsByClassName('evento-collection');
                            var bodyText = document.body.innerText || "";
                            
                            if (items.length > 0) {
                                window.hasSentData = true;
                                window.LinketrackInterface.success(document.documentElement.outerHTML);
                            } else if (bodyText.includes("Objeto não encontrado") || bodyText.includes("Código inválido")) {
                                window.hasSentData = true;
                                window.LinketrackInterface.notFound();
                            }
                        })();
                    """.trimIndent()

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            view?.evaluateJavascript(jsCode, null)
                        }
                    }
                    val url = "https://linketrack.com/track?codigo=$trackingCode"
                    val headers = mapOf("Referer" to "https://www.google.com/")
                    webView.loadUrl(url, headers)
                    val handler = Handler(Looper.getMainLooper())
                    val runnable = object : Runnable {
                        var count = 0
                        override fun run() {
                            if (hasResumed || count > 20) return
                            webView.evaluateJavascript(jsCode, null)
                            count++
                            handler.postDelayed(this, 2000)
                        }
                    }
                    handler.postDelayed(runnable, 3000)

                    handler.postDelayed({
                        if (!hasResumed) {
                            Handler(Looper.getMainLooper()).post {
                                rootView?.removeView(webView)
                                webView.destroy()
                            }
                            Log.e("LINKETRACK", "timeout")
                            safeResume(null)
                        }
                    }, 15000)
                } catch (e: Exception) {
                    Log.e("LINKETRACK", "erro: ${e.message}")
                    safeResume(null)
                }
            }
        }
    }
}