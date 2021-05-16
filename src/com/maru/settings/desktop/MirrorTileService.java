package com.maru.settings.desktop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.mperspective.Perspective;
import android.mperspective.PerspectiveManager;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import androidx.annotation.RequiresApi;

import com.maru.settings.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MirrorTileService extends TileService {
    private static final String TAG = "MirrorTileService";

    private PerspectiveManager mPerspectiveManager;
    private boolean mIsListening;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        if (mPerspectiveManager == null) {
            mPerspectiveManager = (PerspectiveManager) getSystemService(Context.PERSPECTIVE_SERVICE);
        }
        mPerspectiveManager.registerPerspectiveListener(new DesktopPerspectiveListener(), null);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        mIsListening = true;
        if (mPerspectiveManager != null) {
            updateDesktopStateInternal(mPerspectiveManager.isDesktopRunning());
        }
    }

    @Override
    public void onStopListening() {
        mIsListening = false;
        super.onStopListening();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        if (mPerspectiveManager != null) {
            mPerspectiveManager.unregisterPerspectiveListener();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        if (mPerspectiveManager != null) {
            boolean isDesktopRunning = mPerspectiveManager.isDesktopRunning();
            Log.d(TAG, "onClick current desktop running state " + isDesktopRunning);
            if (isDesktopRunning) {
                mPerspectiveManager.stopDesktopPerspective();
            } else {
                mPerspectiveManager.startDesktopPerspective();
            }
        }
    }

    private void updateDesktopStateInternal(boolean isMirroring) {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        Resources resources = getResources();
        int iconId = isMirroring ? R.drawable.ic_mirroring_enabled : R.drawable.ic_mirroring_disabled;
        tile.setIcon(Icon.createWithResource(resources, iconId));
        int descriptionId = isMirroring ? R.string.accessibility_qs_mirroring_changed_on
                : R.string.accessibility_qs_mirroring_changed_off;
        tile.setContentDescription(resources.getString(descriptionId));
        tile.setState(isMirroring ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private void updateDesktopStateIfNeeded(int state) {
        Log.d(TAG, "updateDesktopStateIfNeeded: " + Perspective.stateToString(state)
                + ", listening " + mIsListening);
        if (mIsListening) {
            getMainThreadHandler().post(() -> updateDesktopStateInternal(state == Perspective.STATE_RUNNING));
        }
    }

    private final class DesktopPerspectiveListener implements PerspectiveManager.PerspectiveListener {
        @Override
        public void onPerspectiveStateChanged(int state) {
            Log.d(TAG, "onPerspectiveStateChanged: " + Perspective.stateToString(state));
            updateDesktopStateIfNeeded(state);
        }
    }
}
