import ctypes
from ctypes import *
import time
import zlib
import threading
from datetime import datetime
import os
import sys

USBCAN_2E_U = 21
USBCAN_8E_U = 34
USBCAN2 = 4
USBCAN_4E_U = 31
USBCAN_8E_U = 34


class VCI_CAN_OBJ(Structure):
    _fields_ = [("ID", c_uint), ("TimeStamp", c_uint), ("TimeFlag", c_byte), ("SendType", c_byte),
                ("RemoteFlag", c_byte),
                ("ExternFlag", c_byte), ("DataLen", c_byte), ("Data", c_ubyte * 8), ("Reserved", c_byte * 3)]


class VCI_INIT_CONFIG(Structure):
    _fields_ = [("AccCode", c_long), ("AccMask", c_long), ("Reserved", c_long), ("Filter", c_ubyte),
                ("Timing0", c_ubyte),
                ("Timing1", c_ubyte), ("Mode", c_ubyte)]


class VCI_CAN_STATUS(Structure):
    _fields_ = [("ErrInterrupt", c_ubyte), ("regMode", c_ubyte), ("regStatus", c_ubyte), ("regALCapture", c_ubyte),
                ("regECCapture", c_ubyte),
                ("regEWLimit", c_ubyte), ("regRECounter", c_ubyte), ("regTECounter", c_ubyte), ("Reserved", c_long)]


class VCI_BOARD_INFO(Structure):
    _fields_ = [("hw_Version", c_ushort), ("fw_Version", c_ushort), ("dr_Version", c_ushort), ("in_Version", c_ushort),
                ("irq_Num", c_ushort),
                ("can_Num", c_byte), ("str_Serial_Num", c_char * 20), ("str_hw_Type", c_char * 40),
                ("Reserved", c_ushort * 4)]


class VCI_FILTER_RECORD(Structure):
    _fields_ = [("ExtFrame", c_long), ("Start", c_long), ("End", c_long)]


class ERR_INFO(Structure):
    _fields_ = [("ErrCode", c_uint), ("Passive_ErrData", c_byte * 3), ("ArLost_ErrData", c_byte)]


class VCI_AUTO_SEND_OBJ(Structure):
    _fields_ = [("Enable", c_byte), ("Index", c_byte), ("Interval", c_double), ("Obj", VCI_CAN_OBJ)]


def cycle_send_thread(temp_can_d):
    # 循环发送的进程
    tcd = temp_can_d
    while tcd.cycle_flag:
        for i in range(len(tcd.cycle_id_array)):
            time.sleep(tcd.cycle_delay_array[i] / 1000)
            tcd.send_one_frame(tcd.cycle_data_array[i], tcd.cycle_id_array[i], tcd.cycle_send_index)
        time.sleep((tcd.cycle_delay - sum(tcd.cycle_delay_array)) / 1000)


