package MRC_TP1.RSSreader;

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


import MRC_TP1.RSSreader.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class RssFeedsList extends ListActivity {
    private static final int ACTIVITY_CREATE_FEED=0;
    private static final int ACTIVITY_SHOW_POSTS=1;
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int REFRESH_ID = Menu.FIRST + 2;
    private static final int SEARCH_ID = Menu.FIRST + 3;
    private RssDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feeds_list); 
        setTitle(R.string.feeds_label);
        super.onCreate(savedInstanceState);

        mDbHelper = new RssDbAdapter(this);
        
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        
    	Cursor feedsCursor = mDbHelper.fetchAllFeeds();
        startManagingCursor(feedsCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{RssDbAdapter.KEY_FEED_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.feed_text};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.feeds_row, feedsCursor, from, to);
        setListAdapter(notes);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_feed_insert);     
        menu.add(0, REFRESH_ID, 0, R.string.menu_refresh_all);
        menu.add(0, SEARCH_ID, 0, R.string.search);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createFeed();
                return true;
                
            case REFRESH_ID:
            	 new UpdateChannelsTask().execute();
            	return true;
            	
            case SEARCH_ID:
            	onSearchRequested();
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
    public void updateChannels(){
    	RssChannelUpdater channelupdater=new RssChannelUpdater(mDbHelper);
    	channelupdater.updateChannels();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_feed_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                //apaga o feed selecionado
                mDbHelper.deleteFeed(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createFeed() {
        Intent i = new Intent(this, RssFeedInsert.class);
        startActivityForResult(i, ACTIVITY_CREATE_FEED);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, RssPostsList.class);
        i.putExtra(RssDbAdapter.KEY_FEED_ROWID, id);
        startActivityForResult(i, ACTIVITY_SHOW_POSTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    private class UpdateChannelsTask extends AsyncTask<Void, Void, Void> {

    	private final ProgressDialog dialog = new ProgressDialog(RssFeedsList.this);

    	// can use UI thread here
    	protected void onPreExecute() {

    		this.dialog.setMessage("Loading...");

    		this.dialog.show();

    	}

    	// automatically done on worker thread (separate from UI thread)
    	@Override
    	protected Void doInBackground(Void... params) {
    		RssFeedsList.this.updateChannels();
    		return null;
    	}

    	// can use UI thread here

    	protected void onPostExecute(final Void unused) {

    		if (this.dialog.isShowing()) {
    			this.dialog.dismiss();
    		}
    		
    	}

    }

    
}
