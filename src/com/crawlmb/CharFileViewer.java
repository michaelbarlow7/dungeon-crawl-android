/*
 * Copyright (C) 2008-2010 OpenIntents.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Original copyright: Based on the Android SDK sample application NotePad.
 * Copyright (C) 2007 Google Inc. Licensed under the Apache License, Version
 * 2.0.
 */

package com.crawlmb;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class CharFileViewer extends Activity
{
	private static final String TAG = CharFileViewer.class.getName();

	// This is our state data that is stored when freezing.
	private static final String BUNDLE_ORIGINAL_CONTENT = "original_content";
	private static final String BUNDLE_UNDO_REVERT = "undo_revert";
	private static final String BUNDLE_STATE = "state";
	private static final String BUNDLE_URI = "uri";
	private static final String BUNDLE_SELECTION_START = "selection_start";
	private static final String BUNDLE_SELECTION_STOP = "selection_stop";
	private static final String BUNDLE_FILE_CONTENT = "file_content";
	private static final String BUNDLE_APPLY_TEXT = "apply_text";
	private static final String BUNDLE_APPLY_TEXT_BEFORE = "apply_text_before";
	private static final String BUNDLE_APPLY_TEXT_AFTER = "apply_text_after";

	private int mState;
	private Uri mUri;
	private TextView mText;
	private String mOriginalContent;
	private String mUndoRevert;
	private int mSelectionStart;
	private int mSelectionStop;

	private String mApplyText;
	private String mApplyTextBefore;
	private String mApplyTextAfter;

	private String mFileContent;

	Typeface mCurrentTypeface = null;
	public String mTextTypeface;
	public float mTextSize;
	public boolean mTextUpperCaseFont;
	public int mTextColor;
	public int mBackgroundPadding;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// If an instance of this activity had previously stopped, we can
		// get the original text it started with.
		if (savedInstanceState != null)
		{
			mOriginalContent = savedInstanceState.getString(BUNDLE_ORIGINAL_CONTENT);
			mUndoRevert = savedInstanceState.getString(BUNDLE_UNDO_REVERT);
			mState = savedInstanceState.getInt(BUNDLE_STATE);
			mUri = Uri.parse(savedInstanceState.getString(BUNDLE_URI));
			mSelectionStart = savedInstanceState.getInt(BUNDLE_SELECTION_START);
			mSelectionStop = savedInstanceState.getInt(BUNDLE_SELECTION_STOP);
			mFileContent = savedInstanceState.getString(BUNDLE_FILE_CONTENT);
			if (mApplyText == null && mApplyTextBefore == null && mApplyTextAfter == null)
			{
				// Only read values if they had not been set by
				// onActivityResult() yet:
				mApplyText = savedInstanceState.getString(BUNDLE_APPLY_TEXT);
				mApplyTextBefore = savedInstanceState.getString(BUNDLE_APPLY_TEXT_BEFORE);
				mApplyTextAfter = savedInstanceState.getString(BUNDLE_APPLY_TEXT_AFTER);
			}
		}
		final Intent intent = getIntent();

		mUri = intent.getData();

		mFileContent = readFile(getFile(mUri));
		requestWindowFeature(Window.FEATURE_RIGHT_ICON);
		setContentView(R.layout.char_file_viewer);

		mText = (TextView) findViewById(R.id.note);
//		registerForContextMenu(mText);

	}

	private String readFile(File file)
	{

		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer sb = new StringBuffer();

		try
		{
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0)
			{

				// this statement reads the line from the file and print it to
				// the console.
				sb.append(dis.readLine());
				if (dis.available() != 0)
				{
					sb.append("\n");
				}
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		}
		catch (FileNotFoundException e)
		{
			Log.e(TAG, "File not found", e);
			Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
			return null;
		}
		catch (IOException e)
		{
			Log.e(TAG, "File not found", e);
			Toast.makeText(this, R.string.error_reading_file, Toast.LENGTH_SHORT).show();
			return null;
		}

		return sb.toString();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		getNoteFromFile();
		// Make sure that we don't use the link movement method.
		// Instead, we need a blend between the arrow key movement (for regular
		// navigation) and
		// the link movement (so the user can click on links).
		// TODO: Might be able to edit this, since we don't care about
		// linkifying text
		mText.setMovementMethod(new ArrowKeyMovementMethod()
		{
			public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event)
			{
				// This block is copied and pasted from LinkMovementMethod's
				// onTouchEvent (without the part that actually changes the
				// selection).
				int action = event.getAction();

				if (action == MotionEvent.ACTION_UP)
				{
					int x = (int) event.getX();
					int y = (int) event.getY();

					x -= widget.getTotalPaddingLeft();
					y -= widget.getTotalPaddingTop();

					x += widget.getScrollX();
					y += widget.getScrollY();

					Layout layout = widget.getLayout();
					int line = layout.getLineForVertical(y);
					int off = layout.getOffsetForHorizontal(line, x);

					ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

					if (link.length != 0)
					{
						link[0].onClick(widget);
						return true;
					}
				}

				return super.onTouchEvent(widget, buffer, event);
			}
		});

	}

	private void getNoteFromFile()
	{

		mText.setTextKeepState(mFileContent);
		// keep state does not work, so we have to do it manually:

		// If we hadn't previously retrieved the original text, do so
		// now. This allows the user to revert their changes.
		if (mOriginalContent == null)
		{
			mOriginalContent = mFileContent;
		}

		updateTitleSdCard();
	}

	private void updateTitleSdCard()
	{
		String modified = "";
		if (mOriginalContent != null && !mOriginalContent.equals(mFileContent))
		{
			modified = "* ";
		}
		String filename = "NetHack Config";
		File file = getFile(mUri);
		if (file != null)
		{
			filename = file.getAbsolutePath();
		}
		setTitle(modified + filename);
	}

	private File getFile(Uri uri)
	{
		if (uri != null)
		{
			String filepath = uri.getPath();
			if (filepath != null)
			{
				return new File(filepath);
			}
		}
		return null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// if (debug) Log.d(TAG, "file content: " + mFileContent);

		// Save away the original text, so we still have it if the activity
		// needs to be killed while paused.
		mSelectionStart = mText.getSelectionStart();
		mSelectionStop = mText.getSelectionEnd();
		mFileContent = mText.getText().toString();

		outState.putString(BUNDLE_ORIGINAL_CONTENT, mOriginalContent);
		outState.putString(BUNDLE_UNDO_REVERT, mUndoRevert);
		outState.putInt(BUNDLE_STATE, mState);
		outState.putString(BUNDLE_URI, mUri.toString());
		outState.putInt(BUNDLE_SELECTION_START, mSelectionStart);
		outState.putInt(BUNDLE_SELECTION_STOP, mSelectionStop);
		outState.putString(BUNDLE_FILE_CONTENT, mFileContent);
		outState.putString(BUNDLE_APPLY_TEXT, mApplyText);
		outState.putString(BUNDLE_APPLY_TEXT_BEFORE, mApplyTextBefore);
		outState.putString(BUNDLE_APPLY_TEXT_AFTER, mApplyTextAfter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Build the menus that are shown when editing.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.char_file_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboardManager.setText(mText.getText());
		Toast.makeText(this, R.string.text_copied, Toast.LENGTH_SHORT).show();
		return super.onOptionsItemSelected(item);
	}
}