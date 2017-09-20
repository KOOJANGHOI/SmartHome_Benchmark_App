from zigbee import *
from socket import *
from select import *
import serial
import time
import collections
import sys
import getopt
import traceback
from threading import Thread, Lock
import random
import threading


# -----------------------------------------------------------------------------
# Constants ans Pseudo-Constants
# -----------------------------------------------------------------------------
UDP_RECEIVE_PORT = 5005        # port used for incoming UDP data
UDP_RECEIVE_BUFFER_SIZE = 4096  # max buffer size of an incoming UDP packet

# time for messages to wait for a response before the system clears away that 
# sequence identifier
ZIGBEE_SEQUENCE_NUMBER_CLEAR_TIME_SEC = 5 

# address of our local zigbee radio
ZIGBEE_DEVICE_ADDRESS = "xxxxxxxxxxxxxxxx"

SYSTEM_MASTER_ADDRESS = ("192.168.2.108", 12345) # ip address and portof the system master node


# -----------------------------------------------------------------------------
# Global Variables and Objects
# -----------------------------------------------------------------------------

# zigbee communications object and its mutex
zigbeeConnection = None
zigbeeConnectionMutex = Lock()

# zigbee mapping from long to short object dict
zigbeeLongShortAddr = dict()
zigbeeLongShortAddrMutex = Lock()

# zigbee mapping from a sequence number to a client 
# for correct response handling
zigbeeSeqNumberToClient = dict()
zigbeeSeqNumberToClientMutex = Lock()

zigbeeBindRequest = dict()
zigbeeBindRequestMutex = Lock()

# Keeps record of where to send callbacks to when an HA message is received
zigbeeHACallback = dict()
zigbeeHACallbackMutex = Lock()

# Keeps a record of device addresses whose short addresses have not been 
# determined yet
zigbeeUnregisteredAddresses = []
zigbeeUnregisteredAddressesMutex = Lock()

# used to signal all threads to end
doEndFlag = False


# 2 sockets, one for sending (not bound to a port manually)
# and one for receiving, known port binding by application
# both UDP sockets
sendSocket = socket(AF_INET, SOCK_DGRAM)
receiveSocket = socket(AF_INET, SOCK_DGRAM)

# zigbee address authority list
zigbeeAddressAuthorityDict = dict()



# -----------------------------------------------------------------------------
# Helper Methods
# -----------------------------------------------------------------------------


def parseCommandLineArgs(argv):
    try:
        opts, args = getopt.getopt(
            argv, "h:u:", ["udpport="])

    except getopt.GetoptError:
        print 'test.py -u <udp_port>'
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print 'test.py -u <udp_port>'
            sys.exit()


# -------------
# Convenience (Stateless)
# -------------

def shortToStr(short):
    s=chr(short>>8) + chr(short & 0xff)
    return s

def hexToInt(str):
    ret = 0
    for h in str:
        ret = ret << 4
        ret += int(h, 16)
    return ret

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

def reverse(string):
    ''' Method to change endian of a hex string

        hexList -- string of hex characters
    '''
    return string[::-1]

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

def hexStringToAddr(hexString):
    ''' Method to change a hex string to a string of characters with the hex values

        hexList -- string of hex characters
    '''
    split = splitByN(hexString, 2)
    newstring = '[' + split[0]
    for h in split[1:]:
        newstring += ':' + h
    newstring += ']!'
    return newstring

