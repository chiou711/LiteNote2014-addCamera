package com.cwc.litenote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Note_addNew extends Activity {

    private Long mRowId;
    private String mCameraPicFileName;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    Note_common note_common;
    private boolean mEnSaveDb = true;
	private String mPicFileNameInDB;
	private static DB mDb;
    boolean bUseCameraPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.note_add_new);
        setTitle(R.string.add_new_note_title);// set title
        
        System.out.println("Note_addNew / onCreate");

        note_common = new Note_common(this);
        note_common.UI_init();
        mPicFileNameInDB = "";
        mCameraPicFileName = "";
        bUseCameraPicture = false;
			
        // get row Id from saved instance
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DB.KEY_NOTE_ID);
        
        // get picture file name from instance
        mDb = new DB(Note_addNew.this);
        if(savedInstanceState != null)
        {
	        mDb.doOpen();
	        System.out.println("Note_addNew / mRowId =  " + mRowId);
	        if(mRowId != null)
	        {
	        	mPicFileNameInDB = mDb.getNotePictureStringById(mRowId);
	       		note_common.mCurrentPictureFileName = mPicFileNameInDB;
	        }
	        mDb.doClose();
        }
        
        note_common.populateFields(mRowId);
        
    	// button: add
        Button addButton = (Button) findViewById(R.id.note_add_new_add);
        addButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_input_add, 0, 0, 0);
		
        // listener: add
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                mEnSaveDb = true;
                finish();
            }

        });
        
        // button: cancel
        Button cancelButton = (Button) findViewById(R.id.note_add_new_cancel);
        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        // listener: cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                
            	if(note_common.isEdited())
            	{
            		confirmToSaveDlg();
            	}
            	else
            	{
                    note_common.deleteNote(mRowId);
                    mEnSaveDb = false;
                    finish();
            	}
            }
        });
    }
    
    // confirm to update change or not
    void confirmToSaveDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_addNew.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.add_new_note_confirm_save)
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
					    mEnSaveDb = true;
					    setResult(RESULT_OK);
					    finish();
					}})
			   .setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{   // do nothing
					}})					
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						note_common.deleteNote(mRowId);
	                    mEnSaveDb = false;
	                    setResult(RESULT_CANCELED);
	                    finish();
					}})
			   .show();
    }

    // for Add new note
    // for Add new picture (stage 1)
    // for Rotate screen (stage 2)
    @Override
    protected void onPause() {
//    	System.out.println("Note_addNew / onPause");
        super.onPause();
        mRowId = note_common.saveStateInDB(mRowId,mEnSaveDb,mPicFileNameInDB);
    }

    // for Add new picture (stage 2)
    // for Rotate screen (stage 2)
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//   	 	System.out.println("Note_addNew / onSaveInstanceState");
//   	 	System.out.println("Note_addNew / onSaveInstanceState / mRowId = " + mRowId );
//   	 	System.out.println("Note_addNew / onSaveInstanceState / mEnSaveDb = " + mEnSaveDb );
//   	 	System.out.println("Note_addNew / onSaveInstanceState / mPicFileNameInDB 1 = " + mPicFileNameInDB );
//   	 	System.out.println("Note_addNew / onSaveInstanceState / mCameraPicFileName = " + mCameraPicFileName );
//   	 	System.out.println("Note_addNew / onSaveInstanceState / bUseCameraPicture 1 = " + bUseCameraPicture );
   	 	
        if(bUseCameraPicture)
        {
        	outState.putBoolean("UseCameraPicture",true);
        	outState.putString("showCameraPictureFileName", mCameraPicFileName);
//       	 	System.out.println("Note_addNew / onSaveInstanceState / mPicFileNameInDB 2 = " + mPicFileNameInDB );
        }
        else
        {
        	outState.putBoolean("UseCameraPicture",false);
        	outState.putString("showCameraPictureFileName", "");
        }
        
        
        mRowId = note_common.saveStateInDB(mRowId,mEnSaveDb,mPicFileNameInDB);
        note_common.mRowId = mRowId;
        outState.putSerializable(DB.KEY_NOTE_ID, mRowId);
    }

    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	if(savedInstanceState.getBoolean("UseCameraPicture"))
    		bUseCameraPicture = true;
    	else
    		bUseCameraPicture = false;
    	
    	mCameraPicFileName = savedInstanceState.getString("showCameraPictureFileName");
    	
    	if(bUseCameraPicture)
    		note_common.mCurrentPictureFileName = mCameraPicFileName;
    	
