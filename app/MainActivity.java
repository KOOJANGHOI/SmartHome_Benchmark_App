package com.example.ali.control;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.os.StrictMode;

import android.util.Log;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.HttpAuthHandler;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import iotcloud.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import android.os.Handler;
import android.content.*;

import org.w3c.dom.Text;

import static android.R.id.button1;

public class MainActivity extends AppCompatActivity {

    Table t1 = null;
    Semaphore mutex = new Semaphore(1);
    Thread thread = null;
    private Handler handler1 = new Handler();
    private Handler handler2 = new Handler();

    // Alarm System Able/Disable
    TextView AlarmSystemStatus;
    Button AlarmSystemAble;
    Button AlarmSystemDisable;

    IoTString istr_alarmsystemstatus = new IoTString("all_alarm");
    IoTString istr_able = new IoTString("able");
    IoTString istr_disable = new IoTString("disable");
    IoTString istr_on = new IoTString("on");
    IoTString istr_off = new IoTString("off");
    IoTString istr_true = new IoTString("true");
    IoTString istr_false = new IoTString("false");


    // Each Sensor, Camera Able/Disable
    Switch R1SEN1, R1SEN2, R2SEN3, R2CAM1;
    TextView cam1, sen1, sen2, sen3;
    IoTString icam;
    IoTString[] isen = new IoTString[3];

    // Each Alarm
    TextView Room1, Room2;
    IoTString ialarmr1 , ialarmr2;

    IoTString[] icam_detect = new IoTString[2];
    IoTString[] icam_able = new IoTString[2];
    IoTString[] isen_detect = new IoTString[3];
    IoTString[] isen_able = new IoTString[3];

    //Check current alarm status and display on phone screen.
    private Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            try {
                mutex.acquire();
                t1.update();

                IoTString testval1 = t1.getCommitted(ialarmr1); // alarm status of room1
                IoTString testval2 = t1.getCommitted(ialarmr2); // alarm status of room2
                mutex.release();

                if(testval1 != null && testval1.equals(istr_on)) {
                    Room1.setBackgroundColor(Color.RED);
                    //System.out.println("RRRRRRRRRRRRRRRRRRRR111111111111");
                }else {
                    Room1.setBackgroundColor(Color.WHITE);
                    //System.out.println("WWWWWWWWWWWWWWWWWWWWWWWW1111111111");
                }

                if(testval2 != null && testval2.equals(istr_on)) {
                    Room2.setBackgroundColor(Color.RED);
                    //System.out.println("RRRRRRRRRRRRRRRRRRRR22222222222222");
                }else {
                    Room2.setBackgroundColor(Color.WHITE);
                    //System.out.println("WWWWWWWWWWWWWWWWWWW2222222222222222");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler1.postDelayed(runnable1,1000);
        }
    };

