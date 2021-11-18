#/usr/bin/env bash

# Arguments are operation to apply to each element,
# e.g. ./Apply.sh ./ModifyLiteral.sh '\"goodbye, world\"'

ORIG=$(jq)
TYPE=$(echo $ORIG | jq  -r type)

if [[ $TYPE = "array" ]];
then
    LENGTH=$(echo $ORIG | jq -r length)
    echo $LENGTH
    for el in $(echo $ORIG | jq -r .[])
    do
        RES=$(echo $el | $@)
        echo $RES
    done
else
    RES=$(echo $ORIG | "$@")
    echo $RES
fi
