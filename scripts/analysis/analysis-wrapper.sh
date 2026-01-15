#!/usr/bin/env bash

# SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
# SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
# SPDX-License-Identifier: GPL-3.0-or-later

BRANCH=$1
LOG_USERNAME=$2
LOG_PASSWORD=$3
BUILD_NUMBER=$4
PR_NUMBER=$5

stableBranch="main"
repository="android-singleSignOn"
codacyProject=160903

ruby scripts/analysis/findbugs-up.rb $1 $2 $3
findbugsValue=$?

# exit codes:
# 0: count was reduced
# 1: count was increased
# 2: count stayed the same

echo "Branch: $1"

if [ $1 = $stableBranch ]; then
    echo "New findbugs result for $stableBranch at: https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html"
    curl -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/$stableBranch.html --upload-file lib/build/reports/spotbugs/spotbugs.html

    summary=$(sed -n "/<h1>Summary<\/h1>/,/<h1>Warnings<\/h1>/p" lib/build/reports/spotbugs/spotbugs.html | head -n-1 | sed s'/<\/a>//'g | sed s'/<a.*>//'g | sed s"/Summary/SpotBugs ($stableBranch)/" | tr "\"" "\'" | tr -d "\r\n")
    curl -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X PUT -d "$summary" https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/findbugs-summary-$stableBranch.html
else
    if [ -e $6 ]; then
        name=$stableBranch"-"$(date +%F)
    else 
        name=$6;
    fi

    echo "New findbugs results at https://www.kaminsky.me/nc-dev/$repository-findbugs/$name.html"
    curl 2>/dev/null -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/$name.html --upload-file lib/build/reports/spotbugs/spotbugs.html

    # delete all old comments, starting with Codacy
    oldComments=$(curl 2>/dev/null -u $1:$2 -X GET https://api.github.com/repos/nextcloud/$repository/issues/$7/comments | jq '.[] | (.id |tostring) + "|" + (.user.login | test("nextcloud-android-bot") | tostring) + "|" + (.body | test("<h1>Codacy.*") | tostring)'  | grep "true|true" | tr -d "\"" | cut -f1 -d"|")

    echo $oldComments | while read comment ; do
        curl 2>/dev/null -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X DELETE https://api.github.com/repos/nextcloud/$repository/issues/comments/$comment
    done

    # findbugs file must exist
    if [ ! -s lib/build/reports/spotbugs/spotbugs.html ] ; then
        echo "spotbugs.html file is missing!"
        exit 1
    fi

    # add comment with results
    if [ $stableBranch = "master" ] ; then
        codacyValue=$(curl 2>/dev/null https://app.codacy.com/dashboards/breakdown\?projectId\=$codacyProject | grep "total issues" | cut -d">" -f3 | cut -d"<" -f1)
        codacyResult="<h1>Codacy</h1>$codacyValue"
    else
        codacyResult=""
    fi

    findbugsResultNew=$(sed -n "/<h1>Summary<\/h1>/,/<h1>Warnings<\/h1>/p" lib/build/reports/spotbugs/spotbugs.html |head -n-1 | sed s'/<\/a>//'g | sed s'/<a.*>//'g | sed s"#Summary#<a href=\"https://www.kaminsky.me/nc-dev/$repository-findbugs/$name.html\">SpotBugs</a> (new)#" | tr "\"" "\'" | tr -d "\n")
    findbugsResultOld=$(curl 2>/dev/null https://www.kaminsky.me/nc-dev/$repository-findbugs/findbugs-summary-$stableBranch.html | tr "\"" "\'" | tr -d "\r\n" | sed s"#SpotBugs#<a href=\"https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html\">SpotBugs</a>#" | tr "\"" "\'" | tr -d "\n")

    if ( [ $findbugsValue -eq 1 ] ) ; then
        findbugsMessage="<h1>SpotBugs increased!</h1>"
    fi

    # check gplay limitation: all changelog files must only have 500 chars
    gplayLimitation=$(scripts/checkGplayLimitation.sh)

    if [ ! -z "$gplayLimitation" ]; then
        gplayLimitation="<h1>Following files are beyond 500 char limit:</h1><br><br>"$gplayLimitation
    fi

    curl -u $1:$2 -X POST https://api.github.com/repos/nextcloud/$repository/issues/$7/comments -d "{ \"body\" : \"$codacyResult $findbugsResultNew $findbugsResultOld $findbugsMessage $gplayLimitation \" }"

    if [ ! -z "$gplayLimitation" ]; then
        exit 1
    fi

    if [ $findbugsValue -eq 2 ]; then
        exit 0
    else
        exit $findbugsValue
    fi
fi
