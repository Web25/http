#!/usr/bin/env bash

h2spec -t -k -p 8080 -h localhost -j test-result.xml > /dev/null
junit-viewer --results=test-result.xml --save=test-result.html
