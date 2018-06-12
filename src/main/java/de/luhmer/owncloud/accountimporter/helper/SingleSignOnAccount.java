package de.luhmer.owncloud.accountimporter.helper;

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

public class SingleSignOnAccount {

    public SingleSignOnAccount(String name, String username, String token, String url, String packageName) {
        this.name = name;
        this.username = username;
        this.token = token;
        this.packageName = packageName;
        this.url = url;
    }

    public String name; // Name of the account in android
    public String username;
    public String token;
    public String packageName;
    public String url;
}
