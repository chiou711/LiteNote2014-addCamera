package com.cwc.litenote;

import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ExportToSDCardAct extends Activity{
	Context mContext;
	Intent mEMmailIntent;
	View mView;
	CheckedTextView mCheckTvSelAll;
    ListView mListView;
    List<String> mListStrArr;
    List<Boolean> mCheckedArr;    // 這個用來記錄哪幾個 item 是被打勾的
    int COUNT;
    int mStyle;
	String mSentString;
	SelectPageList selPgLst;

	public ExportToSDCardAct(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		mContext = ExportToSDCardAct.this;
		
		setContentView(R.layout.select_page_list_view);
		
		// checked Text View: select all 
		mCheckTvSelAll = (CheckedTextView) findViewById(R.id.chkSelectAllPages);
		mCheckTvSelAll.setOnClickListener(new OnClickListener()
		{	@Override
			public void onClick(View checkSelAll) 
			{
				boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
				((CheckedTextView)checkSelAll).setChecked(!currentCheck);
				
				if(((CheckedTextView)checkSelAll).isChecked())
					selPgLst.selectAllPages(true);
				else
					selPgLst.selectAllPages(false);
			}
		});
		mStyle = Util.getCurrentPageStyle(mContext);
		
		// list view: selecting which pages to send 
		mListView = (ListView)findViewById(R.id.listView1);
		
//		listForSelect();
		
		// OK button: click to do next
		Button btnSelPageOK = (Button) findViewById(R.id.btnSelPageOK);
		btnSelPageOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// input mail address: dialog
				if(selPgLst.mChkNum > 0)
					inputFileNameDialog(); // call next dialog
				else
	    			Toast.makeText(ExportToSDCardAct.this,
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT).show();
			}
		});
		
		// cancel button
		Button btnSelPageCancel = (Button) findViewById(R.id.btnSelPageCancel);
		btnSelPageCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// step 1: show list for Select
		selPgLst = new SelectPageList(ExportToSDCardAct.this,mListView);
	}
	
	// step 2: input file name 
    String mDefaultFileName;
    SharedPreferences mPref_email;
	EditText editSDCardFileNameText;
	Activity mActVE; // activity from ViewEdit
	String mEMailBodyString;

	void inputFileNameDialog()
	{
		AlertDialog.Builder builder1;

		mPref_email = getSharedPreferences("sd_card_file_name", 0);
	    editSDCardFileNameText = (EditText)getLayoutInflater()
	    							.inflate(R.layout.edit_text_dlg, null);
		builder1 = new AlertDialog.Builder(ExportToSDCardAct.this);
		
		// default file name
		mDefaultFileName = Util.getAppName(ExportToSDCardAct.this) + "_SAVE_" + // file name 
				Util.getCurrentTimeString() + // time
				".xml"; // extension name

		editSDCardFileNameText.setText(mDefaultFileName);
		
		builder1.setTitle(R.string.config_export_SDCard_edit_filename)
				.setMessage(R.string.config_SDCard_filename)
				.setView(editSDCardFileNameText)
				.setNegativeButton(R.string.edit_note_button_back, 
						new DialogInterface.OnClickListener() 
				{   @Override
					public void onClick(DialogInterface dialog, int which) 
					{/*cancel*/finish();}
				})
				.setPositiveButton(R.string.btn_OK, null); //call override
		
		AlertDialog dialog = builder1.create();
		dialog.show();
		
		// override positive button
		Button enterButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		enterButton.setOnClickListener(new CustomListener(dialog));
	}
	
	//for keeping dialog if no input
	class CustomListener implements View.OnClickListener 
	{
		public CustomListener(Dialog dialog){
	    }
	    
	    @Override
	    public void onClick(View v){
	        String strSDCardFileName = editSDCardFileNameText.getText().toString();
	        if(strSDCardFileName.length() > 0)
	        {
				Util util = new Util(ExportToSDCardAct.this);
				
				util.saveToSdCard(strSDCardFileName, // attachment name
						selPgLst.mCheckedArr,false); // checked page array

				Toast.makeText(ExportToSDCardAct.this,
							   R.string.btn_Finish, 
							   Toast.LENGTH_SHORT).show();
				finish();	
	        }
	        else
	        {
    			Toast.makeText(ExportToSDCardAct.this,
						R.string.toast_input_filename,
						Toast.LENGTH_SHORT).show();
	        }
	    }
	}
}