class CanDevice:
    def __init__(self, DevType=USBCAN2, DevIndex=0, CANIndex_max=2, with_close=1):
        self.DevType = DevType
        self.DevIndex = DevIndex
        self.CANIndex_max = CANIndex_max
        self.send_one_frame_lock = threading.Lock()  # 发送报文的锁
        path = os.path.abspath(os.path.dirname(sys.argv[0]))
        #print('canPath', path)
        # self.can = CDLL('../resource/ControlCAN.dll')
        #self.can = CDLL('D:/AppiumFrame-poModel/AppiumFrame-poModel/src/main/resources/ControlCAN.dll')
        self.can = CDLL(path+'\ControlCAN.dll')
        # self.can = CDLL('C:/vp128_agreement/resource/ControlCAN.dll')
        # 为什么此处用相对路径不行
        # self.can = CDLL('ControlCAN.dll')
        self.can_obj_send = VCI_CAN_OBJ(ID=1, TimeStamp=0, TimeFlag=0, SendType=0, RemoteFlag=0, ExternFlag=0,
                                        DataLen=8)
        self.can_obj_rcv = VCI_CAN_OBJ(ID=1, TimeStamp=0, TimeFlag=0, SendType=0, RemoteFlag=0, ExternFlag=0, DataLen=8)
        self.status = VCI_CAN_STATUS()
        self.err = ERR_INFO()
        self.board_info = VCI_BOARD_INFO()
        self.init_config = VCI_INIT_CONFIG(AccCode=0, AccMask=0xFFFFFFFF, Reserved=0, Filter=1, Timing0=0,
                                           Timing1=0, Mode=0)
        self.filter_rec = VCI_FILTER_RECORD(ExtFrame=0, Start=0x00000000, End=0xFFFFFFFF)
        if with_close == 1:
            try:
                self.close()
            except:
                pass
        open_result = self.can.VCI_OpenDevice(self.DevType, self.DevIndex, 0)
        if open_result == 1:
            print('CAN设备打开成功！')
        else:
            print('CAN设备打开失败！')
        self.cycle_flag = False

    def can_start(self, CANIndex, baud_rate):
        if self.DevType == USBCAN_2E_U:
            if baud_rate == '125K':
                self.baud_rate = c_char_p(0x1C0011)
            elif baud_rate == '250K':
                self.baud_rate = c_char_p(0x1C0008)
            elif baud_rate == '500K':
                self.baud_rate = c_char_p(0x060007)
            else:
                self.baud_rate = c_char_p(0x1C0011)
            self.can.VCI_SetReference(self.DevType, self.DevIndex, CANIndex, 0, pointer(self.baud_rate))
        elif (self.DevType == USBCAN_8E_U) or (self.DevType == USBCAN_4E_U):
            if baud_rate == '125K':
                self.baud_rate = c_char_p(125000)
            elif baud_rate == '250K':
                self.baud_rate = c_char_p(250000)
            elif baud_rate == '500K':
                self.baud_rate = c_char_p(500000)
            else:
                self.baud_rate = c_char_p(125000)
            self.can.VCI_SetReference(self.DevType, self.DevIndex, CANIndex, 0, pointer(self.baud_rate))
        if baud_rate == '125K':
            self.init_config = VCI_INIT_CONFIG(AccCode=0, AccMask=0xFFFFFFFF, Reserved=0, Filter=0, Timing0=3,
                                               Timing1=0x1c, Mode=0)
        elif baud_rate == '250K':
            self.init_config = VCI_INIT_CONFIG(AccCode=0, AccMask=0xFFFFFFFF, Reserved=0, Filter=1, Timing0=1,
                                               Timing1=0x1c, Mode=0)
        elif baud_rate == '500K':
            self.init_config = VCI_INIT_CONFIG(AccCode=0, AccMask=0xFFFFFFFF, Reserved=0, Filter=1, Timing0=0,
                                               Timing1=0x1c, Mode=0)
        else:
            self.init_config = VCI_INIT_CONFIG(AccCode=0, AccMask=0xFFFFFFFF, Reserved=0, Filter=0, Timing0=3,
                                               Timing1=0x1c, Mode=0)
        self.can.VCI_InitCAN(self.DevType, self.DevIndex, CANIndex, pointer(self.init_config))

        print(self.can.VCI_StartCAN(self.DevType, self.DevIndex, CANIndex))
        return self.can.VCI_StartCAN(self.DevType, self.DevIndex, CANIndex)

    def send_one_frame(self, data, ID, CANIndex=0):
        # data=[0x,0x,0x,0x,0x,0x,0x,0x]
        self.send_one_frame_lock.acquire()
        self.can_obj_send.Data[0] = data[0]
        self.can_obj_send.Data[1] = data[1]
        self.can_obj_send.Data[2] = data[2]
        self.can_obj_send.Data[3] = data[3]
        self.can_obj_send.Data[4] = data[4]
        self.can_obj_send.Data[5] = data[5]
        self.can_obj_send.Data[6] = data[6]
        self.can_obj_send.Data[7] = data[7]
        self.can_obj_send.ID = ID
        self.can_obj_send.SendType = 0
        self.can_obj_send.ExternFlag = 0
        send_result = self.can.VCI_Transmit(self.DevType, self.DevIndex, CANIndex, pointer(self.can_obj_send), 1)
        self.send_one_frame_lock.release()
        return send_result

    def cycle_send(self, cycle_id_array, cycle_data_array, cycle_delay_array, cycle_time, cycle_send_index):
        # id_array data_array delay_array 是每个循环包含的数据
        # 例如要循环发两个数据A:id=1,data[1]*8和B:id=2,data[2]*8和C:id=3,data=[3]*8，AB中间间隔100毫秒,BC中间间隔200毫秒
        # A到下一次A的时间是600毫秒
        # 则id_array = [1,2,3] |data_array = [[1]*8,[2]*8,[3]*8] |delay_array = [0,100,200] |cycle_time = 600
        self.cycle_id_array = cycle_id_array
        self.cycle_data_array = cycle_data_array
        self.cycle_delay_array = cycle_delay_array
        self.cycle_time = cycle_time
        self.cycle_send_index = cycle_send_index
        if sum(cycle_id_array) > cycle_time:
            print('总的延迟时间比循环时间长，无法创建循环')
            #print(sum(cycle_id_array))
            return
        else:
            self.cycle_send_thread1 = threading.Thread(target=cycle_send_thread, args=(self,))
            self.cycle_flag = True
            self.cycle_send_thread1.setDaemon(True)  # setDaemon(True)，主进程关的时候，子进程同步关闭
            self.cycle_send_thread1.start()

    def cycle_send_stop(self):
        self.cycle_flag = False

    def send_multi_frame(self, ID_array, data_array, CANIndex=0):
        # data 为 data_array(ID,data[8])
        L = min(len(ID_array), len(data_array))
        can_obj_send_array_obj = VCI_CAN_OBJ * L
        self.can_obj_send_array = can_obj_send_array_obj()
        for j in range(L):
            for i in range(8):
                self.can_obj_send_array[j].Data[i] = data_array[j][i]
            self.can_obj_send_array[j].DataLen = 8
            self.can_obj_send_array[j].ID = ID_array[j]
        self.can.VCI_Transmit(self.DevType, self.DevIndex, CANIndex, pointer(self.can_obj_send_array), 1)

    def receive(self, CANIndex=0):
        num = self.can.VCI_GetReceiveNum(self.DevType, self.DevIndex, CANIndex)
        rcv_data_array = [[0] * 8 for row in range(num)]  # 返回ID数组
        rcv_ID_array = [0] * num  # 返回ID数组
        rcv_time_array = [0] * num
        rcv_flag_array = [0] * num
        if num > 0:
            # print('num: ', num)
            rcv_buff_obj = VCI_CAN_OBJ * num
            rcv_buff = rcv_buff_obj()
            self.can.VCI_Receive(self.DevType, self.DevIndex, CANIndex, pointer(rcv_buff), num, 0)
            for i in range(num):
                for j in range(8):
                    rcv_data_array[i][j] = rcv_buff[i].Data[j]
                rcv_ID_array[i] = rcv_buff[i].ID
                rcv_time_array[i] = rcv_buff[i].TimeStamp
                rcv_flag_array[i] = rcv_buff[i].TimeFlag
        return rcv_ID_array, rcv_data_array, rcv_time_array, rcv_flag_array

    def clear_buff(self, CANIndex=0):
        self.can.VCI_ClearBuffer(self.DevType, self.DevIndex, CANIndex)

    def print_err(self):
        s = self.can.VCI_ReadErrInfo(self.DevType, self.DevIndex, 0, pointer(self.err))
        print("err status:", s)
        print("ErrCode", self.err.ErrCode)
        print("Passive_ErrData1", self.err.Passive_ErrData[0])
        print("Passive_ErrData2", self.err.Passive_ErrData[1])
        print("Passive_ErrData3", self.err.Passive_ErrData[2])
        print("ArLost_ErrData", self.err.ArLost_ErrData)

    def reset_can(self, CANIndex=0):
        self.can.VCI_ResetCAN(self.DevType, self.DevIndex, CANIndex)

    def close(self):
        # self.can.VCI_ResetCAN(self.DevType, self.DevIndex, self.CANIndex)
        self.can.VCI_CloseDevice(self.DevType, self.DevIndex)


