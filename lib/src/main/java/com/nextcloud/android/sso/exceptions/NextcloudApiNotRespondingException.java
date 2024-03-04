/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.exceptions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;

public class NextcloudApiNotRespondingException extends SSOException {

    public NextcloudApiNotRespondingException(@NonNull Context context) {
        super(
                context.getString(R.string.nextcloud_files_api_not_responding_message),
                R.string.nextcloud_files_api_not_responding_title,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? R.string.nextcloud_files_api_not_responding_action
                        : null,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        : null
        );
    }

}
