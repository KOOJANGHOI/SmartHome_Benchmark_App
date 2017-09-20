G++ := g++
ARM_G++ := arm-linux-gnueabihf-g++
#ARM_G++ := arm-linux-gnueabi-g++
JAVA := java
JAR := jar
JAVADOC := javadoc
JAVAC := javac
BIN_DIR := $(BASE)/bin
DOCS_DIR := $(BASE)/doc
RUNTIMEJARS := $(BASE)/jars/asm-all-5.0.3.jar
CHECKERJARS := $(BASE)/jars/checker.jar:$(BASE)/jars/javac.jar
PHONEJARS := $(BASE)/jars/java-json.jar
ZIPJARS := $(BASE)/jars/zip4j_1.3.2.jar
BRILLOJARS := $(BASE)/jars/brillo/*:$(BASE)/jars/brillo/libs/*
PARSERJARS := $(BASE)/jars/java-cup-bin-11b-20160615/*