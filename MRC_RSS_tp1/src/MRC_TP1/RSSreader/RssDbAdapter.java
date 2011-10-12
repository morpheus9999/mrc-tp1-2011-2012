package MRC_TP1.RSSreader;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RssDbAdapter {

	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 2;

	public static final String DATABASE_TABLE_FEED = "feed";
	public static final String DATABASE_TABLE_POST = "post";

	public static final String KEY_FEED_ROWID = "_id";
	public static final String KEY_FEED_TITLE = "title";
	public static final String KEY_FEED_URL = "url";

	public static final String KEY_POST_ROWID = "_id";
	public static final String KEY_POST_FEED_ID = "feed_id";
	public static final String KEY_POST_URL = "url";
	public static final String KEY_POST_DATE = "post_date";
	public static final String KEY_POST_BODY = "body";
	public static final String KEY_POST_AUTHOR = "author";
	public static final String KEY_POST_READ = "read";
	public static final String KEY_POST_TITLE = "title";

	private static final String TAG = "RssDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	/**
	 * Database creation sql statement
	 */
	private static final String FEED_DATABASE_CREATE = "create table feed (_id integer primary key autoincrement, "
			+ "title text not null, url TEXT UNIQUE);";

	private static final String POST_DATABASE_CREATE = "create table post (_id integer primary key autoincrement, "
			+ "feed_id INTEGER,title text, url TEXT,post_date DATETIME,body TEXT, author TEXT,read INTEGER(1) DEFAULT '0');";


	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(FEED_DATABASE_CREATE);
			db.execSQL(POST_DATABASE_CREATE);
			
            db.execSQL("CREATE INDEX idx_post1 ON post (_id);");
            db.execSQL("CREATE INDEX idx_post2 ON post (feed_id);");
            db.execSQL("CREATE INDEX idx_feed1 ON feed (_id);");
            
            db.execSQL("CREATE TRIGGER trigger1 BEFORE DELETE on feed "+
            		"BEGIN " +
            		"DELETE from post where feed_id = old._id;" +
            		" END;");
            }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS feed");
			db.execSQL("DROP TABLE IF EXISTS post");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public RssDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public RssDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Return a Cursor over the list of all feeds in the database
	 * 
	 * @return Cursor over all feeds
	 */
	public Cursor fetchAllFeeds() {

		return mDb.query(DATABASE_TABLE_FEED, new String[] { KEY_FEED_ROWID,
				KEY_FEED_TITLE, KEY_FEED_URL }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor over the list of all posts from a feed in the database
	 * 
	 * @return Cursor over all posts from a feed
	 */
	public Cursor fetchPosts(long feed_id) {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_POST, new String[] { KEY_POST_ROWID,
				KEY_POST_TITLE, KEY_POST_DATE, KEY_POST_URL, KEY_POST_BODY,KEY_POST_READ },
				KEY_POST_FEED_ID + "=" + feed_id, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor over the list of all posts from a feed in the database
	 * 
	 * @return Cursor over all posts from a feed
	 */
	public Cursor fetchFeed(long feed_id) {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_FEED, new String[] { KEY_FEED_TITLE,
				KEY_FEED_URL }, KEY_FEED_ROWID + "=" + feed_id, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor with the post specified by the param post_id
	 * @param post_id - id of the post 
	 * @return Cursor with the wanted post
	 */
	public Cursor fetchPost(long post_id) {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_POST, new String[] { KEY_POST_ROWID,
				KEY_POST_TITLE, KEY_POST_DATE, KEY_POST_URL, KEY_POST_BODY,KEY_POST_READ },
				KEY_POST_ROWID + "=" + post_id, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Set the post with post_id to read/unread
	 * @param post_id - id of the post 
	 * @param isRead - read/unread
	 */
	public void setPostStatus(long post_id,boolean isRead) {
		
		mDb.execSQL("UPDATE post set "+KEY_POST_READ+"="+ (isRead?1:0) +" where "+KEY_POST_ROWID+"="+post_id+";");
	}
	

	/**
	 * Create a new feed If the feed is successfully created return the new
	 * rowId for that feed, otherwise return a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the feed
	 * @param url
	 *            the url of the feed
	 * @return rowId or -1 if failed
	 */
	public long createFeed(String title, String url) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_FEED_TITLE, title);
		initialValues.put(KEY_FEED_URL, url);

		return mDb.insert(DATABASE_TABLE_FEED, null, initialValues);
	}

	/**
	 * Delete the feed with the given rowId
	 * 
	 * @param rowId
	 *            id of feed to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteFeed(long rowId) {

		return mDb.delete(DATABASE_TABLE_FEED, KEY_FEED_ROWID + "=" + rowId,
				null) > 0;
	}

	/**
	 * Update the feed using the details provided. The feed to be updated is
	 * specified using the rowId.
	 * 
	 * @param rowId
	 *            id of the feed to update
	 * @param title
	 *            title of the feed
	 * @param url
	 *            value to set url to
	 * @return true if the feed was successfully updated, false otherwise
	 */
	public boolean updateFeed(long rowId, String title, String url) {
		ContentValues args = new ContentValues();
		args.put(KEY_FEED_TITLE, title);
		args.put(KEY_FEED_URL, url);

		return mDb.update(DATABASE_TABLE_FEED, args, KEY_FEED_ROWID + "="
				+ rowId, null) > 0;
	}

	/**
	 * Create a new post If the post is successfully created return the new
	 * rowId for that post, otherwise return a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the feed
	 * @param url
	 *            the url of the feed
	 * @return rowId or -1 if failed
	 */
	public long createPost(long feed_id, String title, String url,
			Date post_date, String body, String author) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_POST_TITLE, title);
		initialValues.put(KEY_POST_URL, url);
		initialValues.put(KEY_POST_BODY, body);

		// set the format to sql date time
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		initialValues.put(KEY_POST_DATE, dateFormat.format(post_date));

		initialValues.put(KEY_POST_URL, url);
		initialValues.put(KEY_POST_AUTHOR, author);
		initialValues.put(KEY_POST_FEED_ID, feed_id);

		return mDb.insert(DATABASE_TABLE_POST, null, initialValues);
	}

	/**
	 * Return a Cursor over the list of all posts from a feed in the database
	 * 
	 * @param title
	 *            the title of the feed
	 * @return Cursor over all posts from a feed
	 * 
	 */
	public Cursor fetchPostsTitle(long feed_id, String title) {

		Cursor mCursor = null;		
		if (feed_id != -1) {
			mCursor =

				mDb.query(true, DATABASE_TABLE_POST, new String[] {
						KEY_POST_ROWID, KEY_POST_TITLE, KEY_POST_DATE,
						KEY_POST_URL, KEY_POST_BODY }, KEY_POST_TITLE + "=" + feed_id + " AND " + KEY_POST_TITLE + " LIKE '%"
						+ title + "%'", null, null, null, null, null);
		} else {
			mCursor =

				mDb.query(true, DATABASE_TABLE_POST, new String[] {
						KEY_POST_ROWID, KEY_POST_TITLE, KEY_POST_DATE,
						KEY_POST_URL, KEY_POST_BODY }, KEY_POST_TITLE + " LIKE '%"
						+ title + "%'", null, null, null, null, null);
		}
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		
		return mCursor;
	}

	/**
	 * Return a Cursor over the list of all posts from a feed in the database
	 * 
	 * @return Cursor over all posts from a feed
	 */
	public Cursor fetchPostsDate(long feed_id, Date date) {

		// set the format to sql date time
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String datef = dateFormat.format(date);

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_POST, new String[] { KEY_POST_ROWID,
				KEY_POST_TITLE, KEY_POST_DATE, KEY_POST_URL, KEY_POST_BODY },
				KEY_POST_FEED_ID + "=" + feed_id + " AND " + KEY_POST_DATE
						+ "=" + datef, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor over the list of all posts from a feed in the database
	 * 
	 * @return Cursor over all posts from a feed
	 */
	public Cursor fetchPostsAuthor(long feed_id, String author) {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_POST, new String[] { KEY_POST_ROWID,
				KEY_POST_TITLE, KEY_POST_DATE, KEY_POST_URL, KEY_POST_BODY },
				KEY_POST_FEED_ID + "=" + feed_id + " AND " + KEY_POST_AUTHOR
						+ " LIKE %" + author + "%", null, null, null, null,
				null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public void droptablePosts(){
		mDb.execSQL("DROP TABLE IF EXISTS post");
		mDb.execSQL(POST_DATABASE_CREATE);
        mDb.execSQL("CREATE INDEX idx_post1 ON post (_id);");
        mDb.execSQL("CREATE INDEX idx_post2 ON post (feed_id);");
	}

}
