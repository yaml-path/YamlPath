#!/bin/sh
rm -r target/classes src/main/java
mkdir -p target/classes
mkdir -p src/main/java

for j in '../'; do
  for i in $(find $j -regex .*target/classes); do
    cp -r $i/* target/classes/
  done
  for i in $(find $j -regex .*src/main/java); do
    cp -r $i/* src/main/java/
  done
done

#we don't care about classes in the 'graal' package, because they are only used in native image generation
find target/classes/ -name graal -exec rm -r {} \;

#needed to make sure the script always succeeds
echo "complete"
