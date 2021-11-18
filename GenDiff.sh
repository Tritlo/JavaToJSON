#/usr/bin/env bash

ORIG=$(jq)
#Set the new source to the new literal
RES=$(echo $ORIG | jq '.old')
NEWRES=$(echo $ORIG | jq '.new')
LOC=$(echo $RES | jq -r '.location')
FILE=$(echo $LOC | jq -r '.file')
STARTLINE=$(echo $LOC | jq -r '.start_line')
STARTCOLLESSONE=$(echo $LOC | jq -r '.start_col - 1')
ENDCOLADDONE=$(echo $LOC | jq -r '.end_col +1')
NUMLINES=$(echo $LOC | jq -r '.start_line - .end_line + 1')
NEWSTR=$(echo $NEWRES | jq -r '.pretty_printed')
NUMLINESNEW=$(echo $NEWSTR | wc -l)
echo "diff --git a/$FILE b/$FILE"
echo "--- a/$FILE"
echo "+++ b/$FILE"
echo "@@ -$STARTLINE,$NUMLINES +$STARTLINE,$NUMLINESNEW @@"
LINE=$(sed "${STARTLINE}q;d" $FILE)
echo "-$LINE"
NEWSTART=$(echo "$LINE" | cut -c -$STARTCOLLESSONE)
NEWEND=$(echo "$LINE" | cut -c $ENDCOLADDONE-)
echo "+${NEWSTART}$NEWSTR${NEWEND}"

