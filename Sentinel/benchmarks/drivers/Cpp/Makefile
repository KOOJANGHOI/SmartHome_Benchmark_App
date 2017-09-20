BASE = ../../..

include $(BASE)/common.mk

all: so-light-arm so-lightskel-arm lightzip so-labroom-arm so-labroomskel-arm labroomzip

ARGS = -shared -fpic

# Compile
#
# LightBulb
PHONY += light
light:
	cd LifxLightBulb/; $(G++) ./LifxLightBulb.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LifxLightBulb.o --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += light-arm
light-arm:
	cd LifxLightBulb/; $(ARM_G++) ./LifxLightBulb.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LifxLightBulb.o --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += so-light
so-light:
	cd LifxLightBulb/; $(G++) $(ARGS) ./LifxLightBulb.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LifxLightBulb.so --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += so-light-arm
so-light-arm:
	cd LifxLightBulb/; $(ARM_G++) $(ARGS) ./LifxLightBulb.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LifxLightBulb.so --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += lightskel
lightskel:
	cd LifxLightBulb/; $(G++) ./LightBulb_Skeleton.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LightBulb_Skeleton.o --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += so-lightskel
so-lightskel:
	cd LifxLightBulb/; $(G++) $(ARGS) ./LightBulb_Skeleton.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LightBulb_Skeleton.so --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += so-lightskel-arm
so-lightskel-arm:
	cd LifxLightBulb/; $(ARM_G++) $(ARGS) ./LightBulb_Skeleton.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LifxLightBulb/LightBulb_Skeleton.so --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb

PHONY += lightzip
lightzip:
	cd  $(BIN_DIR)/iotcode/LifxLightBulb/; zip -r LifxLightBulb.zip *.so

# LabRoom
PHONY += so-labroom-arm
so-labroom-arm:
	cd LabRoom/; $(ARM_G++) $(ARGS) ./LabRoom.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LabRoom/LabRoom.so --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LabRoom/LabRoom.config $(BIN_DIR)/iotcode/LabRoom

PHONY += so-labroomskel-arm
so-labroomskel-arm:
	cd LabRoom/; $(ARM_G++) $(ARGS) ./Room_Skeleton.cpp $(BASE)/../iotjava/iotruntime/cpp/socket/Socket.cpp -o ../$(BIN_DIR)/iotcode/LabRoom/Room_Skeleton.so --std=c++11 -pthread -pg -I$(BASE)/../iotjava/iotruntime/cpp/ -I$(BASE)/../iotjava/iotruntime/cpp/socket/ -I$(BASE)/../iotjava/iotruntime/cpp/setrelation/ -I$(BASE)/../iotjava/iotrmi/C++/ -I$(BASE)/../benchmarks/virtuals/
	cp LabRoom/LabRoom.config $(BIN_DIR)/iotcode/LabRoom

PHONY += labroomzip
labroomzip:
	cd  $(BIN_DIR)/iotcode/LabRoom/; zip -r LabRoom.zip *.so

.PHONY: $(PHONY)
