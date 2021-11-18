#/usr/bin/env bash

#To set it to e.g. "goodbye, world", you must pass it '\"goodbye, world\"'
ORIG=$(jq)
#Make sure we only modify literals (e.g. the "type check") by filtering out
#non literals.
FILTERED=$(echo $ORIG | jq "select(.type? == \"Literal\") ")
if [[ ! -z "$FILTERED" ]];
then
    RES=$(echo $FILTERED | jq ".pretty_printed = \"$1\"")
    jq --null-input --argjson NEW "$RES" --argjson OLD "$FILTERED" '{"old": $OLD, "new": $NEW}'
fi
