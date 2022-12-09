#!/bin/bash
# This is a comment

# defining a variable
javac -cp ".:./CS361FA.jar" re/REDriver.java
echo -e "\nDoing p3tc1"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc1.txt

echo -e "\nDoing p3tc2"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc2.txt

echo -e "\nDoing p3tc3"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc3.txt

echo -e "\nDoing p3tc4"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc4.txt

echo -e "\nDoing p3tc5"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc5.txt

echo -e "\nDoing p3tc6"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc6.txt

echo -e "\nDoing p3tc7"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc7.txt

echo -e "\nDoing p3tc8"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc8.txt

echo -e "\nDoing p3tc9"
java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc9.txt