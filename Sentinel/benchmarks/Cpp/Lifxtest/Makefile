BASE = ../../..

include $(BASE)/common.mk

all: so-lifxtest-arm so-lightstub-arm so-roomstub-arm zip

ARGS = -shared -fpic

PHONY += lifxtest
lifxtest:
	$(G++) ./Lifxtest.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/Lifxtest.o --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/ -I$(BASE)/benchmarks/drivers/Cpp/LifxLightBulb/ -I$(BASE)/benchmarks/drivers/Cpp/LabRoom/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += so-lifxtest
so-lifxtest:
	$(G++) $(ARGS) ./Lifxtest.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/Lifxtest.so --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/ -I$(BASE)/benchmarks/drivers/Cpp/LifxLightBulb/ -I$(BASE)/benchmarks/drivers/Cpp/LabRoom/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += so-lifxtest-arm
so-lifxtest-arm:
	$(ARM_G++) $(ARGS) ./Lifxtest.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/Lifxtest.so --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/ -I$(BASE)/benchmarks/drivers/Cpp/LifxLightBulb/ -I$(BASE)/benchmarks/drivers/Cpp/LabRoom/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += lightstub
lightstub:
	$(G++) ./LightBulbTest_Stub.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/LightBulbTest_Stub.o --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += so-lightstub
so-lightstub:
	$(G++) $(ARGS) ./LightBulbTest_Stub.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/LightBulbTest_Stub.so --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += so-lightstub-arm
so-lightstub-arm:
	$(ARM_G++) $(ARGS) ./LightBulbTest_Stub.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/LightBulbTest_Stub.so --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += so-roomstub-arm
so-roomstub-arm:
	$(ARM_G++) $(ARGS) ./RoomSmart_Stub.cpp $(BASE)/iotjava/iotruntime/cpp/socket/Socket.cpp -o $(BIN_DIR)/Lifxtest/RoomSmart_Stub.so --std=c++11 -pthread -pg -I$(BASE)/iotjava/iotruntime/cpp/ -I$(BASE)/iotjava/iotruntime/cpp/socket/ -I$(BASE)/iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/iotjava/iotrmi/C++/ -I$(BASE)/benchmarks/virtuals/
	cp ./Lifxtest.config $(BIN_DIR)/Lifxtest

PHONY += zip
zip:
	cd  $(BIN_DIR)/Lifxtest; zip -r Lifxtest.zip *.so

.PHONY: $(PHONY)
