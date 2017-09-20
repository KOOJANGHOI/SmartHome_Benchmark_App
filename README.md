# README #

This README would normally document whatever steps are necessary to get your application up and running.

---

# Settings #

### 1.IoT Cloud Server ###

To compile this server code do the following:

- ssh to dc-6.calit2.uci.edu
 - Example: ssh ayounis@dc-6.calit2.uci.edu
- Clone the git repo
- $cd iotcloud/version2/src/server
- $make

Once the code is compiled it must be placed in the correct location:

- ssh to dc-6.calit2.uci.edu
- cd to the location where the code was compiled
- $sudo rm /usr/lib/cgi-bin/iotcloud.fcgi
- $sudo cp iotcloud.fcgi /usr/lib/cgi-bin/iotcloud.fcgi
- $sudo service apache2 restart

The server is now ready.

### 2.On pc ###

- setup file clone
- app code clone


### 3.Driver program(in RPI #1), Benchmark program(in RPI #4) ###
- setting manual is in the project #2 paper.
	
### 4.On LEDE router ###
- a../clean
- b./nat

----

# Running the Benchmark Application #

### 1.On server ###
- $sudo rm -rf /iotcloud/test.iotcloud/* 
	
### 2.On pc ###
- Clone git repo (git clone https://DoHoNii@bitbucket.org/DoHoNii/new.git)
- $cd new/Sentinel
- $bash BuildSetup.bash
- $bash Setup.bash

- Open the android app(Control2) in android studios (It is in 'new' folder)
- Compile and run the application on an android device
- Shut down the application for now
	
### 3.On RPI #1 ###
- Make sure both the camer, board and all sensors are turned on.
- login as “iotuser” by typing “su iotuser” and input password “1qaz2wsx”.
- Go to “/home/iotuser/iot2/iotjava/iotruntime”
- ./cleanrun.bash [At this point, the router may be disconnected from the Internet. Once again, access to the LEDE and run './clean' and './nat'.] 
- ./run.bash

### 4.Now relaunch the android application ###

----

# Key function of the Application #

- The ability to tell the alarm sensor to ignore sensors
- The ability to turn the alarm off
- Notifications to the smart phone when the alarm is triggered
- The ability to monitor sensors

----
