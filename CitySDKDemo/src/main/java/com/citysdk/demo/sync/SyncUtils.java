package com.citysdk.demo.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.citysdk.demo.accounts.GenericAccountService;
import com.citysdk.demo.contracts.PoisContract;


public class SyncUtils {
    private static final long SYNC_FREQUENCY = 60 * 600;  // 1 hour (in seconds)
    private static final String CONTENT_AUTHORITY = PoisContract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    private static Context mContext;

    public static void CreateSyncAccount(Context context) {
        mContext = context;
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        Account account = GenericAccountService.GetAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            newAccount = true;
        }
        if (newAccount || !setupComplete) {
            TriggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    public static void TriggerRefresh() {
        Log.d("SyncUtils", "TriggerRefresh");
        Bundle bundle = new Bundle();

        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),
                PoisContract.CONTENT_AUTHORITY,
                bundle);
    }
}