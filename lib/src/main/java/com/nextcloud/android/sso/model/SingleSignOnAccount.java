package com.nextcloud.android.sso.model;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *  Nextcloud SingleSignOn
 *
 *  @author David Luhmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

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
