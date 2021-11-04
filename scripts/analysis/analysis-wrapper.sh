#!/usr/bin/env bash

#1: GIT_USERNAME
#2: GIT_TOKEN
#3: BRANCH
#4: LOG_USERNAME
#5: LOG_PASSWORD
#6: DRONE_BUILD_NUMBER
#7: PULL_REQUEST_NUMBER

stableBranch="master"
repository="android-singleSignOn"
codacyProject=160903

ruby scripts/analysis/lint-up.rb $1 $2 $3
lintValue=$?

ruby scripts/analysis/findbugs-up.rb $1 $2 $3
findbugsValue=$?


./gradlew lib:ktlint
ktlintValue=$?

./gradlew lib:detekt
detektValue=$?

# exit codes:
# 0: count was reduced
# 1: count was increased
# 2: count stayed the same

echo "Branch: $3"

if [ $3 = $stableBranch ]; then
    echo "New findbugs result for $stableBranch at: https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html"
    curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/$stableBranch.html --upload-file lib/build/reports/spotbugs/spotbugs.html

    summary=$(sed -n "/<h1>Summary<\/h1>/,/<h1>Warnings<\/h1>/p" lib/build/reports/spotbugs/spotbugs.html | head -n-1 | sed s'/<\/a>//'g | sed s'/<a.*>//'g | sed s"/Summary/SpotBugs ($stableBranch)/" | tr "\"" "\'" | tr -d "\r\n")
    curl -u $4:$5 -X PUT -d "$summary" https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/findbugs-summary-$stableBranch.html

    if [ $lintValue -ne 1 ]; then
        echo "New lint result for $stableBranch at: https://www.kaminsky.me/nc-dev/$repository-lint/$stableBranch.html"
        curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-lint/$stableBranch.html --upload-file lib/build/reports/lint/lint.html
        exit 0
    fi
