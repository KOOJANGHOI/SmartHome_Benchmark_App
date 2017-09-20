
from xbee import ZigBee
import serial
import time
import collections
import sys
import getopt
from socket import *
import traceback
from threading import Thread, Lock
import random

# Communication Parameters
ZIGBEE_SERIAL_PORT = "/dev/cu.usbserial-DN01DJIP"
ZIGBEE_SERIAL_BAUD = 115200
UDP_RECEIVE_PORT = 5005

# Radio Parameters
ZIGBEE_DEVICE_ADDRESS = "0000000000000000"
didzigbeeAddressLSBs = False
didzigbeeAddressMSBs = False


def parseCommandLineArgs(argv):
    global ZIGBEE_SERIAL_PORT
    global ZIGBEE_SERIAL_BAUD
    try:
        opts, args = getopt.getopt(
            argv, "hp:b:u:", ["port=", "baud=", "udpport="])

    except getopt.GetoptError:
        print 'test.py -p <serial_port> -b <baud_rate> -u <udp_port>'
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print 'test.py -i <inputfile> -o <outputfile>'
            sys.exit()

        elif opt in ("-p", "--port"):
            ZIGBEE_SERIAL_PORT = arg

        elif opt in ("-b", "--baud"):
            try:
                ZIGBEE_SERIAL_BAUD = int(arg)
            except ValueError:
                print "Buad rate must be an integer"
                sys.exit()

        # elif opt in ("-u", "--udpport"):
        #     try:
        #         UDP_PORT = int(arg)
        #     except ValueError:
        #         print "Udp port must be an integer"
        #         sys.exit()


def printMessageData(data):
    for d in data:
        print d, ' : ',
        for e in data[d]:
            print "{0:02x}".format(ord(e)),
        if (d == 'id'):
            print "({})".format(data[d]),
        print


def convertMessageToString(data):
    retString = ""

    for d in data:
        retString += d + ' : '

        for e in data[d]:
            retString += "{0:02x}".format(ord(e))

        if (d == 'id'):
            retString += "({})".format(data[d])

        retString += "\n"

    return retString


def splitByN(seq, n):
    """A generator to divide a sequence into chunks of n units."""
    while seq:
        yield seq[:n]
        seq = seq[n:]


def hexListToChar(hexList):
    retString = ""
    for h in hexList:
        retString += chr(int(h, 16))
    return retString




def zigbeeMessageCallbackHandler(data):
    global ZIGBEE_DEVICE_ADDRESS
    global didzigbeeAddressLSBs
    global didzigbeeAddressMSBs

    print "================================================================================"
    printMessageData(data)
    print "================================================================================"



if __name__ == "__main__":

    # zigbeeClientCallbackDict["000d6f0003ebf2ee"] = [("127.0.0.1", 5556)]

    # parse the command line arguments
    parseCommandLineArgs(sys.argv[1:])

    # create serial object used for communication to the zigbee radio
    serialConnection = serial.Serial(ZIGBEE_SERIAL_PORT, ZIGBEE_SERIAL_BAUD)
    
    # create a zigbee object that handles all zigbee communication
    # we use this to do all communication to and from the radio
    # when data comes from the radio it will get a bit of unpacking
    # and then a call to the callback specified will be done with the 
    # unpacked data
    zigbeeConnection = ZigBee(
        serialConnection, callback=zigbeeMessageCallbackHandler)



    print "Starting main loop..."
    try:
        # zigbeeConnection.send('tx_explicit',
        #                       frame_id='\x01',
        #                       dest_addr_long='\x00\x0d\x6f\x00\x03\xeb\xf2\xee',
        #                       dest_addr='\xff\xfd',
        #                       src_endpoint='\x00',
        #                       dest_endpoint='\x00',
        #                       cluster='\x00\x00',
        #                       profile='\x00\x00',
        #                       data='\xb4' + '\xee\xf2\xeb\x03\x00\x6f\x0d\x00' + '\x00'
        #                       )

        # zigbeeConnection.send('tx_explicit',
        #                       frame_id='\x01',
        #                       dest_addr_long='\x00\x0d\x6f\x00\x03\xeb\xf2\xee',
        #                       dest_addr='\xff\xfd',
        #                       src_endpoint='\x00',
        #                       dest_endpoint='\x00',
        #                       cluster='\x00\x21',
        #                       profile='\x00\x00',
        #                       data='\xb4' + '\xee\xf2\xeb\x03\x00\x6f\x0d\x00' + '\x01' + '\x05\x04' +
        #                       '\x03' + '\xda\x9a\xd9\x40\x00\xa2\x13\x00' + '\x00'
        #                       )

        # zigbeeConnection.send('tx_explicit',
        #                       frame_id='\x01',
        #                       dest_addr_long='\x00\x0d\x6f\x00\x03\xeb\xf2\xee',
        #                       dest_addr='\x65\x04',
        #                       src_endpoint='\x00',
        #                       dest_endpoint='\x01',
        #                       cluster='\x04\x05',
        #                       profile='\x01\x04',
        #                       data='\x00' + '\xa1' + '\x06' + '\x00' + '\x00\x00' +
        #                       '\x21' + '\x01\x00' + '\x3c\x00' + '\x00\x00'
        #                       )
        # time.sleep(0.1)


        print "looping"
        while True:
            print "sending"
            # zigbeeConnection.send('tx_explicit',
            #           frame_id='\x01',
            #           dest_addr_long='\x00\x0d\x6f\x00\x03\xeb\xf2\xee',
            #           dest_addr='\x9a\xb7',
            #           src_endpoint='\x00',
            #           dest_endpoint='\x00',
            #           cluster='\x00\x00',
            #           profile='\x00\x00',
            #           data='\xb4' + '\xee\xf2\xeb\x03\x00\x6f\x0d\x00' + '\x00'
            #           )


            zigbeeConnection.send('tx_explicit',
                      frame_id='\x01',
                      dest_addr_long='\x00\x0d\x6f\x00\x03\xeb\xf2\xee',
                      dest_addr='\x9a\xb7',
                      src_endpoint='\x00',
                      dest_endpoint='\x00',
                      cluster='\x00\x21',
                      profile='\x00\x00',
                      data='\xb4' + '\xee\xf2\xeb\x03\x00\x6f\x0d\x00' + '\x01' + '\x05\x04' +
                      '\x03' + '\xda\x9a\xd9\x40\x00\xa2\x13\x00' + '\x00'
                      )

            time.sleep(1)

    except KeyboardInterrupt:
        pass
    except:
        traceback.print_exc()


    zigbeeConnection.halt()
    serialConnection.close()




