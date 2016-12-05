package com.cwc.litenote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.cwc.litenote.lib.DragSortController;
import com.cwc.litenote.lib.DragSortListView;
import com.cwc.litenote.lib.SimpleDragSortCursorAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteFragment extends UilListViewBaseFragment 
						  implements LoaderManager.LoaderCallbacks<List<String>> 
{
	private static Cursor mNotesCursor;
	private static DB mDb;
    SharedPreferences mPref_delete_warn;
    SharedPreferences mPref_style;
	SharedPreferences mPref_show_note_attribute;
    private Long mNoteNumber1 = (long) 1;
	private String mNoteTitle1;
	private String mNotePictureString1;
	private String mNoteBodyString1;
	private int mMarkingIndex1;
	private Long mCreateTime1;
	private Long mNoteNumber2 ;
	private String mNotePictureString2;
	private String mNoteTitle2;
	private String mNoteBodyString2;
	private int mMarkingIndex2;
	private Long mCreateTime2;
	private List<Boolean> mSelectedList = new ArrayList<Boolean>();
	
	// This is the Adapter being used to display the list's data.
	NoteListAdapter mAdapter;
	DragSortListView mDndListView;
	ImageView mView2;
	private DragSortController mController;
	int MOVE_TO = 0;
	int COPY_TO = 1;
	int ADD_NEW_TO_TOP = 2;
    int mStyle = 0;
    int mCount;
    
    DisplayImageOptions options;
	public NoteFragment(){}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		  
		listView = (DragSortListView)getActivity().findViewById(R.id.list1);
		mDndListView = listView;
    	mDb = new DB(getActivity()); 
    	
    	mStyle = Util.getCurrentPageStyle(getActivity());

		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
//		.displayer(new RoundedBitmapDisplayer(20))
		.displayer(new RoundedBitmapDisplayer(10))
		.build();
		
    	//listener: view note 
    	mDndListView.setOnItemClickListener(new OnItemClickListener()
    	{   @Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
			{
	    		mDb.doOpen();
	    		mCount = mDb.getAllCount();
	    		mDb.doClose();
    		
				if(position < mCount) // avoid footer error
				{
					Intent i;
					if(Build.VERSION.SDK_INT >= 11)
						i = new Intent(getActivity(), Note_view_pager.class);//???
					else
						i = new Intent(getActivity(), Note_view.class);
			        i.putExtra("POSITION", position);
			        startActivity(i);
				}
			}
    	}
    	);
    	
    	// listener: edit note 
    	mDndListView.setOnItemLongClickListener(new OnItemLongClickListener()
    	{
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
             {	
		        Intent i = new Intent(getActivity(), Note_edit.class);
				mDb.doOpen();
				Long rowId = mDb.getNoteId(position);
		        i.putExtra(DB.KEY_NOTE_ID, mDb.getNoteId(position));
		        i.putExtra(DB.KEY_NOTE_TITLE, mDb.getNoteTitleStringById(rowId));
		        i.putExtra(DB.KEY_NOTE_PICTURE , mDb.getNotePictureStringById(rowId));
		        i.putExtra(DB.KEY_NOTE_BODY, mDb.getNoteBodyStringById(rowId));
		        i.putExtra(DB.KEY_NOTE_CREATED, mDb.getNoteCreatedTimeById(rowId));
				mDb.doClose();
		        startActivity(i);
            	return true;
             }
	    });
    	
        mController = buildController(mDndListView);
        mDndListView.setFloatViewManager(mController);
        mDndListView.setOnTouchListener(mController);
        mDndListView.setDragEnabled(true);
        
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new NoteListAdapter(getActivity());

		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(true); //set progress indicator

		// Prepare the loader. Either re-connect with an existing one or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}
	
    // list view listener: on drag
    private DragSortListView.DragListener onDrag = new DragSortListView.DragListener() 
    {
                @Override
                public void drag(int startPosition, int endPosition) {
                	//add highlight boarder
//                    View v = mDndListView.mFloatView;
//                    v.setBackgroundColor(Color.rgb(255,128,0));
//                	v.setBackgroundResource(R.drawable.listview_item_shape_dragging);
//                    v.setPadding(0, 4, 0,4);
                }
    };
	
    // list view listener: on drop
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() 
    {
        @Override
        public void drop(int startPosition, int endPosition) {
			mDb.doOpen();
			if(startPosition >= mDb.getAllCount()) // avoid footer error
				return;
	    	mDb.doClose();
			
			mSelectedList.set(startPosition, true);
			mSelectedList.set(endPosition, true);
			
			//reorder data base storage
			int loop = Math.abs(startPosition-endPosition);
			for(int i=0;i< loop;i++)
			{
				swapRows(startPosition,endPosition);
				if((startPosition-endPosition) >0)
					endPosition++;
				else
					endPosition--;
			}
			fillData();
        }
    };
	
    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv)
    {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        
        //drag
        controller.setDragHandleId(R.id.img_dragger);// handler
        controller.setDragInitMode(DragSortController.ON_DOWN); // click
//        controller.setDragInitMode(DragSortController.ON_LONG_PRESS); //long click to drag
        controller.setBackgroundColor(Color.argb(128,128,64,0));// background color when dragging
//        controller.setBackgroundColor(Util.mBG_ColorArray[mStyle]);// background color when dragging
        
        // mark
        controller.setMarkEnabled(true);
        controller.setClickMarkId(R.id.img_check);
        controller.setMarkMode(DragSortController.ON_DOWN);

        return controller;
    }        

	@Override
	public Loader<List<String>> onCreateLoader(int id, Bundle args) 
	{
		// This is called when a new Loader needs to be created. 
		return new NoteListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<String>> loader,
							   List<String> data) 
	{
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed()) 
			setListShown(true);
		else 
			setListShownNoAnimation(true);
		
		fillData();
	}

	@Override
	public void onLoaderReset(Loader<List<String>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}
	
    
	/**
	 * fill data
	 */
	
    public void fillData()
    {
    	
    	// save index and top position
        int index = mDndListView.getFirstVisiblePosition();
        View v = mDndListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        
        // set background color of list view
        mDndListView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);

    	//show divider color
        if(mStyle%2 == 0)
	    	mDndListView.setDivider(new ColorDrawable(0xFFffffff));//for dark
        else
           	mDndListView.setDivider(new ColorDrawable(0xff000000));//for light

        mDndListView.setDividerHeight(1);
        
    	mDb.doOpen();
    	mNotesCursor = mDb.mNotesCursor;
    	int count = mDb.getAllCount();
        mDb.doClose();
        // set adapter
        String[] from = new String[] { DB.KEY_NOTE_TITLE};
        int[] to = new int[] { R.id.text_whole };
        ((DragSortListView) mDndListView).setAdapter(new ItemAdapter(
        												getActivity(),
        												R.layout.activity_main_list_row,
        												mNotesCursor,
        												from,
        												to,
        												0
        												));
