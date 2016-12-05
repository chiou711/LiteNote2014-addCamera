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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
public class ViewFile extends Activity {

    private TextView mTitleViewText;
    private TextView mBodyViewText;
    Bundle extras ;
    File file;
    FileInputStream fileInputStream = null;
    HandleXmlByFile obj;
    boolean ENABLE_STORE = true;
    boolean DISABLE_STORE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.view_file);
        
        mTitleViewText = (TextView) findViewById(R.id.view_title);
        mBodyViewText = (TextView) findViewById(R.id.view_body);
        
		if(Build.VERSION.SDK_INT >= 11)
		{
	        getActionBar().setDisplayShowHomeEnabled(false);
		}
        
		fillContent(DISABLE_STORE);

		int style = 2;
        //set title color
		mTitleViewText.setTextColor(Util.mText_ColorArray[style]);
		mTitleViewText.setBackgroundColor(Util.mBG_ColorArray[style]);
		//set body color 
		mBodyViewText.setTextColor(Util.mText_ColorArray[style]);
		mBodyViewText.setBackgroundColor(Util.mBG_ColorArray[style]);
		
        // back button
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

        // confirm button
        Button confirmButton = (Button) findViewById(R.id.view_confirm);
//        confirmButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_revert, 0, 0, 0);
        
        // do cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }

        });
        
		// do confirm 
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                fillContent(ENABLE_STORE); 

       	     //by file reader
//       		 FileReader fr = null;
//       		 try{
//					fr = new FileReader(file);
//				 } catch (FileNotFoundException e){
//					e.printStackTrace();
//				 }
//       			 
//       		 BufferedReader br = new BufferedReader(fr);
//       		 try 
//       		 {
//					String row = br.readLine();
//      			 	while(row != null)
//      			 	{
//      			 		if(row.contains("<page>"))
//      			 		{
//      			 			System.out.println("page =" + row);
//			            		mDb.doOpen();
//			            		int style = Util.getNewPageStyle(FileList.this);
//								mDb.insertTab("TAB_INFO", row, TabsHost.getLastTabId() + 1,style );
//			            		
//			            		// insert table for new tab
//			            		mDb.insertNewTable(TabsHost.getLastTabId() + 1 );
//			            		mDb.doClose();
//      			 		}
//      			 		else if(row.contains("<title>"))
//      			 			System.out.println("title =" + row);
//      			 		else if((row.contains("<body>")))
//      			 			System.out.println("body =" + row);
//      			 		
//      			 		// read next line 
//      			 		row = br.readLine();
//      			 	}
//       		 } catch (IOException e) {
//					e.printStackTrace();
//       		 }
       		 finish();
            }
        });
    }

    private void fillContent(boolean enStore) 
    {
		 extras = getIntent().getExtras();
    	 file = new File(extras.getString("FILE_PATH"));
    	 
		 try {
		 	fileInputStream = new FileInputStream(file);
		 } catch (FileNotFoundException e) {
		 	e.printStackTrace();
		 }
		 
		 obj = new HandleXmlByFile(fileInputStream,ViewFile.this);
	     obj.enableStore(enStore);
	     obj.fetchXML();
	     while(obj.parsingComplete);

	     mTitleViewText.setText(file.getName());
	     mBodyViewText.setText(obj.fileBody);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillContent(DISABLE_STORE);
    }
}
