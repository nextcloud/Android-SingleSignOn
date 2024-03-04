/*
 * Nextcloud Android SingleSignOn Library
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017 David Luhmer <david-dev@live.de>
 * SPDX-FileCopyrightText: 2008-2011 CommonsWare, LLC
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * From _The Busy Coder's Guide to Advanced Android Development_
 * http://commonsware.com/AdvAndroid
 *
 * More information here: https://github.com/abeluck/android-streams-ipc
 */
package com.nextcloud.android.sso.aidl;

interface IInputStreamService {
    ParcelFileDescriptor performNextcloudRequestAndBodyStream(in ParcelFileDescriptor input, 
                                                              in ParcelFileDescriptor requestBodyParcelFileDescriptor);
    ParcelFileDescriptor performNextcloudRequest(in ParcelFileDescriptor input);
    
    ParcelFileDescriptor performNextcloudRequestAndBodyStreamV2(in ParcelFileDescriptor input, 
                                                                in ParcelFileDescriptor requestBodyParcelFileDescriptor);
    ParcelFileDescriptor performNextcloudRequestV2(in ParcelFileDescriptor input);
}