//        mDb.doClose();//??? mNotesCursor is not used
        
		// selected list
		for(int i=0; i< count ; i++ )
		{
			mSelectedList.add(true);
			mSelectedList.set(i,true);
		}
		
        // restore index and top position
        mDndListView.setSelectionFromTop(index, top);
        mDndListView.setDropListener(onDrop);
        mDndListView.setDragListener(onDrag);
        mDndListView.setMarkListener(onMark);
        
        setFooter();
    }
    
    // swap rows
	protected void swapRows(int startPosition, int endPosition) {

		mDb.doOpen();
		mDb.getAllCount();

		mNoteNumber1 = mDb.getNoteId(startPosition);
        mNoteTitle1 = mDb.getNoteTitle(startPosition);
        mNotePictureString1 = mDb.getNotePictureString(startPosition);
        mNoteBodyString1 = mDb.getNoteBodyString(startPosition);
        mMarkingIndex1 = mDb.getNoteMarking(startPosition);
    	mCreateTime1 = mDb.getNoteCreateTime(startPosition); 

		mNoteNumber2 = mDb.getNoteId(endPosition);
        mNoteTitle2 = mDb.getNoteTitle(endPosition);
        mNotePictureString2 = mDb.getNotePictureString(endPosition);
        mNoteBodyString2 = mDb.getNoteBodyString(endPosition);
        mMarkingIndex2 = mDb.getNoteMarking(endPosition);
    	mCreateTime2 = mDb.getNoteCreateTime(endPosition); 
		
        mDb.updateNote(mNoteNumber2,
				 mNoteTitle1,
				 mNotePictureString1,
				 mNoteBodyString1,
				 mMarkingIndex1,
				 mCreateTime1);		        
		
		mDb.updateNote(mNoteNumber1,
		 		 mNoteTitle2,
		 		 mNotePictureString2,
		 		 mNoteBodyString2,
		 		 mMarkingIndex2,
		 		 mCreateTime2);	
    	mDb.doClose();
	}

    // list view listener: on mark
    private DragSortListView.MarkListener onMark =
        new DragSortListView.MarkListener() 
		{   @Override
            public void mark(int position) 
			{
                mDb.doOpen();
                if(position >= mNotesCursor.getCount()) //end of list
                	return ;
                
                String strNote = mDb.getNoteTitle(position);
                String strNotePicture = mDb.getNotePictureString(position);
                String strNoteBody = mDb.getNoteBodyString(position);
                Long idNote =  mDb.getNoteId(position);
			
                DragSortListView parent = mDndListView;
                if(mDb.getNoteMarking(position) == 0)                
              	  mDb.updateNote(idNote, strNote, strNotePicture, strNoteBody , 1, 0);
                else
              	  mDb.updateNote(idNote, strNote, strNotePicture, strNoteBody ,0, 0);
                
                mDb.doClose();
              
                // save index and top position
                int index = parent.getFirstVisiblePosition();
                View v = parent.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
              
                fillData();
              
                // restore index and top position
                ((DragSortListView) parent).setSelectionFromTop(index, top);
                
                return;
            }
        };    
    
    // set footer
    void setFooter()
    {
	    mDb.doOpen();
	    TextView footerTextView = (TextView) getActivity().findViewById(R.id.footerText);
	    if(footerTextView != null) //add this for avoiding null exception when after e-Mail action
	    {
		    footerTextView.setText( mDb.getCheckedItemsCount() + 
		    		                " / " +
		    		                mDb.getAllCount() + 
		    		                " ( " +
		    		                getResources().getText(R.string.footer_checked).toString() + 
		    		                " / " +
		    		                getResources().getText(R.string.footer_total).toString() +
		       		                " ) " );
	    }
	    mDb.doClose();
    }
	
	class ItemAdapter extends SimpleDragSortCursorAdapter 
	{
		public ItemAdapter(Context context, int layout, Cursor c,
							String[] from, int[] to, int flags) 
		{
			super(context, layout, c, from, to, flags);
		}

		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		private class ViewHolder {
			public TextView textTitle;
			public TextView textBody;
			public TextView textTime;
			public ImageView imagePicture;
			public ImageView imageCheck;
		}

		@Override
		public int getCount() {
			mDb.doOpen();
			int count = mDb.getAllCount();
			mDb.doClose();
			return count;
			
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			if (convertView == null) 
			{
				view = getActivity().getLayoutInflater().inflate(R.layout.activity_main_list_row, parent, false);
				holder = new ViewHolder();
				holder.textTitle = (TextView) view.findViewById(R.id.text_title);
				holder.textBody = (TextView) view.findViewById(R.id.text_body);
				holder.textTime = (TextView) view.findViewById(R.id.text_time);
				holder.imageCheck= (ImageView) view.findViewById(R.id.img_check);
				holder.imagePicture = (ImageView) view.findViewById(R.id.img_picture);
				view.setTag(holder);
			} 
			else 
			{
				holder = (ViewHolder) view.getTag();
			}

			// file path, example: file:///sdcard/xxx/
			String path = "file://" + Environment.getExternalStorageDirectory().toString() + 
						  "/" + Util.getAppName(getActivity()) + "/";
			
			mDb.doOpen();
			// show check box, title , picture
			holder.textTitle.setText(mDb.getNoteTitle(position));
			holder.textTitle.setTextColor(Util.mText_ColorArray[mStyle]);   
//			holder.textTitle.setBackgroundColor(Util.mBG_ColorArray[mStyle]);   

			if( mDb.getNotePictureString(position).isEmpty() )
			{
				holder.imagePicture.setVisibility(View.GONE);
			}
			else
			{
				holder.imagePicture.setVisibility(View.VISIBLE);
				imageLoader.displayImage(path + mDb.getNotePictureString(position) ,
										holder.imagePicture, options, animateFirstListener);
			}
	        
			// show body or not
		  	mPref_show_note_attribute = getActivity().getSharedPreferences("show_note_attribute", 0);
		  	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
		  	{
				holder.textBody.setText(mDb.getNoteBodyString(position));
				holder.textBody.setTextColor(Util.mText_ColorArray[mStyle]);
//				holder.textBody.setBackgroundColor(Util.mBG_ColorArray[mStyle]);   
				holder.textTime.setText(Util.getTimeString(mDb.getNoteCreateTime(position)));
				holder.textTime.setTextColor(Util.mText_ColorArray[mStyle]);
//				holder.textTime.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		  	}
		  	else
		  	{
		  		holder.textBody.setVisibility(View.GONE); // 1 invisible
		  		holder.textTime.setVisibility(View.GONE); // 1 invisible
		  	}
			
			if( mDb.getNoteMarking(position) == 1)
				holder.imageCheck.setBackgroundResource(mStyle%2 == 1 ?
		    			R.drawable.btn_check_on_holo_light:
		    			R.drawable.btn_check_on_holo_dark);	
			else
				holder.imageCheck.setBackgroundResource(mStyle%2 == 1 ?
						R.drawable.btn_check_off_holo_light:
						R.drawable.btn_check_off_holo_dark);
			
			mDb.doClose();
			return view;
		}
	}

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener
	{

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) 
		{
			if (loadedImage != null) 
			{
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) 
				{
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	
	/*******************************************
	 * 					menu
	 *******************************************/
    // Menu identifiers
    static final int ADD_NEW_NOTE = R.id.ADD_NEW_NOTE;
    static final int CHECK_ALL = R.id.CHECK_ALL;
    static final int UNCHECK_ALL = R.id.UNCHECK_ALL;
    static final int MOVE_CHECKED_NOTE = R.id.MOVE_CHECKED_NOTE;
    static final int COPY_CHECKED_NOTE = R.id.COPY_CHECKED_NOTE;
    static final int MAIL_CHECKED_NOTE = R.id.MAIL_CHECKED_NOTE;
    static final int DELETE_CHECKED_NOTE = R.id.DELETE_CHECKED_NOTE;
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
	        case ADD_NEW_NOTE:
	            final Intent intent = new Intent(getActivity(), Note_addNew.class);
	            final String[] items = new String[]{
	            		getResources().getText(R.string.add_new_note_top).toString(),
	            		getResources().getText(R.string.add_new_note_bottom).toString() };
	            mDb.doOpen();
	            int noteCount = mDb.getAllCount();
	            mDb.doClose();
	            
	            if(noteCount > 0)
	            {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					  
					builder.setTitle(R.string.add_new_note_option_title)
					.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which) {
					
						if(which ==0)
							startActivityForResult(intent, ADD_NEW_TO_TOP);
						else
							startActivity(intent);
						
						dialog.dismiss();
					}})
					.setNegativeButton(R.string.btn_Cancel, null)
					.show();
	            }
	            else
	            	startActivity(intent);
	            
	            return true;
	        case CHECK_ALL:
	        	checkAll(1); 
	            return true;
	        case UNCHECK_ALL:
	        	checkAll(0); 
	            return true;
	        case MOVE_CHECKED_NOTE:
	        case COPY_CHECKED_NOTE:
	    		mDb.doOpen();
	        	if(mDb.getCheckedItemsCount() > 0)
	        	{
		    		String copyItems[] = new String[mDb.getCheckedItemsCount()];
		    		String copyItemsPicture[] = new String[mDb.getCheckedItemsCount()];
		    		String copyItemsBody[] = new String[mDb.getCheckedItemsCount()];
		    		Long copyItemsTime[] = new Long[mDb.getCheckedItemsCount()];
		    		int cCopy = 0;
		    		for(int i=0;i<mDb.getAllCount();i++)
		    		{
		    			if(mDb.getNoteMarking(i) == 1)
		    			{
		    				copyItems[cCopy] = mDb.getNoteTitle(i);
		    				copyItemsPicture[cCopy] = mDb.getNotePictureString(i);
		    				copyItemsBody[cCopy] = mDb.getNoteBodyString(i);
		    				copyItemsTime[cCopy] = mDb.getNoteCreateTime(i);
		    				cCopy++;
		    			}
		    		}
		    		mDb.doClose();
		           
		    		if(item.getItemId() == MOVE_CHECKED_NOTE)
		    			operateCheckedTo(copyItems, copyItemsPicture, copyItemsBody, copyItemsTime, MOVE_TO); // move to
		    		else if(item.getItemId() == COPY_CHECKED_NOTE)
			    		operateCheckedTo(copyItems, copyItemsPicture, copyItemsBody, copyItemsTime, COPY_TO);// copy to
		    			
	        	}
	        	else
	        	{
	        		mDb.doClose();
	    			Toast.makeText(getActivity(),
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT)
					 .show();
	        	}
	            return true;
	        case MAIL_CHECKED_NOTE:
	    		mDb.doOpen();
	        	if(mDb.getCheckedItemsCount() > 0)
	        	{
		        	// set Sent string Id
					List<Long> rowArr = new ArrayList<Long>();
	            	int j=0;
		    		for(int i=0;i<mDb.getAllCount();i++)
		    		{
		    			if(mDb.getNoteMarking(i) == 1)
		    			{
		    				rowArr.add(j,(long) mDb.getNoteId(i));
		    				j++;
		    			}
		    		}
		    		mDb.doClose();
		    		
		    		// send
		    		Intent intentMail = new Intent(getActivity(), SendMailAct.class);
		    		String extraStr = Util.getSendString(rowArr);
		    		extraStr = Util.addRssVersionAndChannel(extraStr);
		    		intentMail.putExtra("SentString", extraStr);
					startActivity(intentMail);
	        	}
	        	else
	        	{
	        		mDb.doClose();
	    			Toast.makeText(getActivity(),
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT)
					 .show();
	        	}
	        	return true;
	        case DELETE_CHECKED_NOTE:
	        	mDb.doOpen();
	        	if(mDb.getCheckedItemsCount() > 0)
	        	{
	        		mDb.doClose();
	        		deleteCheckedNotes();
	        	}
	        	else
	        	{
	        		mDb.doClose();
	    			Toast.makeText(getActivity(),
	    						   R.string.delete_checked_no_checked_items,
	    						   Toast.LENGTH_SHORT)
	    				 .show();
	        	}
	            return true;     
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        if(requestCode==ADD_NEW_TO_TOP)
        {
			if (resultCode ==  Activity.RESULT_OK)
			{
	            mDb.doOpen();
	            int startCursor = mDb.getAllCount()-1;
	            mDb.doClose();
	            int endCursor = 0;
				
				//reorder data base storage
				int loop = Math.abs(startCursor-endCursor);
				for(int i=0;i< loop;i++)
				{
					swapRows(startCursor,endCursor);
					if((startCursor-endCursor) >0)
						endCursor++;
					else
						endCursor--;
				}
			}
			fillData();
        }   
	}
    
	/**
	 *  check all or uncheck all
	 */
	public void checkAll(int action) 
	{
		mDb.doOpen();
		for(int i=0;i<mDb.getAllCount();i++)
		{
			Long rowId = mDb.getNoteId(i);
			String noteTitle = mDb.getNoteTitle(i);
			String notePicture = mDb.getNotePictureString(i);
			String noteBody = mDb.getNoteBodyString(i);
			mDb.updateNote(rowId, noteTitle, notePicture, noteBody , action, 0);// action 1:check all, 0:uncheck all
		}
		mDb.doClose();
		fillData();
	}
	
    /**
     *   operate checked to: move to, copy to
     * 
     */
	void operateCheckedTo(final String[] copyItems,final String[] copyItemsPicture, final String[] copyItemsBody, final Long[] copyItemsTime, final int action)
	{
		//list all tabs
		mDb.doOpen();
		int tabCount = DB.getAllTabCount();
		final String[] tabNames = new String[tabCount];
		final int[] tableIds = new int[tabCount];
		for(int i=0;i<tabCount;i++)
		{
			tabNames[i] = DB.getTabName(i);
			tableIds[i] = DB.getTabTableId(i);
		}
		tabNames[TabsHost.mCurrentTabIndex] = tabNames[TabsHost.mCurrentTabIndex] + " *"; // add mark to current page 
		mDb.doClose();
		   
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//keep original table id
				String curTableNum = DB.getTableNumber();

				//copy checked item to destination tab
				String destTableNum = String.valueOf(tableIds[which]);
				DB.setTableNumber(destTableNum);
				mDb.doOpen();
				for(int i=0;i< copyItems.length;i++)
				{
					mDb.insert(copyItems[i],copyItemsPicture[i], copyItemsBody[i],copyItemsTime[i]);
				}
				mDb.doClose();
				
				//recover to original table id
				if(action == MOVE_TO)
				{
					DB.setTableNumber(curTableNum);
					mDb.doOpen();
					//delete checked
					for(int i=0;i< mDb.getAllCount() ;i++)
					{
						if(mDb.getNoteMarking(i) == 1)
							mDb.delete(mDb.getNoteId(i));
					}
					mDb.doClose();
					fillData();
				}
				else if(action == COPY_TO)
				{
					DB.setTableNumber(curTableNum);
					if(destTableNum.equalsIgnoreCase(curTableNum))
						fillData();
				}
				
				dialog.dismiss();
			}
		};
		
		if(action == MOVE_TO)
			builder.setTitle(R.string.checked_notes_move_to_dlg);
		else if(action == COPY_TO)
			builder.setTitle(R.string.checked_notes_copy_to_dlg);
		
		builder.setSingleChoiceItems(tabNames, -1, listener)
		  	.setNegativeButton(R.string.btn_Cancel, null);
		
		// override onShow to mark current page status
		AlertDialog alert = builder.create();
		alert.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface alert) {
				// add mark for current page
				Util util = new Util(getActivity());
				util.markCurrent(alert);
			}
		});
		alert.show();
	}
	
	/**
	 * delete checked notes
	 */
	public void deleteCheckedNotes()
	{
		final Context context = getActivity();

		mPref_delete_warn = context.getSharedPreferences("delete_warn", 0);
    	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
           mPref_delete_warn.getString("KEY_DELETE_CHECKED_WARN","yes").equalsIgnoreCase("yes"))
    	{
			Util util = new Util(getActivity());
			util.vibrate();
    		
    		// show warning dialog
			Builder builder = new Builder(context);
			builder.setTitle(R.string.delete_checked_note_title)
					.setMessage(R.string.delete_checked_message)
					.setNegativeButton(R.string.btn_Cancel, 
							new OnClickListener() 
					{	@Override
						public void onClick(DialogInterface dialog, int which) 
						{/*cancel*/} })
					.setPositiveButton(R.string.btn_OK, 
							new OnClickListener() 
					{	@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							mDb.doOpen();
							for(int i=0;i< mDb.getAllCount() ;i++)
							{
								if(mDb.getNoteMarking(i) == 1)
									mDb.delete(mDb.getNoteId(i));
							}
							mDb.doClose();
							fillData();
						}
					});
			
	        AlertDialog d = builder.create();
	        d.show();
    	}
    	else
    	{
    		// not show warning dialog
			mDb.doOpen();
			for(int i=0;i< mDb.getAllCount() ;i++)
			{
				if(mDb.getNoteMarking(i) == 1)
					mDb.delete(mDb.getNoteId(i));
			}
			mDb.doClose();
			fillData();
    	}
	}
    
	@Override
	public void onDestroy() {
		mDb.doClose();
		super.onDestroy();
	}
	
	
	/*
	 * inner class for note list loader
	 */
	public static class NoteListLoader extends AsyncTaskLoader<List<String>> 
	{
		List<String> mApps;

		public NoteListLoader(Context context) {
			super(context);
		}

		@Override
		public List<String> loadInBackground() {
			List<String> entries = new ArrayList<String>();
			return entries;
		}

		@Override
		protected void onStartLoading() {
			System.out.println("_onStartLoading");
//			mDb.doOpen();
			forceLoad();
//			mDb.doClose();
		}
	}

	/*
	 * 	inner class for note list adapter
	 */
	public static class NoteListAdapter extends ArrayAdapter<String> 
	{
		public NoteListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}
		public void setData(List<String> data) {
			clear();
			if (data != null) {		
				if(Build.VERSION.SDK_INT >= 11)
					addAll(data);
			}
		}
	}
}
