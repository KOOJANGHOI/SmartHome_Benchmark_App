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
import threading


# -----------------------------------------------------------------------------
# Constants ans Pseudo-Constants
# -----------------------------------------------------------------------------
UDP_RECEIVE_PORT = 5005        # port used for incoming UDP data
UDP_RECEIVE_BUFFER_SIZE = 4096  # max buffer size of an incoming UDP packet
SYSTEM_MASTER_ADDRESS = ("192.168.1.198", 12345) # ip address and portof the system master node
#SYSTEM_MASTER_ADDRESS = ("192.168.2.108", 22222) # ip address and portof the system master node
#SYSTEM_MASTER_ADDRESS2 = ("192.168.2.108", 11111)
#SYSTEM_MASTER_ADDRESS3 = ("192.168.2.108", 11222)

# time for messages to wait for a response before the system clears away that 
# sequence identifier
ZIGBEE_SEQUENCE_NUMBER_CLEAR_TIME_SEC = 5 

#ZIGBEE_SERIAL_PORT = "/dev/cu.usbserial-DN01DCRH"  # USB-Serial port of local radio
ZIGBEE_SERIAL_PORT = "/dev/ttyUSB0"
ZIGBEE_SERIAL_BAUD = 115200                       # Baud rate for above port

# address of our local zigbee radio
#ZIGBEE_DEVICE_ADDRESS = "0013a20040d99cb4"
ZIGBEE_DEVICE_ADDRESS = "xxxxxxxxxxxxxxxx"

# -----------------------------------------------------------------------------
# Global Variables and Objects
# -----------------------------------------------------------------------------

# signals to see if a request needs to be made
didGetLocalRadioHighAddress = False;
didGetLocalRadioLowAddress = False;

# zigbee communications object and its mutex
zigbeeConnection = None
zigbeeConnectionMutex = Lock()

#singleton mabe by changwoo
matchDescriptorReqSingleton = True
deviceAnnouncementSingleton = True
ManagementPermitJoiningReqSuccess = False

# zigbee mapping from long to short object dict
zigbeeLongShortAddr = dict()
zigbeeLongShortAddrMutex = Lock()

# zigbee mapping from a sequence number to a client 
# for correct response handling
zigbeeSeqNumberToClient = dict()
zigbeeSeqNumberToClientMutex = Lock()

zigeeBindRequest = dict()
zigeeBindRequestMutex = Lock()

# Keeps record of where to send callbacks to when an HA message is received
zibeeHACallback = dict()
zibeeHACallbackMutex = Lock()


# Keeps a record of device addresses whose short addresses have not been 
# determined yet
zigbeeUnregisteredAddresses = []
zigbeeUnregisteredAddressesMutex = Lock()

# used to signal all threads to end
doEndFlag = False


# 2 sockets, one for sending (not bound to a port manually)
# and one for receiving, known port binding by application
# both UDP sockets
sendSoceket = socket(AF_INET, SOCK_DGRAM)
receiveSoceket = socket(AF_INET, SOCK_DGRAM)


# zigbee address authority list
zigbeeAddressAuthorityDict = dict()

# made by changwoo
seqNumberForNotification = dict()

# -----------------------------------------------------------------------------
# Helper Methods
# -----------------------------------------------------------------------------
def reverseShortAddress(shortAddr):
    result = shortAddr[len(shortAddr)/2:]+shortAddr[0:len(shortAddr)/2]
    return result

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
            print 'test.py -p <serial_port> -b <baud_rate> -u <udp_port>'
            sys.exit()

        elif opt in ("-p", "--port"):
            ZIGBEE_SERIAL_PORT = arg

        elif opt in ("-b", "--baud"):
            try:
                ZIGBEE_SERIAL_BAUD = int(arg)
            except ValueError:
                print "Buad rate must be an integer"
                sys.exit()


# -------------
# Convenience (Stateless)
# -------------

def hexListToChar(hexList):
    ''' Method to convert a list/string of characters into their corresponding values

        hexList -- list or string of hex characters
    '''
    retString = ""
    for h in hexList:
        retString += chr(int(h, 16))
    return retString

def splitByN(seq, n):
    ''' Method to split a string into groups of n characters

        seq -- string
        n -- group by number
    '''
    return [seq[i:i+n] for i in range(0, len(seq), n)]

def changeEndian(hexString):
    ''' Method to change endian of a hex string

        hexList -- string of hex characters
    '''
    split = splitByN(hexString, 2) # get each byte
    split.reverse();               # reverse ordering of the bytes

    # reconstruct 
    retString = ''
    for s in split:
        retString += s
    return retString

def printMessageData(data):
    ''' Method to print a zigbee message to the console

        data -- pre-parsed zigbee message
    '''
    for d in data:
        print d, ' : ',
        for e in data[d]:
            print "{0:02x}".format(ord(e)),
        if (d == 'id'):
            print "({})".format(data[d]),
        print

def hexStringToZigbeeHexString(hexString):
    ''' Method to change a hex string to a string of characters with the hex values

        hexList -- string of hex characters
    '''
    return hexListToChar(splitByN(hexString, 2))

def zigbeeHexStringToHexString(zigbeeHexString):
    ''' Method to change string of characters with the hex values to a hex string

        hexList -- string of characters with hex values
    '''

    retString = ''
    for e in zigbeeHexString:
        retString += "{0:02x}".format(ord(e))
    return retString

def zclDataTypeToBytes(zclPayload):
    ''' Method to determine data length of a zcl attribute

        zclPayload -- ZCL payload data, must have dataType as first byte
    '''
    attrType = ord(zclPayload[0])

    if(attrType == 0x00):
        return 0
    elif (attrType == 0x08):
        return 1
    elif (attrType == 0x09):
        return 2
    elif (attrType == 0x0a):
        return 3
    elif (attrType == 0x0b):
        return 4
    elif (attrType == 0x0c):
        return 5
    elif (attrType == 0x0d):
        return 6
    elif (attrType == 0x0e):
        return 7
    elif (attrType == 0x0f):
        return 8
    elif (attrType == 0x10):
        return 1
    elif (attrType == 0x18):
        return 1
    elif (attrType == 0x19):
        return 2
    elif (attrType == 0x1a):
        return 3
    elif (attrType == 0x1b):
        return 4
    elif (attrType == 0x1c):
        return 5
    elif (attrType == 0x1d):
        return 6
    elif (attrType == 0x1e):
        return 7
    elif (attrType == 0x1f):
        return 8
    elif (attrType == 0x20):
        return 1
    elif (attrType == 0x21):
        return 2
    elif (attrType == 0x22):
        return 3
    elif (attrType == 0x23):
        return 4
    elif (attrType == 0x24):
        return 5
    elif (attrType == 0x25):
        return 6
    elif (attrType == 0x26):
        return 7
    elif (attrType == 0x27):
        return 8
    elif (attrType == 0x28):
        return 1
    elif (attrType == 0x29):
        return 2
    elif (attrType == 0x2a):
        return 3
    elif (attrType == 0x2b):
        return 4
    elif (attrType == 0x2c):
        return 5
    elif (attrType == 0x2d):
        return 6
    elif (attrType == 0x2e):
        return 7
    elif (attrType == 0x2f):
        return 8
    elif (attrType == 0x30):
        return 1
    elif (attrType == 0x31):
        return 2
    elif (attrType == 0x38):
        return 2
    elif (attrType == 0x39):
        return 4
    elif (attrType == 0x3a):
        return 8
    elif (attrType == 0x41):
        return ord(zclPayload[1])
    elif (attrType == 0x42):
        return ord(zclPayload[1])
    elif (attrType == 0x43):
        return ord(zclPayload[1]) + (256 * ord(zclPayload[2]))
    elif (attrType == 0x44):
        return ord(zclPayload[1]) + (256 * ord(zclPayload[2]))
    elif (attrType == 0xe0):
        return 4
    elif (attrType == 0xe1):
        return 4
    elif (attrType == 0xe2):
        return 4
    elif (attrType == 0xe8):
        return 2
    elif (attrType == 0xe9):
        return 2
    elif (attrType == 0xea):
        return 4
    elif (attrType == 0xf0):
        return 8
    elif (attrType == 0xf1):
        return 16
    elif (attrType == 0xff):
        return 0

