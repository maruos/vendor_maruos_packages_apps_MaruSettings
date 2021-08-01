/*
 * Copyright 2015-2016 Preetam J. D'Souza
 * Copyright 2016-2021 The Maru OS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maru.settings.desktop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.Display;

import androidx.annotation.RequiresApi;

import com.maru.settings.R;

import java.util.HashSet;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MirrorTileService extends TileService {
    private static final String TAG = "MirrorTileService";

    private DisplayManager mDisplayManager;

    private MDisplayListener mDisplayListener;
    private Set<Integer> mPresentationDisplays;
    private boolean mListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        mDisplayListener = new MDisplayListener();
        mPresentationDisplays = new HashSet<>();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        // Register listener when start listening, and it will be called after Tile added.
        mListening = true;
        mDisplayListener.sync();
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        refreshState();
    }

    @Override
    public void onStopListening() {
        // Unregister listener when stop listening.
        mListening = false;
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        super.onStopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisplayManager = null;
        mDisplayListener = null;
        if (mPresentationDisplays != null) {
            mPresentationDisplays.clear();
        }
        mPresentationDisplays = null;
    }

    @Override
    public void onClick() {
        super.onClick();
        if (mListening) {
            if (mDisplayManager.isPhoneMirroringEnabled()) {
                mDisplayManager.disablePhoneMirroring();
            } else {
                mDisplayManager.enablePhoneMirroring();
            }
            refreshState();
        }
    }

    private void updateMirrorStateInternal(boolean isMirroring) {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        Resources resources = getResources();
        int iconId =
                isMirroring ? R.drawable.ic_mirroring_enabled : R.drawable.ic_mirroring_disabled;
        tile.setIcon(Icon.createWithResource(resources, iconId));
        int descriptionId =
                isMirroring
                        ? R.string.accessibility_qs_mirroring_changed_on
                        : R.string.accessibility_qs_mirroring_changed_off;
        tile.setContentDescription(resources.getString(descriptionId));
        tile.setState(isMirroring ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private void refreshState() {
        if (mListening) {
            boolean isMirroring = mDisplayManager.isPhoneMirroringEnabled();
            getMainThreadHandler().post(() -> updateMirrorStateInternal(isMirroring));
        }
    }

    private class MDisplayListener implements DisplayManager.DisplayListener {
        /**
         * Keep track of public presentation displays. These are displays that will show either Maru
         * Desktop or the mirrored phone screen.
         */
        @Override
        public void onDisplayAdded(int displayId) {
            if (mDisplayManager != null && mPresentationDisplays != null) {
                Display display = mDisplayManager.getDisplay(displayId);

                if (display.isPublicPresentation()) {
                    if (mPresentationDisplays.isEmpty()) {
                        // the first presentation display was added
                        refreshState();
                    }
                    mPresentationDisplays.add(displayId);
                }
            }
        }

        @Override
        public void onDisplayRemoved(int displayId) {
            if (mPresentationDisplays != null
                    && mPresentationDisplays.remove(displayId)
                    && mPresentationDisplays.isEmpty()) {
                // the last presentation display was removed
                refreshState();
            }
        }

        @Override
        public void onDisplayChanged(int displayId) {
            /* no-op */
        }

        /**
         * We may miss a display event since listeners are unregistered when the QS panel is hidden.
         *
         * <p>Call this before registering to make sure the initial state is up-to-date.
         */
        public void sync() {
            if (mPresentationDisplays != null && mDisplayManager != null) {
                mPresentationDisplays.clear();
                Display[] displays =
                        mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                for (Display display : displays) {
                    if (display.isPublicPresentation()) {
                        mPresentationDisplays.add(display.getDisplayId());
                    }
                }
            }
        }
    }
}
