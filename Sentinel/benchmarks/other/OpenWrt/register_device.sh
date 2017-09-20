#!/bin/sh

# Print usage
if [ "$#" -eq 0 ] || [ "$1" == "-h" ]; then
	echo "Device registration utility for Sentinel system"
	echo "This is a simple script that register a new device"
	echo "into /etc/config/dhcp and /etc/hostapd-psk"
	echo "Copyright (c) 2015-2017, Rahmadi Trimananda <rtrimana@uci.edu> PLRG@UCIrvine"
	echo ""
	echo "Usage:"
	echo "	./register_device.sh [-h]"
	echo "	./register_device.sh [-a <mac-address> <ip-address> <key> <device-name>]"
	echo "	./register_device.sh [-l]"
	echo ""
	echo "Options:"
	echo "	-h	show this usage"
	echo "	-a	adding device by putting MAC address, desired IP address, key, and device name (optional)"
	echo "	-l	show list of devices registered"
	echo ""

elif [ "$1" == "-a" ]; then

	if [ "$2" == "" ] || [ "$3" == "" ] || [ "$4" == "" ]; then
		echo "Empty or incomplete parameters! Please run ./register_device.sh -h for usage."
	else
		# Add a new device
		MAC=$2
		IP=$3
		KEY=$4

		# Keep a local log
		echo "$MAC	$IP	$KEY	$5" >> devices.dat

		# Insert into /etc/hostapd-psk
		echo "$MAC $KEY" >> /etc/hostapd-psk

		# Insert into /etc/config/dhcp
		echo "" >> /etc/config/dhcp
		if [ "$5" != "" ]; then # If device-name is not empty
			echo "# $5" >> /etc/config/dhcp
		fi
		echo "config host" >> /etc/config/dhcp
		echo "	option ip '$IP'" >> /etc/config/dhcp
		echo "	option mac '$MAC'" >> /etc/config/dhcp

		if [ "$5" != "" ]; then	# If device-name is not empty
			echo "	option name '$5'" >> /etc/config/dhcp
		fi
		echo "Device added!"
	fi
                                                 
elif [ "$1" == "-l" ]; then
	# Print list of devices
	echo "List of devices"
	cat devices.dat
	echo ""
	echo "/etc/hostapd-psk"
	cat /etc/hostapd-psk
else
	echo "Unknown option. Please run ./register_device.sh -h for usage."
fi

