package com.embed_net.remotemeasure;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {

    String broker       = "tcp://120.79.165.74:1883";
    String clientId     = "000001";
    TextView temperatureTextView,humidityTextView,lightTextView,pressureTextView;
    ImageView redLed,greenLed,blueLed,yellowLed;
    boolean redLedFlag = false,greenLedFlag = false,blueLedFlag = false,yellowLedFlag = false;
    double temperature,humidity,light,pressure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureTextView = (TextView)findViewById(R.id.temperatureTextView);
        humidityTextView = (TextView)findViewById(R.id.humidityTextView);
        lightTextView = (TextView)findViewById(R.id.lightTextView);
        pressureTextView = (TextView)findViewById(R.id.pressureTextView);
        redLed = (ImageView)findViewById(R.id.redImageView);
        greenLed = (ImageView)findViewById(R.id.greenImageView);
        blueLed = (ImageView)findViewById(R.id.blueImageView);
        yellowLed = (ImageView)findViewById(R.id.yellowImageView);

        redLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(redLedFlag){
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"red\",\"value\":0}");
                    mPublishThread.run();
                    redLed.setImageResource(R.drawable.dot_red_dark);
                    redLedFlag = false;
                }else{
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"red\",\"value\":1}");
                    mPublishThread.run();
                    redLed.setImageResource(R.drawable.dot_red);
                    redLedFlag = true;
                }
            }
        });

        greenLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(greenLedFlag){
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"green\",\"value\":0}");
                    mPublishThread.run();
                    greenLed.setImageResource(R.drawable.dot_green_dark);
                    greenLedFlag = false;
                }else{
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"green\",\"value\":1}");
                    mPublishThread.run();
                    greenLed.setImageResource(R.drawable.dot_green);
                    greenLedFlag = true;
                }
            }
        });

        blueLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(blueLedFlag){
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"blue\",\"value\":0}");
                    mPublishThread.run();
                    blueLed.setImageResource(R.drawable.dot_blue_dark);
                    blueLedFlag = false;
                }else{
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"blue\",\"value\":1}");
                    mPublishThread.run();
                    blueLed.setImageResource(R.drawable.dot_blue);
                    blueLedFlag = true;
                }
            }
        });

        yellowLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (yellowLedFlag) {
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"yellow\",\"value\":0}");
                    mPublishThread.run();
                    yellowLed.setImageResource(R.drawable.dot_yellow_dark);
                    yellowLedFlag = false;
                } else {
                    PublishThread mPublishThread = new PublishThread("in000000000002","{\"led\":\"yellow\",\"value\":1}");
                    mPublishThread.run();
                    yellowLed.setImageResource(R.drawable.dot_yellow);
                    yellowLedFlag = true;
                }
            }
        });

        SubscribeThread mSubscribeThread = new SubscribeThread("out000000000002");
        mSubscribeThread.run();
    }

    /**
     * send message
     */
    class PublishThread extends Thread {
        String topic;
        MqttMessage message;
        int qos = 0;
        MemoryPersistence persistence = new MemoryPersistence();
        PublishThread(String topic,String message){
            this.topic = topic;
            this.message = new MqttMessage(message.getBytes());
        }
        public void sendMessage(String topic,String message){
            this.topic = topic;
            this.message = new MqttMessage(message.getBytes());
            run();
        }
        @Override
        public void run() {
            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setKeepAliveInterval(1);
                System.out.println("Connecting to broker: " + broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                System.out.println("Publishing message: " + message.toString());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                sampleClient.disconnect();
                System.out.println("Disconnected");
            }catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
        }
    }

    /**
     * receive message
     */
    class SubscribeThread extends Thread{
        final String topic;
        MemoryPersistence persistence = new MemoryPersistence();
        SubscribeThread(String topic){
            this.topic = topic;
        }
        @Override
        public void run(){
            try {
                final MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                final MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                System.out.println("Connecting to broker: " + broker);
                connOpts.setKeepAliveInterval(5);
                sampleClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("connectionLost");
                        try {
                            sampleClient.connect(connOpts);
                            sampleClient.subscribe(topic);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void messageArrived(String topic, final MqttMessage mqttMessage) throws Exception {
                        System.out.println("messageArrived:"+mqttMessage.toString());
                        System.out.println(topic);
                        System.out.println(mqttMessage.toString());
                        try {
                            JSONTokener jsonParser = new JSONTokener(mqttMessage.toString());//{"temperature":1.4,"humidity":3.9,"light":1,"pressure":12}
                            JSONObject person = (JSONObject) jsonParser.nextValue();
                            temperature = person.getDouble("temperature");
                            humidity = person.getDouble("humidity");
                            light = person.getDouble("light");
                            pressure = person.getDouble("pressure");
                            System.out.println("temperature = " + temperature);
                            System.out.println("humidity = " + humidity);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "收到数据", Toast.LENGTH_SHORT).show();
                                    temperatureTextView.setText(String.format("%.1f", temperature));
                                    humidityTextView.setText(String.format("%.1f", humidity));
                                    lightTextView.setText(String.format("%.1f", light));
                                    pressureTextView.setText(String.format("%.1f", pressure));
                                    Toast.makeText(MainActivity.this, mqttMessage.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("deliveryComplete");
                    }
                });
                sampleClient.connect(connOpts);
                sampleClient.subscribe(topic);
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
        }
    }

    public class AboutDialog extends AlertDialog {
        public AboutDialog(Context context) {
            super(context);
            final View view = getLayoutInflater().inflate(R.layout.about, null);
            setButton(context.getText(R.string.button_close), (OnClickListener) null);
            setTitle(R.string.about_title);
            setView(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_about) {
            new AboutDialog(this).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定退出？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    setResult(RESULT_OK);
                                    System.exit(0);
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                }
                            }).show();

        }
        return true;
    }
}
