#!/usr/bin/env bash

keytool -genkey -alias dsa -keypass test1234 -keystore test.keystore -storepass test1234
keytool -genkeypair -keystore test.keystore -keyalg RSA -alias rsa_test -keypass test1234 -storepass test1234
