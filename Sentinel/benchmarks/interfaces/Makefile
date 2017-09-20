BASE = ../..

include $(BASE)/common.mk

JFLAGS = -d $(BIN_DIR) -cp $(BIN_DIR):.

all: interfaces

PHONY += interfaces
interfaces:
	$(JAVAC) $(JFLAGS) *.java

.PHONY: $(PHONY)
