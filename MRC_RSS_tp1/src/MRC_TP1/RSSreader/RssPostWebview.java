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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class RssPostWebview extends Activity {

	private RssDbAdapter mDbHelper; // BD
	private Long mPostId;
	private WebView mWebView;
	private static final int SHARE_ID = Menu.FIRST;
	private static final int MARK_UNREAD_ID = Menu.FIRST + 1;
	private static final int BROWSER_ID = Menu.FIRST + 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_webview);
		setTitle(R.string.post_webview);

		mDbHelper = new RssDbAdapter(this);
		mDbHelper.open();

		mPostId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(RssDbAdapter.KEY_POST_ROWID);
		if (mPostId == null) {
			Bundle extras = getIntent().getExtras();
			mPostId = extras != null ? extras
					.getLong(RssDbAdapter.KEY_POST_ROWID) : null;
		}
		showWebView();
		mWebView.setWebViewClient(new PostWebViewClient());

	}
	
	private void showWebView() {
		mWebView = (WebView) findViewById(R.id.post_webview);
		mWebView.getSettings().setJavaScriptEnabled(true);

		Cursor postCursor = mDbHelper.fetchPost(mPostId);
		startManagingCursor(postCursor);
		int columnIndex = postCursor.getColumnIndex(RssDbAdapter.KEY_POST_BODY);
		String post_body = postCursor.getString(columnIndex);
		
		mDbHelper.setPostStatus(mPostId, true);

		mWebView.loadData(post_body, "text/html", "utf-8");
		mWebView.setWebViewClient(new PostWebViewClient());
	}

	@Override
	/*
	 * Callback
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		showWebView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SHARE_ID, 0, R.string.menu_post_share);
		menu.add(0, MARK_UNREAD_ID, 0, R.string.menu_post_mark);
		menu.add(0, BROWSER_ID, 0, R.string.menu_post_browser);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SHARE_ID:
			sharePost();
			System.out.println("share item");
			return true;

		case MARK_UNREAD_ID:	
			markPost();
			System.out.println("mark unread item");
			return true;

		case BROWSER_ID:			
			openBrowserWithPost();
			System.out.println("browser item");
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void sharePost() {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body",getPostUrl()); 
        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
	}
	
	private void markPost(){
		mDbHelper.setPostStatus(mPostId,false); //marcar como nao lido na BD
	}
	
	
	private String getPostUrl(){
		Cursor postCursor = mDbHelper.fetchPost(mPostId);
		startManagingCursor(postCursor);
		int columnIndex = postCursor.getColumnIndex(RssDbAdapter.KEY_POST_URL);
		String post_url= postCursor.getString(columnIndex);
		if (!post_url.startsWith("http://") && !post_url.startsWith("https://"))
			   post_url = "http://" + post_url;	
		return post_url;
	}
	
	private void openBrowserWithPost(){
		System.out.println("Entrou no browser");
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(getPostUrl()));
		startActivity(browserIntent);	
	}

	/**
	 * Nested class por causa de abrir links que um post poder� conter
	 * ver http://developer.android.com/resources/tutorials/views/hello-webview.html
	 * @author ilharco
	 *
	 */
	private class PostWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

	}

}