    //
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            try {
                mutex.acquire();
                t1.update();

                IoTString[] testval_cam = new IoTString[2];
                IoTString[] testval_sen = new IoTString[3];

                for(int i = 0 ; i < 2 ; i++) {
                    testval_cam[i] = t1.getCommitted(icam_detect[i]);
                    if((testval_cam[i] != null) && testval_cam[i].equals(istr_true)){
                        if(i == 0) {
                            cam1.setBackgroundColor(Color.RED);
                        } else {
                            continue;
                        }
                    } else {
                        if(i == 0) {
                            cam1.setBackgroundColor(Color.WHITE);
                        } else {
                            continue;
                        }}
                }

                for(int i = 0 ; i < 3 ; i++) {
                    testval_sen[i] = t1.getCommitted(isen_detect[i]);
                    if((testval_sen[i] != null) && testval_sen[i].equals(istr_true)){
                        if(i == 0) {
                            sen1.setBackgroundColor(Color.RED);
                        } else if(i == 1) {
                            sen2.setBackgroundColor(Color.RED);
                        } else if(i == 2) {
                            sen3.setBackgroundColor(Color.RED);
                        } else {
                            continue;
                        }
                    } else {
                        if(i == 0) {
                            sen1.setBackgroundColor(Color.WHITE);
                        } else if(i == 1) {
                            sen2.setBackgroundColor(Color.WHITE);
                        } else if(i == 2) {
                            sen3.setBackgroundColor(Color.WHITE);
                        } else {
                            continue;
                        }}
                }
                mutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler2.postDelayed(runnable2,1000);
        }
    };


    private void someInit() {
        ialarmr1 = new IoTString("alarm0");
        ialarmr2 = new IoTString("alarm1");

        for(int i = 0 ; i < 2 ; i++) {
            icam_able[i] = new IoTString("cam_detect"+Integer.toString(i));
            icam_detect[i] = new IoTString("cam_detect"+Integer.toString(i));
        }

        for(int i = 0 ; i < 3 ; i++) {
            isen_detect[i] = new IoTString("sen_detect"+Integer.toString(i));
            isen_able[i] = new IoTString("sen_able"+Integer.toString(i));
        }

        AlarmSystemStatus = (TextView)findViewById(R.id.all_alarm_status);
        AlarmSystemAble = (Button) findViewById(R.id.Button_Able);
        AlarmSystemDisable = (Button) findViewById(R.id.Button_Disable);

        R1SEN1 = (Switch)findViewById(R.id.r1sen1);
        R1SEN2 = (Switch)findViewById(R.id.r1sen2);
        R2CAM1 = (Switch)findViewById(R.id.r2cam1);
        R2SEN3 = (Switch)findViewById(R.id.r2sen3);

        R1SEN1.setChecked(true);
        R1SEN2.setChecked(true);
        R2CAM1.setChecked(true);
        R2SEN3.setChecked(true);

        cam1 = (TextView)findViewById(R.id.cam1);
        sen1 = (TextView)findViewById(R.id.sen1);
        sen2 = (TextView)findViewById(R.id.sen2);
        sen3 = (TextView)findViewById(R.id.sen3);

        Room1 = (TextView)findViewById(R.id.room1);
        Room2 = (TextView)findViewById(R.id.room2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Log.e("Ali::::", "Here1");
            t1 = new Table("http://dc-6.calit2.uci.edu/test.iotcloud/", "reallysecret", 1000, 6000, MainActivity.this);

            t1.addLocalCommunication(397, "192.168.1.198", 6000);
            Log.e("Ali::::", "Here2");

            t1.rebuild();
            Log.e("Ali::::", "Here3");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e("ALI::::", sw.toString());
        }



        someInit();


        runnable1.run();
        runnable2.run();

        //Enable all alarm systm
        AlarmSystemAble.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mutex.acquire();
                    try {
                        t1.update();
                        t1.startTransaction();
                        t1.addKV(istr_alarmsystemstatus, istr_able);
                        t1.commitTransaction();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        AlarmSystemStatus.setText("All Alarm are Able");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutex.release();
                }
            }
        });

        //Disable all alarm system.
        AlarmSystemDisable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mutex.acquire();
                    try {
                        t1.update();
                        t1.startTransaction();
                        t1.addKV(istr_alarmsystemstatus, istr_disable);
                        t1.commitTransaction();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        AlarmSystemStatus.setText("All Alarm are Disable");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutex.release();
                }

            }
        });

        //Enable or Disable camera in room2
        R2CAM1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                try {
                    mutex.acquire();
                    if (bChecked) {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(icam_able[0], istr_able);
                            t1.commitTransaction();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //cam1.setBackgroundColor(Color.GRAY);
                        }
                    } else {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(icam_able[0], istr_disable);
                            t1.commitTransaction();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Room2.setBackgroundColor(Color.WHITE);
                        }}
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutex.release();
                }
            }
        });
        //Enable or Disable sensor #1 in room1
        R1SEN1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                try {
                    mutex.acquire();
                    if (bChecked) {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(isen_able[0], istr_able);
                            t1.commitTransaction();
                            //t1.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //sen1.setBackgroundColor(Color.GRAY);
                        }
                    } else {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(isen_able[0], istr_disable);
                            t1.commitTransaction();
                            //t1.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Room1.setBackgroundColor(Color.WHITE);
                        }}
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutex.release();
                }
            }
        });
        //Enable or Disable sensor #2 in room1
        R1SEN2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                try {
                    mutex.acquire();
                    if (bChecked) {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(isen_able[1], istr_able);
                            t1.commitTransaction();
                            //t1.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //sen2.setBackgroundColor(Color.GRAY);
                        }
                    } else {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(isen_able[1], istr_disable);
                            t1.commitTransaction();
                            //t1.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Room1.setBackgroundColor(Color.WHITE);
                        }}
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutex.release();
                }
            }
        });
        //Enable or Disable sensor #2 in room2
        R2SEN3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                try {
                    mutex.acquire();
                    if (bChecked) {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(isen_able[2], istr_able);
                            t1.commitTransaction();
                            //t1.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //sen3.setBackgroundColor(Color.GRAY);
                        }
                    } else {
                        try {
                            t1.update();
                            t1.startTransaction();
                            t1.addKV(isen_able[2], istr_disable);
                            t1.commitTransaction();
                            //t1.update();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Room2.setBackgroundColor(Color.WHITE);
                        }}
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutex.release();
                }
            }
        });

    }
}