#!/bin/sh -e

# SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
# SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
# SPDX-License-Identifier: GPL-3.0-or-later

if [ -z $3 ] ; then
    echo "Merge commit to master -> continue with CI"
    exit 0
fi

export BRANCH=$(scripts/analysis/getBranchBase.sh $1 $2 $3 | sed s'/"//'g)
if [ $(git diff --name-only origin/$BRANCH | grep -Ec "^src|build.gradle") -eq 0 ] ; then
    echo "No source files changed"
    exit 1
else
    echo "Source files changed -> continue with CI"
    exit 0
fi
