BASE := ../../../..

include $(BASE)/common.mk

all:
	chmod +x gradlew
	./gradlew assembleDebug

install:
	adb -d install ./app/build/outputs/apk/app-debug.apk

clean:
	./gradlew clean

