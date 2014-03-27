package com.citysdk.demo.contracts;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PoisContract {
    public static final String CONTENT_AUTHORITY = "com.citysdk.demo";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_CATEGORIES = "categories";
    private static final String PATH_POIS = "pois";
    private PoisContract() {
    }

    public static class Category implements BaseColumns {

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/dir_category";

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/category";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String TABLE_NAME = "category";

        public static final String COLUMN_CATEGORY_OID = "categoryId";
        public static final String COLUMN_CATEGORY_OPTION = "option";
        public static final String COLUMN_CATEGORY_NAME = "name";

        public static final String[] PROJECTION_CATEGORY = new String[]{
                PoisContract.Category._ID,
                PoisContract.Category.COLUMN_CATEGORY_OID,
                PoisContract.Category.COLUMN_CATEGORY_OPTION,
                PoisContract.Category.COLUMN_CATEGORY_NAME
        };

        public static final int CATEGORY_COLUMN_ID = 0;
        public static final int CATEGORY_COLUMN_OID = 1;
        public static final int CATEGORY_COLUMN_OPTION = 2;
        public static final int CATEGORY_COLUMN_NAME = 3;

    }

    public static class Pois implements BaseColumns {

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/dir_pois";

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/pois";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POIS).build();

        public static final String TABLE_NAME = "pois";

        public static final String COLUMN_POIS_TYPE = "type";
        public static final String COLUMN_POIS_QUERY = "query";
        public static final String COLUMN_POIS_DATE_CREATED = "dataCreated";
        public static final String COLUMN_POIS_DATE_ACCESSED = "dataAccessed";


        public static final String[] PROJECTION_POIS = new String[]{
                PoisContract.Pois._ID,
                PoisContract.Pois.COLUMN_POIS_TYPE,
                PoisContract.Pois.COLUMN_POIS_QUERY,
                PoisContract.Pois.COLUMN_POIS_DATE_CREATED,
                PoisContract.Pois.COLUMN_POIS_DATE_ACCESSED
        };

        public static final int POIS_COLUMN_ID = 0;
        public static final int POIS_COLUMN_TYPE = 1;
        public static final int POIS_COLUMN_QUERY = 2;
        public static final int POIS_COLUMN_DATE_CREATED = 3;
        public static final int POIS_COLUMN_DATE_ACCESSED = 4;

    }


    //	public static class Pois implements BaseColumns {
    //
    //		public static final String CONTENT_TYPE =
    //				ContentResolver.CURSOR_DIR_BASE_TYPE + "/dir_pois";
    //
    //		public static final String CONTENT_ITEM_TYPE =
    //				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/pois";
    //
    //		public static final Uri CONTENT_URI =
    //				BASE_CONTENT_URI.buildUpon().appendPath(PATH_POIS).build();
    //
    //		public static final String TABLE_NAME = "pois";
    //
    //		public static final String COLUMN_POIS_OID = "poisId";
    //		public static final String COLUMN_POIS_NAME = "name";
    //		public static final String COLUMN_POIS_CATEGORY = "category";
    //		public static final String COLUMN_POIS_COORD = "coords";
    //
    //		public static final String[] PROJECTION_POIS = new String[] {
    //			PoisContract.Pois._ID,
    //			PoisContract.Pois.COLUMN_POIS_OID,
    //			PoisContract.Pois.COLUMN_POIS_NAME,
    //			PoisContract.Pois.COLUMN_POIS_CATEGORY,
    //			PoisContract.Pois.COLUMN_POIS_COORD
    //		};
    //
    //		public static final int POIS_COLUMN_ID = 0;
    //		public static final int POIS_COLUMN_OID = 1;
    //		public static final int POIS_COLUMN_NAME = 2;
    //		public static final int POIS_COLUMN_CATEGORY = 3;
    //		public static final int POIS_COLUMN_COORD = 4;
    //	}
}