/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017-2019 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.model;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SingleSignOnAccount implements Serializable {

    private static final long serialVersionUID = 21523240203234240L; //assign a long value

    public String name; // Name of the account in android
    public String userId;
    public String token;
    public String url;
    public String type;

    public SingleSignOnAccount(String name, String userId, String token, String url, String type) {
        this.name = name;
        this.userId = userId;
        this.token = token;
        this.url = url;
        this.type = type;
    }

    /** Read the object from Base64 string. */
    public static SingleSignOnAccount fromString(String s) throws IOException, ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        final ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream( data ) );
        final SingleSignOnAccount o = (SingleSignOnAccount) ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string.
     * @param o*/
    public static String toString(SingleSignOnAccount o) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
    }

}
