package com.cwc.litenote;


import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Note_view_pager extends UilBaseFragment 
//								 implements OnTouchListener
{
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    // DB
    DB mDb;
    Long mRowId;
    int mEntryPosition;
    int EDIT_VIEW = 5;
    int MAIL_VIEW = 6;
    int mStyle;
    
    ImageLoader imageLoader;
    DisplayImageOptions options;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_view_slide);

		if(Build.VERSION.SDK_INT >= 11)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		imageLoader = ImageLoader.getInstance();
		
		options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();
		
        // DB
		String strFinalPageViewed_tableId = Util.getPrefFinalPageTableId(Note_view_pager.this);
        DB.setTableNumber(strFinalPageViewed_tableId);
        mDb = new DB(Note_view_pager.this);
        
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        
        // set current selection
        mEntryPosition = getIntent().getExtras().getInt("POSITION");
        
        mPager.setCurrentItem(mEntryPosition);
        mDb.doOpen();
        mRowId = mDb.getNoteId(mEntryPosition);
        mStyle = mDb.getTabStyle(TabsHost.mCurrentTabIndex);
        mDb.doClose();

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
//                mPager.getAdapter().notifyDataSetChanged();///
            }
        });
        
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
                
		        Intent i = new Intent(Note_view_pager.this, Note_edit.class);
		        
				mDb.doOpen();
		        i.putExtra(DB.KEY_NOTE_ID, mRowId);
		        i.putExtra(DB.KEY_NOTE_TITLE, mDb.getNoteTitleStringById(mRowId));
		        i.putExtra(DB.KEY_NOTE_PICTURE , mDb.getNotePictureStringById(mRowId));
		        i.putExtra(DB.KEY_NOTE_BODY, mDb.getNoteBodyStringById(mRowId));
		        i.putExtra(DB.KEY_NOTE_CREATED, mDb.getNoteCreatedTimeById(mRowId));
				mDb.doClose();
		        
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
				Intent intent = new Intent(Note_view_pager.this, SendMailAct.class);
		        String extraStr = Util.getSendString(rowArr);
		        extraStr = Util.addRssVersionAndChannel(extraStr);
		        intent.putExtra("SentString", extraStr);
				startActivityForResult(intent, MAIL_VIEW);
            }

        });
        
        //cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        if((requestCode==EDIT_VIEW) || (requestCode==MAIL_VIEW))
        {
        	finish();
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_screen_slide, menu);
        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
        
        // update row Id
        mDb.doOpen();
        mRowId = mDb.getNoteId(mPager.getCurrentItem());
        mDb.doClose();
        
        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                        ? R.string.slide_action_finish
                        : R.string.slide_action_next);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        // set Gray for Last item
        if((mPager.getCurrentItem() == mPagerAdapter.getCount() - 1))
        	menu.findItem(R.id.action_next).setEnabled( false );
        
        return true;
    }

    // for menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launch pad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, TabsHost.class));
                return true;

            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
            	mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//            	mPager.getAdapter().notifyDataSetChanged();///
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
            	mPager.setCurrentItem(mPager.getCurrentItem() + 1);
//            	mPager.getAdapter().notifyDataSetChanged();///
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
//    static String mStrTitle;
//    static String mStrBody;
//    static String mStrTime;


    /**
     * A simple pager adapter 
     */
    private class ScreenSlidePagerAdapter extends PagerAdapter 
