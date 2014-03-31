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
}