#/usr/bin/env bash

# Find the Lit
jq ".. | select(.type? == \"Literal\") | select(.label == \"$1\")"