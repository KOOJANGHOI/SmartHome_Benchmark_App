BASE := ..
BOOFDIR := ./libs/boofcv_libs
BOOFJARS := $(BOOFDIR)/BoofCV-feature-0.21.jar:$(BOOFDIR)/BoofCV-io-0.21.jar:$(BOOFDIR)/BoofCV-visualize-0.21.jar:$(BOOFDIR)/BoofCV-ip-0.21.jar

include $(BASE)/common.mk

all: interfaces annotation drivers Lifxtest SmartLights Irrigation Speaker HomeSecurity

PHONY += interfaces
interfaces:
	$(MAKE) -C interfaces

PHONY += annotation
annotation:
	$(JAVAC) -d $(BIN_DIR) -classpath $(BIN_DIR):. annotation/*java

PHONY += drivers
drivers:
	$(MAKE) -C drivers/Java
	$(MAKE) -C drivers/Cpp

PHONY += Lifxtest
Lifxtest:
	$(MAKE) -C Java/Lifxtest
	#$(MAKE) -C Cpp/Lifxtest

PHONY += SmartLights
SmartLights:
	$(MAKE) -C Java/SmartLightsController

PHONY += Irrigation
Irrigation:
	$(MAKE) -C Java/IrrigationController

PHONY += Speaker
Speaker:
	$(MAKE) -C Java/SpeakerController

PHONY += HomeSecurity
HomeSecurity:
	$(MAKE) -C Java/HomeSecurityController

.PHONY: $(PHONY)
