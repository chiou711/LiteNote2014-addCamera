package com.cwc.litenote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class ConfigAct extends Activity {
//	public class ConfigAct extends Fragment {

	TextView mNewPageTVStyle;
	private int mStyle = 0;

	// vibration
	SharedPreferences mPref_vibration;
	SharedPreferences mPref_FinalPageViewed;
	TextView mTextViewVibration;

	private AlertDialog dialog;
	private Context mContext;
	private LayoutInflater mInflator;
//	String[] mItemArray = new String[]{"black","white","greenish","reddish","bluish","yellowish"};
	String[] mItemArray = new String[]{"1","2","3","4","5","6","7","8","9","10"};
	
	public ConfigAct(){};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		System.out.println("================ Config start ==================");
		
		setContentView(R.layout.config);
		
		if(Build.VERSION.SDK_INT >= 11)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	    //Set text style
		setNewPageTextStyle();
		
		//Set deleting warning 
		setDeleteWarn();
		
		//Set vibration time length
		setVibratoinTimeLength();
		
		//save all notes to SD card
		saveToSdCardDialog();
		
		//save all notes to SD card
		importFromSdCardDialog();
		
		//set to default
		deleteDB_button();
		
		// disable button when API version larger or equal than 11
		if(Build.VERSION.SDK_INT >= 11)
			findViewById(R.id.btnConfig).setVisibility(View.GONE); // 1 invisible
		else
			addListenerOnButton();
	}
    /**
     * Save SD Card 
     * 
     */
    void saveToSdCardDialog()
    {
		final View tvSaveSdCard =  findViewById(R.id.exportToSdCard);


		tvSaveSdCard.setOnClickListener(new OnClickListener() 
		{   @Override
			public void onClick(View v) 
			{
				DB db = new DB(ConfigAct.this);
				db.doOpen();
				if(DB.getAllTabCount()>0)
				{
					Intent intentSend = new Intent(ConfigAct.this, ExportToSDCardAct.class);
					startActivity(intentSend);
				}
				else
				{
					Toast.makeText(ConfigAct.this, R.string.config_export_none_toast, Toast.LENGTH_SHORT).show();
				}
				db.doClose();
			}
		});
    }
    
    /**
     * Import from SD Card 
     * 
     */
    void importFromSdCardDialog()
    {
		View tvImportSdCard =  findViewById(R.id.importFmSdCard);

		tvImportSdCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ConfigAct.this, ImportFromSDCardAct.class);
	        	startActivityForResult(intent, Util.ACTIVITY_IMPORT);
			}
		});
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
        if(requestCode == Util.ACTIVITY_IMPORT)
        {
            mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
            
	        int tabTableId = 0;
			DB db = new DB(ConfigAct.this);
			db.doOpen();
	        for(int i=0;i<DB.getAllTabCount();i++)
	        {
	        	if(	db.getTabId(i)== TabsHost.getLastTabId())
	        		tabTableId =  DB.getTabTableId(i);
	        }
	        db.doClose();
            
            mPref_FinalPageViewed.edit().putString("KEY_FINAL_PAGE_VIEWED",
            										String.valueOf(tabTableId))
            							.commit(); //note: point to table
        }   
	}
    
	/**
	 *  select style
	 *  
	 */
	void setNewPageTextStyle()
	{
		// Get current style
		mNewPageTVStyle = (TextView)findViewById(R.id.TextViewStyleSetting);
		View mViewStyle = findViewById(R.id.setStyle);
		int iBtnId = Util.getNewPageStyle(ConfigAct.this);
		
		// set background color with current style 
		mNewPageTVStyle.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		mNewPageTVStyle.setText(mItemArray[iBtnId]);
		mNewPageTVStyle.setTextColor(Util.mText_ColorArray[iBtnId]);
		
		mViewStyle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectStyleDialog(v);
			}
		});
	}
	
	
	void selectStyleDialog(View view)
	{
		mContext = ConfigAct.this;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		
		builder.setTitle(R.string.config_set_style_title)
			   .setPositiveButton(R.string.btn_OK, listener_ok)
			   .setNegativeButton(R.string.btn_Cancel, null);
		
		// inflate select style layout
		mInflator= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = mInflator.inflate(R.layout.select_style, null);
		RadioGroup RG_view = (RadioGroup)view.findViewById(R.id.radioGroup1);
		
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio0),0);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio1),1);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio2),2);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio3),3);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio4),4);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio5),5);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio6),6);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio7),7);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio8),8);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio9),9);
		
		builder.setView(view);

		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup RG, int id) {
				mStyle = RG.indexOfChild(RG.findViewById(id));
		}});
		
		dialog = builder.create();
		dialog.show();
	}
	
    private void setButtonColor(RadioButton rBtn,int iBtnId)
    {
		rBtn.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		rBtn.setText(mItemArray[iBtnId]);
		rBtn.setTextColor(Util.mText_ColorArray[iBtnId]);
		
		//set checked item
//		if(iBtnId == mPref_style.getInt("KEY_STYLE",Util.STYLE_DEFAULT))
		if(iBtnId == Util.getNewPageStyle(mContext))
			rBtn.setChecked(true);
		else
			rBtn.setChecked(false);
    }
		   
    DialogInterface.OnClickListener listener_ok = new DialogInterface.OnClickListener()
   {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			SharedPreferences mPref_style = getSharedPreferences("style", 0);
			mPref_style.edit().putInt("KEY_STYLE",mStyle).commit();
			// update the style selection directly
			mNewPageTVStyle.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
			mNewPageTVStyle.setText(mItemArray[mStyle]);
			mNewPageTVStyle.setTextColor(Util.mText_ColorArray[mStyle]);
			//end
			dialog.dismiss();
		}
   };
	
	/**
	 *  set deleting warning 
	 */
	void setDeleteWarn()
	{
		View deleteWarn = findViewById(R.id.deleteWarn); 
		deleteWarn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectWarnItemDialog(v);
			}
		});
	}

	void selectWarnItemDialog(View view)	
	{
		SelectWarnItemDlg selectWarnItemDlg = new SelectWarnItemDlg(view,ConfigAct.this);
		selectWarnItemDlg.SelectWarnPref();
	}
	

	/**
	 *  select vibration time length
	 */
	void setVibratoinTimeLength()
	{
		//  set current
		mPref_vibration = getSharedPreferences("vibration", 0);
		View viewVibration = findViewById(R.id.vibrationSetting);
		mTextViewVibration = (TextView)findViewById(R.id.TextViewVibrationSetting);
	    String strVibTime = mPref_vibration.getString("KEY_VIBRATION_TIME","25");
		if(strVibTime.equalsIgnoreCase("00"))
			mTextViewVibration.setText(getResources().getText(R.string.config_status_disabled).toString());
		else
			mTextViewVibration.setText(strVibTime +"ms");

		// Select new 
		viewVibration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectVibrationLengthDialog();
			}
		});
	}

	void selectVibrationLengthDialog()
	{
		   final String[] items = new String[]{getResources().getText(R.string.config_status_disabled).toString(),
				   		    				"15ms","25ms","35ms","45ms"};
		   AlertDialog.Builder builder = new AlertDialog.Builder(ConfigAct.this);
		   DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		   {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String len = null;
					
					if(which ==0)
						len = "00";
					else
						len = (String) items[which].subSequence(0,2);
					mPref_vibration.edit().putString("KEY_VIBRATION_TIME",len).commit();
					// change the length directly
					if(len.equalsIgnoreCase("00"))
						mTextViewVibration.setText(getResources().getText(R.string.config_status_disabled).toString());
					else
						mTextViewVibration.setText(len + "ms");					
					
					//end
					dialog.dismiss();
				}
		   };
		   builder.setTitle(R.string.config_set_vibration_title)
				  .setSingleChoiceItems(items, -1, listener)
				  .setNegativeButton(R.string.btn_Cancel, null)
				  .show();
	}

   
   /**
    * Delete DB
    *  
    */
   //delete DB
	public void deleteDB_button(){
		View tvDelDB = findViewById(R.id.SetDeleteDB);
		tvDelDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmDeleteDB(v);
			}
		});
	}
	
	private void confirmDeleteDB(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ConfigAct.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.config_delete_DB_confirm_content)
			   .setPositiveButton(R.string.btn_OK, listener_delete_DB)
			   .setNegativeButton(R.string.btn_Cancel, null)
			   .show();
	}

    DialogInterface.OnClickListener listener_delete_DB = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			DB.deleteDB();
			
			//set last tab Id to 0, otherwise TabId will not start from 0 when deleting all
			TabsHost.setLastTabId(0);
			//reset tab Index to 0 
			//fix: select tab over next import amount => clean all => import => export => error
			TabsHost.mCurrentTabIndex = 0;
			
    		//update final page currently viewed: tab Id
			SharedPreferences mPref_final_page_viewed;
			mPref_final_page_viewed = getSharedPreferences("final_page_viewed", 0);
			mPref_final_page_viewed.edit().remove("KEY_FINAL_PAGE_VIEWED").commit();
			dialog.dismiss();
		}
    };
	
	public void addListenerOnButton(){
		Button btnBack = (Button) findViewById(R.id.btnConfig);
		btnBack.setOnClickListener(new OnClickListener() 
		{		@Override
				public void onClick(View v) {
					finish();
		    		Intent intent=new Intent(getBaseContext(),TabsHost.class);
		    		startActivity(intent);
				}
		});
		}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
    		finish();
    		Intent intent=new Intent(getBaseContext(),TabsHost.class);
    		startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}