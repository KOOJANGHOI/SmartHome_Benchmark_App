BASE = ../../../..

include $(BASE)/common.mk

#GCCFLAGS = -Wall -ansi -pedantic -g -std=c++11 -pthread -pg
GCCFLAGS = -std=c++11 -pthread -pg
INCLUDE =  -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/ -I$(BASE)/benchmarks/drivers/Cpp/LifxLightBulb -I$(BASE)/benchmarks/drivers/Cpp/LabRoom -I$(BASE)/benchmarks/Cpp/Lifxtest/
CCCLINKERFLAGS = -ldl

all: java cpp

PHONY += java
java:
	javac *.java

PHONY += cpp
cpp:
	$(G++) $(GCCFLAGS) -o IoTSlave.o IoTSlave.cpp $(INCLUDE) $(CCCLINKERFLAGS)

PHONY += cpp-arm
cpp-arm:
	$(ARM_G++) $(GCCFLAGS) -o IoTSlave.o IoTSlave.cpp $(INCLUDE) $(CCCLINKERFLAGS)
	cp IoTSlave.o $(BASE)/bin/iotruntime/slave/

PHONY += cpp-test
cpp-test:
	$(G++) $(GCCFLAGS) -o SetRelationTest.o SetRelationTest.cpp $(INCLUDE) $(CCCLINKERFLAGS)

PHONY += run
run:
	java IoTSlave

PHONY += readlog
readlog:
	cat *.log

PHONY += clean
clean:
	rm -rf *.class
	rm -rf *.o
	rm -rf *.log
	rm -rf gmon.out
	rm -rf *.zip
	rm -rf *.jar
	#rm -rf *.so
	pkill IoTSlave

PHONY += kill
kill:
	pkill IoTSlave

.PHONY: $(PHONY)
