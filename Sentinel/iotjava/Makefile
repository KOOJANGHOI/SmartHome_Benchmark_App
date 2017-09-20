BASE := ..

include $(BASE)/common.mk

all: tree parser compiler rmi runtime installer

infra: runtime installer

# Parser compilation and run
PHONY += tree
tree:
	$(JAVAC) -cp .:$(PARSERJARS) -d $(BIN_DIR) iotpolicy/tree/*.java

PHONY += parser
parser:
	$(JAVAC) -cp .:$(PARSERJARS) -d $(BIN_DIR) iotpolicy/parser/*.java

PHONY += compiler
compiler:
	$(JAVAC) -cp .:$(PARSERJARS) -d $(BIN_DIR) iotpolicy/*.java

PHONY += run-compiler-dev
run-compiler-dev:
	cp ../localconfig/iotpolicy/development/*.pol $(BIN_DIR)/iotpolicy/
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler testclasspolicy_profiling.pol testclassrequires_profiling.pol -cplus Cplus -java Java
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler testclasspolicy.pol testclassrequires.pol callbackpolicy.pol callbackrequires.pol -cplus Cplus -java Java
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler callbackpolicy.pol callbackrequires.pol testclasspolicy_advanced.pol testclassrequires_advanced.pol -cplus Cplus -java Java
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler callbackpolicy.pol callbackrequires.pol testclasspolicy_callbacks.pol testclassrequires_callbacks.pol -cplus Cplus -java Java
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler testclasspolicy_callbacks.pol testclassrequires_callbacks.pol callbackpolicy.pol callbackrequires.pol -cplus Cplus -java Java
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler testclasspolicy_callbacks_three.pol testclassrequires_callbacks_three.pol callbackpolicy.pol callbackrequires.pol callbackpolicy_two.pol callbackrequires_two.pol -cplus Cplus -java Java


PHONY += run-compiler-lbtest
run-compiler-lbtest:
	cp ../localconfig/iotpolicy/LifxLightBulb/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/LifxLightBulb/*.req $(BIN_DIR)/iotpolicy/
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler lifxlightbulb.pol lifxtest.req -java Java
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler lifxlightbulb.pol lifxtest.req -cplus Cplus

# SmartLightsController
PHONY += run-compiler-smartlight
run-compiler-smartlight:
	cp ../localconfig/iotpolicy/LifxLightBulb/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/LifxLightBulb/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler lifxlightbulb.pol smartlightsbulb.req amcrestcamera.pol smartlightscam.req motiondetection.pol motiondetection.req room.pol roomsmart.req -java Java

PHONY += run-compiler-lifx
run-compiler-lifx:
	cp ../localconfig/iotpolicy/LifxLightBulb/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/LifxLightBulb/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler lifxlightbulb.pol smartlightsbulb.req -java Java
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler lifxlightbulb.pol smartlightsbulb.req -cplus C++

PHONY += run-compiler-room
run-compiler-room:
	cp ../localconfig/iotpolicy/Room/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/Room/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler room.pol roomsmart.req -java Java
	#cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler room.pol roomsmart.req -cplus C++

PHONY += run-compiler-cam
run-compiler-cam:
	cp ../localconfig/iotpolicy/AmcrestCamera/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/AmcrestCamera/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler amcrestcamera.pol smartlightscam.req motiondetection.pol motiondetection.req -java Java

# IrrigationController
PHONY += run-compiler-irrigation
run-compiler-irrigation:
	cp ../localconfig/iotpolicy/LifxLightBulb/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/LifxLightBulb/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler greenlawn.pol smartlawn.req espsprinkler.pol smartsprinkler.req  weatherphonegateway.pol smartweathergateway.req weathergatewaycallback.pol smartweathergatewaycallback.req sprucesensor.pol smartsensor.req moisturesensorcallback.pol moisturesensorcallback.req -java Java

PHONY += run-compiler-lawn
run-compiler-lawn:
	cp ../localconfig/iotpolicy/GreenLawn/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/GreenLawn/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler greenlawn.pol smartlawn.req -java Java

PHONY += run-compiler-sprk
run-compiler-sprk:
	cp ../localconfig/iotpolicy/EspSprinkler/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/EspSprinkler/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler espsprinkler.pol smartsprinkler.req -java Java

PHONY += run-compiler-wgw
run-compiler-wgw:
	cp ../localconfig/iotpolicy/WeatherPhoneGateway/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/WeatherPhoneGateway/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler weatherphonegateway.pol smartweathergateway.req weathergatewaycallback.pol smartweathergatewaycallback.req -java Java

PHONY += run-compiler-moist
run-compiler-moist:
	cp ../localconfig/iotpolicy/SpruceSensor/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/SpruceSensor/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler sprucesensor.pol smartsensor.req moisturesensorcallback.pol moisturesensorcallback.req -java Java

# SpeakerController
PHONY += run-compiler-speaker
run-compiler-speaker:
	cp ../localconfig/iotpolicy/LifxLightBulb/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/LifxLightBulb/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler ihome.pol smartspeaker.req speakercallback.pol smartspeakercallback.req gpsphonegateway.pol smartgpsgateway.req gpsgatewaycallback.pol smartgpsgatewaycallback.req -java Java

PHONY += run-compiler-spkr
run-compiler-spkr:
	cp ../localconfig/iotpolicy/IHome/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/IHome/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler ihome.pol smartspeaker.req speakercallback.pol smartspeakercallback.req -java Java

PHONY += run-compiler-ggw
run-compiler-ggw:
	cp ../localconfig/iotpolicy/GPSPhoneGateway/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/GPSPhoneGateway/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler gpsphonegateway.pol smartgpsgateway.req gpsgatewaycallback.pol smartgpsgatewaycallback.req -java Java

# HomeSecurityController
PHONY += run-compiler-homesec
run-compiler-homesec:
	cp ../localconfig/iotpolicy/LifxLightBulb/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/LifxLightBulb/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler espalarm.pol smartalarm.req smartthingssensor.pol smartthingssensor.req smartthingssensorcallback.pol smartthingssensorcallback.req -java Java

PHONY += run-compiler-alarm
run-compiler-alarm:
	cp ../localconfig/iotpolicy/EspAlarm/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/EspAlarm/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler espalarm.pol smartalarm.req -java Java

PHONY += run-compiler-smart
run-compiler-smart:
	cp ../localconfig/iotpolicy/SmartthingsSensor/*.pol $(BIN_DIR)/iotpolicy/
	cp ../localconfig/iotpolicy/SmartthingsSensor/*.req $(BIN_DIR)/iotpolicy/
	cd $(BIN_DIR)/iotpolicy; $(JAVA) -cp .:..:../$(PARSERJARS):../$(BIN_DIR) iotpolicy.IoTCompiler smartthingssensor.pol smartthingssensor.req smartthingssensorcallback.pol smartthingssensorcallback.req -java Java

# TODO: Can remove this later - just to test-compile the resulted files from the compiler
PHONY += compile
compile:
	cp ./iotrmi/Java/basics/* $(BIN_DIR)/iotpolicy/output_files/Java
	#cp ./iotrmi/C++/basics/* $(BIN_DIR)/iotpolicy/output_files/Cplus
	cd $(BIN_DIR)/iotpolicy/output_files; cp *.java ./Java
	cd $(BIN_DIR)/iotpolicy/output_files; cp *.hpp ./Cplus
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) TestClass_ProfilingStub.java
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) TestClass_Skeleton.java
	#cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) TestClassAdvanced_Stub.java
	#cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) TestClassCallbacks_Stub.java
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) TestClassInterface_Skeleton.java -Xlint:unchecked
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) TestClassComplete_Stub.java -Xlint:unchecked
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) CallBackInterfaceWithCallBack_Stub.java -Xlint:unchecked
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) CallBackInterface_Skeleton.java -Xlint:unchecked
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) CallBackInterface_CallbackSkeleton.java
#	cd $(BIN_DIR)/iotpolicy/output_files/Java; $(JAVAC) -cp .:..:../../../$(BIN_DIR) CallBackInterfaceWithCallBack_CallbackStub.java

	cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(G++) ./TestClassInterface_Skeleton.cpp -o ./TestClassInterface_Skeleton.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(G++) ./TestClassComplete_Stub.cpp -o ./TestClassComplete_Stub.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cp ./iotrmi/C++/ConcurrentLinkedListQueue.cpp $(BIN_DIR)/iotpolicy/output_files/Cplus
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(G++) ./ConcurrentLinkedListQueue.cpp -o ./ConcurrentLinkedListQueue.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/

	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(G++) ./TestClassAdvanced_Stub.cpp -o ./TestClassAdvanced_Stub.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(G++) ./TestClass.hpp -o ./TestClass.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(G++) ./TestClassInterface_Skeleton.cpp -o ./TestClassInterface_Skeleton.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(ARM_G++) ./TestClassInterface_Skeleton.cpp -o ./TestClassInterface_Skeleton.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(ARM_G++) ./TestClassCallbacks_Stub.cpp -o ./TestClassCallbacks_Stub.out --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(ARM_G++) ./TestClassComplete_Stub.hpp --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/
	#cd $(BIN_DIR)/iotpolicy/output_files/Cplus; $(ARM_G++) ./TestClassInterface_Skeleton.hpp --std=c++11 -pthread -pg -I../../../../iotjava/iotrmi/C++/

PHONY += run-java-skeleton
run-java-skeleton:
	cd ../bin/iotpolicy/output_files/Java; $(JAVA) -cp .:../../../$(BIN_DIR) TestClass_Skeleton

PHONY += run-java-stub
run-java-stub:
	cd ../bin/iotpolicy/output_files/Java; $(JAVA) -cp .:../../../$(BIN_DIR) TestClass_ProfilingStub
#	cd ../bin/iotpolicy/output_files/Java; $(JAVA) -cp .:../../../$(BIN_DIR) TestClass_Stub
#	cd ../bin/iotpolicy/output_files/Java; $(JAVA) -cp .:../../../$(BIN_DIR) TestClassAdvanced_Stub
#	cd ../bin/iotpolicy/output_files/Java; $(JAVA) -cp .:../../../$(BIN_DIR) TestClassCallbacks_Stub

PHONY += run-cplus-skeleton
run-cplus-skeleton:
#	../bin/iotpolicy/output_files/Cplus/TestClass_Skeleton.out
	../bin/iotpolicy/output_files/Cplus/TestClassInterface_Skeleton.out

PHONY += run-cplus-stub
run-cplus-stub:
#	../bin/iotpolicy/output_files/Cplus/TestClass_Stub.out
#	../bin/iotpolicy/output_files/Cplus/TestClassAdvanced_Stub.out
#	../bin/iotpolicy/output_files/Cplus/TestClassCallbacks_Stub.out
	../bin/iotpolicy/output_files/Cplus/TestClassComplete_Stub.out

PHONY += folderclean
folderclean:
	rm -rf $(BIN_DIR)/iotpolicy/output_files

PHONY += clean
clean:
	cd $(BIN_DIR)/iotpolicy/output_files; rm -rf *.class *.gch
	cd $(BIN_DIR)/iotpolicy/output_files/Java; rm -rf *.class
	cd $(BIN_DIR)/iotpolicy/output_files/Cplus; rm -rf *.gch
	cd $(BIN_DIR)/iotpolicy/output_files/Cplus; rm -rf *.out

# RMI compilation and run
PHONY += rmi
rmi:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -cp .:../$(BIN_DIR) -d $(BIN_DIR) iotrmi/Java/*.java
	#$(JAVAC) -cp .:../$(BIN_DIR) -d $(BIN_DIR) iotrmi/Java/sample/*.java
	#mkdir -p $(BIN_DIR)/iotrmi/C++/sample
	#$(G++) iotrmi/C++/sample/TestClass_Stub.cpp -o $(BIN_DIR)/iotrmi/C++/sample/TestClass_Stub.out --std=c++11 -pthread -pg
	#$(G++) iotrmi/C++/sample/TestClass_Skeleton.cpp -o $(BIN_DIR)/iotrmi/C++/sample/TestClass_Skeleton.out --std=c++11 -pthread -pg

PHONY += run-rmiserver
run-rmiserver:
	$(JAVA) -cp .:$(BIN_DIR) iotrmi.Java.sample.TestClass_Skeleton

PHONY += run-rmiclient
run-rmiclient:
	$(JAVA) -cp .:$(BIN_DIR) iotrmi.Java.sample.TestClass_Stub

PHONY += installer
installer:
	$(JAVAC)  -d $(BIN_DIR) iotinstaller/*.java
	cp ../localconfig/iotruntime/MySQLInterface.config $(BIN_DIR)/iotruntime/
	cp ../localconfig/iotruntime/MySQLInterface.config $(BIN_DIR)/iotinstaller/

PHONY += runtime
runtime:
	$(JAVAC) -classpath .:$(RUNTIMEJARS):$(PHONEJARS):$(ZIPJARS) iotruntime/*.java iotruntime/master/*.java iotruntime/slave/*.java iotruntime/messages/*.java iotruntime/stub/*.java iotruntime/zigbee/*.java -d $(BIN_DIR)
	cp ../localconfig/iotruntime/IoTMaster.config $(BIN_DIR)/iotruntime/
	cp ../localconfig/iotruntime/IoTSlave.config $(BIN_DIR)/iotruntime/
	cp ../others/sslkey/*.jks $(BIN_DIR)/iotruntime/
	cp -r ../localconfig/mysql $(BIN_DIR)/iotruntime/
	cp -r ../localconfig/tomoyo $(BIN_DIR)/iotruntime/

PHONY += doc
doc: runtime installer rmi compiler
	$(JAVADOC) -d $(DOCS_DIR) iotinstaller/*.java iotruntime/*.java iotrmi/*.java iotcompiler/*.java

.PHONY: $(PHONY)
