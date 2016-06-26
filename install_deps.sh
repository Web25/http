#!/usr/bin/env bash

git clone https://gitlab.com/xjs/dynamic.git dynamic
cd dynamic
mvn install -Dmaven.javadoc.skip=true -DskipTests=true -B -V
cd ..

pip install gunicorn httpbin
gunicorn httpbin:app