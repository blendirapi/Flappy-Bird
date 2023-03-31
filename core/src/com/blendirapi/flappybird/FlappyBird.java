package com.blendirapi.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
    SpriteBatch batch;

    Texture background;
    Texture[] bird;
    Texture ground;
    Texture getReady;
    Texture topTube;
    Texture bottomTube;
    Texture gameOver;

    int dWidth, dHeight;
    int gameState = 0;
    int flapState = 2;
    int birdY;
    int gap = 360;
    int maxOffset, minOffset;
    int numberOfTubes = 25;
    int numberOfGround = 3;
    int distanceBetweenTubes;
    int tubeVelocity = 8;
    int score = 0;
    int scoringTube = 0;
    int[] tubeX = new int[numberOfTubes];
    int[] topTubeY = new int[numberOfTubes];
    int[] groundX = new int[numberOfGround];

    double velocity = 0;
    double gravity = 1;

    Circle birdCircle;

    Rectangle[] topTubeRectangle;
    Rectangle[] bottomTubeRectangle;

    Random random;

    Sound die, hit, point, swoosh, wing;

    BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        dWidth = Gdx.graphics.getWidth();
        dHeight = Gdx.graphics.getHeight();

        background = new Texture("background.png");
        getReady = new Texture("getReady.png");
        bird = new Texture[4];
        bird[0] = new Texture("yellowbird-downflap.png");
        bird[1] = new Texture("yellowbird-midflap.png");
        bird[2] = new Texture("yellowbird-upflap.png");
        bird[3] = new Texture("yellowbird-midflap.png");
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");
        ground = new Texture("ground.png");
        gameOver = new Texture("gameover.png");

        birdCircle = new Circle();
        distanceBetweenTubes = dWidth * 2 / 3;
        minOffset = gap + ground.getHeight();
        maxOffset = dHeight - minOffset;
        random = new Random();

        die = Gdx.audio.newSound(Gdx.files.internal("die.ogg"));
        hit = Gdx.audio.newSound(Gdx.files.internal("hit.ogg"));
        point = Gdx.audio.newSound(Gdx.files.internal("point.ogg"));
        swoosh = Gdx.audio.newSound(Gdx.files.internal("swoosh.ogg"));
        wing = Gdx.audio.newSound(Gdx.files.internal("wing.ogg"));

        topTubeRectangle = new Rectangle[numberOfTubes];
        bottomTubeRectangle = new Rectangle[numberOfTubes];

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(15);

        startGame();
    }

    public void startGame() {
        birdY = dHeight - (dHeight * 2 / 5);

        //the hitboxes
        for (int i = 0; i < numberOfTubes; i++) {
            tubeX[i] = dWidth + i * distanceBetweenTubes;
            topTubeY[i] = minOffset + random.nextInt(maxOffset - minOffset + 1);
            topTubeRectangle[i] = new Rectangle();
            bottomTubeRectangle[i] = new Rectangle();
        }

        for (int i = 0; i < numberOfGround; i++) {
            groundX[i] = i * ground.getWidth() - dWidth;
        }
    }

    @Override
    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, dWidth, dHeight);
        batch.end();

        //moving the ground to the left
        if (gameState == 0) {
            for (int i = 0; i < numberOfGround; i++) {
                batch.begin();
                groundX[i] -= tubeVelocity;
                if (groundX[i] < -ground.getWidth()){
                    groundX[i] += (numberOfGround) * dWidth;
                }
                batch.draw(ground, groundX[i], 0);
                batch.draw(getReady, dWidth/2 - getReady.getWidth()/2, dHeight/2 - getReady.getHeight()/2);
                batch.end();
            }

        } else if (gameState == 1) {

            if (tubeX[scoringTube] < dWidth / 10) {
                score++;
                point.play();

                if (scoringTube < numberOfTubes - 1) {
                    scoringTube++;
                } else {
                    scoringTube = 0;
                }
            }

            if (Gdx.input.justTouched()) {
                velocity -= 40;
                wing.play();
            }

            //moving the tubes to the left
            for (int i = 0; i < numberOfTubes; i++) {
                batch.begin();
                tubeX[i] -= tubeVelocity;

                if (tubeX[i] < -topTube.getWidth()) {
                    tubeX[i] += numberOfTubes * distanceBetweenTubes;
                    topTubeY[i] = minOffset + random.nextInt(maxOffset - minOffset + 1);
                }
                batch.draw(topTube, tubeX[i], topTubeY[i] + gap);
                batch.draw(bottomTube, tubeX[i], topTubeY[i] - topTube.getHeight());

                topTubeRectangle[i] = new Rectangle(tubeX[i], topTubeY[i] + gap, topTube.getWidth(), topTube.getHeight());
                bottomTubeRectangle[i] = new Rectangle(tubeX[i], topTubeY[i] - topTube.getHeight(), bottomTube.getWidth(), bottomTube.getHeight());

                batch.end();
            }

            //spawn new ground textures
            for (int i = 0; i < numberOfGround; i++) {
                batch.begin();

                groundX[i] -= tubeVelocity;

                if (groundX[i] < -ground.getWidth()){
                    groundX[i] += numberOfGround * ground.getWidth();
                }
                batch.draw(ground, groundX[i], 0);

                batch.end();
            }

            if (birdY > ground.getHeight()) {
                velocity = velocity + gravity;
                birdY -= velocity;
            } else {
                gameState = 2;
            }
            if (birdY >= dHeight) {
                birdY = dHeight - 60;
                velocity = 1;
            }
            if (velocity < -15) {
                velocity = -15;
            }

            if (velocity > 30) {
                velocity = 30;
            }

        } else if (gameState == 2) {
            batch.begin();
            batch.draw(gameOver, dWidth / 2 - gameOver.getWidth() / 2, dHeight - (dHeight * 1 / 4));
            batch.end();
            if (Gdx.input.justTouched()) {
                gameState = 0;
            }
        }

        if (gameState == 0 && Gdx.input.justTouched()) {
            gameState = 1;
            startGame();
            score = 0;
            scoringTube = 0;
            velocity = 0;
        }


        if (gameState == 0 || gameState == 1) {
            if (flapState < 3) {
                flapState++;
            } else {
                flapState = 0;
            }
        }

        birdCircle.set(dWidth / 3, birdY + bird[0].getHeight() / 2, bird[0].getWidth() / 2);

        for (int i = 0; i < numberOfTubes; i++) {
            if (Intersector.overlaps(birdCircle, topTubeRectangle[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangle[i])) {
                gameState = 2;
//                hit.play();
//                die.play();
//                swoosh.play();
                break;
            }
        }

        batch.begin();
        batch.draw(bird[flapState], dWidth / 3 - bird[flapState].getWidth() / 2, birdY);
        font.draw(batch, String.valueOf(score), 100, 250);
        batch.end();
    }
}
