BASE := ..

include $(BASE)/common.mk

all: checker

PHONY += checker

checker:
	$(JAVAC) -d  $(BASE)/bin -classpath .:$(BASE)/bin:$(CHECKERJARS) iotchecker/*.java
	cp iotchecker/messages.properties $(BASE)/bin/iotchecker

.PHONY: $(PHONY)
