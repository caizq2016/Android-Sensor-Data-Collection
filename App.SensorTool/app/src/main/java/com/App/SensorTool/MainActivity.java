package com.App.SensorTool;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.App.SensorTool.Config.SystemSampleParametersConfig;
import com.App.SensorTool.util.CurrentDateTimeInstnce;
import com.App.SensorTool.util.FileHelper;
import org.w3c.dom.Text;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
public class MainActivity extends Activity implements  ScreenListener.ScreenStateListener, View.OnClickListener {
    private TextToSpeech textToSpeech;
    private ScreenListener screenListener;
    private TextView accData1;
    private TextView accData2;
    private TextView accData3,accData4,accData5,accData6,accData7, accData8,accData9;
    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private Sensor sensor2 = null;
    private Sensor gyroScopeSensor = null;
    private float[] accValue = new float[3];
    private FileHelper fileHelper;
    private CurrentDateTimeInstnce currentDateTimeInstance;
    private StringBuffer bufferGrivaty = new StringBuffer();
    private StringBuffer bufferLinerAcceleration = new StringBuffer();
    private StringBuffer bufferGyroscope = new StringBuffer();
    private int index0 = 0,index1 = 0,index2 = 0;
    private Button btnRun, btnSit, btnBicycle, btnUpstaris, btnDownStari, btnWalk;
    private int count = 5;
    private volatile String mode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speakInit();
        viewInit();
        currentDateTimeInstance = CurrentDateTimeInstnce.getInstnce();
        fileHelper = new FileHelper(getApplicationContext());
        findViewById(R.id.btnSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak("开始采集数据", TextToSpeech.QUEUE_ADD, null);
                registerSensor();

            }
        });
        findViewById(R.id.stopSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak("停止数据采集", TextToSpeech.QUEUE_ADD, null);
                unRegisterSensor();
            }
        });

    }

    // textToSpeech tool init
    private void speakInit()
    {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.CHINESE);
            }
        });
    }
    // shut down resource
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech != null)
        {
            textToSpeech.shutdown();
        }
    }
    // view init
    private void viewInit()
    {

        btnRun = (Button) findViewById(R.id.btnRun);
        btnBicycle = (Button) findViewById(R.id.btnBicycle);
        btnDownStari = (Button) findViewById(R.id.btnDownStrais);
        btnUpstaris = (Button) findViewById(R.id.btnUpdtaris);
        btnWalk = (Button) findViewById(R.id.btnWalk);
        btnSit = (Button) findViewById(R.id.btnSit);

        btnDownStari.setOnClickListener( this);
        btnBicycle.setOnClickListener(this);
        btnUpstaris.setOnClickListener(this);
        btnWalk.setOnClickListener(this);
        btnSit.setOnClickListener(this);
        btnRun.setOnClickListener(this);


        accData1 = (TextView) findViewById(R.id.accelerometerData1);
        accData2 = (TextView) findViewById(R.id.accelerometerData2);
        accData3 = (TextView) findViewById(R.id.accelerometerData3);
        accData4 = (TextView) findViewById(R.id.accelerometerData4);
        accData5 = (TextView) findViewById(R.id.accelerometerData5);
        accData6 = (TextView) findViewById(R.id.accelerometerData6);
        accData7 = (TextView) findViewById(R.id.accelerometerData7);
        accData8 = (TextView) findViewById(R.id.accelerometerData8);
        accData9 = (TextView) findViewById(R.id.accelerometerData9);
        screenListener = new ScreenListener(this);
        screenListener.start(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gyroScopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

   /* @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 10000);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 10000);
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),10000);
    }*/
    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
                accValue[0] = sensorEvent.values[0];
                accValue[1] = sensorEvent.values[1];
                accValue[2] = sensorEvent.values[2];
                String locationX = String.valueOf(accValue[0]);
                String locationY = String.valueOf(accValue[1]);
                String locationZ = String.valueOf(accValue[2]);
                String splitLine = "\n";
            synchronized (this)
            {
                switch ( sensorEvent.sensor.getType())
                {
                    case Sensor.TYPE_GRAVITY:
                        accData4.setText(String.valueOf(accValue[0]));
                        accData5.setText( String.valueOf(accValue[1]));
                        accData6.setText(String.valueOf(accValue[2]));
                        bufferGrivaty.append(locationX);
                        bufferGrivaty.append(" ");
                        bufferGrivaty.append(locationY);
                        bufferGrivaty.append(" ");
                        bufferGrivaty.append(locationZ);
                        bufferGrivaty.append(splitLine);
                        index0 ++;
                        if(index0 == SystemSampleParametersConfig.SAMPLE_lENGTH)
                        {
                            String folderName1 = "Gravity"+mode + currentDateTimeInstance.formatDateTime()+".txt";
                            try {
                                fileHelper.createSDFile(folderName1).getAbsoluteFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            fileHelper.writeSDFile(bufferGrivaty.toString(), folderName1);
                            bufferGrivaty.delete(0, bufferGrivaty.length());
                            if(hasAllWriteOver(index0, index1, index2, SystemSampleParametersConfig.SAMPLE_lENGTH))
                            {
                                unRegisterSensor();
                                index0 = 0;
                                index1 = 0;
                                index2 = 0;
                                textToSpeech.speak(mode + "数据采集已结束，正在保存数据，请稍后", TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        accData1.setText( String.valueOf(accValue[0]));
                        accData2.setText(String.valueOf(accValue[1]));
                        accData3.setText(String.valueOf(accValue[2]));
                        bufferLinerAcceleration.append(locationX);
                        bufferLinerAcceleration.append(" ");
                        bufferLinerAcceleration.append(locationY);
                        bufferLinerAcceleration.append(" ");
                        bufferLinerAcceleration.append(locationZ);
                        bufferLinerAcceleration.append(splitLine);
                        index1 ++;
                        if(index1 == SystemSampleParametersConfig.SAMPLE_lENGTH)
                        {
                            String folderName2 =  "Accelreation"+ mode + currentDateTimeInstance.formatDateTime()+".txt";
                            try {
                                fileHelper.createSDFile(folderName2).getAbsoluteFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            fileHelper.writeSDFile(bufferLinerAcceleration.toString(), folderName2);
                            bufferLinerAcceleration.delete(0, bufferLinerAcceleration.length());
                            if(hasAllWriteOver(index0, index1, index2, SystemSampleParametersConfig.SAMPLE_lENGTH))
                            {
                                unRegisterSensor();
                                index0 = 0;
                                index1 = 0;
                                index2 = 0;
                                textToSpeech.speak(mode + "数据采集已结束，正在保存数据，请稍后", TextToSpeech.QUEUE_ADD, null);
                            }

                        }
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        accData7.setText( String.valueOf(accValue[0]));
                        accData8.setText(String.valueOf(accValue[1]));
                        accData9.setText(String.valueOf(accValue[2]));
                        bufferGyroscope.append(locationX);
                        bufferGyroscope.append(" ");
                        bufferGyroscope.append(locationY);
                        bufferGyroscope.append(" ");
                        bufferGyroscope.append(locationZ);
                        bufferGyroscope.append(splitLine);
                        index2 ++;
                        if(index2 == SystemSampleParametersConfig.SAMPLE_lENGTH)
                        {
                            String folderName3 = "Gyroscope" +mode + currentDateTimeInstance.formatDateTime()+".txt";
                            try {
                                fileHelper.createSDFile(folderName3).getAbsoluteFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            fileHelper.writeSDFile(bufferGyroscope.toString(), folderName3);
                            bufferGyroscope.delete(0, bufferGyroscope.length());
                            if(hasAllWriteOver(index0, index1, index2, SystemSampleParametersConfig.SAMPLE_lENGTH))
                            {
                                unRegisterSensor();
                                index0 = 0;
                                index1 = 0;
                                index2 = 0;
                                textToSpeech.speak(mode + "数据采集已结束，正在保存数据，请稍后", TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                }

            }


            }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterSensor();
    }

    @Override
    public void onScreenOn() {

    }

    @Override
    public void onScreenOff() {
        // register again
        sensorManager.unregisterListener(listener);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 10000);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 10000);
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),10000);

    }

    @Override
    public void onUserPresent() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case  R.id.btnRun:
                  mode = "Run";
                  new Thread(new Runnable() {
                      @Override
                      public void run() {
                          while(true)
                          {
                              try {

                                  if(count == 5)
                                  {
                                      textToSpeech.speak("我即将开始采集数据了", TextToSpeech.QUEUE_ADD, null);
                                      Thread.sleep(500);
                                      textToSpeech.speak("请做好准备", TextToSpeech.QUEUE_ADD, null);
                                      Thread.sleep(3000);
                                      textToSpeech.speak(String.valueOf(5), TextToSpeech.QUEUE_ADD, null);
                                  }
                                  Thread.sleep(1200);
                                  textToSpeech.speak(String.valueOf(--count), TextToSpeech.QUEUE_ADD, null);
                                  if(count == 0)
                                  {
                                      count = 5;
                                      Thread.sleep(600);
                                      textToSpeech.speak("run go", TextToSpeech.QUEUE_ADD, null);
                                      registerSensor();
                                      break;
                                  }
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }

                          }

                      }
                  }).start();

                break;
            case  R.id.btnSit:
                  mode = "Sit";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true)
                            {
                                try {

                                    if(count == 5)
                                    {
                                        textToSpeech.speak("我即将开始采集数据了", TextToSpeech.QUEUE_ADD, null);
                                        Thread.sleep(500);
                                        textToSpeech.speak("请做好准备", TextToSpeech.QUEUE_ADD, null);
                                        Thread.sleep(3000);
                                        textToSpeech.speak(String.valueOf(5), TextToSpeech.QUEUE_ADD, null);
                                    }
                                    Thread.sleep(1200);
                                    textToSpeech.speak(String.valueOf(--count), TextToSpeech.QUEUE_ADD, null);
                                    if(count == 0)
                                    {
                                        count = 5;
                                        Thread.sleep(600);
                                        textToSpeech.speak("sit go", TextToSpeech.QUEUE_ADD, null);
                                        registerSensor();
                                        break;
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                        }

                    }
                }).start();

                break;
            case  R.id.btnUpdtaris:
                mode = "Upstrirs";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true)
                        {
                            try {

                                if(count == 5)
                                {
                                    textToSpeech.speak("我即将开始采集数据了", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(500);
                                    textToSpeech.speak("请做好准备", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(3000);
                                    textToSpeech.speak(String.valueOf(5), TextToSpeech.QUEUE_ADD, null);
                                }
                                Thread.sleep(1200);
                                textToSpeech.speak(String.valueOf(--count), TextToSpeech.QUEUE_ADD, null);
                                if(count == 0)
                                {
                                    count = 5;
                                    Thread.sleep(600);
                                    textToSpeech.speak("up stairs go", TextToSpeech.QUEUE_ADD, null);
                                    registerSensor();
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();


                break;
            case  R.id.btnDownStrais:
                mode = "DownStrais";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true)
                        {
                            try {

                                if(count == 5)
                                {
                                    textToSpeech.speak("我即将开始采集数据了", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(500);
                                    textToSpeech.speak("请做好准备", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(3000);
                                    textToSpeech.speak(String.valueOf(5), TextToSpeech.QUEUE_ADD, null);
                                }
                                Thread.sleep(1200);
                                textToSpeech.speak(String.valueOf(--count), TextToSpeech.QUEUE_ADD, null);
                                if(count == 0)
                                {
                                    count = 5;
                                    Thread.sleep(600);
                                    textToSpeech.speak("down stairs go", TextToSpeech.QUEUE_ADD, null);
                                    registerSensor();
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();


                break;
            case  R.id.btnWalk:
                mode = "Walk";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true)
                        {
                            try {

                                if(count == 5)
                                {
                                    textToSpeech.speak("我即将开始采集数据了", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(500);
                                    textToSpeech.speak("请做好准备", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(3000);
                                    textToSpeech.speak(String.valueOf(5), TextToSpeech.QUEUE_ADD, null);
                                }
                                Thread.sleep(1200);
                                textToSpeech.speak(String.valueOf(--count), TextToSpeech.QUEUE_ADD, null);
                                if(count == 0)
                                {
                                    count = 5;
                                    Thread.sleep(600);
                                    textToSpeech.speak("walk go", TextToSpeech.QUEUE_ADD, null);
                                    registerSensor();
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();


                break;
            case  R.id.btnBicycle:
                mode = "Bicycle";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true)
                        {
                            try {

                                if(count == 5)
                                {
                                    textToSpeech.speak("我即将开始采集数据了", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(500);
                                    textToSpeech.speak("请做好准备", TextToSpeech.QUEUE_ADD, null);
                                    Thread.sleep(3000);
                                    textToSpeech.speak(String.valueOf(5), TextToSpeech.QUEUE_ADD, null);
                                }
                                Thread.sleep(1200);
                                textToSpeech.speak(String.valueOf(--count), TextToSpeech.QUEUE_ADD, null);
                                if(count == 0)
                                {
                                    count = 5;
                                    Thread.sleep(600);
                                    textToSpeech.speak("bicycle go", TextToSpeech.QUEUE_ADD, null);
                                    registerSensor();
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();

                break;
            default: break;

        }
    }
    private void registerSensor()
    {
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 10000);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 10000);
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),10000);
    }
    private void unRegisterSensor()
    {

        sensorManager.unregisterListener(listener);
    }
    private boolean hasAllWriteOver(int index0 , int index1, int index2, int samplePoint)
    {
        if(index0 >= samplePoint && index2 >= samplePoint && index1 >= samplePoint)
        {
            return true;
        }
        return  false;
    }
}
