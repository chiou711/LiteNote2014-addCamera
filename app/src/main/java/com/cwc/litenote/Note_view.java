/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cwc.litenote;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
public class Note_view extends Activity {

    private TextView mTitleViewText;
    private TextView mBodyViewText;  
    private TextView mTxtVw_Time;
    private Long mRowId;
    private DB mDb;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    int EDIT_VIEW = 5;
    int MAIL_VIEW = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.note_view);
        // set title
        setTitle(R.string.view_note_title);
        
		if(Build.VERSION.SDK_INT >= 11)
		{
	        getActionBar().setDisplayShowHomeEnabled(false);
		}
        
    	DB.setTableNumber(DB.getTableNumber()); // add for install first time error
        mDb = new DB(this);

        View pageView = (View) findViewById(R.id.view_page);
        mTitleViewText = (TextView) findViewById(R.id.view_title);
        mBodyViewText = (TextView) findViewById(R.id.view_body);
        mTxtVw_Time = (TextView) findViewById(R.id.textTime);
        
        mDb.doOpen();
		int style = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
        mDb.doClose();
        
        pageView.setBackgroundColor(Util.mBG_ColorArray[style]);
		mTitleViewText.setTextColor(Util.mText_ColorArray[style]);
		mBodyViewText.setTextColor(Util.mText_ColorArray[style]);
		mTxtVw_Time.setTextColor(Util.mText_ColorArray[style]);
		
		// edit note button
        Button editButton = (Button) findViewById(R.id.view_edit);
        editButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_edit, 0, 0, 0);
		
        // send note button
        Button sendButton = (Button) findViewById(R.id.view_send);
        sendButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_send, 0, 0, 0);
        
        // back button
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        
		//edit 
        editButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);

		        Intent i = new Intent(Note_view.this, Note_edit.class);
		        i.putExtra(DB.KEY_NOTE_ID, mRowId);
		        startActivityForResult(i, EDIT_VIEW);
            }

        });
        
		//send 
        sendButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                
                // set Sent string Id
				List<Long> rowArr = new ArrayList<Long>();
				rowArr.add(0,mRowId);
				
                // mail
				Intent intent = new Intent(Note_view.this, SendMailAct.class);
		        String extraStr = Util.getSendString(rowArr);
		        extraStr = Util.addRssVersionAndChannel(extraStr);
		        intent.putExtra("SentString", extraStr);
				startActivity(intent);
            }

        });
        
        //cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }

        });
			
        mDb.doOpen();
        mRowId = mDb.getNoteId(getIntent().getExtras().getInt("POSITION"));
        mDb.doClose();

		populateFields();
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        if((requestCode==EDIT_VIEW) || (requestCode==MAIL_VIEW))
        {
        	finish();
        }
	}

    private void populateFields() {
    	mDb.open();
        if (mRowId != null) {
            Cursor note = mDb.get(mRowId);
            
            Long time = note.getLong(
	          		note.getColumnIndexOrThrow(DB.KEY_NOTE_CREATED));
            String mStrTime = Util.getTimeString(time);
            mTxtVw_Time.setText(mStrTime);
            
            String strTitleEdit = note.getString(
	          		note.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
            mTitleViewText.setText(strTitleEdit);
            
            String strBodyEdit = note.getString(
	          		note.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
            mBodyViewText.setText(strBodyEdit);
            
        }
        mDb.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DB.KEY_NOTE_ID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

}
