#/usr/bin/env bash

if [ "$1" == "private" ]; then
	VISBILITY=$1
else
	VISIBILITY="public"
fi

if [ "$2" == "method" ]; then
	TYPE="spoon.support.reflect.declaration.CtMethodImpl"
else
	TYPE="spoon.support.reflect.declaration.CtClassImpl"
fi


jq ".. | select(.children?) | select(.children | .[] | .children? | .[] | .label == \"$VISIBILITY\") | select(.spoon_class == \"$TYPE\")"  <&0
