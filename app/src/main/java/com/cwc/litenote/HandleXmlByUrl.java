package com.cwc.litenote;

import java.io.InputStream;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

public class HandleXmlByUrl {

   private String tabname,title,body;
   private static DB mDb;
   private Context mContext;

   private String urlString = null;
   private String xmlContent = Util.NEW_LINE;
//   private XmlPullParserFactory xmlFactoryObject;
   public volatile boolean parsingComplete = true;
   
   public HandleXmlByUrl(String url,Context context)
   {
      mDb = new DB(context);
	  mContext = context; 
      this.urlString = url;
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
	                  
		        	  mDb.doOpen();
		        	  int style = Util.getNewPageStyle(mContext);
		        	  // style is not set in XML file, so insert default style instead
		        	  mDb.insertTab("TAB_INFO", tabname, TabsHost.getLastTabId() + 1,style );
		        		
		        	  // insert table for new tab
		        	  mDb.insertNewTable(TabsHost.getLastTabId() + 1 );
		        	  // update last tab Id after Insert
		        	  TabsHost.setLastTabId(TabsHost.getLastTabId() + 1);
		        	  mDb.doClose();
		        	  
		        	  xmlContent = xmlContent.concat(tabname);

	               }
	               else if(name.equals("title"))
	               {
		              text = text.replace("[n]"," ");
		              text = text.replace("[s]"," ");
		              title = text.trim();
		              
		              xmlContent = xmlContent.concat(title);
		           }
	               else if(name.equals("body"))
	               { 	
	            	  body = text.trim();
	            	  DB.setTableNumber(String.valueOf(TabsHost.getLastTabId()));  
	            	  mDb.doOpen();
	            	  if(title.length() !=0 || body.length() != 0)
	            		  mDb.insert(title, "", body, (long)0); //???
	            	  mDb.doClose();
	            	  
	            	  xmlContent = xmlContent.concat(body);
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
		         URL url = new URL(urlString);
		         InputStream stream = url.openConnection().getInputStream();
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
   
   public String getXmlContent()
   {
	  return xmlContent;
   }
}