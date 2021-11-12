#/usr/bin/env bash

# Find the Literal we want to replace using JQ.
RES=$(jq ".. | select(.type? == \"Literal\") | select(.label == \"$1\")")
#Set the new source to the new literal
NEWRES=$(echo $RES | jq ".pretty_printed = $2")
LOC=$(echo $RES | jq -r '.location')
FILE=$(echo $LOC | jq -r '.file')
STARTLINE=$(echo $LOC | jq -r '.start_line')
STARTCOLLESSONE=$(echo $LOC | jq -r '.start_col - 1')
ENDCOLADDONE=$(echo $LOC | jq -r '.end_col +1')
NUMLINES=$(echo $LOC | jq -r '.start_line - .end_line + 1')
NUMLINESNEW=$(echo $NEWRES | jq -r '.pretty_printed' | wc -l)
echo $NUMLINESNEW
echo "diff --git a/$FILE b/$FILE"
echo "--- a/$FILE"
echo "+++ b/$FILE"
echo "@@ -$STARTLINE,$NUMLINES +$STARTLINE,$NUMLINESNEW @@"
LINE=$(sed "${STARTLINE}q;d" $FILE)
echo "-$LINE"
NEWSTART=$(echo "$LINE" | cut -c -$STARTCOLLESSONE)
NEWEND=$(echo "$LINE" | cut -c $ENDCOLADDONE-)
echo "+${NEWSTART}$2${NEWEND}"

