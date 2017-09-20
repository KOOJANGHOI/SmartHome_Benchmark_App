BASE = ../..

include $(BASE)/common.mk

JFLAGS = -d $(BIN_DIR) -cp $(BIN_DIR):$(CHECKERJARS):.

# checker option
#
CHECKER_OPT = -processor iotchecker.IoTJavaChecker

ASTUBS = -Astubs=../../checker/astubs/ 

all: check

PHONY += check
check:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) *.java

PHONY += nocheck
nocheck:
	$(JAVAC) $(JFLAGS) *.java

.PHONY: $(PHONY)