# -------------
# Other
# -------------

def createSequenceNumberForClient(addr, packetId):
    ''' Method to get and store a sequence number with a specific client 
        for a specific message.

        addr -- UDP address of the client to associate with the seq. number
        packetId -- packet id from the UDP packet
    '''
    # keep trying to find a number to return
    while(True):

        # get the current time
        epoch_time = int(time.time())

        # get the current list of used numbers
        zigbeeSeqNumberToClientMutex.acquire()
        keysList = zigbeeSeqNumberToClient.keys()
        zigbeeSeqNumberToClientMutex.release()
    
        # if all the numbers are already used
        if(len(keysList) == 256):

            # get a list of all the items
            zigbeeSeqNumberToClientMutex.acquire()
            itemsList = zigbeeSeqNumberToClient.items()
            zigbeeSeqNumberToClientMutex.release()

            # search for a number that is old enough to get rid of otherwise use -1
            seqNum = -1
            for item in itemsList:
                if((epoch_time - item[1][1]) > ZIGBEE_SEQUENCE_NUMBER_CLEAR_TIME_SEC):
                    seqNumber = item[0]
                    break

            if(seqNum != -1):
                # replace the record with new data if we found one to replace
                zigbeeSeqNumberToClientMutex.acquire()
                zigbeeSeqNumberToClient[seqNumber] = (addr, epoch_time, packetId)
                zigbeeSeqNumberToClientMutex.release()

            return seqNumber
            
        else:
            # not all numbers used yet so pick one randomly
            randNum = random.randrange(0,256)

            # check if we are using the number yet
            if(randNum not in keysList):

                # we are not so insert to keep track who this number is for and return it
                zigbeeSeqNumberToClientMutex.acquire()
                zigbeeSeqNumberToClient[randNum] = (addr, epoch_time, packetId)
                zigbeeSeqNumberToClientMutex.release()
                return randNum

def getConnectedRadioLongAddress():
    """ Method to make sure we get the MAC address of our local radio"""
    global zigbeeConnection
    global zigbeeMutex

    # keep looping until we get both the MSBs and the LSBs
    while ((not didGetLocalRadioHighAddress) or (not didGetLocalRadioLowAddress)):

        # reissue requests
        zigbeeConnection.send('at', command="SH")
        zigbeeConnection.send('at', command="SL")
        
        # sleep for a bit to give the radio time to respond before we check again
        #time.sleep(2)
        time.sleep(0.5)

def addressUpdateWorkerMethod():
    ''' Method to keep refreshing the short addresses of the known zigbee devices'''
    global doEndFlag
    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigbeeUnregisteredAddresses
    global zigbeeUnregisteredAddressesMutex
    global zigbeeConnectionMutex
    global zigbeeConnection

    # keep looping until signaled to quit
    while(not doEndFlag):

        addrList = []

        # add unregistered (short addresses unknown) devices so
        # that we can look them up
        zigbeeUnregisteredAddressesMutex.acquire()
        addrList.extend(zigbeeUnregisteredAddresses)
        zigbeeUnregisteredAddressesMutex.release()

        # add the devices that we have short addresses for so we can 
        # get their most recent short addresses
        zigbeeLongShortAddrMutex.acquire()
        addrList.extend(zigbeeLongShortAddr.keys())
        zigbeeLongShortAddrMutex.release()

        # Loop through all the addresses and send messages for each address
        for ad in addrList:

            # create payload for a query on the network for a short address
            payload = '\x00'
            payload += hexStringToZigbeeHexString(changeEndian(ad))
            payload += '\x00'

            # create and send binding command
            zigbeeConnectionMutex.acquire()
	    
            zigbeeConnection.send('tx_explicit',
                                frame_id='\x01',
                                dest_addr_long=hexStringToZigbeeHexString(ad),
                                dest_addr='\xff\xfd',
                                src_endpoint='\x00',
                                dest_endpoint='\x00',
                                cluster='\x00\x00',  
                                profile='\x00\x00',
                                data=payload
                                )
            zigbeeConnectionMutex.release()

        #time.sleep(8)
        time.sleep(1)


# -------------
# UDP 
# -------------

def sendUdpSuccessFail(addr, packetTypeStr, packetIdStr, sucOrFail, reason=None):
    ''' Method to send a success or fail back to a client.

        addr -- UDP address to send packet to
        packetTypeStr -- name of this specific packet
        packetIdStr -- packet id to send
        sucOrFail -- whether this is a success or fail message (True = success)
        reason -- reason of failure (if needed, default is None)

    '''

    global sendSoceket

    # construct the message
    message = "type: " + packetTypeStr.strip() + "\n"
    message += "packet_id: " + packetIdStr + "\n"

    if(sucOrFail):
        message += "response: success \n"
    else:
        message += "response: fail \n"
        message += "reason: " + reason + "\n"

    # send message in a UDP packet
    sendSoceket.sendto(message,addr)

def processUdpZdoBindReqMessage(parsedData, addr):

    shortAddr = None

    if(zigbeeAddressAuthorityDict.has_key(addr)):
        l = zigbeeAddressAuthorityDict[addr]
        if(parsedData['device_address_long'] not in l):
            return
    else:
        return

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()

    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])
        
        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'zdo_bind_request', parsedData['packet_id'], False, 'out_of_space')
            return

        # a bind request was made so must store and wait for response 
        # before we setup callbacks, so keep just the data we need to create the callback
        zigeeBindRequestMutex.acquire()
        zigeeBindRequest[seqNumber] = (parsedData['device_address_long'],
                                        parsedData['cluster_id'], 
                                        parsedData['packet_id'], 
                                        addr)
        zigeeBindRequestMutex.release()

        # construct the short and long addresses of the message for sending
        # make sure they are in the correct format
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)

        # create the payload data
        payloadData = ""
        payloadData += chr(seqNumber)
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['device_address_long']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['device_endpoint']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['cluster_id'])) 
        payloadData += '\x03' 
        payloadData += hexStringToZigbeeHexString(changeEndian(ZIGBEE_DEVICE_ADDRESS))
        payloadData += '\x00'

        # create and send binding command
        zigbeeConnectionMutex.acquire()
        zigbeeConnection.send('tx_explicit',
                            frame_id='\x01',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x00',
                            dest_endpoint='\x00',
                            cluster='\x00\x21',  
                            profile='\x00\x00',
                            data=payloadData
                            )
        zigbeeConnectionMutex.release()


    else:
        # send a failure packet since there is no short address available
        sendUdpSuccessFail(addr, 'zdo_bind_request', parsedData['packet_id'], False, 'short_address_unknown')
        pass

