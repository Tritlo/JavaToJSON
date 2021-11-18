#/usr/bin/env bash

# Find the Literal. Note for e.g. "hello, world", you must write '\"hello, world\"'
jq ".. | select(.type? == \"Literal\") | select(.label == \"$1\")"