//	 			                          implements OnTouchListener

    {
		private LayoutInflater inflater;

        public ScreenSlidePagerAdapter(FragmentManager fm) 
        {
            inflater = getLayoutInflater();
        }
        
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

        @Override
		public Object instantiateItem(ViewGroup view, int position) 
        {
        	System.out.println("Note_view_slide / instantiateItem / position = " + position);
            // Inflate the layout containing 
        	// 1. text group: title, body, time 
        	// 2. picture
        	
        	// text group
//		    TextView mTxtVw_Title, mTxtVw_Body, mTxtVw_Time;
		    TextView mTxtVw_Title, mTxtVw_Time;
		    WebView mTxtVw_Body ;
		    View layoutView;
        	layoutView = (ViewGroup) inflater.inflate(R.layout.note_view_slide_pager, view, false);
            layoutView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);

            ViewGroup textGroup = (ViewGroup) layoutView.findViewById(R.id.textGroup);
//            mTxtVw_Title = ((TextView) textGroup.findViewById(R.id.textTitle));
//            mTxtVw_Body = ((TextView) textGroup.findViewById(R.id.textBody));
            mTxtVw_Body = ((WebView) textGroup.findViewById(R.id.textBody));
//            mTxtVw_Time = ((TextView) textGroup.findViewById(R.id.textTime));

        	mDb.doOpen();
        	String strPicture = mDb.getNotePictureString(position);
        	String strTitle = mDb.getNoteTitle(position);
        	String strBody = mDb.getNoteBodyString(position);
        	Long createTime = mDb.getNoteCreateTime(position);
        	mDb.doClose();
        	
//        	mTxtVw_Title.setText(strTitle);
//        	mTxtVw_Body.setText(strBody);
        	///
        	String content = 
        		       "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
        		       "<html><head>"+
        		       "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"+
        		       "<head><body>";

        		content += strBody + "</body></html>";

//        		WebView WebView1 = (WebView) findViewById(R.id.webView1);
        		mTxtVw_Body.loadData(content, "text/html; charset=utf-8", "UTF-8");
        		mTxtVw_Body.getSettings().setBuiltInZoomControls(true);
        	///
        	
//        	mTxtVw_Time.setText(Util.getTimeString(createTime));
        	
//			mTxtVw_Title.setTextColor(Util.mText_ColorArray[mStyle]);
//			mTxtVw_Body.setTextColor(Util.mText_ColorArray[mStyle]);
//			mTxtVw_Time.setTextColor(Util.mText_ColorArray[mStyle]);

//			// set gesture detector
//			mScaleGestureDetector = new ScaleGestureDetector(Note_view_pager.this, new OnScaleGestureListener());
//	        
//			// set tag for findViewByTag in _onScale
//			textGroup.setTag(String.valueOf(position));
//			textGroup.setOnTouchListener(new OnTouchListener()
//	        {
//
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					mScaleGestureDetector.onTouchEvent(event);
//					return true;
//				}
//	        	
//	        });
	        
			// set picture
        	TouchImageView pictureView = new TouchImageView(view.getContext());
			pictureView = ((TouchImageView) layoutView.findViewById(R.id.img_picture));
			final ProgressBar spinner = (ProgressBar) layoutView.findViewById(R.id.loading);
			showPictureByTouchImageView(spinner, pictureView, strPicture);
			
        	view.addView(layoutView, 0);
			return layoutView;			

//			view.addView(mTxtVw_Body, 0);
//			return mTxtVw_Body;			
        }

        // show picture or not
        private void showPictureByTouchImageView(final View spinner, TouchImageView pictureView, String strPicture) 
        {
        	
        	if(strPicture.isEmpty())
        	{
        		pictureView.setVisibility(View.GONE);
        	}
        	else
        	{
	        	Uri imageUri = Util.getPictureUri(strPicture, Note_view_pager.this);
	        	imageLoader.displayImage(imageUri.toString(), 
										 pictureView,
										 options,
										 new SimpleImageLoadingListener()
				{
					@Override
					public void onLoadingStarted(String imageUri, View view) 
					{
						spinner.setVisibility(View.VISIBLE);
						view.setVisibility(View.GONE);
					}
	
					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
					{
						String message = null;
						switch (failReason.getType()) 
						{
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
						}
						Toast.makeText(Note_view_pager.this, message, Toast.LENGTH_SHORT).show();
						spinner.setVisibility(View.GONE);
					}
	
					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
					{
						spinner.setVisibility(View.GONE);
						view.setVisibility(View.VISIBLE);
					}
				});
        	}
		}
        
		@Override
        public int getCount() 
        {
        	mDb.doOpen();
        	int count = mDb.getAllCount();
        	mDb.doClose();
        	return count;
        }

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		ScaleGestureDetector mScaleGestureDetector;
	    public class OnScaleGestureListener extends SimpleOnScaleGestureListener 
	    {	
	    	@Override
			public boolean onScale(ScaleGestureDetector detector) {
	    		
	    	    TextView mTxtVw_Title;
	    	    TextView mTxtVw_Body;
	    	    TextView mTxtVw_Time;
	    	    
	    	    String posStr = String.valueOf(mPager.getCurrentItem() );
	    	    System.out.println("onScale / posStr = " + posStr );
	    	    ViewGroup textGroup = (ViewGroup) mPager.findViewWithTag(posStr);
	        	
//				mTxtVw_Title =((TextView)textGroup.findViewById(R.id.textTitle));
//				mTxtVw_Body = ((TextView)textGroup.findViewById(R.id.textBody));
				mTxtVw_Time = ((TextView)textGroup.findViewById(R.id.textTime));
	    	    
				mDb.doOpen();
	        	String strTitle = mDb.getNoteTitle(mPager.getCurrentItem());
//	        	String strBody = mDb.getNoteBodyString(mPager.getCurrentItem());
	        	Long createTime = mDb.getNoteCreateTime(mPager.getCurrentItem());
	        	mDb.doClose();
	        	
//	        	mTxtVw_Title.setText(strTitle);
//	        	mTxtVw_Body.setText(strBody);
	        	mTxtVw_Time.setText(Util.getTimeString(createTime));

//			    float sizeTitle = mTxtVw_Title.getTextSize();
//			    float sizeBody = mTxtVw_Body.getTextSize();
			    float sizeTime = mTxtVw_Time.getTextSize();
//			    Log.d("TextSizeStart", String.valueOf(sizeBody));
			
			    float factor = detector.getScaleFactor();
			    Log.d("Factor", String.valueOf(factor));
			
//			    float productTitle = sizeTitle*factor;
//			    float productBody = sizeBody*factor;
			    float productTime = sizeTime*factor;
			    
//			    productTitle = Math.min(productTitle, MAX_SIZE);
//			    productTitle = Math.max(productTitle, MIN_SIZE);
//			    productBody = Math.min(productBody, MAX_SIZE);
//			    productBody = Math.max(productBody, MIN_SIZE);
			    productTime = Math.min(productTime, MAX_SIZE);
			    productTime = Math.max(productTime, MIN_SIZE);
			    
//			    Log.d("TextSize", String.valueOf(productBody));
//			    mTxtVw_Title.setTextSize(TypedValue.COMPLEX_UNIT_PX, productTitle);
//			    mTxtVw_Body.setTextSize(TypedValue.COMPLEX_UNIT_PX, productBody);
			    mTxtVw_Time.setTextSize(TypedValue.COMPLEX_UNIT_PX, productTime);
			
//			    sizeTitle = mTxtVw_Title.getTextSize();
//			    sizeBody = mTxtVw_Body.getTextSize();
			    sizeTime = mTxtVw_Time.getTextSize();
			    
//			    Log.d("TextSizeEnd", String.valueOf(sizeBody));
			    return true;
			}
		}

    }
	int MAX_SIZE = 719;
	int MIN_SIZE = 24;
}

abstract class UilBaseFragment extends FragmentActivity 
{
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) {
			default:
				return false;
		}
	}
}