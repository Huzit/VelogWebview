#### ✨만들게된 이유✨ 
>매번 휴대폰 브라우저를 통해 들어오는 것 보다 컴퓨터 바로가기를 클릭하는 것 처럼 들어오면 어떨까? 하는 생각에 만들게 됬습니다.

---

우선 개발로 들어가기전 웹뷰란 무엇인지부터 살펴보겠습니다.

## 1. 웹뷰란 무엇인가?
![](https://images.velog.io/images/appletorch/post/4e9165e3-b985-4ba8-a175-19f36d5e9f18/img.png)
웹뷰는 [API Level 1](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)의 기능으로 웹 컨텐츠를 액티비티 레이아웃에 표시해줍니다. 완전히 개발된 브라우저에 비해 기능은 부족하지만 앱에 맞춰 특별히 설계된 환경에서 웹 페이지를 표시할 때 매우 유용하게 쓸 수 있습니다.

---
## 2. 구현
>구현하기에 앞서 **모든 코드는 코틀린**으로 작성되있으므로 자바코드가 필요하신 분들은 **코드변환기**를 쓰시면 편하게 볼 수 있습니다.

구현해야할 기능을 정리해보면 4가지로 나눌 수 있습니다.
>1. 웹뷰 표시
2. 프로그래스바 적용
3. 버튼 더블클릭 시 페이지최상단으로 이동
4. 뒤로가기
5. 웹 사이트 Favicon을 앱 이미지로 등록

<br>

### 2-1) 웹뷰 표시 & 프로그래스 바
> 매우 간단하기 때문에 프로젝트를 새로 생성했을 때 만들어지는 액티비티 하나에 다 구현했습니다.

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
    <WebView
            android:id="@+id/webView"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
    </WebView>
      <ProgressBar
            app:layout_constraintTop_toTopOf="parent"
            android:id="@+id/pBar"
            android:layout_width="match_parent"
            android:layout_height="5dp"
            android:layout_gravity="top"
            android:scaleY="4"
            style="@style/Widget.AppCompat.ProgressBar.Horizontal"
            android:indeterminate="true"
    />
</android.support.constraint.ConstraintLayout>
```
레이아웃에 웹뷰 컴포넌트를 만들어줍니다.
***

소스코드로 넘어가서 
가장 먼저해야할 일은 Build.gradle에 뷰 바인딩을 true로 해주는 것입니다.

```xml
buildFeatures{
	viewBinding = true
}
```

MainActivity로 넘어가서 뷰 바인딩을 해줍니다.

```kotlin
lateinit var mainActivity: ActivityMainBinding

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)

  //뷰 바인딩
  mainActivity = ActivityMainBinding.inflate(layoutInflater)
  setContentView(mainActivity.root)

}
```

이제 웹뷰 로드와 프로그래스바를 구현할 차례인데 onCreate에 모두 구현하면 코드가 길어지기 때문에 새로운 함수를 만들어 줄겁니다.


```kotlin
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

            //로딩 종료
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pBar.visibility = View.GONE
            }
        }
        //자바스크립트 설정
        webView.settings.javaScriptEnabled = true
        //웹뷰 url 설정
        webView.loadUrl(url)
    }
```

* 제일 처음으로 해줄 일은 프로그래스바를 숨기는 것입니다. 기본값으로 놔두게 되면 화면에 계속 표시되므로 View.GONE 속성을 넣어줍니다. 

* 웹코어를 설정해줍니다. 웹 코어를 설정해주지 않는다면 URL을 로드했을 때 **페이지이동**이 있을 경우 **휴대폰에 기본으로 설정된 브라우저를 통해 이동**하게 됩니다. 웹뷰 내에서 이동하려면 웹 코어를 설정해줘야합니다. 

* 로딩확인을 위한 프로그래스바의 표시유무는 WebViewClient() 객체 생성시 같이 선언해줍니다.

* 마지막으로 webView.settings.javaScriptEnabled는 기본 false로 되있습니다. true로 바꿔줘야 브라우저 이벤트가 정상적으로 동작합니다.

* * *

### 2-2) 더블클릭 이벤트

우선 화면상단을 클릭할 수 있도록 레이아웃에 버튼을 만들어 줍니다.
```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
    <Button
            android:id="@+id/topButton"
            android:layout_width="100dp"
            android:layout_height="50dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            android:background="@android:color/transparent"
    />
    <WebView
            android:id="@+id/webView"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
    </WebView>
    <ProgressBar
            app:layout_constraintTop_toTopOf="parent"
            android:id="@+id/pBar"
            android:layout_width="match_parent"
            android:layout_height="5dp"
            android:layout_gravity="top"
            android:scaleY="4"
            style="@style/Widget.AppCompat.ProgressBar.Horizontal"
            android:indeterminate="true"
    />
