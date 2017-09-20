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

all: smartlights

check-all: check-smartlights

PHONY += smartlights
smartlights:
	$(JAVAC) $(JFLAGS) *.java
	cp SmartLightsController.config $(BIN_DIR)/SmartLightsController
	cd $(BIN_DIR)/SmartLightsController; $(JAR) $(JARFLAGS) SmartLightsController.jar ../SmartLightsController/SmartLightsController*.class ../SmartLightsController/ColorTemperature*.class ../SmartLightsController/MotionDetection*.class ../SmartLightsController/*.class ../iotcode/interfaces/Camera*.class ../iotcode/interfaces/Room*.class ../iotcode/interfaces/LightBulb*.class ../iotcode/interfaces/Resolution*.class

PHONY += check-smartlights
check-smartlights:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) *.java
	cp SmartLightsController.config $(BIN_DIR)/SmartLightsController
	cd $(BIN_DIR)/SmartLightsController; $(JAR) $(JARFLAGS) SmartLightsController.jar ../SmartLightsController/SmartLightsController*.class ../SmartLightsController/ColorTemperature*.class ../SmartLightsController/MotionDetection*.class ../SmartLightsController/*.class ../iotcode/interfaces/Camera*.class ../iotcode/interfaces/Room*.class ../iotcode/interfaces/LightBulb*.class ../iotcode/interfaces/Resolution*.class

.PHONY: $(PHONY)
