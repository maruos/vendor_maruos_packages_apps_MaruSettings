package com.maru.settings;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.maru.settings.widget.SwitchBar;

public class SettingsActivity extends SettingsDrawerActivity {
    private static final String TAG = "SettingsActivity";
    private static final String META_DATA_KEY_FRAGMENT_CLASS =
            "com.android.settings.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_TITLE =
            "com.android.settings.title";

    private String mFragmentClass;
    private String mTitle;

    private SwitchBar mSwitchBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_prefs);
        mSwitchBar = findViewById(R.id.switch_bar);
        getMetaData();
        switchToFragment(mFragmentClass, mTitle);
    }

    public SwitchBar getSwitchBar() {
        return mSwitchBar;
    }

    private void getMetaData() {
        try {
            ActivityInfo ai =
                    getPackageManager()
                            .getActivityInfo(
                                    getComponentName(),
                                    PackageManager.GET_META_DATA
                            );
            if (ai == null || ai.metaData == null) {
                return;
            }
            mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
            mTitle = ai.metaData.getString(META_DATA_KEY_TITLE);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.d(TAG, "Cannot get Metadata for: " + getComponentName().toString());
        }
    }

    private void switchToFragment(String fragmentName, CharSequence title) {
        Bundle args = new Bundle();
        Fragment f = Fragment.instantiate(this, fragmentName, args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, f);
        if (title != null) {
            transaction.setBreadCrumbTitle(title);
        }
        transaction.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
    }
}