</android.support.constraint.ConstraintLayout>
```
실제 앱을 구동했을 때 사용자화면에 버튼이 보이면 안되므로 background에 @android:color/transparent속성을 넣어줍니다. 
***

버튼클릭 이벤트를 onCreate()에 작성해줍니다.

```kotlin
var doubleClickFlag = 0

//상단 더블클릭시 최상위 페이지로
mainActivity.topButton.setOnClickListener {
	doubleClickFlag++
	val clickRun = Runnable { doubleClickFlag = 0 }
	
    	//1번 터치시
	if (doubleClickFlag == 1) {
		//터치간격 조절
		Handler().postDelayed(clickRun, 250)
	} else {
		mainActivity.webView.pageUp(true)
	}
}
```
* 더블클릭 이벤트가 따로 없으므로 클릭이벤트를 선언해줍니다.

* 클릭 간격을 설정하지 않으면 간격에 상관없이 인식되므로, 터치간격을 조절할 수 있도록 Handler을 통해 0.25초 내로 추가 이벤트 발생이 없으면 doubleClickFlag가 0이 됩니다.
* * *
### 2-3) 뒤로가기
따로 설정하지 않으으면 뒤로가기 이벤트 발생시 앱이 꺼지는 현상이 생기므로 설정해줘야햡니다.
```kotlin
//뒤로가기버튼 눌려졌을 때
override fun onBackPressed() {
	//웹뷰내에서 뒤로가기
	if (mainActivity.webView.canGoBack()) {
		mainActivity.webView.goBack()
	} else {
		super.onBackPressed()
	}
}
```
***
### 2-4) 사이트 아이콘 설정
웹뷰를 통한 웹사이트 바로가기앱을 만들었기 때문에 앱 아이콘도 사이트를 바로 알 수 있는 Favicon으로 하면 쉽게 알 수 있습니다.
velog에서 개발자 도구(F12)를 열어줍니다.
![개발자도구](https://images.velog.io/images/appletorch/post/daf8d2c9-a67a-43c6-9efa-ed00f44eb7fc/image.png)
favicon을 검색해서 shortcut dkdlzhsrhk  사이즈별 아이콘을 전부 받아줍니다.
크기가 미묘하게 작을 수 있지만 비슷한 크기대로 mipmap-hdpi, mipmap-mhdpi, mipmap-xhdpi, mipmap-xxhdpi에 넣어줍니다.

다음 AndroidManifest.xml에서 아이콘 이미지를 설정해줍니다.

	android:icon="@mipmap/붙여넣은 사진"
    android:roundIcon="@mipmap/붙여넣은 사진"
    
이렇게 해주면 아이콘까지 완성된 웹뷰 어플을 완성했습니다.
***
## 마치며

![](https://jjalbot.com/media/2018/12/B1ZlAT4GgN/20171212_5a2ea6e820140.gif)
<br>
웹뷰를 통해 간단한 어플을 만들어 보았습니다. 일반 계정로그인은 메일을 통한 로그인으로 메일 확인절차시 브라우저로 넘어가기때문에 해결방법을 찾지 못했고, 소셜 로그인 중 구글로그인은 Google's OAuth 2.0 정책에 위배되어 불가능합니다.
그리고 웹뷰는 위와같은 원인으로 [21년도 10월 30일에 블락](https://developers.googleblog.com/2021/06/upcoming-security-changes-to-googles-oauth-2.0-authorization-endpoint.html)되어 앞으로의 전망은 모르겠습니다만
앱을 개발해보면서 더블클릭이나 favicon과 같이 몰랐던 것을 알 수 있게 되서 좋았습니다.
