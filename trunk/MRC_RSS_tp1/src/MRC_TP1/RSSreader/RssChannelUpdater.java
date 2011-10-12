package MRC_TP1.RSSreader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.database.Cursor;

public class RssChannelUpdater extends DefaultHandler{
	
	
	 private RssDbAdapter mDbHelper;
	 private List<Post> posts;
	 private Post currentPost=null;
	 private StringBuilder builder;
	 
	 // names of the XML tags
	 static final String PUB_DATE = "pubDate";
	 static final String DESCRIPTION = "description";
	 static final String LINK = "link";
	 static final String TITLE = "title";
	 static final String ITEM = "item";
	 static final String AUTHOR = "creator";
	 static final String AUTHOR2 = "author";
	 
	
	
	public List<Post> getPosts() {
		return posts;
	}

	public RssChannelUpdater(RssDbAdapter dbhelper){
		//open database
        mDbHelper = dbhelper;	
	}
	
	
	public boolean [] updateChannels(){
		mDbHelper.droptablePosts();
		
		Cursor c = mDbHelper.fetchAllFeeds();

		if (c.getCount()>0){
			boolean [] error = new boolean [c.getCount()];
			c.moveToFirst();
			int columnindex = c.getColumnIndex(RssDbAdapter.KEY_FEED_URL);
			int feedidcolumnindex = c.getColumnIndex(RssDbAdapter.KEY_FEED_ROWID);
			for (int i=0; i<c.getCount();i++){
				try {
					updateChannel(c.getLong(feedidcolumnindex), c.getString(columnindex));
					error[i]=false;
				} catch (Exception e) {
					// TODO: ver isto como deve ser
					e.printStackTrace();
					error[i]=true;
				}

				c.moveToNext();
			}
			c.close();
			return error;
		}else{ 
			c.close();
			return null;		
		}
	}

	public void updateChannel(long feed_id,String url) throws ParserConfigurationException, SAXException, IOException{
        
		SAXParserFactory saxparserfactory = SAXParserFactory.newInstance();
        SAXParser saxparser = saxparserfactory.newSAXParser();
        XMLReader xmlreader = saxparser.getXMLReader();

        xmlreader.setContentHandler(this);
        //
        xmlreader.setErrorHandler(this);

        URL rssurl = new URL(url);
        
        URLConnection c = rssurl.openConnection();
        xmlreader.parse(new InputSource(c.getInputStream()));
        
        System.out.println("a inserir os posts com feed_id: "+feed_id);
        System.out.println("##############posts#################");
        int i = 0;
        for (Post p : posts){
        	System.out.println("i:"+i++);
        	System.out.println(p.getTitle().trim());
        	System.out.println("link");
        	System.out.println(p.getLink().toString());
        	System.out.println("description");
        	System.out.println(p.getDescription()==null?"":p.getDescription().trim());
        	System.out.println("author");
        	System.out.println(p.getAuthor()==null?"":p.getAuthor().trim());
        	System.out.println("date");
        	System.out.println(p.getDate());
        }
        System.out.println("####################################");
        
        for (Post p : posts){
        	mDbHelper.createPost(feed_id, p.getTitle().trim(),p.getLink().toString(),p.getDate(), p.getDescription()==null?"":p.getDescription().trim(),p.getAuthor()==null?"":p.getAuthor().trim());
        }
	}
	
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
	        super.startElement(namespaceURI, localName, qName, atts);
	        
	        if (localName.equalsIgnoreCase(ITEM)){
	            this.currentPost = new Post();
	        }
		
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qName){
	        try {
				super.endElement(namespaceURI, localName, qName);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if (this.currentPost != null){
	            if (localName.equalsIgnoreCase(TITLE)){
	                currentPost.setTitle(builder.toString());
	            } else if (localName.equalsIgnoreCase(LINK)){
	                currentPost.setLink(builder.toString());
	            } else if (localName.equalsIgnoreCase(DESCRIPTION)){
	                currentPost.setDescription(builder.toString());
	            } else if (localName.equalsIgnoreCase(PUB_DATE)){
	                currentPost.setDate(builder.toString());
	            }else if (localName.equalsIgnoreCase(AUTHOR)){
	            	currentPost.setAuthor(builder.toString());
	            }else if (localName.equalsIgnoreCase(AUTHOR2)){
	            	currentPost.setAuthor(builder.toString());
	            } else if (localName.equalsIgnoreCase(ITEM)){
	                posts.add(currentPost);
	                currentPost=null;
	            }
	                
	        }
	        builder.setLength(0);
		
	}
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length){
	      try {
			super.characters(ch, start, length);
			builder.append(ch, start, length);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
    }
	
	@Override
	public void startDocument(){
	    try {
			super.startDocument();
	        posts = new ArrayList<Post>();
	        builder = new StringBuilder();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