def addrToHexString(addr):
    hex=addr[1:25]
    list=[hex[i:i+2] for i in range(0, len(hex), 3)]    
    retstring = ''
    for e in list:
        retstring += e
    return retstring
    
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
    global ZIGBEE_DEVICE_ADDRESS
    
    data = ddo_get_param(None, 'SH')
    valuehi = ""
    for e in data:
        valuehi += "{0:02x}".format(ord(e))

    data = ddo_get_param(None, 'SL')
    valuelo = ""
    for e in data:
        valuelo += "{0:02x}".format(ord(e))
    ZIGBEE_DEVICE_ADDRESS = valuehi + valuelo
    
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
            # The Format of the tuple is:
            #  (address_string, endpoint, profile_id, cluster_id)
           
            DESTINATION=(hexStringToAddr(ad), 0x0, 0x0, 0x0)

            zigbeeConnection.sendto(payload, 0, DESTINATION);
            zigbeeConnectionMutex.release()

        time.sleep(30)


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

    global sendSocket

    # construct the message
    message = "type: " + packetTypeStr.strip() + "\n"
    message += "packet_id: " + packetIdStr + "\n"

    if(sucOrFail):
        message += "response: success \n"
    else:
        message += "response: fail \n"
        message += "reason: " + reason + "\n"

    # send message in a UDP packet
    sendSocket.sendto(message,addr)

def processUdpZdoBindReqMessage(parsedData, addr):
    ''' Method handle a zdo bind request message

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''
    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigbeeBindRequestMutex
    global zigbeeBindRequest
    global zigbeeConnectionMutex
    global zigbeeConnection


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
        zigbeeBindRequestMutex.acquire()
        zigbeeBindRequest[seqNumber] = (parsedData['device_address_long'],
                                        parsedData['cluster_id'], 
                                        parsedData['packet_id'], 
                                        addr)
        zigbeeBindRequestMutex.release()

        # construct the short and long addresses of the message for sending
        # make sure they are in the correct format
        destLongAddr = parsedData['device_address_long']
        destShortAddr = hexStringToZigbeeHexString(shortAddr)

        # create the payload data
        payloadData = ""
        payloadData += chr(seqNumber)
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['device_address_long']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['device_endpoint']))
        payloadData += hexStringToZigbeeHexString(changeEndian(parsedData['cluster_id'])) 
        payloadData += '\x03' 
        payloadData += hexStringToZigbeeHexString(changeEndian(ZIGBEE_DEVICE_ADDRESS))
        payloadData += '\x01'

        # create and send binding command
        zigbeeConnectionMutex.acquire()

        # The Format of the tuple is:
        #  (address_string, endpoint, profile_id, cluster_id)
        DESTINATION = (hexStringToAddr(destLongAddr), 0x0, 0x0, 0x21)
        zigbeeConnection.sendto(payloadData, 0, DESTINATION)

        zigbeeConnectionMutex.release()


    else:
        # send a failure packet since there is no short address available
        sendUdpSuccessFail(addr, 'zdo_bind_request', parsedData['packet_id'], False, 'short_address_unknown')
        pass


def processUdpZdoUnBindReqMessage(parsedData, addr):
    zigbeeHACallbackMutex.acquire();
    if(zigbeeHACallback.has_key(parsedData['device_address_long'], parsedData['cluster_id'])):
        zigbeeHACallback(parsedData['device_address_long'], parsedData['cluster_id']).remove(addr)
    zigbeeHACallbackMutex.release()
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
    global sendSocket

    if(zigbeeAddressAuthorityDict.has_key(addr)):
        l = zigbeeAddressAuthorityDict[addr]
        if(parsedData['device_address_long'] not in l):
            return
    else:
        return

    
    # construct success message
    message = "type: send_address_response\n"
    message += "packet_id: " + parsedData['packet_id'] + "\n"
    message += "response: success\n"

    # tell client that we got their request
    sendSocket.sendto(message,addr)

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

def processUdpZclReadAttributesMessage(parsedData, addr):
    ''' Method handle a ZCL read attribute command

        parsedData -- Pre-parsed Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''

    global zigbeeLongShortAddr
    global zigbeeLongShortAddrMutex
    global zigbeeBindRequestMutex
    global zigbeeBindRequest
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
        profileId = parsedData['profile_id']
        clusterId = parsedData['cluster_id']
        dstEndpoint = parsedData['device_endpoint']

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


        #  (address_string, endpoint, profile_id, cluster_id)
        DESTINATION = (hexStringToAddr(zigbeeHexStringToHexString(destLongAddr)), hexToInt(dstEndpoint), hexToInt(profileId), hexToInt(clusterId))

        # create and send binding command
        zigbeeConnectionMutex.acquire()
        zigbeeConnection.sendto(payloadData, 0, DESTINATION)
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
    global zigbeeBindRequestMutex
    global zigbeeBindRequest
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
        profileId = parsedData['profile_id']
        clusterId = parsedData['cluster_id']
        dstEndpoint = parsedData['device_endpoint']

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

       DESTINATION = (hexStringToAddr(zigbeeHexStringToHexString(destLongAddr)), hexToInt(dstEndpoint), hexToInt(profileId), hexToInt(clusterId))

        # create and send binding command
        zigbeeConnectionMutex.acquire()

        zigbeeConnection.sendto(payloadData, 0, DESTINATION)
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
    
    # do nothing if wrong source
    if addr == SYSTEM_MASTER_ADDRESS:
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
    if addr == SYSTEM_MASTER_ADDRESS:
        zigbeeAddressAuthorityDict.clear()

    
