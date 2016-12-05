package com.cwc.litenote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Util {
    SharedPreferences mPref_vibration;
    Context mContext;
    Activity mActivity;
    String mEMailString;
    private static DB mDb;
    static String NEW_LINE = "\r" + System.getProperty("line.separator");

	static int STYLE_DEFAULT = 1;
    
	static int ACTIVITY_CREATE = 0;
    static int ACTIVITY_VIEW_NOTE = 1;
    static int ACTIVITY_EDIT_NOTE = 2;
    static int ACTIVITY_IMPORT = 3;
    
    int clrGgDefault;
    int clrTxtDefault;

    static boolean bShowExpandedImage = false;
    // style
    // 0,2,4,6,8: dark background, 1,3,5,7,9: light background
	static int[] mBG_ColorArray = new int[]{Color.rgb(34,34,34), //#222222
											Color.rgb(255,255,255),
											Color.rgb(38,87,51), //#265733
											Color.rgb(186,249,142),
											Color.rgb(87,38,51),//#572633
											Color.rgb(249,186,142),
											Color.rgb(38,51,87),//#263357
											Color.rgb(142,186,249),
											Color.rgb(87,87,51),//#575733
											Color.rgb(249,249,140)};
	static int[] mText_ColorArray = new int[]{Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0),
											  Color.rgb(255,255,255),
											  Color.rgb(0,0,0)};

    
    public Util(){};
    
	public Util(FragmentActivity activity) {
		mContext = activity;
		mActivity = activity;
	}
	
	public Util(Context context) {
		mContext = context;
	}
	
	// set vibration time
	void vibrate()
	{
		mPref_vibration = mContext.getSharedPreferences("vibration", 0);
    	if(mPref_vibration.getString("KEY_ENABLE_VIBRATION","yes").equalsIgnoreCase("yes"))
    	{
			Vibrator mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			if(mPref_vibration.getString("KEY_VIBRATION_TIME","25") != "")
			{
				int vibLen = Integer.valueOf(mPref_vibration.getString("KEY_VIBRATION_TIME","25"));
				mVibrator.vibrate(vibLen); //length unit is milliseconds
				System.out.println("vibration len = " + vibLen);
			}
    	}
	}
	
	// save to SD card: for checked pages
	String saveToSdCard(String filename, List<Boolean> checkedArr,boolean enableToast)
	{   
	    // SD card path + "/" + directory path
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + 
	    		              Util.getAppName(mContext);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		File file = new File(dir, filename);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e1) {
			System.out.println("_FileWriter error");
			e1.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		//first row text
		
		//get data from DB
		String data ="";
		if(checkedArr == null)
			data = queryDB(data,null);// all pages
		else
			data = queryDB(data,checkedArr);
		
		data = addRssVersionAndChannel(data);
		mEMailString = data;
		
		try {
			bw.write(data);
			bw.flush();
			bw.close();
			if(enableToast)
				Toast.makeText(mContext, R.string.config_export_SDCard_toast ,Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		return mEMailString;
	}
	
	// save to SD card: for NoteView class
	String saveStringToSdCard(String filename, String curString)
	{   
	    // SD card path + "/" + directory path
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + 
	    		              Util.getAppName(mContext);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		File file = new File(dir, filename);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e1) {
			System.out.println("_FileWriter error");
			e1.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		//sent data
		String data = "";
		data = data.concat(curString);
		
		mEMailString = data;
		
		try {
			bw.write(data);
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		return mEMailString;
	}
	
    /**
     * Query current data base
     * @param checkedArr 
     * 
     */
    String queryDB(String data, List<Boolean> checkedArr)
    {
    	String curData = data;
    	
		String strFinalPageViewed_tableId = Util.getPrefFinalPageTableId((Activity) mContext);
        DB.setTableNumber(strFinalPageViewed_tableId);
    	
    	mDb = new DB(mContext);
    	mDb.doOpen();
    	int tabCount = DB.getAllTabCount();
    	mDb.doClose();
    	for(int i=0;i<tabCount;i++)
    		
    	{
    		// null: all pages
        	if((checkedArr == null ) || ( checkedArr.get(i) == true  ))
    		{
	        	// set Sent string Id
				List<Long> rowArr = new ArrayList<Long>();
        		mDb.doOpen();
				DB.setTableNumber(String.valueOf(DB.getTabTableId(i)));
				mDb.doClose();
				
        		mDb.doOpen();
	    		for(int k=0;k<mDb.getAllCount();k++)
	    		{
    				rowArr.add(k,(long) mDb.getNoteId(k));
	    		}
	    		mDb.doClose();
	    		curData = curData.concat(getSendString(rowArr));
    		}
    	}
    	return curData;
    	
    }
    
    // get current time string
    static String getCurrentTimeString()
    {
		// set time
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONDAY)+ 1; //month starts from 0
		int date = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);//12h 
//		int hour = cal.get(Calendar.HOUR_OF_DAY);//24h
		String am_pm = (cal.get(Calendar.AM_PM)== 0) ?"AM":"PM"; // 0 AM, 1 PM
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String strTime = year 
				+ "-" + month
				+ "-" + date
				+ "_" + am_pm
				+ "-" + hour
				+ "-" + min
				+ "-" + sec ;
//		System.out.println("time = "+  strTime );
		return strTime;
    }
    
    // get time string
    static String getTimeString(Long time)
    {
		// set time
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONDAY)+ 1; //month starts from 0
		int date = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);//24h
