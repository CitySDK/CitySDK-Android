package com.citysdk.demo.contracts;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.citysdk.demo.utils.SelectionBuilder;

public class PoisProvider extends ContentProvider {

    public static final int CATEGORY = 1;
    public static final int CATEGORY_ID = 2;
    public static final int POIS = 3;
    public static final int POIS_ID = 4;
    private static final String AUTHORITY = PoisContract.CONTENT_AUTHORITY;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "categories", CATEGORY);
        sUriMatcher.addURI(AUTHORITY, "categories/*", CATEGORY_ID);
        sUriMatcher.addURI(AUTHORITY, "pois", POIS);
        sUriMatcher.addURI(AUTHORITY, "pois/*", POIS_ID);
    }
    FeedDatabase mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new FeedDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CATEGORY:
                return PoisContract.Category.CONTENT_TYPE;
            case CATEGORY_ID:
                return PoisContract.Category.CONTENT_ITEM_TYPE;
            case POIS:
                return PoisContract.Pois.CONTENT_TYPE;
            case POIS_ID:
                return PoisContract.Pois.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);

        String id;
        Cursor c;
        Context ctx = getContext();

        switch (uriMatch) {
            case CATEGORY_ID:
                id = uri.getLastPathSegment();
                builder.where(PoisContract.Category._ID + "=?", id);
            case CATEGORY:
                builder.table(PoisContract.Category.TABLE_NAME)
                        .where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                return queryAux(ctx, c, uri);
            case POIS_ID:
                id = uri.getLastPathSegment();
                builder.where(PoisContract.Pois._ID + "=?", id);
            case POIS:
                builder.table(PoisContract.Pois.TABLE_NAME)
                        .where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                return queryAux(ctx, c, uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Cursor queryAux(Context ctx, Cursor c, Uri uri) {
        assert ctx != null;
        c.setNotificationUri(ctx.getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        long id;
        switch (match) {
            case CATEGORY:
                id = db.insertOrThrow(PoisContract.Category.TABLE_NAME, null, values);
                result = Uri.parse(PoisContract.Category.CONTENT_URI + "/" + id);
                break;
            case CATEGORY_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case POIS:
                id = db.insertOrThrow(PoisContract.Pois.TABLE_NAME, null, values);
                result = Uri.parse(PoisContract.Pois.CONTENT_URI + "/" + id);
                break;
            case POIS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case CATEGORY:
                count = builder.table(PoisContract.Category.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case CATEGORY_ID:
                id = uri.getLastPathSegment();
                count = builder.table(PoisContract.Category.TABLE_NAME)
                        .where(PoisContract.Category._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case POIS:
                count = builder.table(PoisContract.Pois.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case POIS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(PoisContract.Pois.TABLE_NAME)
                        .where(PoisContract.Pois._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case CATEGORY:
                count = builder.table(PoisContract.Category.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case CATEGORY_ID:
                id = uri.getLastPathSegment();
                count = builder.table(PoisContract.Category.TABLE_NAME)
                        .where(PoisContract.Category._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case POIS:
                count = builder.table(PoisContract.Pois.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case POIS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(PoisContract.Pois.TABLE_NAME)
                        .where(PoisContract.Pois._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    static class FeedDatabase extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "citysdk.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_BLOB = " BLOB";

        private static final String COMMA_SEP = ",";

        private static final String SQL_CREATE_CATEGORIES =
                "CREATE TABLE " + PoisContract.Category.TABLE_NAME + " (" +
                        PoisContract.Category._ID + " INTEGER PRIMARY KEY," +
                        PoisContract.Category.COLUMN_CATEGORY_OID + TYPE_TEXT + COMMA_SEP +
                        PoisContract.Category.COLUMN_CATEGORY_OPTION + TYPE_TEXT + COMMA_SEP +
                        PoisContract.Category.COLUMN_CATEGORY_NAME + TYPE_TEXT + ")";

        private static final String SQL_CREATE_POIS =
                "CREATE TABLE " + PoisContract.Pois.TABLE_NAME + " (" +
                        PoisContract.Pois._ID + " INTEGER PRIMARY KEY," +
                        PoisContract.Pois.COLUMN_POIS_TYPE + TYPE_TEXT + COMMA_SEP +
                        PoisContract.Pois.COLUMN_POIS_QUERY + TYPE_TEXT + COMMA_SEP +
                        PoisContract.Pois.COLUMN_POIS_DATE_CREATED + TYPE_TEXT + COMMA_SEP +
                        PoisContract.Pois.COLUMN_POIS_DATE_ACCESSED + TYPE_TEXT + ")";

        private static final String SQL_DELETE_CATEGORIES =
                "DROP TABLE IF EXISTS " + PoisContract.Category.TABLE_NAME;

        private static final String SQL_DELETE_POIS =
                "DROP TABLE IF EXISTS " + PoisContract.Pois.TABLE_NAME;

        public FeedDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_CATEGORIES);
            db.execSQL(SQL_CREATE_POIS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_CATEGORIES);
            db.execSQL(SQL_DELETE_POIS);
            onCreate(db);
        }
    }
}