def processUdpZdoUnBindReqMessage(parsedData, addr):
    zibeeHACallbackMutex.acquire();
    if(zibeeHACallback.has_key(parsedData['device_address_long'], parsedData['cluster_id'])):
        zibeeHACallback(parsedData['device_address_long'], parsedData['cluster_id']).remove(addr)
    zibeeHACallbackMutex.release()
    sendUdpSuccessFail(addr, 'zdo_unbind_request', parsedData['packet_id'], True)



def processUdpSendAddressMessage(parsedData, addr):
    ''' Method handle a send address command

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''
    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigbeeUnregisteredAddresses
    global zigbeeUnregisteredAddressesMutex
    global sendSoceket

    print "process send address"
    

    # construct success message
    message = "type: send_address_response\n"
    message += "packet_id: " + parsedData['packet_id'] + "\n"
    message += "response: success\n"

    # tell client that we got their request
    sendSoceket.sendto(message,addr)
    print "responding", message
    
    # construct 
    zigbeeLongShortAddrMutex.acquire()
    doesHaveKey = zigbeeLongShortAddr.has_key(parsedData['device_address_long'])
    zigbeeLongShortAddrMutex.release()

    if(doesHaveKey):
        # long address is already registered with the system so no need to do anything
        return

    # long address not registered so add it for short address lookup
    zigbeeUnregisteredAddressesMutex.acquire()
    zigbeeUnregisteredAddresses.append(parsedData['device_address_long'])
    zigbeeUnregisteredAddressesMutex.release()



#made by changwoo
def processUdpEnrollmentResponse(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])
        
        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'zcl_enrollment_response', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])
	profileId = hexStringToZigbeeHexString(parsedData['profile_id'])

        # create the payload data
        payloadData = ""
        payloadData += '\x01'
        payloadData += chr(seqNumber)
        payloadData += '\x00'
        payloadData += '\x00\x00'

        # create and send binding command
        zigbeeConnectionMutex.acquire()
        zigbeeConnection.send('tx_explicit',
                            frame_id='\x40',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x01',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,  
                            profile=profileId,
                            data=payloadData
                            )
	print '> EnrollmentResponse is sent'
        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'zcl_enrollment_response', parsedData['packet_id'], False, 'short_address_unknown')
        pass




#made by changwoo
def processUdpZclWriteAttributesMessage(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()

    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):
        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])
        
        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'zcl_write_attributes', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
	profileId = hexStringToZigbeeHexString(parsedData['profile_id'])
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])

        # create the payload data
        payloadData = ""
        payloadData += '\x00'
        payloadData += chr(seqNumber)
        payloadData += '\x02'
        payloadData += '\x10\x00'
        payloadData += '\xF0'
#        payloadData += '\xDA\x9A\xD9\x40\x00\xA2\x13\x00'
        payloadData += hexStringToZigbeeHexString(changeEndian(ZIGBEE_DEVICE_ADDRESS))

        zigbeeConnectionMutex.acquire()
        zigbeeConnection.send('tx_explicit',
                            frame_id='\x08',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x01',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,
                            profile=profileId,
                            data=payloadData
                            )

	print ''
	print '> WriteAttributesReq is sent : '+str(shortAddr)
        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'zcl_write_attributes', parsedData['packet_id'], False, 'short_address_unknown')
        pass

#made by changwoo
def processUdpZclChangeSwitchReqMessage(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])

        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'change_switch_request', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])
	clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
	profileId = hexStringToZigbeeHexString(parsedData['profile_id'])
	value = hexStringToZigbeeHexString(parsedData['value'])

        # create and send binding command
        zigbeeConnectionMutex.acquire()

        zigbeeConnection.send('tx_explicit',
                            frame_id='\x40',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x01',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,  
                            profile=profileId,
                            data='\x01'+chr(seqNumber)+value
                            )
        time.sleep(1)
	if parsedData['value']==1:
		print '> The outlet sensor turned on'
	else :
		print '> The outlet sensor turned off'

        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'zcl_read_attributes', parsedData['packet_id'], False, 'short_address_unknown')
        pass


#made by Jiawei
def processUdpZclLockOrUnlockDoorReqMessage(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])

        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'lock_or_unlock_door_request', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
        profileId = hexStringToZigbeeHexString(parsedData['profile_id'])
        value = hexStringToZigbeeHexString(parsedData['value'])

        # create and send binding command
        zigbeeConnectionMutex.acquire()

        zigbeeConnection.send('tx_explicit',
                            frame_id='\x40',
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x01',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,  
                            profile=profileId,
                            data='\x01'+chr(seqNumber)+value
                            )
        time.sleep(1)
        if value == '\x01':
            print '> The door lock is unlocked'
        elif value == '\x00':
            print '> The door lock is locked'
        else:
            print '> Unknown door lock value: ' + str(value)

        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'lock_or_unlock_door_request', parsedData['packet_id'], False, 'short_address_unknown')


#made by Jiawei
def processUdpZclReadDoorStatusReqMessage(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])

        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'read_door_status_request', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
        profileId = hexStringToZigbeeHexString(parsedData['profile_id'])
        # framecontrol = hexStringToZigbeeHexString(parsedData['framecontrol'])
        # commandframe = hexStringToZigbeeHexString(parsedData['commandframe'])
        # attribute_id = hexStringToZigbeeHexString(parsedData['attribute_id'])

        # create and send binding command
        zigbeeConnectionMutex.acquire()

        zigbeeConnection.send('tx_explicit',
                            frame_id='\x40',
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x01',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,  
                            profile=profileId,
                            data='\x10' + chr(seqNumber) + '\x00' + '\x00\x00'
                            )
        time.sleep(1)

        zigbeeConnectionMutex.release()
        print "send read door status"

    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'read_door_status_request', parsedData['packet_id'], False, 'short_address_unknown')


# made by changwoo
def processUdpBroadcastingRouteRecordReqMessage(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])

        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'broadcast_route_record_request', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])

        # create and send binding command
        zigbeeConnectionMutex.acquire()

        zigbeeConnection.send('tx_explicit',
                            frame_id='\x01',
                            # frame_id=chr(seqNumber),
                            dest_addr_long='\x00\x00\x00\x00\x00\x00\xff\xff',
                            dest_addr='\xff\xfe',
                            src_endpoint='\x00',
                            dest_endpoint=dstEndpoint,
                            cluster='\x00\x32',  
                            profile='\x00\x00',
                            data='\x12'+'\x01'
                            )
        time.sleep(1)
	print '> BroadcastingRouteRecordReq is sent'

        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'zcl_read_attributes', parsedData['packet_id'], False, 'short_address_unknown')
        pass


#made by changwoo
def processUdpManagementPermitJoiningReqMessage(parsedData, addr):

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    global matchDescriptorReqSingleton
    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])
        
        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'management_permit_joining_request', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])

        # create the payload data
        payloadData = ""
        payloadData += chr(seqNumber)
        payloadData += '\x5a'
        payloadData += '\x00'

        # create and send binding command
        zigbeeConnectionMutex.acquire()
        zigbeeConnection.send('tx_explicit',
                            frame_id='\x01',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x00',
                            dest_endpoint='\x00',
                            cluster=clusterId,  
                            profile='\x00\x00',
                            data=payloadData
                            )
	print '> ManagementPermitJoiningReq is sent'

	#stop answering 0x6
	matchDescriptorReqSingleton= False
        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'management_permit_joining_request', parsedData['packet_id'], False, 'short_address_unknown')
        pass


def processUdpZclReadAttributesMessage(parsedData, addr):
    ''' Method handle a ZCL read attribute command

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection



    if(zigbeeAddressAuthorityDict.has_key(addr)):
        l = zigbeeAddressAuthorityDict[addr]
        if(parsedData['device_address_long'] not in l):
            return
    else:
        return


    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()


    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])
        
        # send back failure
        if(seqNumber == -1):

            # send an error message, could not get a sequence number to use at this time
            sendUdpSuccessFail(addr, 'zcl_read_attributes', parsedData['packet_id'], False, 'out_of_space')
            return

        # get the info for sending
        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        profileId = hexStringToZigbeeHexString(parsedData['profile_id'])
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])

        # get all the attributes
        attributeIds = parsedData['attribute_ids'].split(',')

        # create the payload data
        payloadData = ""
        payloadData += '\x00'
        payloadData += chr(seqNumber)
        payloadData += '\x00'

        # make all the attributes payloads
        for attr in attributeIds:
            attr = attr.strip()
            attr = changeEndian(attr)
            payloadData += hexStringToZigbeeHexString(attr)

        # create and send binding command
        zigbeeConnectionMutex.acquire()
        zigbeeConnection.send('tx_explicit',
                            frame_id='\x01',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x00',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,  
                            profile=profileId,
                            data=payloadData
                            )
        zigbeeConnectionMutex.release()


    else:
        # send a fail response
        sendUdpSuccessFail(addr, 'zcl_read_attributes', parsedData['packet_id'], False, 'short_address_unknown')
        pass

