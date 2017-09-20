BASE = ../../..

include $(BASE)/common.mk

JFLAGS = -d $(BIN_DIR) -cp $(BIN_DIR):$(CHECKERJARS):.
JARFLAGS = cf 
JARFLAGS_SMARTLIGHTS = cf Lifxtest.jar

# checker option
#
CHECKER_OPT = -processor iotchecker.IoTJavaChecker -AprintErrorStack

ASTUBS = -Astubs=../../../checker/astubs/ 

all: lifxtest

check-all: check-lifxtest

PHONY += lifxtest
lifxtest:
	$(JAVAC) $(JFLAGS) *.java
	cp Lifxtest.config $(BIN_DIR)/Lifxtest
	cd $(BIN_DIR)/Lifxtest; $(JAR) $(JARFLAGS) Lifxtest.jar ../Lifxtest/Lifxtest*.class ../Lifxtest/LightBulb*.class ../iotcode/LifxLightBulb/*.class ../iotcode/interfaces/LightBulb*.class

PHONY += check-lifxtest
check-lifxtest:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) *.java
	cp Lifxtest.config $(BIN_DIR)/Lifxtest
	cd $(BIN_DIR)/Lifxtest; $(JAR) $(JARFLAGS) Lifxtest.jar ../Lifxtest/Lifxtest*.class ../Lifxtest/LightBulb*.class ../iotcode/LifxLightBulb/*.class ../iotcode/interfaces/LightBulb*.class

.PHONY: $(PHONY)
