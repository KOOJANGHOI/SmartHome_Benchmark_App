import iotcloud.*;
import java.util.*;

class Setup{

    private static final int CAM_NUM = 2;
    private static final int SEN_NUM = 3;
    private static final int ROOM_NUM = 2;

    public static void main(String[] args) throws Exception{
        Table t1 = new Table("http://dc-6.calit2.uci.edu/test.iotcloud","reallysecret",321,-1);
        t1.initTable();

        IoTString all_alarm = new IoTString("all_alarm");           //disable or enable all alarms
        IoTString[] cam_able = new IoTString[CAM_NUM];              //disable or enable each cameras
        IoTString[] cam_detect = new IoTString[CAM_NUM];            //check if a camera dectect somthing
        IoTString[] sen_able = new IoTString[SEN_NUM];              //disable or enable each sensors
        IoTString[] sen_detect = new IoTString[SEN_NUM];            //check if a sensor dectect somthing
        IoTString[] room_alarm = new IoTString[ROOM_NUM];           //check if the alarm is ringing in the room

        t1.createNewKey(all_alarm,397);

        for(int i=0; i<CAM_NUM; i++){
            cam_able[i] = new IoTString("cam_able"+Integer.toString(i));
            cam_detect[i] = new IoTString("cam_detect"+Integer.toString(i));
            t1.createNewKey(cam_able[i],397);
            t1.createNewKey(cam_detect[i],397);
        }

        for(int i=0; i<SEN_NUM; i++){
            sen_able[i] = new IoTString("sen_able"+Integer.toString(i));
            sen_detect[i] = new IoTString("sen_detect"+Integer.toString(i));
            t1.createNewKey(sen_able[i],397);
            t1.createNewKey(sen_detect[i],397);
        }

        for(int i=0; i<ROOM_NUM; i++){
            room_alarm[i] = new IoTString("cam_able"+Integer.toString(i));
            t1.createNewKey(room_alarm[i],397);
        }
        t1.update();
    }
}
