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

import java.io.IOException;
import java.util.Date;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Note_common {

    private EditText mTitleEditText;
    String mOriginalTitle;
    
    ImageView mPicImageView;
    String mPictureFileNameInDB;
    String mOriginalPictureFileName;
    String mCurrentPictureFileName;

    private EditText mBodyEditText;
    
    String mOriginalBody;
    
    Long mRowId, mOriginalCreatedTime, mOriginalMarking;
    
    boolean bRollBackData;
    boolean bRemovePicFileName = false;
    boolean bEditPicture = false;

    private DB mDb;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    static Activity mAct;
    
    public Note_common(Activity act,Long rowId,String strTitle, String pictureFileName, String strBody, Long createdTime)
    {
    	mAct = act;
    	mRowId = rowId;
    			
    	mOriginalTitle = strTitle;
	    mOriginalBody = strBody;
	    mOriginalPictureFileName = pictureFileName;
	    mOriginalCreatedTime = createdTime;
	    mCurrentPictureFileName = pictureFileName;
	    
    	DB.setTableNumber(DB.getTableNumber()); 
        mDb = new DB(mAct);
    	mDb.doOpen();
	    mOriginalMarking = mDb.getNoteMarkingById(rowId);
		mDb.doClose();

		bRollBackData = false;
		bEditPicture = true;
    }
    
    public Note_common(Activity act)
    {
    	mAct = act;
    	DB.setTableNumber(DB.getTableNumber()); 
        mDb = new DB(mAct);
    }
    
    void UI_init()
    {
        mTitleEditText = (EditText) mAct.findViewById(R.id.edit_title);
        mPicImageView = (ImageView) mAct.findViewById(R.id.edit_picture);
        mBodyEditText = (EditText) mAct.findViewById(R.id.edit_body);
        
        mDb.doOpen();
		int style = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
		mDb.doClose();
        
		//set title color
		mTitleEditText.setTextColor(Util.mText_ColorArray[style]);
		mTitleEditText.setBackgroundColor(Util.mBG_ColorArray[style]);
		
		mPicImageView.setBackgroundColor(Util.mBG_ColorArray[style]);
		
		//set body color 
		mBodyEditText.setTextColor(Util.mText_ColorArray[style]);
		mBodyEditText.setBackgroundColor(Util.mBG_ColorArray[style]);	
		
		// set thumb nail listener
        mPicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try 
                {
                	bRemovePicFileName = false;
					Util.zoomImageFromThumb(mPicImageView, mPictureFileNameInDB /*mCurrentPictureFileName*/, mAct);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
            }
        });
        
		// set thumb nail long click listener
        mPicImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
            	if(bEditPicture)
            		deletePictureImage();
                return false;
            }
        });
    }
    
    
    void deletePictureImage()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
		builder.setTitle("Delete picture image")
	           .setMessage("Do you want ot delete current picture?")
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						//just delete picture file name
						mCurrentPictureFileName = "";
						mOriginalPictureFileName = "";
				    	removePictureStringFromCurrentEditNote(mRowId);
				    	populateFields(mRowId);
				    	bRemovePicFileName = true;
					}})
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						// cancel
					}})
			   .show();
    }
    
    void deleteNote(Long rowId)
    {
        mDb.open();
        // for Add new note (mRowId is null first), but decide to cancel 
        if(rowId != null)
        	mDb.delete(rowId);
        mDb.close();
    }
    
    void populateFields(Long rowId) 
    {
    	mDb.open();
    	
    	if (rowId != null) 
    	{
    		mPictureFileNameInDB = mDb.getNotePictureStringById(rowId);
    		Uri imageUri = Util.getPictureUri(mPictureFileNameInDB, mAct);
            
    		// set picture
			try 
			{
				System.out.println("populateFields / imageUri = " + imageUri);
				mPicImageView.setVisibility(View.VISIBLE );
				mPicImageView.setImageBitmap(Util.decodeSampledBitmapFromUri(imageUri, 50, 50, mAct));
			} 
			catch (Exception e) 
			{
				mPicImageView.setVisibility(View.GONE);
//                Toast.makeText(mAct, "Picture file name is not found", Toast.LENGTH_SHORT).show();
		        Log.w("Picture file name is not found", e.toString());
		    }
			
			String strTitleEdit = mDb.getNoteTitleStringById(rowId);
            mTitleEditText.setText(strTitleEdit);
            mTitleEditText.setSelection(strTitleEdit.length());

            String strBodyEdit = mDb.getNoteBodyStringById(rowId);
            mBodyEditText.setText(strBodyEdit);
            mBodyEditText.setSelection(strBodyEdit.length());
        }
        mDb.close();
    }
    
    boolean isTitleModified()
    {
    	return !mOriginalTitle.equals(mTitleEditText.getText().toString());
    }
    
    boolean isPictureModified()
    {
    	return !mOriginalPictureFileName.equals(mPictureFileNameInDB);
    }
    
    boolean isBodyModified()
    {
    	return !mOriginalBody.equals(mBodyEditText.getText().toString());
    }
    
    boolean isTimeCreatedModified()
    {
    	return false; 
    }
    
    boolean isModified()
    {
    	boolean bModified = false;
    	if( isTitleModified() || isPictureModified() || isBodyModified() || bRemovePicFileName)
    	{
    		bModified = true;
    	}
    	
    	return bModified;
    }
    
    boolean isEdited()
    {
    	boolean bEdit = false;
    	String curTitle = mTitleEditText.getText().toString();
    	String curBody = mBodyEditText.getText().toString();
    	if(!curTitle.isEmpty() || !curBody.isEmpty() || !(null ==  mPictureFileNameInDB))    		
    		bEdit = true;
    	
    	return bEdit;
    }

	public Long saveStateInDB(Long rowId,boolean enSaveDb, String picFileName) {
		boolean mEnSaveDb = enSaveDb;
    	mDb.open();
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();

        if(mEnSaveDb)
        {
	        if (rowId == null) // for Add new
	        {
	        	if( (!title.isEmpty()) || (!body.isEmpty()) ||(!picFileName.isEmpty()))
	        	{
	        		// insert
	        		System.out.println("Note_common / saveState / insert");
	        		rowId = mDb.insert(title, picFileName, body, (long) 0);// add new note, get return row Id
	        	}
        		mCurrentPictureFileName = picFileName; // update file name

	        } 
	        else // for Edit
	        {
    	        Date now = new Date(); 
	        	if( (!title.isEmpty()) || (!body.isEmpty()) || (!picFileName.isEmpty()))
	        	{
	        		// update
	        		if(bRollBackData) //roll back
	        		{
			        	System.out.println("Note_common / saveState / update: roll back");
	        			title = mOriginalTitle;
	        			body = mOriginalBody;
	        			Long time = mOriginalCreatedTime;
	        			mDb.updateNote(rowId, title, picFileName, body, mOriginalMarking, time);
	        		}
	        		else // update new
	        		{
	        			mDb.updateNote(rowId, title, picFileName, body, 0, now.getTime()); // update note
	        		}
		        	
	        		mCurrentPictureFileName = picFileName; // update file name
	        	}
	        	else if(title.isEmpty() && body.isEmpty() && picFileName.isEmpty())
	        	{
	        		// delete
//	        		System.out.println("Note_common / saveState / delete");
	        		mDb.delete(rowId);
	        	}
	        }
        }
        mDb.close();
        
		return rowId;
	}
	
	public void removePictureStringFromOriginalNote(Long rowId) {
    	mDb.open();
    	mDb.updateNote(rowId, 
    				   mOriginalTitle,
    				   "", 
    				   mOriginalBody,
    				   mOriginalMarking,
    				   mOriginalCreatedTime );
        mDb.close();
	}
	
	public void removePictureStringFromCurrentEditNote(Long rowId) {
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();
        
    	mDb.open();
    	mDb.updateNote(rowId, 
    				   title,
    				   "", 
    				   body,
    				   mOriginalMarking,
    				   mOriginalCreatedTime );
        mDb.close();
	}
}