def processUdpZclConfigureReportingMessage(parsedData, addr):
    ''' Method handle a zcl configure reporting message

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection

    if(zigbeeAddressAuthorityDict.has_key(addr)):
        l = zigbeeAddressAuthorityDict[addr]
        if(parsedData['device_address_long'] not in l):
            return
    else:
        return


    shortAddr = None

    # get the short address for this device long address if possible
    zigbeeLongShortAddrMutex.acquire()
    if(zigbeeLongShortAddr.has_key(parsedData['device_address_long'])):
        shortAddr = zigbeeLongShortAddr[parsedData['device_address_long']]
    zigbeeLongShortAddrMutex.release()

    # if there is a short address than we can send the message
    # if there is not one then we cannot since we need both the short and
    # the long address
    if(shortAddr != None):

        # get a request number
        seqNumber = createSequenceNumberForClient(addr, parsedData['packet_id'])
        
        # send back failure
        if(seqNumber == -1):
            sendUdpSuccessFail(addr, 'zcl_configure_reporting', parsedData['packet_id'], False, 'out_of_space')
            return

        destLongAddr = hexStringToZigbeeHexString(parsedData['device_address_long'])
        destShortAddr = hexStringToZigbeeHexString(shortAddr)
        profileId = hexStringToZigbeeHexString(parsedData['profile_id'])
        clusterId = hexStringToZigbeeHexString(parsedData['cluster_id'])
        dstEndpoint = hexStringToZigbeeHexString(parsedData['device_endpoint'])

        # create the payload data
        payloadData = ""
        payloadData += '\x00'
        payloadData += chr(seqNumber)
        payloadData += '\x06'
        payloadData += '\x00'
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['attribute_id']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['data_type']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['min_reporting_interval']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['max_reporting_interval']))

        if(parsedData.has_key('reportable_change')):
            payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['reportable_change']))


        # create and send binding command
        zigbeeConnectionMutex.acquire()
        zigbeeConnection.send('tx_explicit',
                            frame_id='\x01',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=destLongAddr,
                            dest_addr=destShortAddr,
                            src_endpoint='\x00',
                            dest_endpoint=dstEndpoint,
                            cluster=clusterId,  
                            profile=profileId,
                            data=payloadData
                            )
        zigbeeConnectionMutex.release()


    else:
        sendUdpSuccessFail(addr, 'zcl_configure_reporting', parsedData['packet_id'], False, 'short_address_unknown')
        pass

def processUdpPolicySet(parsedData, addr):
    ''' Method handle a policy set message

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''
    print "=================================================================="
    print "Policy set: ", parsedData
    print 'addr : ', addr


    # do nothing if wrong source
    #if addr == SYSTEM_MASTER_ADDRESS or addr == SYSTEM_MASTER_ADDRESS2 or addr == SYSTEM_MASTER_ADDRESS3 :
    if addr == SYSTEM_MASTER_ADDRESS :
        key = (parsedData['ip_address'], int(parsedData['port']))
        if (zigbeeAddressAuthorityDict.has_key(key)):
            zigbeeAddressAuthorityDict[key].append(parsedData['device_address_long'])
        else:
            zigbeeAddressAuthorityDict[key] = [parsedData['device_address_long']]


