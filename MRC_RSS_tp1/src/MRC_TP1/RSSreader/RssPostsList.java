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

package MRC_TP1.RSSreader;

import MRC_TP1.RSSreader.R;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class RssPostsList extends ListActivity {

	private static final int ACTIVITY_SHOW_POST_WEBVIEW=0;
	private static final int MARK_READ_OR_UNREAD = 0;
	private RssDbAdapter mDbHelper; // BD
	private Long mfeedId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posts_list);
		setTitle(R.string.posts_label);
		mDbHelper = new RssDbAdapter(this);
		mDbHelper.open();
		
		mfeedId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(RssDbAdapter.KEY_FEED_ROWID);
		if (mfeedId == null) {
			Bundle extras = getIntent().getExtras();
			mfeedId = extras != null ? extras.getLong(RssDbAdapter.KEY_FEED_ROWID)
									: null;
		}
		fillWithPosts();
		registerForContextMenu(getListView());
	}

	private void fillWithPosts() {
		
		 // This will be used by our SimpleCursorAdapter to bind fields in each row to
	    // data from our cursor.  Note that we have an extra field in the cursor that
	    // determines a display attribute for the field we display.
	    class ShowViewBinder implements SimpleCursorAdapter.ViewBinder {

	        // this cursor is built by calling a function that returns a few things.
	        // right now I'm interested in the first (title) and the third
	        //(downloaded status)

	        private static final int DATA_COLUMN = 1;
	        private static final int STATUS_COLUMN = 5;
	        boolean retval = false;

	        //@Override
	        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	        	
	            if ( columnIndex == DATA_COLUMN) {
	                int status = cursor.getInt(STATUS_COLUMN);
	                TextView tv = (TextView) view;
	                
	                switch ( status ) {
	                    case 1:
	                        tv.setTextColor(Color.WHITE);
	                        tv.setText(cursor.getString(DATA_COLUMN));
	                        retval = true;
	                        break;
	                    case 0:
	                    	tv.setTextColor(Color.GREEN);
	                        tv.setText(cursor.getString(DATA_COLUMN));
	                        retval = true;
	                        break;
	                    default:
	                        tv.setTextColor(Color.WHITE);
	                        tv.setText(cursor.getString(DATA_COLUMN));
	                        retval = true;
	                        break;
	                }
	            }
	            return retval;
	        }
	    }
		
		Cursor postsCursor = mDbHelper.fetchPosts(mfeedId);
		startManagingCursor(postsCursor);

		// Create an array to specify the fields we want to display in the list
		// (only The post title)
		String[] from = new String[] { RssDbAdapter.KEY_POST_TITLE,RssDbAdapter.KEY_POST_READ };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.post_text };

		// Now create a simple cursor adapter and set it to display posts
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,R.layout.posts_row, postsCursor, from, to);
		notes.setViewBinder(new ShowViewBinder());
		setListAdapter(notes);

	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this,RssPostWebview.class);
        i.putExtra(RssDbAdapter.KEY_POST_ROWID, id);//passa o id do post para a actividade seguinte
        startActivityForResult(i, ACTIVITY_SHOW_POST_WEBVIEW);
    }

    @Override
    /* Callback
     * 
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillWithPosts();
    }
    
    @Override
    public boolean onSearchRequested() {
    	Bundle appData = new Bundle();
    	appData.putLong(RssDbAdapter.KEY_FEED_ROWID, mfeedId);
    	startSearch(null, false, appData, false);
    	return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MARK_READ_OR_UNREAD, 0, R.string.context_menu_post_mark);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MARK_READ_OR_UNREAD:
            	markPostAsReadOrUnread(item);
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    public void markPostAsReadOrUnread(MenuItem item){
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        //verifica se o post foi lido ou nao
    	Cursor postCursor = mDbHelper.fetchPost(info.id);
        startManagingCursor(postCursor);
        int columnIndex = postCursor.getColumnIndex(RssDbAdapter.KEY_POST_READ);
		int is_read = postCursor.getInt(columnIndex);
		System.out.println("is_read= "+is_read);
		mDbHelper.setPostStatus(info.id, (is_read == 0));
		fillWithPosts();
    }
}
