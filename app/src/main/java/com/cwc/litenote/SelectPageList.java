package com.cwc.litenote;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectPageList {
	View mView;
	CheckedTextView mCheckTvSelAll;
    ListView mListView;
    List<String> mListStrArr; // list view string array
    List<Boolean> mCheckedArr; // checked list view items array
    private static DB mDb;
    int COUNT;
//    int mStyle;
    Activity mActivity;
    
	public SelectPageList(Activity act, View view) 
	{
		mActivity = act;
		
		// checked Text View: select all 
		mCheckTvSelAll = (CheckedTextView) mActivity.findViewById(R.id.chkSelectAllPages);
		mCheckTvSelAll.setOnClickListener(new OnClickListener()
		{	@Override
			public void onClick(View checkSelAll) 
			{
				boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
				((CheckedTextView)checkSelAll).setChecked(!currentCheck);
				
				if(((CheckedTextView)checkSelAll).isChecked())
					selectAllPages(true);
				else
					selectAllPages(false);
			}
		});
//		mStyle = Util.getCurrentPageStyle(mActivity);
		
		// list view: selecting which pages to send 
		mListView = (ListView)view;
		listForSelect();
    }
    
	// select all pages
	void selectAllPages(boolean enAll) {
		mChkNum = 0;
        mDb.doOpen();
        COUNT = DB.getAllTabCount();

        for(int i=0;i<COUNT;i++)
        {
	         CheckedTextView chkTV = (CheckedTextView) mListView.findViewById(R.id.checkTV);
	         chkTV.setChecked(enAll);
	         mCheckedArr.set(i, enAll);
             mListStrArr.set(i,DB.getTabName(i));
        }
        mDb.doClose();
        
    	mChkNum = (enAll == true)? COUNT : 0;
        
        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mActivity, mListStrArr);
        
        // list view: set adapter 
        mListView.setAdapter(listAdapter);
	}

	// show list for Select
    int mChkNum;
    void listForSelect()
    {
		mDb = new DB(mActivity);
		mChkNum = 0;
        // set list view
        mListView = (ListView) mActivity.findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new OnItemClickListener()
               {
                    public void onItemClick(AdapterView<?> parent, View vw, int position, long id)
                    {
                         CheckedTextView chkTV = (CheckedTextView) vw.findViewById(R.id.checkTV);
                         chkTV.setChecked(!chkTV.isChecked());
                         mCheckedArr.set(position, chkTV.isChecked());
                         if(mCheckedArr.get(position) == true)
                        	 mChkNum++;
                         else
                        	 mChkNum--;
                         
                         if(!chkTV.isChecked())
                         {
                        	 mCheckTvSelAll.setChecked(false);
                         }
                    }
               });
 
        // set list string array
        mCheckedArr = new ArrayList<Boolean>();
        mListStrArr = new ArrayList<String>();
        
        // DB
		String strFinalPageViewed_tableId = Util.getPrefFinalPageTableId(mActivity);
        DB.setTableNumber(strFinalPageViewed_tableId);
        mDb = new DB(mActivity);
        
        mDb.doOpen();
        COUNT = DB.getAllTabCount();

        for(int i=0;i<COUNT;i++)
        {
        	 // list string array: init
             mListStrArr.add(DB.getTabName(i));
             // checked mark array: init
             mCheckedArr.add(false);
        }
        mDb.doClose();
        
        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mActivity, mListStrArr);
        
        // list view: set adapter 
        mListView.setAdapter(listAdapter);
    }
    
	// list adapter
    public class ListAdapter extends BaseAdapter
    {
        private Activity activity;
        private List<String> mList;
        private LayoutInflater inflater = null;
         
        public ListAdapter(Activity a, List<String> list)
        {
            activity = a;
            mList = list;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
     
        public int getCount()
        {
            return mList.size();
        }
     
        public Object getItem(int position)
        {
            return mCheckedArr.get(position);
        }
     
        public long getItemId(int position)
        {
            return position;
        }
         
        public View getView(int position, View convertView, ViewGroup parent)
        {
            mView = inflater.inflate(R.layout.select_page_list_row, null);
            
            // set checked text view
            CheckedTextView chkTV = (CheckedTextView) mView.findViewById(R.id.checkTV);
            mDb.doOpen();
            
            // show style
            chkTV.setBackgroundColor(Util.mBG_ColorArray[mDb.getTabStyle(position)]);
            chkTV.setTextColor(Util.mText_ColorArray[mDb.getTabStyle(position)]);
            
            // show selected
            if(DB.getTabTableId(position) == Integer.valueOf(DB.getTableNumber()))
            {
            	chkTV.setText(mList.get(position).toString() + " *");
        		chkTV.setTypeface(null, Typeface.BOLD_ITALIC);
            }
            else	
            	chkTV.setText(mList.get(position).toString());
            
            mDb.doClose();
            // set checked mark
            for(int x=0;x<COUNT;x++)
            	chkTV.setChecked(mCheckedArr.get(position));
            
            return mView;
        }
    }

}