def processZigbeeRxExplicitCommandMessage(rawData, src_addr):
    ''' Method to process a rx-explicit zigbee message

        parsedData -- Pre-parsed (into a dict) data from message.
    '''

#    The format for addr is the tuple (address_string, endpoint, profile_id, cluster_id).
    global zigbeeBindRequestMutex
    global zigbeeBindRequest
    parsedData = dict()
    parsedData['source_addr_long']=hexStringToZigbeeHexString(addrToHexString(src_addr[0]))
    parsedData['cluster']=shortToStr(src_addr[3])
    parsedData['profile']=shortToStr(src_addr[2])
    parsedData['rf_data']=rawData
    
    # get the long and short addresses from the message payload since we can 
    # use these to update the short addresses since this short address is fresh
    longAddr = zigbeeHexStringToHexString(parsedData['source_addr_long'])

    # check if this short address is for a device that has yet to be 
    # registered

    # if this is a ZDO message/response
    if(parsedData['profile'] == '\x00\x00'):

        # if this is a device announcement so we can get some useful data from it
        if(parsedData['cluster'] == '\x00\x13'):
            
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

        # if this is a response to a zdo bind_req message
        elif(parsedData['cluster'] == '\x80\x21'):

            # get the status and sequence number from the message
            seqNumber = parsedData['rf_data'][0]
            statusCode = parsedData['rf_data'][1]

            # get the bind tuple information
            # for this specific bind request
            tup = None
            zigbeeBindRequestMutex.acquire() 
            if(zigbeeBindRequest.has_key(ord(seqNumber))):
                tup = zigbeeBindRequest[ord(seqNumber)]
            zigbeeBindRequestMutex.release()

            if(tup == None):
                # cant really do anything in this case...
                # don't have any information on who the data is for
                return

            # successful binding
            if(ord(statusCode) == 0):

                # add a callback for this specific device and cluster 
                # to the HA callback dict 
                if(zigbeeHACallback.has_key((tup[0], tup[1]))):
                    if(tup[3] not in zigbeeHACallback[(tup[0], tup[1])]):
                        zigbeeHACallback[(tup[0], tup[1])].append(tup[3])
                else:
                    zigbeeHACallback[(tup[0], tup[1])] = [tup[3]]

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

    # if this is a home automation zcl message/response
    elif (parsedData['profile'] == '\x01\x04'):

        # get the zcl message header
        zclFrameControl = parsedData['rf_data'][0]
        zclSeqNumber = parsedData['rf_data'][1]
        zclCommand = parsedData['rf_data'][2]

        # this is a zcl read attribute response
        if(zclCommand == '\x01'):

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
            sendSocket.sendto(message,tup[0])

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
                sendSocket.sendto(message,tup[0])
            
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
                sendSocket.sendto(message,tup[0])

        # this is a zcl report attribute message
        elif(zclCommand == '\x0a'):

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
            zigbeeHACallbackMutex.acquire()
            if(zigbeeHACallback.has_key(callbackIndex)):
                retAddr = zigbeeHACallback[callbackIndex]
            zigbeeHACallbackMutex.release()

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

            # send to all client that want this callback
            for ra in retAddr:
                sendSocket.sendto(message,ra)