def processUdpPolicyClear(parsedData, addr):
    ''' Method handle a policy set message

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''
    print "=================================================================="
    print "Clear policy: ", parsedData
    
    # do nothing if wrong source
    #if addr == SYSTEM_MASTER_ADDRESS or addr == SYSTEM_MASTER_ADDRESS2 or addr == SYSTEM_MASTER_ADDRESS3:
    if addr == SYSTEM_MASTER_ADDRESS :
        zigbeeAddressAuthorityDict.clear()


# -------------
# Zigbee 
# -------------

def processZigbeeATCommandMessage(parsedData):
    ''' Method to process an AT zigbee message

        parsedData -- Pre-parsed (into a dict) data from message.
    '''
    global ZIGBEE_DEVICE_ADDRESS
    global didGetLocalRadioHighAddress
    global didGetLocalRadioLowAddress

    # command response for the high bytes of the local device long address
    if(parsedData['command'] == 'SH'):
        # convert the parameter to a string value (human readable)
        value = ""
        for e in parsedData['parameter']:
            value += "{0:02x}".format(ord(e))

        # set the correct portion of the address
        ZIGBEE_DEVICE_ADDRESS = value + ZIGBEE_DEVICE_ADDRESS[8:]
        
        #signal that we got this part of the address
        didGetLocalRadioHighAddress = True

    # command response for the low bytes of the local device long address
    elif(parsedData['command'] == 'SL'):
        # convert the parameter to a string value (human readable)
        value = ""
        for e in parsedData['parameter']:
            value += "{0:02x}".format(ord(e))

        # set the correct portion of the address
        ZIGBEE_DEVICE_ADDRESS = ZIGBEE_DEVICE_ADDRESS[0:8] + value

        #signal that we got this part of the address
        didGetLocalRadioLowAddress = True

def processZigbeeRxExplicitCommandMessage(parsedData):
    ''' Method to process a rx-explicit zigbee message

        parsedData -- Pre-parsed (into a dict) data from message.
    '''
    global zigeeBindRequestMutex
    global zigeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection
    global ManagementPermitJoiningReqSuccess

    # get the long and short addresses from the message payload since we can 
    # use these to update the short addresses since this short address is fresh
    longAddr = zigbeeHexStringToHexString(parsedData['source_addr_long'])
    shortAddr = zigbeeHexStringToHexString(parsedData['source_addr'])

    # check if this short address is for a device that has yet to be 
    # registered
    zigbeeUnregisteredAddressesMutex.acquire()
    if(longAddr in zigbeeUnregisteredAddresses):
        zigbeeUnregisteredAddresses.remove(longAddr)
    zigbeeUnregisteredAddressesMutex.release()

    # update/ or insert the short address
    zigbeeLongShortAddrMutex.acquire()
    zigbeeLongShortAddr[longAddr] = shortAddr
    zigbeeLongShortAddrMutex.release()

    global matchDescriptorReqSingleton
    global deviceAnnouncementSingleton
    global seqNumberForNotification

    #made by Jiawei
    #doorlock response
    if (parsedData['cluster'] == '\x01\x01' and parsedData['profile'] == '\x01\x04'):
        zclSeqNumber = parsedData['rf_data'][1]
        tup = None
        zigbeeSeqNumberToClientMutex.acquire()
        if(zigbeeSeqNumberToClient.has_key(ord(zclSeqNumber))):
            tup = zigbeeSeqNumberToClient[ord(zclSeqNumber)]
            del zigbeeSeqNumberToClient[ord(zclSeqNumber)]
        zigbeeSeqNumberToClientMutex.release()

        rfdata = parsedData['rf_data']
        framecontrol = rfdata[0]
        command = rfdata[2]

        if tup != None:
            packetId = tup[2]
        
        if framecontrol == '\x19':
            if(command == '\x00'):
                print ''
                print "( 0x0101 ) Door Lock: Lock Door Response"
                print time.strftime("%H:%M:%S", time.localtime())
                value = rfdata[3]
                if(value == '\x00'):
                    print "Door locked successfully"
                else:
                    print "An error occurred in door locking"
            elif(command == '\x01'):
                print ''
                print "( 0x0101 ) Door Lock: Unlock Door Response"
                print time.strftime("%H:%M:%S", time.localtime())
                value = rfdata[3]
                if(value == '\x00'):
                    print "Door unlocked successfully"
                else:
                    print "An error occurred in door unlocking"
            return
        elif framecontrol == '\x18':
            if(command == '\x01'):
                attributeId = (ord(rfdata[3]) * 256) + ord(rfdata[4])
                if attributeId == 0x0000:
                    value = rfdata[7]
                    print "Door status: "
                    if value == '\x00':
                        print "Not fully locked"
                    elif value == '\x01':
                        print "Locked"
                    elif value == '\x02':
                        print "Unlocked"
                    else:
                        print "Unknown value: " + zigbeeHexStringToHexString(value)
                           
                    message = "type : zcl_read_attributes_response \n"
                    message += "packet_id: " + packetId + "\n"
                    message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
                    message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
                    message += "attributes: "

                    attrIdStr = "%0.4x" % attributeId
                    attrIdStr = changeEndian(attrIdStr)
                    message += attrIdStr
                    message += ", "

                    zclPayload = parsedData['rf_data'][3:]
                    zclPayload = zclPayload[3:]
                    attributeType = zclPayload[0]
                    message += "%0.2x" % ord(attributeType)
                    message += ", "

                    message += "success"
                    message += ", "

                    message += "%0.2x" % ord(value)
                    message += ";"

                    message += ";"
                    
                    message = message[0:len(message) - 1]
                    message += "\n"

                    # no one to send the response to so just move on
                    if(tup == None):
                        # cant really do anything here
                        return
                    sendSoceket.sendto(message,tup[0])
            elif command == '\x07':
                status = rfdata[3]
                print ''
                print "( 0x0101 ) Door Lock: Configure reporting response"
                print 'rfdata : ' + zigbeeHexStringToHexString(rfdata)
                if status == '\x00':
                    print "Configure report successfully"
                    message = "type : zcl_configure_reporting_response \n"
                    message += "packet_id: " + packetId + "\n"
                    message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
                    message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
                    message += "attributes: " 
                    message +=  "all_success \n";

                    # no one to send the response to so just move on
                    if(tup == None):
                        # cant really do anything here
                        return
                    sendSoceket.sendto(message,tup[0])
                else:     
                    print "Configure report unsuccessfully, status =", zigbeeHexStringToHexString(status)
            elif(command == '\x0A'):
                print "New update"
                attributeId = (ord(rfdata[3]) * 256) + ord(rfdata[4])
                if attributeId == 0x0000:
                    value = rfdata[6]
                    if value == '\x00':
                        print "Not fully locked"
                    elif value == '\x01':
                        print "Locked"
                    elif value == '\x02':
                        print "Unlocked"
                    else:
                        print "Unknown value: " + zigbeeHexStringToHexString(value)

                    message = "type : zcl_read_attributes_response \n"
                    message += "packet_id: " + ("%0.2x" % ord(zclSeqNumber)) + "\n"
                    message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
                    message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
                    message += "attributes: "

                    attrIdStr = "%0.4x" % attributeId
                    attrIdStr = changeEndian(attrIdStr)
                    message += attrIdStr
                    message += ", "

                    zclPayload = parsedData['rf_data'][3:]
                    zclPayload = zclPayload[3:]
                    attributeType = zclPayload[0]
                    message += "%0.2x" % ord(attributeType)
                    message += ", "

                    message += "success"
                    message += ", "

                    message += "%0.2x" % ord(value)
                    message += ";"

                    message += ";"
                    
                    message = message[0:len(message) - 1]
                    message += "\n"

                    # get callback clients to respond to
                    callbackIndex = (zigbeeHexStringToHexString(parsedData['source_addr_long']), zigbeeHexStringToHexString(parsedData['cluster']))
                    retAddr = None
                    zibeeHACallbackMutex.acquire()
                    if(zibeeHACallback.has_key(callbackIndex)):
                        retAddr = zibeeHACallback[callbackIndex]
                    zibeeHACallbackMutex.release()

                    # no one to respond to so do nothing here
                    if(retAddr == None):
                        return
                    for ra in retAddr:
                        sendSoceket.sendto(message,ra)
            return

    # if this is a ZDO message/response
    #print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    #print parsedData
    #print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    if(parsedData['profile'] == '\x00\x00'):

	# made by changwoo
        # if this is a Match Descriptor Request so we need to answer.
        if(parsedData['cluster'] == '\x00\x06' and matchDescriptorReqSingleton):
            zigbeeConnectionMutex.acquire()
            zigbeeConnection.send('tx_explicit',
                            frame_id='\x08',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=parsedData['source_addr_long'],
                            dest_addr=parsedData['source_addr'],
                            src_endpoint='\x00',
                            dest_endpoint='\x00',
                            cluster='\x00\x06',
                            profile='\x00\x00',
                            data=parsedData['rf_data']
                            )
            time.sleep(1)
            zigbeeConnection.send('tx_explicit',
                            frame_id='\x40',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=parsedData['source_addr_long'],
                            dest_addr=parsedData['source_addr'],
                            src_endpoint='\x00',
                            dest_endpoint='\x00',
                            cluster='\x80\x06',
                            profile='\x00\x00',
                            data=parsedData['rf_data'][0]+ '\x00\x00\x00' + '\x01\x01'
                            )
            time.sleep(1)
            print ''
            print '[ 0x0006 ] Match Descriptor Request - answered'
            print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])
            zigbeeConnectionMutex.release()


        # if this is a device announcement so we can get some useful data from it
        elif(parsedData['cluster'] == '\x00\x13' and deviceAnnouncementSingleton):
            #print parsedData
            # pick out the correct parts of the payload
            longAddr = zigbeeHexStringToHexString(parsedData['rf_data'][3:11])
            shortAddr = zigbeeHexStringToHexString(parsedData['rf_data'][1:3])

            # change the endian of the address
            longAddr = changeEndian(longAddr)
            shortAddr = changeEndian(shortAddr)

            # update the table with the new information
            zigbeeLongShortAddrMutex.acquire()
            zigbeeLongShortAddr[longAddr] = shortAddr
            zigbeeLongShortAddrMutex.release()

            # check if this short address is for a device that has yet to be 
            # registered
            zigbeeUnregisteredAddressesMutex.acquire()
            if(longAddr in zigbeeUnregisteredAddresses):
                zigbeeUnregisteredAddresses.remove(longAddr)
            zigbeeUnregisteredAddressesMutex.release()


	    # made by changwoo
            zigbeeConnectionMutex.acquire()
            zigbeeConnection.send('tx_explicit',
                            frame_id='\x08',
                            # frame_id=chr(seqNumber),
                            dest_addr_long=parsedData['source_addr_long'],
                            dest_addr=parsedData['source_addr'],
                            src_endpoint='\x00',
                            dest_endpoint='\x00',
                            cluster='\x00\x13',
                            profile='\x00\x00',
                            data=parsedData['rf_data']
                            )
	    print ''
	    print '[ 0x0013 ] device announcement - answered'
	    print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])
	    deviceAnnouncementSingleton = False
            zigbeeConnectionMutex.release()


        # if this is a response to a zdo bind_req message
        elif(parsedData['cluster'] == '\x80\x21'):

            # get the status and sequence number from the message
            seqNumber = parsedData['rf_data'][0]
            statusCode = parsedData['rf_data'][1]
            print ">response to a zdo bind_req message parsedData>"

            # get the bind tuple information
            # for this specific bind request
            tup = None
            zigeeBindRequestMutex.acquire() 
            if(zigeeBindRequest.has_key(ord(seqNumber))):
                tup = zigeeBindRequest[ord(seqNumber)]
            zigeeBindRequestMutex.release()

            if(tup == None):
                # cant really do anything in this case...
                # don't have any information on who the data is for
                return

            # successful binding
            if(ord(statusCode) == 0):

                # add a callback for this specific device and cluster 
                # to the HA callback dict 
                zibeeHACallbackMutex.acquire();
                if(zibeeHACallback.has_key((tup[0], tup[1]))):
                    if(tup[3] not in zibeeHACallback[(tup[0], tup[1])]):
                        zibeeHACallback[(tup[0], tup[1])].append(tup[3])
                else:
                    zibeeHACallback[(tup[0], tup[1])] = [tup[3]]
                zibeeHACallbackMutex.release()

                # send success message
                sendUdpSuccessFail(tup[3], 'zdo_bind_request', tup[2], True)

            # Not Supported
            elif (ord(statusCode) == 170):
                sendUdpSuccessFail(tup[3], 'zdo_bind_request', tup[2], False, 'not_supported')

            # Table Full
            elif (ord(statusCode) == 174):
                sendUdpSuccessFail(tup[3], 'zdo_bind_request', tup[2], False, 'table_full')

            # Other issue, dont have code for
            else:
                sendUdpSuccessFail(tup[3], 'zdo_bind_request', tup[2], False, 'other')

        # if this is a response to a short address query
        elif(parsedData['cluster'] == '\x80\x00'):
            print ">response to a short address query 0x8000"
            
            # get a status code
            statusCode = parsedData['rf_data'][0]

            # does not matter if this is not a success, we can try again later
            if(statusCode != '\x00'):
                # status code was not success so do not do anything
                return

            # get the short and long address information
            longAddr = changeEndian(zigbeeHexStringToHexString(parsedData['rf_data'][2:10]))
            shortAddr = changeEndian(zigbeeHexStringToHexString( parsedData['rf_data'][10:12]))

            # remove device from list of unregistered devices if it is in it
            zigbeeUnregisteredAddressesMutex.acquire()
            if(longAddr in zigbeeUnregisteredAddresses):
                zigbeeUnregisteredAddresses.remove(longAddr)
            zigbeeUnregisteredAddressesMutex.release()

            # update/insert the short address
            zigbeeLongShortAddrMutex.acquire()
            zigbeeLongShortAddr[longAddr] = shortAddr
            zigbeeLongShortAddrMutex.release()

	#made by changwoo
        elif(parsedData['cluster'] == '\x80\x06'):
	    print ''
	    print '[ 0x8006 ] get Match Descriptor Response'
	    print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])

	#made by changwoo
        elif(parsedData['cluster'] == '\x80\x36'):
	    print ''
	    print '[ 0x8036 ] get Management Permit Joining Response'
	    print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])

	    ManagementPermitJoiningReqSuccess = True

	#made by changwoo
        else :
	    print ''
	    print '[ '+zigbeeHexStringToHexString(parsedData['cluster'])+' ] ...'
	    print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])


    # if this is a home automation zcl message/response
    elif (parsedData['profile'] == '\x01\x04'):

        # get the zcl message header
        zclFrameControl = parsedData['rf_data'][0]
        zclSeqNumber = parsedData['rf_data'][1]
        zclCommand = parsedData['rf_data'][2]
	zclStatus = parsedData['rf_data'][3]

	#made by changwoo
        if(zclCommand == '\x00'):
	    print ''
	    print '> ('+zigbeeHexStringToHexString(zclStatus)+') notification! : '+ zigbeeHexStringToHexString( parsedData['rf_data'] )
	    
	    # find who to send response 
	    tup = None
            zigbeeSeqNumberToClientMutex.acquire()

	    if(longAddr in seqNumberForNotification):
		key = longAddr
                if(zigbeeSeqNumberToClient.has_key(seqNumberForNotification[key])):
                    tup = zigbeeSeqNumberToClient[seqNumberForNotification[key]]
                    #del zigbeeSeqNumberToClient[seqNumberForNotification] # don't delete.
            zigbeeSeqNumberToClientMutex.release()

            # no one to send the response to so just move on
            if(tup == None):
                # cant really do anything here
                return
            # create the response message
            packetId = tup[2]
            message = "type : zcl_zone_status_change_notification\n"
            message += "packet_id: " + packetId + "\n"
            message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
            message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
            message += "status: " + zigbeeHexStringToHexString(zclStatus) + "\n"
            message += "attributes: success"
            message += "\n"
            # send the socket
            sendSoceket.sendto(message,tup[0])
	    print(">port : ", tup[0][1])



        # this is a zcl read attribute response
        elif(zclCommand == '\x01'):

            # get the zcl payload
            zclPayload = parsedData['rf_data'][3:]
            attibuteResponseList = []

            # get the data for each data
            while(len(zclPayload) > 0):
                attributeId = zclPayload[0:2]
                attributeStatus = zclPayload[2]
                zclPayload = zclPayload[3:]
                
                if(ord(attributeStatus) != 0):
                    # if attribute is not supported then it has no data
                    # package the data and add it to the list
                    attibuteResponseList.append((attributeId,"not_supported"))
                else:

                    # get the data type and data length of the attributre
                    attributeType = zclPayload[0]
                    dataLength = zclDataTypeToBytes(zclPayload)

                    # consume zcl payload data
                    if ((ord(attributeType) == 0x41) or (ord(attributeType) == 0x42)):
                        zclPayload = zclPayload[2:]
                    elif ((ord(attributeType) == 0x43) or (ord(attributeType) == 0x44)):
                        zclPayload = zclPayload[3:]
                    else:
                        zclPayload = zclPayload[1:]

                    # package the data and add it to the list
                    newData = (attributeId,"success", attributeType ,zclPayload[0:dataLength])
                    attibuteResponseList.append(newData)

                    # consume the data size of the payload
                    zclPayload = zclPayload[dataLength:]

            # find who to send response to 
            tup = None
            zigbeeSeqNumberToClientMutex.acquire()
            if(zigbeeSeqNumberToClient.has_key(ord(zclSeqNumber))):
                tup = zigbeeSeqNumberToClient[ord(zclSeqNumber)]
                del zigbeeSeqNumberToClient[ord(zclSeqNumber)]
            zigbeeSeqNumberToClientMutex.release()

            # no one to send the response to so just move on
            if(tup == None):
                # cant really do anything here
                return
            
            # create the response message
            packetId = tup[2]
            message = "type : zcl_read_attributes_response \n"
            message += "packet_id: " + packetId + "\n"
            message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
            message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
            message += "attributes: " 

            # create the message for each attribute
            for t in attibuteResponseList:
                attrId = ord(t[0][0]) + (256 * ord(t[0][1]))
                if(t[1] == "success"):
                    attrIdStr = "%0.4x" % attrId
                    attrIdStr = changeEndian(attrIdStr)

                    message += attrIdStr
                    message += ", "
                    message +=  "success"
                    message += ", "
                    message += "%0.2x" % ord(t[2])
                    message += ", "

                    dat = ""
                    for c in (t[3]):
                        dat += "%0.2x" % ord(c)
                    dat = changeEndian(dat)
                    message += dat
                    message += ";"
                else:
                    attrIdStr = "%0.4x" % attrId
                    attrIdStr = changeEndian(attrIdStr)

                    message += attrIdStr
                    message += ", "
                    message +=  "not_supported"
                    message += ";"

            message = message[0:len(message) - 1]
            message += "\n"
            # send the socket
            sendSoceket.sendto(message,tup[0])




	# made by changwoo
        # this is a zcl write attribute response
	elif(zclCommand == '\x04'):

            # get the zcl payload
            zclPayload = parsedData['rf_data'][3]
	    # the response is '70' which means already resister the mac address or 'success', then let JAVA knows it
	    if(zclStatus == '\x70' or zclPayload == '\x00'):

                # find who to send response to 
                tup = None
                zigbeeSeqNumberToClientMutex.acquire()
                if(zigbeeSeqNumberToClient.has_key(ord(zclSeqNumber))):
                    tup = zigbeeSeqNumberToClient[ord(zclSeqNumber)]
		    seqNumberForNotification[longAddr] = ord(zclSeqNumber)
                    #del zigbeeSeqNumberToClient[ord(zclSeqNumber)]
                zigbeeSeqNumberToClientMutex.release()
                # no one to send the response to so just move on
                if(tup == None):
                    # cant really do anything here
                    return
            
                # create the response message
                packetId = tup[2]
                message = "type : zcl_write_attributes_response\n"
                message += "packet_id: " + packetId + "\n"
                message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
                message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
                message += "attributes: success"
                message += "\n"
                # send the socket
                sendSoceket.sendto(message,tup[0])
	        print ''
	        print '[ 0x0500 ] get Write Attribute Response success'
	        print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])

	    else:
	        print ''
	        print '[ 0x0500 ] get Write Attribute Response'
	        print '> rfdata : '+zigbeeHexStringToHexString(parsedData['rf_data'])



        # this is a zcl configure attribute response
        elif(zclCommand == '\x07'):

            # find who to send response to 
            tup = None
            zigbeeSeqNumberToClientMutex.acquire()
            if(zigbeeSeqNumberToClient.has_key(ord(zclSeqNumber))):
                tup = zigbeeSeqNumberToClient[ord(zclSeqNumber)]
                del zigbeeSeqNumberToClient[ord(zclSeqNumber)]
            zigbeeSeqNumberToClientMutex.release()

            # no one to send the response to so just move on
            if(tup == None):
                # cant really do anything here
                return

            # get zcl payload
            zclPayload = parsedData['rf_data'][3:]
            
            # construct the message
            packetId = tup[2]
            message = "type : zcl_configure_reporting_response \n"
            message += "packet_id: " + packetId + "\n"
            message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
            message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
            message += "attributes: " 

            if(len(zclPayload) == 1):
                # if all the configurations are a success then only send back a success
                # based on zigbee specs
                message +=  "all_success \n";
                sendSoceket.sendto(message,tup[0])
            
            else:
                attibuteResponseList = []
                
                # get each attributes data
                while(len(zclPayload) > 0):
                    attributeStatus = zclPayload[0]
                    attributeDirection = zclPayload[1]
                    attributeId = zclPayload[2:4]
                    zclPayload = zclPayload[4:]

                    newData = (attributeStatus,attributeDirection, attributeId)
                    attibuteResponseList.append(newData)

                # package each attribute 
                for t in attibuteResponseList:
                    attrId = ord(t[2][0]) + (256 * ord(t[2][1]))
                    attrIdStr = "%0.4x" % attrId
                    attrIdStr = changeEndian(attrIdStr)

                    message += attrIdStr
                    message += ", "
                    if(ord(t[0]) == 0):
                        message +=  "success"
                    else:
                        message +=  "error"

                    message += ", "

                    if(ord(t[1]) == 0):
                        message +=  "reported"
                    else:
                        message +=  "received"
                    message += ";"

                message = message[0:len(message) - 1]
                message += "\n"
                sendSoceket.sendto(message,tup[0])

        # this is a zcl report attribute message
        elif(zclCommand == '\x0a'):
	    print "get Report attribute "
            # get teh zcl payload
            zclPayload = parsedData['rf_data'][3:]
            attibuteResponseList = []
 
            # extract the attribute data
            while(len(zclPayload) > 0):
                attributeId = zclPayload[0:2]
                zclPayload = zclPayload[2:]
                attributeType = zclPayload[0]
                dataLength = zclDataTypeToBytes(zclPayload)

                if ((ord(attributeType) == 0x41) or (ord(attributeType) == 0x42)):
                    zclPayload = zclPayload[2:]
                elif ((ord(attributeType) == 0x43) or (ord(attributeType) == 0x44)):
                    zclPayload = zclPayload[3:]
                else:
                    zclPayload = zclPayload[1:]

                newData = (attributeId, attributeType ,zclPayload[0:dataLength])
                attibuteResponseList.append(newData)
                zclPayload = zclPayload[dataLength:]


            # get callback clients to respond to
            callbackIndex = (zigbeeHexStringToHexString(parsedData['source_addr_long']), zigbeeHexStringToHexString(parsedData['cluster']))
            retAddr = None
            zibeeHACallbackMutex.acquire()
            if(zibeeHACallback.has_key(callbackIndex)):
                retAddr = zibeeHACallback[callbackIndex]
            zibeeHACallbackMutex.release()

            # no one to respond to so do nothing here
            if(retAddr == None):
                return

            # construct the message
            message = "type : zcl_report_attributes \n"
            message += "packet_id: " + ("%0.2x" % ord(zclSeqNumber)) + "\n"
            message += "cluster_id: " + zigbeeHexStringToHexString(parsedData['cluster']) + "\n"
            message += "profile_id: " + zigbeeHexStringToHexString(parsedData['profile']) + "\n"
            message += "attributes: " 

            # package the attributes
            for t in attibuteResponseList:
                attrId = ord(t[0][0]) + (256 * ord(t[0][1]))
                attrIdStr = "%0.4x" % attrId
                attrIdStr = changeEndian(attrIdStr)

                message += attrIdStr
                message += ", "
                message += "%0.2x" % ord(t[1])
                message += ", "

                dat = ""
                for c in (t[2]):
                    dat += "%0.2x" % ord(c)
                dat = changeEndian(dat)
                message += dat
                message += ";"

            message = message[0:len(message) - 1]
            message += "\n"
	    print "Sending", message
	    
            # send to all client that want this callback
            for ra in retAddr:
                sendSoceket.sendto(message,ra)

# -----------------------------------------------------------------------------
# Communication Callback/Parse Methods
# -----------------------------------------------------------------------------
def handleNewZigbeeMessage(parsedData):
    ''' Method to process a zigbee message from the local radio.

        parsedData -- Pre-parsed (into a dict) data from message.
    '''
    #print "=================================================================="
    #print ''
    print "New Zigbee Message"
    #printMessageData(parsedData)

    # dispatch to the correct zigbee handler
    if (parsedData['id'] == 'at_response'):
        print "parsedDataID : at_response"
        processZigbeeATCommandMessage(parsedData)

    elif (parsedData['id'] == 'rx_explicit'):
        print "parsedDataID : rx_explicit"
        processZigbeeRxExplicitCommandMessage(parsedData)

    else:
        print "Unknown API format"

    #print "=================================================================="



def handleNewUdpPacket(data, addr):
    ''' Method to parse and handle an incoming UDP packet.

        data -- Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''
    global ManagementPermitJoiningReqSuccess

    #print "=================================================================="
    #print ''
    #print "Got New UDP packet..."
    #print data


    # data comes in as 'key: value\n key: value...' string and so needs to be
    # parsed into a dict
    parsedData = dict()

    # 1 key, value pair per line
    for line in data.split('\n'):

        # key and values are split based on a ':'
        fields = line.split(':')

        # make sure properly formated otherwise just ignore it
        if len(fields) == 2:

            # do strips to remove any white spacing that may have resulted
            # from improper packing on the sender side
            parsedData[fields[0].strip()] = fields[1].strip()


    # wrap in try statement just in case there is an improperly formated packet we
    # can deal with it
    try:
        # dispatch to the correct process method
        if(parsedData["type"] == "zdo_bind_request"):
            print "> processUdpZdoBindReqMessage call"
            processUdpZdoBindReqMessage(parsedData, addr)
        elif(parsedData["type"] == "zdo_unbind_request"):
            processUdpZdoUnBindReqMessage(parsedData, addr)
        elif(parsedData["type"] == "send_address"):
            print "> processUdpSendAddressMessage call"
            processUdpSendAddressMessage(parsedData, addr)
        elif(parsedData["type"] == "zcl_read_attributes"):
            processUdpZclReadAttributesMessage(parsedData, addr)
        elif(parsedData["type"] == "zcl_configure_reporting"):
            print "> zcl_configure_reporting call"
            processUdpZclConfigureReportingMessage(parsedData, addr)
        elif(parsedData["type"] == "policy_set"):
            processUdpPolicySet(parsedData, addr)
        elif(parsedData["type"] == "policy_clear"):
            processUdpPolicyClear(parsedData, addr)
	elif(parsedData["type"] == "management_permit_joining_request"): #made by changwoo
	    processUdpManagementPermitJoiningReqMessage(parsedData, addr)
	elif(parsedData["type"] == "zcl_write_attributes" and ManagementPermitJoiningReqSuccess): #made by changwoo
            processUdpZclWriteAttributesMessage(parsedData, addr)
	elif(parsedData["type"] == "zcl_enrollment_response"): #made by changwoo
	    processUdpEnrollmentResponse(parsedData, addr)
	elif(parsedData["type"] == "zdo_broadcast_route_record_request"): #made by changwoo
	    processUdpBroadcastingRouteRecordReqMessage(parsedData, addr)
	elif(parsedData["type"] == "zcl_change_switch_request"): #made by changwoo
	    processUdpZclChangeSwitchReqMessage(parsedData, addr)
        elif(parsedData["type"] == "zcl_lock_or_unlock_door_request"): #made by Jiawei
            processUdpZclLockOrUnlockDoorReqMessage(parsedData, addr)
        elif(parsedData["type"] == "zcl_read_door_status_request"): #made by Jiawei
            processUdpZclReadDoorStatusReqMessage(parsedData, addr)
        else:
            #print "unknown Packet: " + parsedData["type"]
            pass
    except:
        # if we ever get here then something went wrong and so just ignore this
        # packet and try again later
        print "I didn't expect this error:", sys.exc_info()[0]
        traceback.print_exc()

    #print "=================================================================="


