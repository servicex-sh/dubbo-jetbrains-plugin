#!/usr/bin/env just --justfile

# build plugin with changelog
build-plugin:
   ./gradlew -x test patchPluginXml buildPlugin

# publish plugin
publish-plugin:
   ./gradlew -x test patchPluginXml buildPlugin publishPlugin