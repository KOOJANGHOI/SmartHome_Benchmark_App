BASE := ../../../..

include $(BASE)/common.mk

all:
	chmod +x gradlew
	./gradlew assembleDebug

install:
	adb -d install -r  ./app/build/outputs/apk/app-debug.apk

clean:
	./gradlew clean

