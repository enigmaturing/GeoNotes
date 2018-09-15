package android.and06.geonotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//AND08D S.66
public class OsmWebViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osm_web_view);
        @SuppressLint("ResourceType") WebView webView = (WebView) findViewById(R.id.webview_osm);
        webView.getSettings().setJavaScriptEnabled(true);
        //The method setWebViewClient() sets which client should load the Url specified. It should be
        //called BEFORE calling the method loadUrl, as specified on:
        //https://stackoverflow.com/questions/7746409
        //If the method setWebViewClient() is not called, then the Url will be shown using a external internet
        //explorer, and not in our webView directly.
        webView.setWebViewClient(new WebViewClient());
        //access to a website -> IMPORTANT: we must include in the address the element:  http://
        //webView.loadUrl("http://www.google.de");
        //access to the raw resources -> IMPORTANT: THREE SLASHES: /// after file: and NOT two // like in http:
        webView.loadUrl("file:///android_res/raw/index.html");
    }
}
