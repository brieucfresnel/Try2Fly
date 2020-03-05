package fr.univartois.iutlens.Try2Fly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {

    // Loop

    Handler handler;
    Runnable runnable;
    final int UPDATE_MILLIS=25;

    boolean gameRunning = true;

    // Activity
    StartGame game;

    // Display and background

    Bitmap background;
    Display display;
    Point point;
    int dWidth, dHeight; // Device size
    Rect rect; // Background will be painted on this rect

    // Score
    int score = 0;
    int displayScore = 0;

    // Bird sprites array

    Bitmap[] birds;
    int birdIndex = 0;

    // "Physics" and movement of bird

    int velocity=0, gravity=3;
    int birdX, birdY;

    // Tubes

    Bitmap topTube, botTube;

    int gapBetweenTubes = 400;
    int minTubeOffset, maxTubeOffset;
    int numberOfTubes = 4;
    int distanceBetweenTubes;
    int tubeVelocity = 12;
    int[] tubeX = new int[numberOfTubes];
    int[] topTubeY = new int[numberOfTubes]; // tubeX is the same for tube pairs, and we use topTubeY to calculate the rest
    Random random;
    // Scale and rotate matrices for tubes and bird

    Matrix topTubeMatrix;
    Matrix botTubeMatrix;
    Matrix birdMatrix;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        handler = new Handler();

        runnable = new Runnable() {
            public void run() {
                invalidate(); // onDraw() call
            }
        };

        game = (StartGame) this.getContext();

        // Set up display and background

        background = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getSize(point);

        dWidth = point.x;
        dHeight = point.y;
        rect = new Rect(0, 0, dWidth, dHeight);


        // Set up bird sprites and position

        birds = new Bitmap[2];
        birds[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bird_downflap);
        birds[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bird_upflap);

        // bird initially at the center of the screen

        birdMatrix = new Matrix();
        birdMatrix.postScale(0.05f, 0.05f);

        birds[0] = Bitmap.createBitmap(birds[0], 0, 0, birds[0].getWidth(), birds[0].getHeight(), birdMatrix, true);
        birds[1] = Bitmap.createBitmap(birds[1], 0, 0, birds[1].getWidth(), birds[1].getHeight(), birdMatrix, true);


        birdX = dWidth/2 - birds[0].getWidth()/2;
        birdY = dHeight/2 - birds[0].getHeight()/2;

        // Setting tubes sprites

        botTube = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_red);
        topTube = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_red);

        // As we use the same sprite for top and bot, we rotate to make the top tube
        topTubeMatrix = new Matrix();
        topTubeMatrix.postRotate(180);
        topTubeMatrix.postScale(1.5f, 1.5f);
        topTube = Bitmap.createBitmap(topTube, 0, 0, topTube.getWidth(), topTube.getHeight(), topTubeMatrix, true);

        // Scale everything up because the sprites are small. Top tube was already scaled.

        botTubeMatrix = new Matrix();
        botTubeMatrix.postScale(1.5f, 1.5f);
        botTube = Bitmap.createBitmap(botTube, 0, 0, botTube.getWidth(), botTube.getHeight(), botTubeMatrix, true);


        // Setting tubes placement
        distanceBetweenTubes = dWidth*3/4; // Distance between tubes is 3/4 of screen width
        minTubeOffset = gapBetweenTubes/2;
        maxTubeOffset = dHeight - minTubeOffset - gapBetweenTubes;

        random = new Random();  // Makes topTubeY randomized in the tube offset range
        for (int i = 0 ; i < numberOfTubes ; i++) {
            tubeX[i] = dWidth + i*distanceBetweenTubes;
            topTubeY[i] = minTubeOffset + random.nextInt(maxTubeOffset - minTubeOffset);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(background, null, rect, null);

        // Switch between the two bird sprites
        if(birdIndex == 0) {
            birdIndex = 1;
        } else {
            birdIndex = 0;
        }

        // if game is running, move the bird
        if(gameRunning) {
            velocity += gravity;
            birdY += velocity;

            for (int i = 0 ; i < numberOfTubes ; i++) {
                tubeX[i] -= tubeVelocity;

                // Collisions
                if(birdX > tubeX[i] - topTube.getWidth()/2 && birdX < tubeX[i]+botTube.getWidth() && (birdY < topTubeY[i] || birdY > topTubeY[i]+gapBetweenTubes)) {
                    gameRunning = false;
                    Intent intent = new Intent(getContext(), GameOverActivity.class);
                    intent.putExtra("score", Integer.toString(score));

                    getContext().startActivity(intent);
                    ((Activity)getContext()).finish();
                }

                // Score++

                if(birdX > tubeX[i] + topTube.getWidth() && birdX < tubeX[i] + topTube.getWidth() + 9) {
                    score++;
                }

                // Reset tube position if off-screen
                if(tubeX[i] < -topTube.getWidth()) {
                    tubeX[i] += numberOfTubes * distanceBetweenTubes;
                    topTubeY[i] = minTubeOffset + random.nextInt(maxTubeOffset - minTubeOffset);
                }

                canvas.drawBitmap(topTube, tubeX[i], topTubeY[i] - topTube.getHeight(), null);
                canvas.drawBitmap(botTube, tubeX[i], topTubeY[i] + gapBetweenTubes, null);
            }

        }

        // Keep the bird from going off-screen
        if (birdY > dHeight + birds[0].getHeight()) {
            birdY = dHeight;
        }

        // Draw the bird
        canvas.drawBitmap(birds[birdIndex], birdX, birdY, null);
        handler.postDelayed(runnable, UPDATE_MILLIS);

        // Update scoreView
        game.setScoreViewText(displayScore);

        if(tubeVelocity < 25)
            tubeVelocity+=0.08f;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) { // On tap, the bird moves
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) { // Tap detection
            velocity = -30;
        }

        return true; // Indicates that no further action is required by Android
    }


}
