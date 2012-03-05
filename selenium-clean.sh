#!/bin/bash
find . -name "[0-9a-f]*-[0-9a-f]*-[0-9a-f]*-[0-9a-f]*-[0-9a-f]*" -exec rm '{}' \;
