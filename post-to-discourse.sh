#!/bin/bash

set -e

if [ -z $DISCOURSE_HOSTNAME ];
then
    echo "DISCOURSE_HOSTNAME is not set.";
    exit 1;
fi

if [ -z $DISCOURSE_API_KEY ];
then
    echo "DISCOURSE_API_KEY is not set.";
    exit 1;
fi

if [ -z $DISCOURSE_USERNAME ];
then
    echo "DISCOURSE_USERNAME is not set.";
    exit 1;
fi

if [ -z $DISCOURSE_CATEGORY_ID ];
then
    echo "DISCOURSE_CATEGORY_ID is not set.";
    exit 1;
fi

if [ -z "$DISCOURSE_SDK_NAME" ];
then
    echo "DISCOURSE_SDK_NAME is not set.";
    exit 1;
fi

if [ -n $1 ];
then
    release_version_number=$1
    release_version_number="${release_version_number:1}"

    cd $DISCOURSE_DIR

    awk "
      /## $release_version_number/{flag=1;next}
      /## [0-9]/{flag=0}
      flag
    " USERGUIDE.md > _releasenotes.md

    RELEASE_NOTES=$(cat _releasenotes.md)

    curl --location --request POST "https://$DISCOURSE_HOSTNAME/posts.json" \
    --header "Content-Type: application/json; charset=utf-8" \
    --header "Api-Key: $DISCOURSE_API_KEY" \
    --header "Api-Username: $DISCOURSE_USERNAME" \
    --data-raw "{
      \"category\": $DISCOURSE_CATEGORY_ID,
      \"title\": \"$DISCOURSE_SDK_NAME v${release_version_number} released 🚀\",
      \"tags\": [\"android\", \"mobile\", \"mobilesdk\", \"release\", \"mag-sdk\"],
      \"raw\": \"Hi,\n We are pleased to announce that we have released $DISCOURSE_SDK_NAME SDK v${release_version_number}!\n$RELEASE_NOTES\"
    }"
else
    echo "version input (format: vX.Y.Z) is missing.";
    exit 1;
fi