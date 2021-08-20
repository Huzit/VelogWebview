package com.example.velogwebview

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.velogwebview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var mainActivity: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //뷰 바인딩
        mainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivity.root)
        val url = "https://velog.io/"
        var doubleClickFlag = 0

        //웹뷰 초기설정
        initWebView(url)
        //상단 더블클릭시 최상위 페이지로
        mainActivity.topButton.setOnClickListener {
            doubleClickFlag++
            val clickRun = Runnable { doubleClickFlag = 0 }

            if (doubleClickFlag == 1) {
                //터치간격 조절
                Handler().postDelayed(clickRun, 250)
            } else {
                mainActivity.webView.pageUp(true)
            }
        }
    }

    fun initWebView(url: String) {
        val webView = mainActivity.webView
        val pBar = mainActivity.pBar

        //진행바 숨기기
        pBar.visibility = View.GONE

        //웹코어 설정
        webView.webViewClient = object : WebViewClient() {
            //로딩 시작시
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                pBar.visibility = View.VISIBLE
            }

            //로딩종료
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pBar.visibility = View.GONE
            }
        }
        //자바스크립트 코어 설정
        webView.settings.javaScriptEnabled = true
        //웹뷰 url 설정
        webView.loadUrl(url)
    }

    //뒤로가기버튼 눌려졌을 때
    override fun onBackPressed() {
        //웹뷰내에서 뒤로가기
        if (mainActivity.webView.canGoBack()) {
            mainActivity.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}