def initArgs():
    # 报文体
    body = []
    for i in range(1, 9):
        num = sys.argv[i].strip('[,]')
        body.append(int(num, 16))

    # data = [0x00, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00]
    head = int(sys.argv[9].strip('[,]'), 16)  # 报文ID获取
    baud_rate = sys.argv[10].strip('[,]')  # 波特率获取
    deviceID = int(sys.argv[11].strip('[,]'))  # 设备ID
    canIndex = int(sys.argv[12].strip('[,]'))  # can通道

    sendCount = int(sys.argv[13].strip('[,]'))
    # canDev = CanDevice(USBCAN2, deviceID, 2, 1)
    # print("打开CAN", canDev.can_start(canIndex, baud_rate))
    sendDate(deviceID, canIndex, head, body, baud_rate, sendCount)


def sendDate(deviceID, canIndex, head, body, baud_rate, sendCount):
    canDev = CanDevice(USBCAN2, deviceID, 2, 1)
    canDev.can_start(canIndex, baud_rate)
    print('CAN标记:',deviceID,"通道:", canIndex)
    for i in range(sendCount):
        print('第', (i + 1), "次发送",body)
        canDev.send_one_frame(body, head, CANIndex=canIndex)
        if 0 <= i < sendCount - 1:
            time.sleep(0.05)


if __name__ == '__main__':
    #     data = [0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00]
    #     ID = 0x12d
    # data1 = [0x00, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00]
    # data2 = [0x00, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00]
    # cycle_data_array = [data1,data2]
    # cycle_id_array = [0x012d,0x12d]
    # cycle_delay_array = [5]
    # cycle_time = 2000
    # cycle_send_index = 1
    # canDev = CanDevice(USBCAN2, 0, 2, 1)
    # print("打开CAN0", canDev.can_start(0, '500K'))
    # canDev.send_one_frame(data, ID, CANIndex=0)
    # canDev.cycle_send(cycle_id_array, cycle_data_array, cycle_delay_array, cycle_time, cycle_send_index)
    print("开始执行Python脚本...")
    initArgs()
    print("Python脚本结束")
    # canDev.reset_can(0)
