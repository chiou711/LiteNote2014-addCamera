package com.cwc.litenote;

import java.io.FileInputStream;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

public class HandleXmlByFile {

   private String tabname,title,body;
   private static DB mDb;
   private Context mContext;
   
   FileInputStream fileInputStream = null;
   public volatile boolean parsingComplete = true;
   public String fileBody = ""; 
   public boolean enableStore = true;
   
   public HandleXmlByFile(FileInputStream fileInputStream,Context context)
   {
      mDb = new DB(context);
      mContext = context;
      this.fileInputStream = fileInputStream;
   }
   
   public String getTitle()
   {
      return title;
   }
   
   public String getBody()
   {
      return body;
   }
   
   public String getPage()
   {
      return tabname;
   }
   
   public void parseXMLAndStoreIt(XmlPullParser myParser) 
   {
	  
      int event;
      String text=null;
      try 
      {
         event = myParser.getEventType();
         while (event != XmlPullParser.END_DOCUMENT) 
         {
        	 String name=myParser.getName(); //name: null, link, item, title, description
        	 switch (event)
	         {
	            case XmlPullParser.START_TAG:
		        break;
		        
	            case XmlPullParser.TEXT:
			       text = myParser.getText();
	            break;
	            
	            case XmlPullParser.END_TAG:
		           if(name.equals("tabname"))
		           {
	                  tabname = text.trim();
	                  
	                  if(enableStore)
	                  {
			        	  mDb.doOpen();
			        	  int style = Util.getNewPageStyle(mContext);
			        	  // style is not set in XML file, so insert default style instead
			        	  mDb.insertTab("TAB_INFO", tabname, TabsHost.getLastTabId() + 1,style );
			        		
			        	  // insert table for new tab
			        	  mDb.insertNewTable(TabsHost.getLastTabId() + 1 );
			        	  // update last tab Id after Insert
			        	  TabsHost.setLastTabId(TabsHost.getLastTabId() + 1);
			        	  mDb.doClose();
			        	  
//			        	  System.out.println("_insert new table");
	                  }
		        	  fileBody = fileBody.concat(Util.NEW_LINE + "--- " + "Page:" + " " + tabname + " ---");

	               }
	               else if(name.equals("title"))
	               {
		              text = text.replace("[n]"," ");
		              text = text.replace("[s]"," ");
		              title = text.trim();
		           }
	               else if(name.equals("body"))
	               { 	
	            	  body = text.trim();
	            	  if(enableStore)
	            	  {
		            	  DB.setTableNumber(String.valueOf(TabsHost.getLastTabId()));  
		            	  mDb.doOpen();
		            	  if(title.length() !=0 || body.length() != 0)
		            		  mDb.insert(title, "", body, (long) 0); //set picture null
		            	  mDb.doClose();
	            	  }
		              fileBody = fileBody.concat(Util.NEW_LINE + "title:" + " " + title);
		        	  fileBody = fileBody.concat(Util.NEW_LINE + "body:" + " " + body);
	            	  fileBody = fileBody.concat(Util.NEW_LINE);
	               }
	               break;
	         }		 
        	 event = myParser.next(); 
         }
         
         parsingComplete = false;
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

   public void fetchXML()
   {
	   Thread thread = new Thread(new Runnable()
	   {
		   @Override
		   public void run() 
		   {
		      try 
		      {
		         InputStream stream = fileInputStream;
		         XmlPullParser myparser = XmlPullParserFactory.newInstance().newPullParser();
		         myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		         myparser.setInput(stream, null);
		         parseXMLAndStoreIt(myparser);
		         stream.close();
		      } 
		      catch (Exception e) 
		      { }
		  }
	  });
	  thread.start(); 
   }
   
   public void enableStore(boolean en)
   {
	   enableStore = en;
   }
}