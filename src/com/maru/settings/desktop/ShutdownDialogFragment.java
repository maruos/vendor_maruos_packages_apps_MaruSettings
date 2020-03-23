/*
 * Copyright (C) 2015-2016 Preetam J. D'Souza
 * Copyright (C) 2016 The Maru OS Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.maru.settings.R;

/**
 * A shutdown confirmation dialog to explicitly ensure that the
 * user wants to shutdown the desktop perspective.
 */
public class ShutdownDialogFragment extends DialogFragment {
    private static final String TAG = ShutdownDialogFragment.class.getName();

    public interface ShutdownDialogListener {
        void onShutdownCancel(DialogFragment dialog);

        void onShutdown(DialogFragment dialog);
    }

    ShutdownDialogListener listener;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.desktop_shutdown_dialog_title)
                .setMessage(R.string.desktop_shutdown_dialog_details)
                .setNegativeButton(
                        R.string.desktop_shutdown_dialog_negative_action,
                        (dialogInterface, i)
                                -> listener.onShutdownCancel(ShutdownDialogFragment.this)
                )
                .setPositiveButton(
                        R.string.desktop_shutdown_dialog_positive_action,
                        (dialogInterface, i) -> listener.onShutdown(ShutdownDialogFragment.this)
                );
        return builder.create();
    }
}
