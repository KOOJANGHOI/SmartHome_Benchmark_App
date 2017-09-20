BASE = ../../..

include $(BASE)/common.mk

BOOFDIR := ../../libs/boofcv_libs
BOOFJARS := $(BOOFDIR)/BoofCV-feature-0.21.jar:$(BOOFDIR)/BoofCV-io-0.21.jar:$(BOOFDIR)/BoofCV-visualize-0.21.jar:$(BOOFDIR)/BoofCV-ip-0.21.jar:$(CHECKERJARS)
JLAYERDIR := ../../libs/jlayer_libs
JLAYERJARS := $(JLAYERDIR)/jl1.0.1.jar

JFLAGS = -d $(BIN_DIR) -cp $(BOOFJARS):$(BIN_DIR):.:$(JLAYERJARS):$(CHECKERJARS)
JARFLAGS = cf 

# checker option
#
CHECKER_OPT = -processor iotchecker.IoTJavaChecker -AprintErrorStack

ASTUBS = -Astubs=../../../checker/astubs/ 

all: speaker

check-all: check-speaker

PHONY += speaker
speaker:
	$(JAVAC) $(JFLAGS) *.java
	cd  $(BIN_DIR)/SpeakerController; $(JAR) $(JARFLAGS) SpeakerController.jar ../SpeakerController/*.class ../iotcode/interfaces/*.class
	cp SpeakerController.config $(BIN_DIR)/SpeakerController
	zip -r SpeakerController.zip ./music
	mv SpeakerController.zip $(BIN_DIR)/SpeakerController

PHONY += check-speaker
check-speaker:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) *.java
	cd  $(BIN_DIR)/SpeakerController; $(JAR) $(JARFLAGS) SpeakerController.jar ../SpeakerController/*.class ../iotcode/interfaces/*.class
	cp SpeakerController.config $(BIN_DIR)/SpeakerController
	zip -r SpeakerController.zip ./music
	mv SpeakerController.zip $(BIN_DIR)/SpeakerController

.PHONY: $(PHONY)