//    	System.out.println("Note_addNew / onRestoreInstanceState / bUseCameraPicture = " + bUseCameraPicture);
//    	System.out.println("Note_addNew / onRestoreInstanceState / mCameraPicFileName = " + mCameraPicFileName);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
    	if(Util.bShowExpandedImage == true)
	    	Util.closeExpandImage();
	    else
	    {
	    	if(note_common.isEdited())
	    	{
	    		confirmToSaveDlg();
	    	}
	    	else
	    	{
	            note_common.deleteNote(mRowId);
	            mEnSaveDb = false;
	            finish();
	    	}
	    }
    }
    
    static final int TAKE_PICTURE = R.id.TAKE_PICTURE;
	private static int TAKE_PICTURE_ACT = 1;    
	private Uri imageUri;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(Build.VERSION.SDK_INT >= 11)
		{
		    menu.add(0, TAKE_PICTURE, 0, "Take Picture" )
		    .setIcon(android.R.drawable.ic_menu_camera)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else
		{	
			menu.add(0, TAKE_PICTURE, 1,  "Take Picture" )
		    .setIcon(R.drawable.ic_input_add);
		}	

		return super.onCreateOptionsMenu(menu);
	}
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
        {
            case TAKE_PICTURE:
            	
            	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				mPicFileNameInDB = System.currentTimeMillis() + ".jpg";
            	imageUri = Util.getPictureUri(mPicFileNameInDB, Note_addNew.this);
			    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			    startActivityForResult(intent, TAKE_PICTURE_ACT); 
			    
			    mEnSaveDb = true;
			    
			    if(Util.mExpandedImageView != null)
			    	Util.closeExpandImage();
			    
			    return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) 
	{
		if (requestCode == TAKE_PICTURE_ACT)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				imageUri = Util.getPictureUri(note_common.mCurrentPictureFileName, Note_addNew.this);
	            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
	            note_common.populateFields(mRowId);
	            
	            // set for Rotate any times
	            bUseCameraPicture = true; 
	            mCameraPicFileName = note_common.mCurrentPictureFileName;
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				bUseCameraPicture = false;
				Toast.makeText(Note_addNew.this, "Take Picture: Cancel", Toast.LENGTH_LONG).show();
				
//		    	System.out.println("Note_addNew / onActivityResult / RESULT_CANCELED  / mCameraPicFileName = " + mCameraPicFileName);

				mEnSaveDb = true;
				if(!mCameraPicFileName.isEmpty())
				{
					// update
					note_common.saveStateInDB(mRowId,mEnSaveDb,mCameraPicFileName); // replace with existing picture
//					System.out.println("Note_addNew / onActivityResult / RESULT_CANCELED / update mCameraPicFileName = " + mCameraPicFileName);
					note_common.populateFields(mRowId);
		            
					// set for Rotate any times
		            bUseCameraPicture = true;
		            mPicFileNameInDB = note_common.mCurrentPictureFileName; // for pause
		            mCameraPicFileName = note_common.mCurrentPictureFileName; // for save instance
				}
				else
				{
					// delete unused picture name
//					System.out.println("Note_addNew / onActivityResult / RESULT_CANCELED / set picture file name in DB = " + "");
					note_common.saveStateInDB(mRowId,mEnSaveDb,""); 
					
					// reset DB status
					mRowId = null;
					mPicFileNameInDB = "";
				}
			}
		}
	}
}
