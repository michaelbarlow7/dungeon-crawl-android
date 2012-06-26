package com.crawlmb;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CrawlAppActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(stringFromJNI());
    }
    
	public void fatal(String msg) 
	{
		System.out.println("############ this seriously got called???");
	}

    public native String  stringFromJNI();

    static {
        System.loadLibrary("crawl");
    }
}