else
    if [ -e $6 ]; then
        name=$stableBranch"-"$(date +%F)
    else 
        name=$6;
    fi
    echo "New lint results at https://www.kaminsky.me/nc-dev/$repository-lint/$name.html"
    curl 2>/dev/null -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-lint/$name.html --upload-file lib/build/reports/lint/lint.html

    echo "New findbugs results at https://www.kaminsky.me/nc-dev/$repository-findbugs/$name.html"
    curl 2>/dev/null -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/$name.html --upload-file lib/build/reports/spotbugs/spotbugs.html

    # delete all old comments, starting with Codacy
    oldComments=$(curl 2>/dev/null -u $1:$2 -X GET https://api.github.com/repos/nextcloud/$repository/issues/$7/comments | jq '.[] | (.id |tostring) + "|" + (.user.login | test("nextcloud-android-bot") | tostring) + "|" + (.body | test("<h1>Codacy.*") | tostring)'  | grep "true|true" | tr -d "\"" | cut -f1 -d"|")

    echo $oldComments | while read comment ; do
        curl 2>/dev/null -u $1:$2 -X DELETE https://api.github.com/repos/nextcloud/$repository/issues/comments/$comment
    done

    # lint and findbugs file must exist
    if [ ! -s lib/build/reports/lint/lint.html ] ; then
        echo "lint.html file is missing!"
        exit 1
    fi

    if [ ! -s lib/build/reports/spotbugs/spotbugs.html ] ; then
        echo "spotbugs.html file is missing!"
        exit 1
    fi

    # add comment with results
    lintResultNew=$(grep "Lint Report.* [0-9]* warning" lib/build/reports/lint/lint.html | cut -f2 -d':' |cut -f1 -d'<')

    lintErrorNew=$(echo $lintResultNew | grep "[0-9]* error" -o | cut -f1 -d" ")
    if ( [ -z $lintErrorNew ] ); then
        lintErrorNew=0
    fi

    lintWarningNew=$(echo $lintResultNew | grep "[0-9]* warning" -o | cut -f1 -d" ")
    if ( [ -z $lintWarningNew ] ); then
        lintWarningNew=0
    fi

    lintResultOld=$(curl 2>/dev/null https://raw.githubusercontent.com/nextcloud/$repository/$stableBranch/scripts/analysis/lint-results.txt)
    lintErrorOld=$(echo $lintResultOld | grep "[0-9]* error" -o | cut -f1 -d" ")
    if ( [ -z $lintErrorOld ] ); then
        lintErrorOld=0
    fi

    lintWarningOld=$(echo $lintResultOld | grep "[0-9]* warning" -o | cut -f1 -d" ")
    if ( [ -z $lintWarningOld ] ); then
        lintWarningOld=0
    fi

    if [ $stableBranch = "master" ] ; then
        codacyValue=$(curl 2>/dev/null https://app.codacy.com/dashboards/breakdown\?projectId\=$codacyProject | grep "total issues" | cut -d">" -f3 | cut -d"<" -f1)
        codacyResult="<h1>Codacy</h1>$codacyValue"
    else
        codacyResult=""
    fi

    lintResult="<h1>Lint</h1><table width='500' cellpadding='5' cellspacing='2'><tr class='tablerow0'><td>Type</td><td><a href='https://www.kaminsky.me/nc-dev/"$repository"-lint/"$stableBranch".html'>$stableBranch</a></td><td><a href='https://www.kaminsky.me/nc-dev/"$repository"-lint/"$name".html'>PR</a></td></tr><tr class='tablerow1'><td>Warnings</td><td>"$lintWarningOld"</td><td>"$lintWarningNew"</td></tr><tr class='tablerow0'><td>Errors</td><td>"$lintErrorOld"</td><td>"$lintErrorNew"</td></tr></table>"
    findbugsResultNew=$(sed -n "/<h1>Summary<\/h1>/,/<h1>Warnings<\/h1>/p" lib/build/reports/spotbugs/spotbugs.html |head -n-1 | sed s'/<\/a>//'g | sed s'/<a.*>//'g | sed s"#Summary#<a href=\"https://www.kaminsky.me/nc-dev/$repository-findbugs/$name.html\">SpotBugs</a> (new)#" | tr "\"" "\'" | tr -d "\n")
    findbugsResultOld=$(curl 2>/dev/null https://www.kaminsky.me/nc-dev/$repository-findbugs/findbugs-summary-$stableBranch.html | tr "\"" "\'" | tr -d "\r\n" | sed s"#SpotBugs#<a href=\"https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html\">SpotBugs</a>#" | tr "\"" "\'" | tr -d "\n")


    if ( [ $lintValue -eq 1 ] ) ; then
        lintMessage="<h1>Lint increased!</h1>"
    fi

    if ( [ $findbugsValue -eq 1 ] ) ; then
        findbugsMessage="<h1>SpotBugs increased!</h1>"
    fi

    if ( [ $ktlintValue -eq 1 ] ) ; then
        sed -i ':a;N;$!ba;s#\n#<br />#g;s#^<br />##g' lib/build/ktlint.txt
        curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-ktlint/$name.html --upload-file lib/build/ktlint.txt
        ktlintMessage="<h1>Kotlin lint found errors</h1><a href='https://www.kaminsky.me/nc-dev/$repository-ktlint/$name.html'>Lint</a>"
    fi

    if ( [ $detektValue -eq 1 ] ) ; then
        curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-detekt/$name.html --upload-file lib/build/reports/detekt/detekt.html
        detektMessage="<h1>Detekt errors found</h1><a href='https://www.kaminsky.me/nc-dev/$repository-detekt/$name.html'>Lint</a>"
    fi

    # check gplay limitation: all changelog files must only have 500 chars
    gplayLimitation=$(scripts/checkGplayLimitation.sh)

    if [ ! -z "$gplayLimitation" ]; then
        gplayLimitation="<h1>Following files are beyond 500 char limit:</h1><br><br>"$gplayLimitation
    fi

    curl -u $1:$2 -X POST https://api.github.com/repos/nextcloud/$repository/issues/$7/comments -d "{ \"body\" : \"$codacyResult $lintResult $findbugsResultNew $findbugsResultOld $lintMessage $findbugsMessage $ktlintMessage $detektMessage $gplayLimitation \" }"

    if [ ! -z "$gplayLimitation" ]; then
        exit 1
    fi

    if [ ! $lintValue -eq 2 ]; then
        exit $lintValue
    fi


    if [ $ktlintValue -eq 1 ]; then
        exit 1
    fi

    if [ $detektValue -eq 1 ]; then
        exit 1
    fi

    if [ $findbugsValue -eq 2 ]; then
        exit 0
    else
        exit $findbugsValue
    fi
fi
