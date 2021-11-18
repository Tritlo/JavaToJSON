#!/usr/bin/env bash

#jq '.. | select(.children?) | .children[].spoon_class? |= (split(".") | .[-1] | split("$") | .[-1])
#      | .children[].parent = .location | .children[].parent.name = .spoon_class | ., .children[] | select(.parent?) | del(.children) | (. | select(.parent.name == null)), (select(.parent.name?) | (.parent.name |= (split(".") | .[-1] | split("$") | .[-1]))) | [.spoon_class, .location?.start_line, .location?.end_line, .location?.start_col, .location?.end_col, .location?.file, .label, .parent?.start_line, .parent?.end_line, .parent?.start_col, .parent?.end_col, .parent?.name] | @csv' -r


jq '(.. | select(.children?) | select(.spoon_class?)
       | (.spoon_class |= (split(".") | .[-1] | split("$") | .[-1]))
       | .children[].spoon_class |= (split(".") | .[-1] | split("$") | .[-1])
       | .children[].parent = .)
       | (. | select(.type == "RootPac")), .children[]
       | [.spoon_class
         , .location?.start_line, .location?.end_line, .location?.start_col,.location?.end_col, .location?.file
         , .label?
         , .parent?.location?.start_line, .parent?.location?.end_line, .parent?.location?.start_col,.parent?.location?.end_col, .parent?.spoon_class
         , if (.type == "Class") then (.children[]?.children[].label | select( . == "public" or .=="private")) else null end
         ] | @csv' -r