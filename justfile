#!/usr/bin/env just --justfile

# build plugin with changelog
build-plugin: clean
   ./gradlew -x test patchPluginXml buildPlugin

# publish plugin
publish-plugin:
   ./gradlew -x test patchPluginXml buildPlugin publishPlugin

# clean compiled resources
clean:
   rm -rf build/classes
   rm -rf build/resources
   rm -rf build/distributions