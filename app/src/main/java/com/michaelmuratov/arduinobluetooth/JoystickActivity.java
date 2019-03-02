package com.michaelmuratov.arduinobluetooth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.UnsupportedEncodingException;

public class JoystickActivity extends AppCompatActivity {

    public final int movingSpeed = 5;
    boolean down = true;
    boolean sent = false;
    float cursorX;
    float cursorY;

    float hSpeed;
    float vSpeed;

    float centreX;
    float centreY;

    float hDifference;
    float vDifference;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coordinate_screen);

        addJoystick();
        //view = new Coordinate_View(this);

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addJoystick(){
        final ImageView circle = findViewById(R.id.circle);
        final ImageView control = findViewById(R.id.control);

        centreX = circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
        centreY = circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);


        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });


        circle.setOnTouchListener(new View.OnTouchListener(){
            Coordinate_View view = findViewById(R.id.drawing_screen);

            @Override
            public boolean onTouch(View v, final MotionEvent event){
                view.setCentreCircle(circle.getX()+ (circle.getWidth() >> 1),
                        circle.getY()+ (circle.getHeight() >> 1));
                if(event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("X\0");
                    cursorX = circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
                    cursorY = circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);
                    down = false;
                    view.setCentreCircle(0,0);
                    view.setCentreControl(0,0);
                    view.setTouchCoordinates(0,0);
                    view.updateOverlay();
                }

                else if(event.getAction() == MotionEvent.ACTION_DOWN){
                    down = true;
                    cursorX = event.getX() + circle.getX();
                    cursorY = event.getY() + circle.getY();
                    new Thread(new Runnable() {
                        public void run() {
                            while(down){
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                control.setX(cursorX);
                                control.setY(cursorY);

                                hSpeed = movingSpeed * hDifference / circle.getWidth() * 2;
                                vSpeed = movingSpeed * vDifference / circle.getHeight() * 2;
                            }

                        }
                    }).start();
                }

                else if(event.getAction() == MotionEvent.ACTION_MOVE){
                    int speedy = (425 - (int)event.getY()) * 200/425;
                    int speedx = (425 - (int)event.getX()) * 200/425;
                    sendCommand("F"+speedy+"\0");
                    sendCommand("S"+speedx+"\0");
                    Log.d("Y",""+speedy);
                    Log.d("X",""+speedx);
                    float x = event.getX()- (circle.getWidth() >> 1);
                    float y = event.getY()- (circle.getHeight() >> 1);
                    double distance = Math.sqrt(Math.pow(x,2)+ (float) Math.pow(y,2));
                    //Log.d("Distance",""+distance);
                    if (distance > circle.getWidth()/2){
                        double angle  = Math.atan2(x,y);
                        cursorX = (float) ((circle.getWidth()/2)*Math.sin(angle));
                        cursorY = (float) ((circle.getHeight()/2)*Math.cos(angle));
                        cursorX+=circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
                        cursorY+=circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);
                    }
                    else{
                        cursorX = x+circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
                        cursorY = y+circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);
                    }
                    control.setX(cursorX);
                    control.setY(cursorY);

                    hDifference = control.getX() - circle.getX() - (circle.getWidth() >> 1);
                    if (hDifference > 0)
                        hDifference = control.getX() + control.getWidth() - circle.getX() - (circle.getWidth() >> 1);

                    vDifference = control.getY() - circle.getY() - (circle.getHeight() >> 1);
                    if (vDifference > 0)
                        vDifference = control.getY() + control.getHeight() - circle.getY() - (circle.getHeight() >> 1);

                    view.setCentreControl(  cursorX+ (control.getWidth() >> 1),
                            cursorY+ (control.getHeight() >> 1));
                    view.setTouchCoordinates(event.getRawX(),
                            event.getRawY()-control.getHeight());
                    view.updateOverlay();
                }
                return false;
            }
        });
    }

    public void sendCommand(String message){
        byte[] value;
        try {
            //send data to service
            value = message.getBytes("UTF-8");
            GlobalService.mService.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
