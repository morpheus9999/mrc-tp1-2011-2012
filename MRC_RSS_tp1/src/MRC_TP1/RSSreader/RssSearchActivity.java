package MRC_TP1.RSSreader;

import MRC_TP1.RSSreader.R;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RssSearchActivity extends ListActivity {

	private static final int ACTIVITY_SHOW_POST_WEBVIEW = 0;
	private static final int SEARCH_ID = Menu.FIRST;

	private long mfeedId = -1; // feed to be searched or -1 to search all feeds

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			setTitle("Search: " + query); // TODO move this to strings.xml somehow
			Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
			if (appData != null) {
				mfeedId = appData.getLong(RssDbAdapter.KEY_FEED_ROWID);
			}

			RssDbAdapter mDbHelper = new RssDbAdapter(this);
			mDbHelper.open();
			Cursor c = mDbHelper.fetchPostsTitle(mfeedId, query);
			startManagingCursor(c);

			String[] from = new String[] { RssDbAdapter.KEY_POST_TITLE };
			int[] to = new int[] { android.R.id.text1 };

			SimpleCursorAdapter posts = new SimpleCursorAdapter(this,
					android.R.layout.simple_list_item_1, c, from, to);
			setListAdapter(posts);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, RssPostWebview.class);
		i.putExtra(RssDbAdapter.KEY_POST_ROWID, id);
		startActivityForResult(i, ACTIVITY_SHOW_POST_WEBVIEW);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SEARCH_ID, 0, R.string.search);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SEARCH_ID:
			onSearchRequested();
		}

		return super.onMenuItemSelected(featureId, item);
	}

}
