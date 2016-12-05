package com.cwc.litenote;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Switching between the tabs of a TabHost through fragments, using FragmentTabHost.
 */
public class TabsHost extends FragmentActivity 
{
    private FragmentTabHost mTabHost; 
    static int mTabCount;
	String TAB_SPEC_PREFIX = "tab";
	String TAB_SPEC;
	boolean bTabNameByDefault = true; 
	// for DB
	private static DB mDb;
	private static Cursor mTabCursor;
	
	private static SharedPreferences mPref_FinalPageViewed;
	private static SharedPreferences mPref_delete_warn;
	private static SharedPreferences mPref_show_note_attribute;
	private static int mFinalPageViewed_TabIndex;
	public static int mCurrentTabIndex;
	private ArrayList<String> mTabIndicator_ArrayList = new ArrayList<String>();
	private static Context mContext;
	private static int mFirstExist_TabId =0;
	private static int mLastExist_TabId =0;
	private static HorizontalScrollView mHorScrollView;
	private static Menu mMenu;
	private static Config mConfigFragment;
	private static int mShiftCount;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) 
    {
//		super.setTheme( android.R.style.Theme_Holo_Light_DarkActionBar);
		
        super.onCreate(savedInstanceState);
        System.out.println("================start application ==================");
        // set content view
        setContentView(R.layout.activity_main);
    	// set global context
    	mContext = this.getBaseContext();
    	
        // set tab host
        setTabHost();
        setTab();
    } 
	
	/**
	 * set tab host
	 * 
	 */
	protected void setTabHost()
	{
		// declare tab widget
        TabWidget tabWidget = (TabWidget) findViewById(android.R.id.tabs);
        
        // declare linear layout
        LinearLayout linearLayout = (LinearLayout) tabWidget.getParent();
        
        // set horizontal scroll view
        HorizontalScrollView horScrollView = new HorizontalScrollView(this);
        horScrollView.setLayoutParams(new FrameLayout.LayoutParams(
								            FrameLayout.LayoutParams.MATCH_PARENT,
								            FrameLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(horScrollView, 0);
        linearLayout.removeView(tabWidget);
        
        horScrollView.addView(tabWidget);
        horScrollView.setHorizontalScrollBarEnabled(true); //set scroll bar
        horScrollView.setHorizontalFadingEdgeEnabled(true); // set fading edge
        mHorScrollView = horScrollView;

		// tab host
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        
        //for android-support-v4.jar
        //mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent); 
        
        //add frame layout for android-support-v13.jar
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
	}
	
	void setTab()
	{
        //set tab indicator
    	setTabIndicator();
    	
    	// set tab listener
    	setTabChangeListener();
    	setTabEditListener();
	}
	
	/**
	 * set tab indicator
	 * 
	 */
	protected void setTabIndicator()
	{
        // get final viewed table Id
        mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
        String strFinalPageViewed_tableId = mPref_FinalPageViewed.getString("KEY_FINAL_PAGE_VIEWED","1");
        System.out.println("TabsHost / setTabIndicator / strFinalPageViewed_tableId = " + strFinalPageViewed_tableId);
		Context context = getApplicationContext();

		DB.setTableNumber(strFinalPageViewed_tableId);
		mDb = new DB(context);
		
		// insert when table is empty, activated only for the first time 
		mDb.doOpen();
		if(DB.getAllTabCount() == 0)
		{//???
			mDb.insertTab("TAB_INFO","N1",1,0);
			mDb.insertTab("TAB_INFO","N2",2,1); 
			mDb.insertTab("TAB_INFO","N3",3,2); 
			mDb.insertTab("TAB_INFO","N4",4,3); 
			mDb.insertTab("TAB_INFO","N5",5,4); 
//			mDb.insertTab("TAB_INFO","N6",6,5); 
//			mDb.insertTab("TAB_INFO","N7",7,6); 
//			mDb.insertTab("TAB_INFO","N8",8,7); 
//			mDb.insertTab("TAB_INFO","N9",9,8); 
//			mDb.insertTab("TAB_INFO","N10",10,9); 
		}
		mDb.doClose();
		
		mDb.doOpen();
		mTabCursor = DB.mTabCursor;
		mTabCount = DB.getAllTabCount();

		// get first tab id and last tab id
		int i = 0;
		while(i < mTabCount)
    	{
    		mTabIndicator_ArrayList.add(i,DB.getTabName(i));  
    		
			if(mTabCursor.isFirst())
			{
				mFirstExist_TabId = mDb.getTabId(i) ;
			}
			if(mTabCursor.isLast())
			{
				mLastExist_TabId = mDb.getTabId(i) ;
			}
			i++;
    	}
    	
		// get final view table id of last time
		for(int iPosition =0;iPosition<mTabCount;iPosition++)
		{
			if(Integer.valueOf(strFinalPageViewed_tableId) == 
					DB.getTabTableId(iPosition))
			{
				mFinalPageViewed_TabIndex = iPosition;	// starts from 0
			}
		}
		mDb.doClose();
		
	
    	//add tab
//        mTabHost.getTabWidget().setStripEnabled(true); // enable strip
        i = 0;
        while(i < mTabCount)
        {
        	mDb.doOpen();
            TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(mDb.getTabId(i)));
        	mDb.doClose();
            mTabHost.addTab(mTabHost.newTabSpec(TAB_SPEC)
									.setIndicator(mTabIndicator_ArrayList.get(i)),
							NoteFragment.class, //interconnection
							null);
            
            //set round corner and background color
            mDb.doOpen();
            int style = mDb.getTabStyle(i);
            mDb.doClose();
    		switch(style)
    		{
    			case 0:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_0);
    				break;
    			case 1:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_1);
    				break;
    			case 2:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_2);
    				break;
    			case 3:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_3);
    				break;
    			case 4:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_4);
    				break;	
    			case 5:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_5);
    				break;	
    			case 6:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_6);
    				break;	
    			case 7:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_7);
    				break;	
    			case 8:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_8);
    				break;		
    			case 9:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_9);
    				break;		
    			default:
    				break;
    		}
    		
    		
            //set text color
	        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
		    if((style%2) == 1)
    		{	
		        tv.setTextColor(Color.argb(255,0,0,0));
    		}
           	else
           	{
		        tv.setTextColor(Color.argb(255,255,255,255));
           	}
        	
            // set tab text center
	    	int tabCount = mTabHost.getTabWidget().getTabCount();
	    	for (int j = 0; j < tabCount; j++) {
	    	    final View view = mTabHost.getTabWidget().getChildTabViewAt(j);
	    	    if ( view != null ) {
	    	        //  get title text view
	    	        final View textView = view.findViewById(android.R.id.title);
	    	        if ( textView instanceof TextView ) {
	    	            ((TextView) textView).setGravity(Gravity.CENTER);
	    	            ((TextView) textView).setSingleLine(true);
	    	            ((TextView) textView).setPadding(6, 0, 6, 0);
	    	            ((TextView) textView).setMinimumWidth(96);
	    	            ((TextView) textView).setMaxWidth(Util.getScreenWidth(TabsHost.this)/2);
	    	            textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
	    	        }
	    	    }
	    	}
	    	i++;
        }
        
        setTabMargin();

		mCurrentTabIndex = mFinalPageViewed_TabIndex;
		
		//set background color to selected tab 
		mTabHost.setCurrentTab(mCurrentTabIndex); 
        
		// scroll to last view
        mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
		        mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
		        int scrollX = mPref_FinalPageViewed.getInt("KEY_FINAL_SCROLL_X", 0);
	        	mHorScrollView.scrollTo(scrollX, 0);
	        } 
	    });
	}
	

	void setTabMargin()
	{
    	if(Build.VERSION.SDK_INT >= 11)
    	{
    	    mTabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
        	mTabHost.getTabWidget().setDividerDrawable(R.drawable.ic_tab_divider);
    	}
    	
        TabWidget tabWidget = (TabWidget) findViewById(android.R.id.tabs);
        
        LinearLayout.LayoutParams tabWidgetLayout;
        for (int j = 0; j < mTabCount; j++) 
        {
        	tabWidgetLayout = (LinearLayout.LayoutParams) tabWidget.getChildAt(j).getLayoutParams();
        	int oriLeftMargin = tabWidgetLayout.leftMargin;
        	int oriRightMargin = tabWidgetLayout.rightMargin;
        	
        	// fix right edge been cut issue when single one note
        	if(mTabCount == 1)
        		oriRightMargin = 0;
        	
        	if (j == 0) {
        		tabWidgetLayout.setMargins(0, 2, oriRightMargin, 5);
        	} else if (j == (mTabCount - 1)) {
        		tabWidgetLayout.setMargins(oriLeftMargin, 2, 0, 5);
        	} else {
        		tabWidgetLayout.setMargins(oriLeftMargin, 2, oriRightMargin, 5);
        	}
        }
        tabWidget.requestLayout();
	}
	
	
	/**
	 * set tab change listener
	 * 
	 */
	@SuppressWarnings("deprecation")
	protected void setTabChangeListener()
	{
        // set on tab changed listener
	    mTabHost.setOnTabChangedListener(new OnTabChangeListener()
	    {
			@Override
			public void onTabChanged(String tabSpec)
			{
				// get scroll X
				int scrollX = mHorScrollView.getScrollX();
				
				//update final page currently viewed: scroll x
		        mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
				mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",scrollX).commit();
				
				mDb.doOpen();
				for(int i=0;i<mTabCount;i++)
				{
					int iTabId = mDb.getTabId(i);
					int iTabTableId = DB.getTabTableId(i);
					TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(iTabId)); // TAB_SPEC starts from 1
			    	
					if(TAB_SPEC.equals(tabSpec) )
			    	{
			    		mCurrentTabIndex = i;
			    		//update final page currently viewed: tab Id
		                mPref_FinalPageViewed.edit().putString("KEY_FINAL_PAGE_VIEWED",
		                		String.valueOf(iTabTableId)).commit();
				    	mTabHost.setCurrentTab(mCurrentTabIndex); 
			    		
			    		DB.setTableNumber(String.valueOf(iTabTableId));
				    	new NoteFragment();
			    	} 
				}
				mDb.doClose();
			}
		}
	    );    
	}
	
	/**
	 * set tab Edit listener
	 * 
	 */
	@SuppressWarnings("deprecation")
	protected void setTabEditListener()
	{
	    // set listener for editing tab info
	    int i = 0;
	    while(i < mTabCount)
		{
			final int tabCursor = i;
			View tabView= mTabHost.getTabWidget().getChildAt(i);
			
			// on long click listener
			tabView.setOnLongClickListener(new OnLongClickListener() 
	    	{	
				@Override
				public boolean onLongClick(View v) 
				{
					editTab(tabCursor);
					return true;
				}
			});
			i++;
		}
	}
	
	/**
	 * update change 
	 */
	void updateChange()
	{
   		mTabHost.clearAllTabs(); //must add this in order to clear onTanChange event

    	if(Build.VERSION.SDK_INT >= 11) 
    	{
    		setTab();
    	}
    	else 
    	{
    		finish();
    		Intent intent=new Intent(mContext,TabsHost.class);
    		startActivity(intent);
    	}
	}
	

	/**
	 * Menu
	 * 
	 */
    // Menu identifiers
    static final int ADD_NEW_NOTE = R.id.ADD_NEW_NOTE;
    static final int HANDLE_CHECKED_NOTES = R.id.HANDLE_CHECKED_NOTES;
    static final int CHECK_ALL = R.id.CHECK_ALL;
    static final int UNCHECK_ALL = R.id.UNCHECK_ALL;
    static final int MOVE_CHECKED_NOTE = R.id.MOVE_CHECKED_NOTE;
    static final int COPY_CHECKED_NOTE = R.id.COPY_CHECKED_NOTE;
    static final int MAIL_CHECKED_NOTE = R.id.MAIL_CHECKED_NOTE;
    static final int DELETE_CHECKED_NOTE = R.id.DELETE_CHECKED_NOTE;
    static final int ADD_NEW_PAGE = R.id.ADD_NEW_PAGE;
    static final int CHANGE_PAGE_COLOR = R.id.CHANGE_PAGE_COLOR;
    static final int SHIFT_PAGE = R.id.SHIFT_PAGE;
    static final int SHOW_BODY = R.id.SHOW_BODY;
    static final int SEND_PAGES = R.id.SEND_PAGES;
	static final int CONFIG_PREF = R.id.CONFIG_PREF;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(Build.VERSION.SDK_INT >= 11)
		{
		    menu.add(0, ADD_NEW_NOTE, 0, R.string.add_new_note )
		    .setIcon(R.drawable.ic_input_add)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else
		{	
			menu.add(0, ADD_NEW_NOTE, 1,  R.string.add_new_note)
		    .setIcon(R.drawable.ic_input_add);
		}	
		mMenu = menu;
		
		// set sub menu 0: handle checked note
	    SubMenu subMenu0 = menu.addSubMenu(0, 0, 1, R.string.checked_notes);//order starts from 0
	    
	    // add item
	    subMenu0.add(0, CHECK_ALL, 1, R.string.checked_notes_check_all)
        		.setIcon(R.drawable.btn_check_on_holo_dark);
	    subMenu0.add(0, UNCHECK_ALL, 2, R.string.checked_notes_uncheck_all)
				.setIcon(R.drawable.btn_check_off_holo_dark);
	    subMenu0.add(0, MOVE_CHECKED_NOTE, 3, R.string.checked_notes_move_to)
        		.setIcon(R.drawable.ic_menu_goto);	    
	    subMenu0.add(0, COPY_CHECKED_NOTE, 4, R.string.checked_notes_copy_to)
        		.setIcon(R.drawable.ic_menu_copy_holo_dark);
	    subMenu0.add(0, MAIL_CHECKED_NOTE, 5, R.string.config_mail_notes_btn)
        		.setIcon(android.R.drawable.ic_menu_send);
	    subMenu0.add(0, DELETE_CHECKED_NOTE, 6, R.string.checked_notes_delete)
        		.setIcon(R.drawable.ic_menu_clear_playlist);

	    // icon
	    MenuItem subMenuItem0 = subMenu0.getItem();
	    subMenuItem0.setIcon(R.drawable.ic_menu_mark);
	    
	    // set sub menu display
		if(Build.VERSION.SDK_INT >= 11)
			subMenuItem0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
					                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		// set sub menu 1: overflow
	    SubMenu subMenu1 = menu.addSubMenu(0, 0, 2, R.string.options);//order starts from 0
	    
	    // add item
	    subMenu1.add(0, ADD_NEW_PAGE, 1, R.string.add_new_page)
	            .setIcon(R.drawable.ic_menu_add_new_page);
	    
	    subMenu1.add(0, CHANGE_PAGE_COLOR, 2, R.string.change_page_color)
        	    .setIcon(R.drawable.ic_color_a);
	    
	    subMenu1.add(0, SHIFT_PAGE, 3, R.string.rearrange_page)
	            .setIcon(R.drawable.ic_menu_goto);
    	
	    mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
    	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
    		subMenu1.add(0, SHOW_BODY, 4, R.string.preview_note_body)
     	   		    .setIcon(R.drawable.ic_media_group_collapse);
    	else
    		subMenu1.add(0, SHOW_BODY, 4, R.string.preview_note_body)
        	        .setIcon(R.drawable.ic_media_group_expand);
    	
	    subMenu1.add(0, SEND_PAGES, 5, R.string.config_mail_notes_title)
 	   			.setIcon(android.R.drawable.ic_menu_send);
    	
	    subMenu1.add(0, CONFIG_PREF, 6, R.string.settings)
	    	   .setIcon(R.drawable.ic_menu_preferences);
	    
	    // set icon
	    MenuItem subMenuItem1 = subMenu1.getItem();
	    subMenuItem1.setIcon(R.drawable.ic_menu_moreoverflow);
	    
	    // set sub menu display
		if(Build.VERSION.SDK_INT >= 11)
			subMenuItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
					                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * on options item selected
	 * 
	 */
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	FragmentManager fragmentManager = null;
    	
    	if(Build.VERSION.SDK_INT >= 11 )
    		fragmentManager = getSupportFragmentManager();
    	
        switch (item.getItemId()) 
        {
            case ADD_NEW_PAGE:
                addNewPage(mLastExist_TabId + 1);
                return true;
                
            case CHANGE_PAGE_COLOR:
            	changePageColor();
                return true;    
                
            case SHIFT_PAGE:
            	mShiftCount = 0;
            	shiftPage();
                return true;  
                
            case SHOW_BODY:
            	mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
            	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
            		mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY","no").commit();
            	else
            		mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY","yes").commit();
            	updateChange();
                return true; 
                
            case SEND_PAGES:
				Intent intentSend = new Intent(TabsHost.this, SendMailAct.class);
				startActivity(intentSend);
				updateChange();
            	return true;

            case CONFIG_PREF:
            	mMenu.setGroupVisible(0, false); //hide the menu
            	if(Build.VERSION.SDK_INT >= 11 )
            	{
                	mConfigFragment = new Config();
	            	FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();
	            	mFragmentTransaction.add(android.R.id.content, mConfigFragment, "Config");
	            	mFragmentTransaction.addToBackStack(null); 
	            	mFragmentTransaction.commit();
	            	mFragmentTransaction.show(mConfigFragment);
            	}
            	else
            	{
            		finish();
    	        	Intent intent = new Intent(mContext, ConfigAct.class);
    	        	startActivity(intent);
            	}
                return true;
                
            case android.R.id.home:
            	if(Build.VERSION.SDK_INT >= 11)
            	{
            		getSupportFragmentManager().popBackStack();
            		mConfigFragment = null;
            		recreate();
            	}
            	else
            	{
            		Intent intent=new Intent(mContext,TabsHost.class);
            		startActivity(intent);
            		finish();
            	}
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    //back key event
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && 
        		mConfigFragment != null) 
        {
    		getSupportFragmentManager().popBackStack();
    		recreate();
    		mConfigFragment = null;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Add new page
     * @param newTabId
     */
	public  void addNewPage(final int newTabId) {
		// get tab name
		String tabName = "N".concat(String.valueOf(newTabId));
        
        final EditText editText1 = new EditText(getBaseContext());
        editText1.setText(tabName);
        editText1.setSelection(tabName.length()); // set edit text start position
        
        //update tab info
        Builder builder = new Builder(mTabHost.getContext());
        builder.setTitle(R.string.edit_page_tab_title)
                .setMessage(R.string.edit_page_tab_message)
                .setView(editText1)   
                .setNegativeButton(R.string.edit_page_button_ignore, new OnClickListener(){   
                	@Override
                    public void onClick(DialogInterface dialog, int which)
                    {/*nothing*/}
                })
                .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                		
	    	            final String[] items = new String[]{
	    	            		getResources().getText(R.string.add_new_page_leftmost).toString(),
	    	            		getResources().getText(R.string.add_new_page_rightmost).toString() };
	    	            
						AlertDialog.Builder builder = new AlertDialog.Builder(TabsHost.this);
						  
						builder.setTitle(R.string.add_new_page_select_position)
						.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
						
							if(which ==0)
								insertTabLeftmost(newTabId, editText1.getText().toString());
							else
								insertTabRightmost(newTabId, editText1.getText().toString());
							//end
							dialog.dismiss();
						}})
						.setNegativeButton(R.string.btn_Cancel, null)
						.show();
                    }
                })	 
                .setIcon(android.R.drawable.ic_menu_edit);
        
		        final AlertDialog d = builder.create();
		        d.show();
		        // android.R.id.button1 for negative: cancel 
		        ((Button)d.findViewById(android.R.id.button1))
		        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
		        // android.R.id.button2 for positive: save
		        ((Button)d.findViewById(android.R.id.button2))
		        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}
	
	void insertTabLeftmost(int newTabId,String tabName)
	{
 	    // insert tab name
		mDb.doOpen();
		int style = Util.getNewPageStyle(mContext);
		mDb.insertTab("TAB_INFO",tabName,newTabId,style );
		
		// insert table for new tab
		mDb.insertNewTable(newTabId);
		mTabCount++;
		mDb.doClose();
		
		//change to leftmost tab Id
		mDb.doOpen();
		int tabTotalCount = DB.getAllTabCount();
		mDb.doClose();
		for(int i=0;i <(tabTotalCount-1);i++)
		{
			int tabIndex = tabTotalCount -1 -i ;
			swapTabInfo(tabIndex,tabIndex-1);
			updateFinalPageViewed();
		}
		
        // set scroll X
		final int scrollX = 0; // leftmost
		
		// commit: scroll X
		mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",scrollX).commit();
		
		updateChange();
    	
        mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	        	mHorScrollView.scrollTo(scrollX, 0);
        		mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",scrollX).commit();
	        } 
	    });
	}
	
	protected void updateFinalPageViewed()
	{
        // get final viewed table Id
        mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
        String strFinalPageViewed_tableId = mPref_FinalPageViewed.getString("KEY_FINAL_PAGE_VIEWED","1");
        
		Context context = getApplicationContext();

		DB.setTableNumber(strFinalPageViewed_tableId);
		mDb = new DB(context);
		
		mDb.doOpen();
		// get final view tab index of last time
		for(int i =0;i<DB.getAllTabCount();i++)
		{
			if(Integer.valueOf(strFinalPageViewed_tableId) == DB.getTabTableId(i))
				mFinalPageViewed_TabIndex = i;	// starts from 0
			
        	if(	mDb.getTabId(i)== mFirstExist_TabId)
        	{
        		mPref_FinalPageViewed.edit()
				 			.putString("KEY_FINAL_PAGE_VIEWED",String.valueOf(DB.getTabTableId(i)))
				 			.commit();
        	}
		}
		mDb.doClose();
	}
	
	void insertTabRightmost(int newTabId,String tabName)
	{
 	    // insert tab name
		mDb.doOpen();
		int style = Util.getNewPageStyle(mContext);
		mDb.insertTab("TAB_INFO",tabName,newTabId,style );
		
		// insert table for new tab
		mDb.insertNewTable(newTabId);
		mTabCount++;
		mDb.doClose();
		
		// commit: final page viewed
        mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
        mPref_FinalPageViewed.edit().putString("KEY_FINAL_PAGE_VIEWED",
    		    String.valueOf(newTabId)).commit(); 
		
        // set scroll X
		final int scrollX = (mTabCount) * 60 * 5; //over the last scroll X
		
		// commit: scroll X
		mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",scrollX).commit();
		
		updateChange();
    	
        mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	        	mHorScrollView.scrollTo(scrollX, 0);
        		mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",scrollX).commit();
	        } 
	    });
	}
	
	
	void changePageColor()
	{
		// set color
		final AlertDialog.Builder builder = new AlertDialog.Builder(mTabHost.getContext());
		builder.setTitle(R.string.edit_page_background_color_title)
	    	   .setPositiveButton(R.string.edit_page_button_ignore, new OnClickListener(){   
	            	@Override
	                public void onClick(DialogInterface dialog, int which)
	                {/*cancel*/}
	            	});
		// inflate select style layout
		LayoutInflater mInflator= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflator.inflate(R.layout.select_style, null);
		RadioGroup RG_view = (RadioGroup)view.findViewById(R.id.radioGroup1);
		
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio0),0);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio1),1);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio2),2);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio3),3);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio4),4);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio5),5);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio6),6);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio7),7);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio8),8);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio9),9);
		
		builder.setView(view);
		
		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);
		    
		final AlertDialog dlg = builder.create();
	    dlg.show();
	    
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup RG, int id) {
				mStyle = RG.indexOfChild(RG.findViewById(id));
	 			mDb = new DB(mContext);
	 			mDb.doOpen();
	 			mDb.updateTabInfo(mDb.getTabId(mCurrentTabIndex),
	 							  DB.getTabName(mCurrentTabIndex),
	 							  DB.getTabTableId(mCurrentTabIndex),
	 							  mStyle );
	 			mDb.doClose();
	 			dlg.dismiss();
				updateChange();
		}});
	}

	
	/**
	 * delete page
	 * 
	 */
	public  void deletePage(int TabId) {
		
		// check if only one page left
		mDb.doOpen();
		if(mTabCursor.getCount() != 1)
		{
			final int tabId =  mDb.getTabId(mCurrentTabIndex);
			//if current page is the first page and will be delete,
			//try to get next existence of note page
	        if(tabId == mFirstExist_TabId)
	        {
	        	int cGetNextExistIndex = mCurrentTabIndex+1;
	        	boolean bGotNext = false;
				while(!bGotNext){
		        	try{
		        	   	mFirstExist_TabId =  mDb.getTabId(cGetNextExistIndex);
		        		bGotNext = true;
		        	}catch(Exception e){
    		        	 bGotNext = false;
    		        	 cGetNextExistIndex++;}}		            		        	
	        }
            
	        //change to first existing page
	        int tabTableId = 0;
	        for(int i=0;i<DB.getAllTabCount();i++)
	        {
	        	if(	mDb.getTabId(i)== mFirstExist_TabId)
	        		tabTableId =  DB.getTabTableId(i);
	        }
			mPref_FinalPageViewed.edit()
            					   .putString("KEY_FINAL_PAGE_VIEWED",String.valueOf(tabTableId))
            					   .commit();
		}
		else{
             Toast.makeText(TabsHost.this, R.string.toast_keep_one_page , Toast.LENGTH_SHORT).show();
             return;
		}
		
		// set scroll X
		int scrollX = 0; //over the last scroll X
        mPref_FinalPageViewed = getSharedPreferences("final_page_viewed", 0);
		mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",scrollX).commit();
	 	  
		mTabCursor = DB.mTabCursor;
		
		// drop table
		int iTabTableId = DB.getTabTableId(mCurrentTabIndex);

 	    // delete tab name
		mDb.dropTable(iTabTableId);
		mDb.deleteTabInfo("TAB_INFO",TabId);

		mTabCount--;
		mDb.doClose();
		updateChange();
    	
    	// Note: _onTabChanged will reset scroll X to another value,
    	// so we need to add the following to set scroll X again
        mHorScrollView.post(new Runnable() 
        {
	        @Override
	        public void run() {
	        	mHorScrollView.scrollTo(0, 0);
        		mPref_FinalPageViewed.edit().putInt("KEY_FINAL_SCROLL_X",0).commit();
	        }
	    });
	}

	/**
	 * edit tab 
	 * 
	 */
	int mStyle = 0;
	void editTab(int tabCursor)
	{
		mDb.doOpen();
		mTabCursor = DB.mTabCursor;
		final int tabId = mDb.getTabId(tabCursor);
		if(mTabCursor.isFirst())
			mFirstExist_TabId = tabId;
		
		if(tabCursor == mCurrentTabIndex )
		{
			// get tab name
			String tabName = DB.getTabName(tabCursor);
			mDb.doClose();
	        
	        final EditText editText1 = new EditText(getBaseContext());
	        editText1.setText(tabName);
	        editText1.setSelection(tabName.length()); // set edit text start position
	        //update tab info
	        Builder builder = new Builder(mTabHost.getContext());
	        builder.setTitle(R.string.edit_page_tab_title)
	                .setMessage(R.string.edit_page_tab_message)
	                .setView(editText1)   
	                .setNegativeButton(R.string.btn_Cancel, new OnClickListener()
	                {   @Override
	                    public void onClick(DialogInterface dialog, int which)
	                    {/*cancel*/}
	                })
	                .setNeutralButton(R.string.edit_page_button_delete, new OnClickListener()
	                {   @Override
	                    public void onClick(DialogInterface dialog, int which)
	                	{
	                		// delete
							//warning:start
		                	mPref_delete_warn = mContext.getSharedPreferences("delete_warn", 0);
		                	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
		 	                   mPref_delete_warn.getString("KEY_DELETE_PAGE_WARN","yes").equalsIgnoreCase("yes")) 
		                	{
		            			Util util = new Util(TabsHost.this);
		        				util.vibrate();
		        				
		                		Builder builder1 = new Builder(mTabHost.getContext()); 
		                		builder1.setTitle(R.string.confirm_dialog_title)
	                            .setMessage(R.string.confirm_dialog_message_page)
	                            .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener(){
	                            	@Override
	                                public void onClick(DialogInterface dialog1, int which1){
	                            		/*nothing to do*/}})
	                            .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener(){
	                            	@Override
	                                public void onClick(DialogInterface dialog1, int which1){
	                                	deletePage(tabId);
	                            	}})
	                            .show();
		                	} //warning:end
		                	else
		                	{
		                		deletePage(tabId);
		                	}
	                    }
	                })	
	                .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
	                {   @Override
	                    public void onClick(DialogInterface dialog, int which)
	                    {
	                		// save
	                	    mDb.doOpen();
        					final int tabId =  mDb.getTabId(mCurrentTabIndex);
        					final int tabTableId =  DB.getTabTableId(mCurrentTabIndex);
        					
	                        int tabStyle = mDb.getTabStyle(mCurrentTabIndex);
							mDb.updateTabInfo(tabId, editText1.getText().toString(),tabTableId, tabStyle );
	                        
							// Before _recreate, store latest page number currently viewed
	                        mPref_FinalPageViewed.edit().putString("KEY_FINAL_PAGE_VIEWED", String.valueOf(tabTableId)).commit();
	                        mDb.doClose();
	                        
	                        updateChange();
	                    }
	                })	
	                .setIcon(android.R.drawable.ic_menu_edit);
	        
			        AlertDialog d1 = builder.create();
			        d1.show();
			        // android.R.id.button1 for positive: save
			        ((Button)d1.findViewById(android.R.id.button1))
			        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
			        
			        // android.R.id.button2 for negative: color 
			        ((Button)d1.findViewById(android.R.id.button2))
  			        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
			        
			        // android.R.id.button3 for neutral: delete
			        ((Button)d1.findViewById(android.R.id.button3))
			        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
			}
	}
	
    /**
     * shift page right or left
     * 
     */
    void shiftPage()
    {
        Builder builder = new Builder(mTabHost.getContext());
        builder.setTitle(R.string.rearrange_page_title)
          	   .setMessage(null)
               .setNegativeButton(R.string.rearrange_page_left, null)
               .setNeutralButton(R.string.edit_note_button_back, null)
               .setPositiveButton(R.string.rearrange_page_right,null)
               .setIcon(R.drawable.ic_menu_goto);
        final AlertDialog d = builder.create();
        
        // disable dim background 
    	d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    	d.show();
    	
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
        {  @Override
           public void onClick(View v)
           {
        		//change to OK
        		Button mButton=(Button)d.findViewById(android.R.id.button3);
    	        mButton.setText(R.string.btn_Finish);
    	        mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);
    	        
        		mShiftCount--;
        		if(mShiftCount <= -2 )
        		{
        			mHorScrollView.scrollBy(-((mTabHost.getTabWidget().getChildAt(mCurrentTabIndex).getWidth())*2), 0);
        			mShiftCount=0;
        		}
				
        		d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        	    if(mCurrentTabIndex == 0)
        	    {
        	    	Toast.makeText(mTabHost.getContext(), R.string.toast_leftmost ,Toast.LENGTH_SHORT).show();
        	    	d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);//avoid long time toast
        	    }
        	    else
        	    {
        	    	mDb.doOpen();
                    mPref_FinalPageViewed.edit().putString("KEY_FINAL_PAGE_VIEWED",
							 String.valueOf(DB.getTabTableId(mCurrentTabIndex)))
							 					.commit();
                    mDb.doClose();
					swapTabInfo(mCurrentTabIndex,mCurrentTabIndex-1);
					updateChange();
        	    }
           }
        });
        
        d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
        {   @Override
           public void onClick(View v)
           {
               d.dismiss();
           }
        });
        
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {  @Override
           public void onClick(View v)
           {
        		d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        		
        		// middle button text: change to OK
	    		Button mButton=(Button)d.findViewById(android.R.id.button3);
		        mButton.setText(R.string.btn_Finish);
		        mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);
        	
	    		mShiftCount++;
	    		if(mShiftCount >= 2)
	    		{
	    			mHorScrollView.scrollBy( mTabHost.getTabWidget().getChildAt(mCurrentTabIndex).getWidth()*2, 0);
	    			mShiftCount=0;
	    		}
				
	    		d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
   	    		mDb.doOpen();
   	    		int count = DB.getAllTabCount();
                mDb.doClose();
   	    		
       	    	if(mCurrentTabIndex == (count-1))
       	    	{
       	    		// end of the right side
       	    		Toast.makeText(mTabHost.getContext(),R.string.toast_rightmost,Toast.LENGTH_SHORT).show();
       	    		d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);//avoid long time toast
       	    	}
       	    	else
       	    	{
                    mPref_FinalPageViewed.edit().putString("KEY_FINAL_PAGE_VIEWED",
							 String.valueOf(DB.getTabTableId(mCurrentTabIndex)))
							 					 .commit(); 
					swapTabInfo(mCurrentTabIndex,mCurrentTabIndex+1);
					updateChange();
       	    	}
           }
        });
        
        // android.R.id.button1 for positive: next 
        ((Button)d.findViewById(android.R.id.button1))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_forward, 0, 0, 0);
        // android.R.id.button2 for negative: previous
        ((Button)d.findViewById(android.R.id.button2))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        // android.R.id.button3 for neutral: cancel
        ((Button)d.findViewById(android.R.id.button3))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
    }
    
    /**
     * swap tab info
     * 
     */
    void swapTabInfo(int start, int end)
    {
		mDb.doOpen();
        mDb.updateTabInfo(mDb.getTabId(end),
        		DB.getTabName(start),
        		DB.getTabTableId(start),
        		mDb.getTabStyle(start));		        
		
		mDb.updateTabInfo(mDb.getTabId(start),
				DB.getTabName(end),
				DB.getTabTableId(end),
				mDb.getTabStyle(end));
    	mDb.doClose();
    }
    
	/**
	 * on destroy
	 */
	@Override
	protected void onDestroy() {
			mTabCursor.close();
			mDb.close();
		super.onDestroy();
	}
	
	static public int getLastTabId()
	{
		return mLastExist_TabId;
	}
	
	static public void setLastTabId(int lastTabId)
	{
		mLastExist_TabId = lastTabId;
	}
}