package android.and06.geonotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

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
        //We get the extras in the intent, where we can retrieve the actual notes
        Bundle extras = getIntent().getExtras();
        //But only if there are notes saved in the extras
        if (extras == null) return;
        //We retrieve the notes in the extras with the method getParcelable
        //We need to define the variable nots as final, because we will acess to it from an inner class later on (class WebInterface)
        final ArrayList<GeoNotesDatabaseHelper.Note> notes = extras.getParcelableArrayList("notes");
        //We also get the index of the note that was selected before prssing the button "show on OSM"
        GeoNotesDatabaseHelper.Note currentNote = (GeoNotesDatabaseHelper.Note) extras.getParcelable("currentNote");
        final int currentNoteIndex = notes.indexOf(currentNote);

        //We declare an inner class for the communication with the Javascript
        class WebInterface{
            @JavascriptInterface
            public int getNoteCount(){
                return notes.size();
            }
            @JavascriptInterface
            public String getLat(int i){
                return String.valueOf(notes.get(i).latitude);
            }
            @JavascriptInterface
            public String getLon(int i){
                return String.valueOf(notes.get(i).longitude);
            }
            @JavascriptInterface
            public String getSubject(int i){
                return notes.get(i).subject;
            }
            @JavascriptInterface
            public String getNote(int i){
                return notes.get(i).note;
            }
            @JavascriptInterface
            public int getCurrentNoteIndex(){
                return currentNoteIndex;
            }
        }

        //we call the method addJavascriptInterface of the object webview in order for the created innerclass to communicate with the Javascript
        webView.addJavascriptInterface(new WebInterface(), "Android");
    }
}
