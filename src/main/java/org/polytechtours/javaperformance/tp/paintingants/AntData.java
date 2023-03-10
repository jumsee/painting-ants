package org.polytechtours.javaperformance.tp.paintingants;


import javafx.scene.paint.Color;

import java.util.Random;

public class AntData {
    private Color droppedColor;
    private Color followedColor;

    private float x;
    private float y;
    private int direction;
    private int trailSize;

    private char moveType; //'o' for oblique move 'd' for straight
    private float forwardProb; //probability to go forward
    private float leftProb; //probability to go left
    private float rightProb; //probability to go right
    private float followProb;

    public AntData(Color dColor, Color fColor, Random randGen){
        droppedColor = dColor;
        followedColor = fColor;

        x = randGen.nextFloat();
        y = randGen.nextFloat();

        direction = randGen.nextInt(8);
        trailSize = randGen.nextInt(4);

        forwardProb = randGen.nextFloat();
        leftProb = (float) (randGen.nextFloat() * (1.0 - forwardProb));
        rightProb = (float) (1.0 - (forwardProb + leftProb));
        followProb = (float) (0.5 + 0.5 * randGen.nextFloat());

        // 50% chance to have an oblique move
        if (randGen.nextFloat() < 0.5f) {
            moveType = 'd';
        } else {
            moveType = 'o';
        }
    }

    public Color getDroppedColor() {
        return droppedColor;
    }

    public Color getFollowedColor() {
        return followedColor;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getDirection() {
        return direction;
    }

    public int getTrailSize() {
        return trailSize;
    }

    public char getMoveType() {
        return moveType;
    }

    public float getForwardProb() {
        return forwardProb;
    }

    public float getLeftProb() {
        return leftProb;
    }

    public float getRightProb() {
        return rightProb;
    }

    public float getFollowProb() {
        return followProb;
    }
}