//		int hour = cal.get(Calendar.HOUR);//12h 
//		String am_pm = (cal.get(Calendar.AM_PM)== 0) ?"AM":"PM"; // 0 AM, 1 PM
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String strTime = year 
				+ "/" + month
				+ "/" + date
//				+ "_" + am_pm
				+ " " + hour
				+ ":" + min
				+ ":" + sec ;
//		System.out.println("time = "+  strTime );
		return strTime;
    }
    
//    void deleteAttachment(String mAttachmentFileName)
//    {
//		// delete file after sending
//		String attachmentPath_FileName = Environment.getExternalStorageDirectory().getPath() + "/" +
//										 mAttachmentFileName;
//		File file = new File(attachmentPath_FileName);
//		boolean deleted = file.delete();
//		if(deleted)
//			System.out.println("delete file is OK");
//		else
//			System.out.println("delete file is NG");
//    }
    

	void markCurrent(DialogInterface alert)
	{
		mDb = new DB(mActivity);
	    ListView listView = ((AlertDialog) alert).getListView();
	    final ListAdapter originalAdapter = listView.getAdapter();
	    final int style = Util.getCurrentPageStyle(mActivity);
        TextView textViewDefault = new TextView(mActivity) ;
        clrGgDefault = textViewDefault.getDrawingCacheBackgroundColor();
        clrTxtDefault = textViewDefault.getCurrentTextColor();
		
	    listView.setAdapter(new ListAdapter()
	    {
	
	        @Override
	        public int getCount() {
	            return originalAdapter.getCount();
	        }
	
	        @Override
	        public Object getItem(int id) {
	            return originalAdapter.getItem(id);
	        }
	
	        @Override
	        public long getItemId(int id) {
	            return originalAdapter.getItemId(id);
	        }
	
	        @Override
	        public int getItemViewType(int id) {
	            return originalAdapter.getItemViewType(id);
	        }
	
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = originalAdapter.getView(position, convertView, parent);
	            TextView textView = (TextView)view;
				mDb.doOpen();
	            if(DB.getTabTableId(position) == Integer.valueOf(DB.getTableNumber()))
	            {
		            textView.setTypeface(null, Typeface.BOLD_ITALIC);
		            textView.setBackgroundColor(mBG_ColorArray[style]);
		            textView.setTextColor(mText_ColorArray[style]);
	            }
	            else
	            {
	            	textView.setTypeface(null, Typeface.NORMAL);
		            textView.setBackgroundColor(clrGgDefault);
		            textView.setTextColor(clrTxtDefault);
	            }
				mDb.doClose();
	            return view;
	        }

	        @Override
	        public int getViewTypeCount() {
	            return originalAdapter.getViewTypeCount();
	        }

	        @Override
	        public boolean hasStableIds() {
	            return originalAdapter.hasStableIds();
	        }
	
	        @Override
	        public boolean isEmpty() {
	            return originalAdapter.isEmpty();
	        }

	        @Override
	        public void registerDataSetObserver(DataSetObserver observer) {
	            originalAdapter.registerDataSetObserver(observer);
	
	        }
	
	        @Override
	        public void unregisterDataSetObserver(DataSetObserver observer) {
	            originalAdapter.unregisterDataSetObserver(observer);
	
	        }
	
	        @Override
	        public boolean areAllItemsEnabled() {
	            return originalAdapter.areAllItemsEnabled();
	        }
	
	        @Override
	        public boolean isEnabled(int position) {
	            return originalAdapter.isEnabled(position);
	        }
	    });
	}
	
	// get App name
	static public String getAppName(Context context)
	{
		return context.getResources().getString(R.string.app_name);
	}
	
	// get style
	static public int getNewPageStyle(Context context)
	{
		SharedPreferences mPref_style;
		mPref_style = context.getSharedPreferences("style", 0);
		return mPref_style.getInt("KEY_STYLE",STYLE_DEFAULT);
	}
	
	static String[] mItemArray = new String[]{"1","2","3","4","5","6","7","8","9","10"};
    public static void setButtonColor(RadioButton rBtn,int iBtnId)
    {
		rBtn.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		rBtn.setText(mItemArray[iBtnId]);
		rBtn.setTextColor(Util.mText_ColorArray[iBtnId]);
    }
	
	static public int getCurrentPageStyle(Context context)
	{
		int style = 0;
		mDb = new DB(context);
		mDb.doOpen();
		style = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
		mDb.doClose();
		
		return style;
	}
	
	static String getSendString(List<Long> rowArr)
	{
        String PAGE_TAG_B = "<page>";
        String TAB_TAG_B = "<tabname>";
        String TAB_TAG_E = "</tabname>";
        String TITLE_TAG_B = "<title>";
        String TITLE_TAG_E = "</title>";
        String BODY_TAG_B = "<body>";
        String BODY_TAG_E = "</body>";
        String PAGE_TAG_E = "</page>";
        
        String sentString = NEW_LINE;

    	// when page has tab name only, no notes
    	if(rowArr.size() == 0)
    	{
        	mDb.doOpen();
        	sentString = sentString.concat(NEW_LINE + PAGE_TAG_B );
	        sentString = sentString.concat(NEW_LINE + TAB_TAG_B + DB.getCurrentTabName() + TAB_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + TITLE_TAG_B + TITLE_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + BODY_TAG_B +  BODY_TAG_E);
	    	sentString = sentString.concat(NEW_LINE + PAGE_TAG_E );
    		sentString = sentString.concat(NEW_LINE);
    		mDb.doClose();
    	}
    	else
    	{
	        for(int i=0;i< rowArr.size();i++)
	        {
	        	mDb.doOpen();
		    	Cursor cursorNote = mDb.get(rowArr.get(i));
		    	
		        String strTitleEdit = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
		        
		        String strBodyEdit = cursorNote.getString(
		        		cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
		    	
		        int mark = cursorNote.getInt(cursorNote.getColumnIndexOrThrow(DB.KEY_NOTE_MARKING));
		        String srtMark = (mark == 1)? "[s]":"[n]";
		        
		        if(i==0)
		        {
		        	sentString = sentString.concat(NEW_LINE + PAGE_TAG_B );
		        	sentString = sentString.concat(NEW_LINE + TAB_TAG_B + DB.getCurrentTabName() + TAB_TAG_E );
		        }
		        
		        sentString = sentString.concat(NEW_LINE + TITLE_TAG_B + srtMark + strTitleEdit + TITLE_TAG_E);
		        sentString = sentString.concat(NEW_LINE + BODY_TAG_B + strBodyEdit + BODY_TAG_E);
		    	sentString = sentString.concat(NEW_LINE);
		    	if(i==rowArr.size()-1)
		        	sentString = sentString.concat(NEW_LINE +  PAGE_TAG_E);
		    		
		    	mDb.doClose();
	        }
    	}
    	return sentString;
	}
	
	// add RSS tag
	public static String addRssVersionAndChannel(String str)
	{
        String RSS_TAG_B = NEW_LINE + "<rss version=\"2.0\">";
        String RSS_TAG_E = "</rss>";
        String CHANNEL_TAG_B = "<channel>";
        String CHANNEL_TAG_E = "</channel>";
        
        String data = RSS_TAG_B + CHANNEL_TAG_B;
        data = data.concat(str);
		data = data.concat(CHANNEL_TAG_E + RSS_TAG_E);
		
		return data;
	}

	public String trimXML(String string) {
		string = string.replace("<rss version=\"2.0\">","");
		string = string.replace("<channel>","");
		string = string.replace("<page>","");
		string = string.replace("<tabname>","--- Page: ");
		string = string.replace("</tabname>"," ---");
		string = string.replace("<title>","Title: ");
		string = string.replace("</title>","");
		string = string.replace("<body>","Body: ");
		string = string.replace("</body>","");
		string = string.replace("[s]","");
		string = string.replace("[n]","");
		string = string.replace("</page>"," ");
		string = string.replace("</channel>","");
		string = string.replace("</rss>","");
		string = string.trim();
		return string;
	}
	
	public static int getScreenWidth(Activity activity)
	{
	    Display display = activity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.x;
		}
		else
		{
			return display.getWidth();
		}
	}
	
	public static int getScreenHeight(Activity activity)
	{
	    Display display = activity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.y;
		}
		else
		{
			return display.getHeight();
		}
	}
	
	public static String getPrefFinalPageTableId(Activity act)
	{
	       // get final viewed table Id
		SharedPreferences mPref_FinalPageViewed = act.getSharedPreferences("final_page_viewed", 0);
		String strFinalPageViewed_tableId = mPref_FinalPageViewed.getString("KEY_FINAL_PAGE_VIEWED","1");
		return strFinalPageViewed_tableId;
	}
	
	public static Uri getPictureUri(String pictureName, Activity act)
    {
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + Util.getAppName(act);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();
		

		File photo = new File(dir,  pictureName);
	    return Uri.fromFile(photo);
    }
	
	
    static Animator mCurrentAnimator;
    private static int mShortAnimationDuration;
	
    /**
     * "Zooms" in a thumbnail view by assigning the high resolution image to a hidden "zoomed-in"
     * image view and animating its bounds to fit the entire activity content area. More
     * specifically:
     *
     * <ol>
     *   <li>Assign the high-res image to the hidden "zoomed-in" (expanded) image view.</li>
     *   <li>Calculate the starting and ending bounds for the expanded view.</li>
     *   <li>Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
     *       simultaneously, from the starting bounds to the ending bounds.</li>
     *   <li>Zoom back out by running the reverse animation on click.</li>
     * </ol>
     *
     * @param thumbView  The thumbnail view to zoom in.
     * @param imageResId The high-resolution version of the image represented by the thumbnail.
     * @throws IOException 
     */
     static ImageView mExpandedImageView;
     static View mThumbView;
     static Rect mStartBounds;
     static float mStartScaleFinal;

     static void zoomImageFromThumb(final View thumbView, String picFileName, Activity mAct) throws IOException {
        // If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        mExpandedImageView = (ImageView) mAct.findViewById(R.id.expanded_image);
        mThumbView = thumbView;
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = mAct.getResources().getInteger(android.R.integer.config_shortAnimTime);

		Uri imageUri = Util.getPictureUri(picFileName, mAct);
        
		// set picture
		mExpandedImageView.setVisibility(View.VISIBLE );
		bShowExpandedImage = true;
		
		ExifInterface exif = new ExifInterface(imageUri.getPath());
		String len = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
		String wi = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
		
		mExpandedImageView.setImageBitmap(decodeSampledBitmapFromUri(imageUri,
										Integer.valueOf(len)/5,
										Integer.valueOf(wi)/5,
										mAct));
									
        // Calculate the starting and ending bounds for the zoomed-in image. This step
        // involves lots of math. Yay, math.
        mStartBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail, and the
        // final bounds are the global visible rectangle of the container view. Also
        // set the container view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        thumbView.getGlobalVisibleRect(mStartBounds);
        mAct.findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        mStartBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final bounds using the
        // "center crop" technique. This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) mStartBounds.width() / mStartBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) mStartBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - mStartBounds.width()) / 2;
            mStartBounds.left -= deltaWidth;
            mStartBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) mStartBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - mStartBounds.height()) / 2;
            mStartBounds.top -= deltaHeight;
            mStartBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation begins,
        // it will position the zoomed-in view in the place of the thumbnail.
        thumbView.setAlpha(0f);
        mExpandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
        // the zoomed-in view (the default is the center of the view).
        mExpandedImageView.setPivotX(0f);
        mExpandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mExpandedImageView, View.X, mStartBounds.left,
                        finalBounds.left))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.Y, mStartBounds.top,
                        finalBounds.top))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        mStartScaleFinal = startScale;
        mExpandedImageView.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View view) 
            {
            	closeExpandImage();
            }
        });
    }
     
    public static void closeExpandImage()
    {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Animate the four positioning/sizing properties in parallel, back to their
        // original values.
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mExpandedImageView, View.X, mStartBounds.left))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.Y, mStartBounds.top))
                .with(ObjectAnimator
                        .ofFloat(mExpandedImageView, View.SCALE_X, mStartScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(mExpandedImageView, View.SCALE_Y, mStartScaleFinal));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mThumbView.setAlpha(1f);
                mExpandedImageView.setVisibility(View.GONE);
            	bShowExpandedImage = false;
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mThumbView.setAlpha(1f);
                mExpandedImageView.setVisibility(View.GONE);
            	bShowExpandedImage = false;
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }
    
    public static Bitmap decodeSampledBitmapFromUri(Uri uri,
            int reqWidth, int reqHeight, Activity mAct) throws IOException 
    {
    	Bitmap thumbNail;
    	ContentResolver cr = mAct.getContentResolver();
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
		thumbNail = BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
		// scaled
		thumbNail = Bitmap.createScaledBitmap(thumbNail, reqWidth,reqHeight, true);
		// rotate bitmap
		thumbNail = Bitmap.createBitmap(thumbNail, 0, 0, reqWidth, reqHeight, getMatrix(uri), true);

		return thumbNail;
    }
    
    private static Matrix getMatrix(Uri imageUri) throws IOException
    {
		Matrix matrix = new Matrix();

		ExifInterface exif = new ExifInterface(imageUri.getPath());
		int rotSetting = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		int rotInDeg;
		
	    if (rotSetting == ExifInterface.ORIENTATION_ROTATE_90) 
	      rotInDeg = 90;  
	    else if(rotSetting == ExifInterface.ORIENTATION_ROTATE_180) 
	      rotInDeg = 180;  
	    else if (rotSetting == ExifInterface.ORIENTATION_ROTATE_270) 
	      rotInDeg = 270;
	    else
	      rotInDeg =0; 
		
		if (rotSetting != 0f) {matrix.preRotate(rotInDeg);}
    	
		return matrix;
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        
//        System.out.println("bitmap height = " + height);
//        System.out.println("bitmap width = " + width);
        
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        
        return inSampleSize;
    }

}