# -----------------------------------------------------------------------------
# Main Running Methods
# -----------------------------------------------------------------------------

def main():
    '''Main function used for starting the application as the main driver'''

    global ZIGBEE_SERIAL_PORT
    global ZIGBEE_SERIAL_BAUD
    global UDP_RECEIVE_PORT
    global zigbeeConnection
    global zigbeeMutex
    global doEndFlag

    parseCommandLineArgs(sys.argv[1:])

    # create serial object used for communication to the zigbee radio
    sc = serial.Serial(ZIGBEE_SERIAL_PORT, ZIGBEE_SERIAL_BAUD)

    # create a zigbee object that handles all zigbee communication
    # we use this to do all communication to and from the radio
    # when data comes from the radio it will get a bit of unpacking
    # and then a call to the callback specified will be done with the
    # unpacked data
    zigbeeConnection = ZigBee(sc, callback=handleNewZigbeeMessage)

    # get the long address of our local radio before we start doing anything
    getConnectedRadioLongAddress();

    # setup incoming UDP socket and bind it to self and specified UDP port
    # sending socket does not need to be bound to anything
    #receiveSoceket.bind(('192.168.2.227', UDP_RECEIVE_PORT))
    receiveSoceket.bind(('192.168.1.192', UDP_RECEIVE_PORT))

    # create the thread that does short address lookups
    addressUpdateWorkerThread = threading.Thread(target=addressUpdateWorkerMethod)
    addressUpdateWorkerThread.start()

    try:
        # Main running loop
        while(True):
            print "=================================================================="
            print ''
	    print "Waiting..."
            print "=================================================================="

            # wait for an incoming UDP packet
            # this is a blocking call
            data, addr = receiveSoceket.recvfrom(4096)

            # handle the UDP packet appropriately
            handleNewUdpPacket(data, addr)

    except KeyboardInterrupt:
        # use the keyboard interupt to catch a ctrl-c and kill the application
        pass

    except:
        # something went really wrong and so exit with error message
        traceback.print_exc()

    # signal all threads to exit
    doEndFlag = True

    # wait for threads to finish before closing of the resources
    addressUpdateWorkerThread.join()


    # make sure to close all the connections
    zigbeeConnection.halt()
    receiveSoceket.close()
    sendSoceket.close()

if __name__ == "__main__":
    # call main function since this is being run as the start
    main()
