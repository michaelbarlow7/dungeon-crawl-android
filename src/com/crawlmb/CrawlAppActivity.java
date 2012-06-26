package com.crawlmb;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CrawlAppActivity extends Activity {
    private TextView textView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        textView = (TextView) findViewById(R.id.text);
        stringFromJNI();
    }
    
	public void fatal(final String msg) 
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				textView.setText(msg);
			}
		});
	}

    public native String  stringFromJNI();

    static {
        System.loadLibrary("crawl");
    }
}