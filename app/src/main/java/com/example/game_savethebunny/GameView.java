package com.example.game_savethebunny;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    //declaring variables
    Bitmap background, ground, bunny;
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 120;
    int points = 0;
    int life = 3;
    //device width and height
    static int dWidth, dHeight;
    // random object reference
    Random random;
    //flaoting variables to store bunny coordinates
    float bunnyX, bunnyY;
    // variables ot reposition bunny during touch
    float oldX;
    float oldBunnyX;
    //array list for spikes and explosions
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;




    public GameView(Context context) {
        super(context);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        bunny = BitmapFactory.decodeResource(getResources(), R.drawable.bunny);
        //instantiate display object
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        //instantiate point method
        Point size = new Point();
        //get size method
        display.getSize(size);
        //get device width and heights
        dWidth = size.x;
        dHeight = size.y;
        rectBackground = new Rect(0,0, dWidth,dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);
        //instantiate handler object, runnable class will schedule handler to run after delay
        handler = new Handler();
        //instantiate the runnable object
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        textPaint.setColor(Color.rgb(255, 165,0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenneyblocks));
        //setting color of health paint to gree
        healthPaint.setColor(Color.GREEN);
        //instantiate random object
        random = new Random();
        //initializing bunny coordinates so that bunny can be drawn horizontal center and on top of ground
        bunnyX = dWidth / 2 - bunny.getWidth() / 2;
        bunnyY = dHeight - ground.getHeight() - bunny.getHeight();
        //instantiate spikes and explosions array lists
        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        //create 3 spike objects and add to spikes array list
        for(int i = 0; i < 3; i++) {
            Spike spike = new Spike(context);
            spikes.add(spike);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //call drawBitmap on canvas object to draw the following - background, ground and the bunny
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(bunny, bunnyX, bunnyY, null);
        //iterate for loop 3 times
        for(int i = 0; i < spikes.size(); i++){
            //draw spike element with current draw frame
            canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
            //increment spike frame for current element
            spikes.get(i).spikeFrame++;
            // for smooth spinning animation go for following if statement
            if (spikes.get(i).spikeFrame > 2){
                spikes.get(i).spikeFrame = 0;
            }
            //increment SpikeY for top-down movement
            spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
            //checking if bottom of spike touches to top edge of the ground
            if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()){
               //if above is true, move by 10 points
                points += 10;
                //creating an explosion object
               Explosion explosion = new Explosion(context);
               explosion.explosionX = spikes.get(i).spikeX;
               explosion.explosionY = spikes.get(i).spikeY;
               //add explosions object to explosions Array List
               explosions.add(explosion);
               // call reset position on current spike element form spikes array list
               spikes.get(i).resetPosition();
            }
        }
        //iterating for loop from 0 to spike size -1
        for(int i = 0; i < spikes.size(); i++){

            //checking if spike right edge touches the bunny's left edge,
            if(spikes.get(i).spikeX + spikes.get(i).getSpikeWidth() >= bunnyX
                && spikes.get(i).spikeX <= bunnyX + bunny.getWidth()
                && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() >= bunnyY
                && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() <= bunnyY + bunny.getHeight()){
                life--;
                spikes.get(i).resetPosition();;
                if(life == 0){
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }

        }
        //for loop from zero to explosions size -1
        for (int i= 0; i<explosions.size(); i++){
            //draw explosion bitmaps
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX, explosions.get(i).explosionY, null);
            //increment explosion frame for every explosion in array list
            explosions.get(i).explosionFrame++;
            //once explosion element become more than 3, remove the object from array list
            if(explosions.get(i).explosionFrame > 3){
                explosions.remove(i);
            }
            //if life becomes 2, color it yellow
           if (life == 2){
               healthPaint.setColor(Color.YELLOW);
           } else if (life == 1){
                healthPaint.setColor(Color.RED);
           }
        }
        //drawing the health bar
        canvas.drawRect(dWidth-200, 30, dWidth-200+60*life, 80, healthPaint);
        //draw points
        canvas.drawText(""+ points, 20, TEXT_SIZE,textPaint);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //storing two float variables for touch coordinates
        float touchX = event.getX();
        float touchY = event.getY();
        //condition - only if touch is on or below the bunny, we accept it
        if(touchY >= bunnyY){
            int action = event.getAction();
            if(action==MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldBunnyX = bunnyX;
            }
            if (action==MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;
                float newBunnyX = oldBunnyX - shift;
                if(newBunnyX <= 0)
                    bunnyX = 0;
                else if(newBunnyX>=dWidth - bunny.getWidth())
                    bunnyX = dWidth-bunny.getWidth();
                else
                    bunnyX = newBunnyX;

            }
        }

        return true;
    }
}
