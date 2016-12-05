package com.cwc.litenote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ImportFromSDCardAct extends ListActivity 
{
    
    private List<String> items = null;
    List<String> fileNames = null;
   
    
    @Override
    public void onCreate(Bundle bundle) 
    {
        super.onCreate(bundle);
        setContentView(R.layout.sd_file_list_view);
		String dirString = Environment.getExternalStorageDirectory().toString() + 
					          "/" + 
					          Util.getAppName(this);
        getFiles(new File(dirString).listFiles());
        
        // back button
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

        // do cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }

        });
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long rowId)
    {
        int selectedRow = (int)rowId;
        if(selectedRow == 0)
        {
        	//root
            getFiles(new File("/").listFiles());
        }
        else
        {
            final String filePath = items.get(selectedRow);
            final File file = new File(filePath);
            if(file.isDirectory())
            {
            	//directory
                getFiles(file.listFiles());
            }
            else
            {
            	// view the file content
	           	Intent i = new Intent(ImportFromSDCardAct.this, ViewFile.class);
	           	i.putExtra("FILE_PATH", filePath);
	           	startActivity(i);
            }
        }
    }
    private void getFiles(File[] files)
    {
        if(files == null)
        {
        	Toast.makeText(ImportFromSDCardAct.this,R.string.toast_import_SDCard_no_file,Toast.LENGTH_SHORT).show();
        	finish();
        }
        else
        {
        	System.out.println("files lenght = " + files.length);
            items = new ArrayList<String>();
            fileNames = new ArrayList<String>();
            items.add("");
            fileNames.add("ROOT");
            
	        for(File file : files)
	        {
	            items.add(file.getPath());
	            fileNames.add(file.getName());
	        }
	        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
	        														R.layout.sd_file_list_row,
	        														fileNames);
	        setListAdapter(fileList);
        }
    }
}