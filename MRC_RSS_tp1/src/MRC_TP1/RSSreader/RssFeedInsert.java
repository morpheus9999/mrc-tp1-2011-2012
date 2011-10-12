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
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RssFeedInsert extends Activity {

	//TODO: ver se o título do feed deve ser editável ou não; se preciso, mudar no feed_insert.xml
    private EditText mFeedURL, mFeedTitle;
    private Long mRowId;
    private RssDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new RssDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.feed_insert);
        setTitle(R.string.insert_feed);

        mFeedURL = (EditText) findViewById(R.id.feed_url);
        mFeedTitle = (EditText) findViewById(R.id.feed_title);

        Button confirmButton = (Button) findViewById(R.id.confirm);

       mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(RssDbAdapter.KEY_FEED_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(RssDbAdapter.KEY_FEED_ROWID)
									: null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(RssDbAdapter.KEY_FEED_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveState() {
        String feed_url = mFeedURL.getText().toString();
        String feed_title = mFeedTitle.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createFeed(feed_title, feed_url);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateFeed(mRowId, feed_title, feed_url);
        }
    }
    
    private void populateFields() {
        if (mRowId != null) {
            Cursor feed = mDbHelper.fetchFeed(mRowId);
            startManagingCursor(feed);
            mFeedTitle.setText(feed.getString(
                    feed.getColumnIndexOrThrow(RssDbAdapter.KEY_FEED_TITLE)));
            mFeedURL.setText(feed.getString(
                    feed.getColumnIndexOrThrow(RssDbAdapter.KEY_FEED_URL)));
        }
    }

}