# -----------------------------------------------------------------------------
# Communication Callback/Parse Methods
# -----------------------------------------------------------------------------
def handleNewZigbeeMessage(parsedData, src_addr):
    ''' Method to process a zigbee message from the local radio.

        parsedData -- Pre-parsed (into a dict) data from message.
    '''
    processZigbeeRxExplicitCommandMessage(parsedData, src_addr)


def handleNewUdpPacket(data, addr):
    ''' Method to parse and handle an incoming UDP packet.

        data -- Data that was in the UDP packet.
        addr -- Address (IP and Port) of the UDP packet origin.
    '''



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
            processUdpZdoBindReqMessage(parsedData, addr)
        elif(parsedData["type"] == "zdo_unbind_request"):
            processUdpZdoUnBindReqMessage(parsedData, addr)
        elif(parsedData["type"] == "send_address"):
            processUdpSendAddressMessage(parsedData, addr)
        elif(parsedData["type"] == "zcl_read_attributes"):
            processUdpZclReadAttributesMessage(parsedData, addr)
        elif(parsedData["type"] == "zcl_configure_reporting"):
            processUdpZclConfigureReportingMessage(parsedData, addr)
        elif(parsedData["type"] == "policy_set"):
            processUdpPolicySet(parsedData, addr)
        elif(parsedData["type"] == "policy_clear"):
            processUdpPolicyClear(parsedData, addr)
        else:
            pass
    except:
        # if we ever get here then something went wrong and so just ignore this
        # packet and try again later
        print "I didn't expect this error:", sys.exc_info()[0]
        traceback.print_exc()



def pollMessages():
    payload, src_addr = zigbeeConnection.recvfrom(1024)
    handleNewZigbeeMessage(payload, src_addr)

    
# -----------------------------------------------------------------------------
# Main Running Methods
# -----------------------------------------------------------------------------

def main():
    '''Main function used for starting the application as the main driver'''

    global UDP_RECEIVE_PORT
    global zigbeeConnection
    global zigbeeMutex
    global doEndFlag

    parseCommandLineArgs(sys.argv[1:])


    # create a zigbee object that handles all zigbee communication
    # we use this to do all communication to and from the radio
    # when data comes from the radio it will get a bit of unpacking
    # and then a call to the callback specified will be done with the
    # unpacked data
    zigbeeConnection = socket(AF_ZIGBEE, SOCK_DGRAM, XBS_PROT_APS)

    zigbeeConnection.bind(("", 0x0, 0, 0))
    
    # get the long address of our local radio before we start doing anything
    getConnectedRadioLongAddress();

    # setup incoming UDP socket and bind it to self and specified UDP port
    # sending socket does not need to be bound to anything
    receiveSocket.bind(("", UDP_RECEIVE_PORT))

    # create the thread that does short address lookups
    addressUpdateWorkerThread = threading.Thread(target=addressUpdateWorkerMethod)
    addressUpdateWorkerThread.start()
    zigbeeConnection.setblocking(0)
    receiveSocket.setblocking(0)
    
    
    try:
        # Main running loop
        while(True):
            rlist = [ receiveSocket, zigbeeConnection ]
            wlist = []
            xlist = []
            rlist, wlist, xlist = select(rlist, [], [])
            if zigbeeConnection in rlist:
                pollMessages()

            if receiveSocket in rlist:
                data, addr = receiveSocket.recvfrom(4096)
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
    zigbeeConnection.close()
    receiveSocket.close()
    sendSocket.close()

if __name__ == "__main__":
    # call main function since this is being run as the start
    main()
