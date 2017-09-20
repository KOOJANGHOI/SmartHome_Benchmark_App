BASE = ../../..

include $(BASE)/common.mk

BOOFDIR := ../../libs/boofcv_libs
BOOFJARS := $(BOOFDIR)/BoofCV-feature-0.21.jar:$(BOOFDIR)/BoofCV-io-0.21.jar:$(BOOFDIR)/BoofCV-visualize-0.21.jar:$(BOOFDIR)/BoofCV-ip-0.21.jar:$(CHECKERJARS)

JFLAGS = -d $(BIN_DIR) -cp $(BOOFJARS):$(BIN_DIR):$(CHECKERJARS):.
JARFLAGS = cf 

# checker option
#
CHECKER_OPT = -processor iotchecker.IoTJavaChecker -AprintErrorStack

ASTUBS = -Astubs=../../../checker/astubs/ 

all: irrigation

check-all: check-irrigation

PHONY += irrigation
irrigation:
	$(JAVAC) $(JFLAGS) *.java
	cd  $(BIN_DIR)/IrrigationController; $(JAR) $(JARFLAGS) IrrigationController.jar ../IrrigationController/*.class ../iotcode/interfaces/*.class
	cp IrrigationController.config $(BIN_DIR)/IrrigationController
	cp -rf ./resources ./help_files $(BIN_DIR)/IrrigationController
	cd  $(BIN_DIR)/IrrigationController; zip -r IrrigationController.zip ./resources ./help_files

PHONY += check-irrigation
check-irrigation:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) *.java
	cd  $(BIN_DIR)/IrrigationController; $(JAR) $(JARFLAGS) IrrigationController.jar ../IrrigationController/*.class ../iotcode/interfaces/*.class
	cp IrrigationController.config $(BIN_DIR)/IrrigationController
	cp -rf ./resources ./help_files $(BIN_DIR)/IrrigationController
	cd  $(BIN_DIR)/IrrigationController; zip -r IrrigationController.zip ./resources ./help_files

.PHONY: $(PHONY)
