/*
 * Created by steve on 17/10/16.
 * Grid is from bottom left to right, bottom to top.
 */

package com.summerland.android.twozerofoureightapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import android.util.Log;


enum Moves { left, right, up, down }


public class TwoZeroFourEight implements Serializable {

    public enum actions {ADD, BLANK, SLIDE, COMPACT, REFRESH}

    static final int TARGET = 2048;
    private static final int GRID_CNT = 16, ROW_CNT = 4, COL_CNT = 4, BLANK = 0;
    private int score = 0, numEmpty = 16, maxTile = 0;

    private ArrayList<Transition> transitions = null;
    private int[] tiles = new int[16];

    TwoZeroFourEight() {
        this.createNewTransitions();
        this.addNewTile();
        this.addNewTile();
    }

    private void createNewTransitions() {
        transitions = new ArrayList<>();
    }

    public ArrayList<Transition> getTransitions() {
        return transitions;
    }

    public void rePlot() {
        this.createNewTransitions();
        for (int i = 0; i < GRID_CNT; i++) {
            this.transitions.add(new Transition(actions.REFRESH, tiles[i], i));
        }
    }

    private boolean addNewTile() {
        if (numEmpty == 0) return false;

        Random rand = new Random();
        int val = (rand.nextInt(2) + 1) * 2;
        int pos = rand.nextInt(numEmpty);
        int numBlanksFound = 0;

        for (int i = 0; i < GRID_CNT; i++) {
            if (tiles[i] == BLANK) {
                if (numBlanksFound == pos) {
                    tiles[i] = val;
                    if (val > maxTile) maxTile = val;
                    if (transitions != null) {
                        transitions.add(new Transition(actions.ADD, val, i));
                        numEmpty--;
                        return true;
                    }
                }
                numBlanksFound++;
            }
        }
        return false;
    }

    public int getMaxTile() {
        return maxTile;
    }

    //public int getValue(int i) { return (tiles[i]); }

    public int getScore() {
        return score;
    }


    public boolean hasMovesRemaining() {

        if (numEmpty > 0) return true;

        // check left-right for compact moves remaining.
        int arrLimit = GRID_CNT - COL_CNT;
        for (int i = 0; i < arrLimit; i++) {
            if (tiles[i] == tiles[i + COL_CNT]) return true;
        }

        // check up-down for compact moves remaining.
        arrLimit = GRID_CNT - 1;
        for (int i = 0; i < arrLimit; i++) {
            if ((i + 1) % ROW_CNT > 0) {
                if (tiles[i] == tiles[i + 1]) return true;
            }
        }
        return false;
    }

    private boolean slideTileRowOrColumn(int index1, int index2, int index3, int index4) {

        boolean moved = false;
        int[] tmpArr = {index1, index2, index3, index4};

        // Do we have some sliding to do, or not?
        int es = 0;  // empty spot index
        for (int j = 1; j < tmpArr.length; j++) {
            if (tiles[tmpArr[es]] != BLANK) {
                es++;
            } else if (tiles[tmpArr[j]] == BLANK) {
                continue;
            } else {
                // Otherwise we have a slide condition
                tiles[tmpArr[es]] = tiles[tmpArr[j]];
                tiles[tmpArr[j]] = BLANK;
                transitions.add(new Transition(actions.SLIDE, tiles[tmpArr[es]], tmpArr[es], tmpArr[j]));
                transitions.add(new Transition(actions.BLANK, BLANK, tmpArr[j]));
                moved = true;
                es++;
            }
        }
        return moved;
    }

    private boolean compactTileRowOrColumn(int index1, int index2, int index3, int index4) {

        boolean compacted = false;
        int[] tmpArr = {index1, index2, index3, index4};

        for (int j = 0; j < (tmpArr.length-1); j++) {

            if (tiles[tmpArr[j]] != BLANK && tiles[tmpArr[j]] == tiles[tmpArr[j+1]]) { // we found a matching pair
                int ctv = tiles[tmpArr[j]] * 2;   // = compacted tile value
                tiles[tmpArr[j]] = ctv;
                tiles[tmpArr[j+1]] = BLANK;
                score += ctv;
                if (ctv > maxTile) { maxTile = ctv; }  // is this the biggest tile # so far
                if (transitions != null) {
                    transitions.add(new Transition(actions.COMPACT, ctv, tmpArr[j], tmpArr[j+1]));
                    transitions.add(new Transition(actions.BLANK, BLANK, tmpArr[j+1]));
                }
                compacted = true;
                numEmpty++;
            }
        }
        return compacted;
    }

    private boolean slideLeft() {
        boolean a = slideTileRowOrColumn(0, 4, 8, 12);
        boolean b = slideTileRowOrColumn(1, 5, 9, 13);
        boolean c = slideTileRowOrColumn(2, 6, 10, 14);
        boolean d = slideTileRowOrColumn(3, 7, 11, 15);
        return (a || b || c || d);
    }

    private boolean slideRight() {
        boolean a = slideTileRowOrColumn(12, 8, 4, 0);
        boolean b = slideTileRowOrColumn(13, 9, 5, 1);
        boolean c = slideTileRowOrColumn(14, 10, 6, 2);
        boolean d = slideTileRowOrColumn(15, 11, 7, 3);
        return (a || b || c || d);
    }

    private boolean slideUp() {
        boolean a = slideTileRowOrColumn(0, 1, 2, 3);
        boolean b = slideTileRowOrColumn(4, 5, 6, 7);
        boolean c = slideTileRowOrColumn(8, 9, 10, 11);
        boolean d = slideTileRowOrColumn(12, 13, 14, 15);
        return (a || b || c || d);
    }

    private boolean slideDown() {
        boolean a = slideTileRowOrColumn(3, 2, 1, 0);
        boolean b = slideTileRowOrColumn(7, 6, 5, 4);
        boolean c = slideTileRowOrColumn(11, 10, 9, 8);
        boolean d = slideTileRowOrColumn(15, 14, 13, 12);
        return (a || b || c || d);
    }

    private boolean compactLeft() {
        boolean a = compactTileRowOrColumn(0, 4, 8, 12);
        boolean b = compactTileRowOrColumn(1, 5, 9, 13);
        boolean c = compactTileRowOrColumn(2, 6, 10, 14);
        boolean d = compactTileRowOrColumn(3, 7, 11, 15);
        return (a || b || c || d);
    }

    private boolean compactRight() {
        boolean a = compactTileRowOrColumn(12, 8, 4, 0);
        boolean b = compactTileRowOrColumn(13, 9, 5, 1);
        boolean c = compactTileRowOrColumn(14, 10, 6, 2);
        boolean d = compactTileRowOrColumn(15, 11, 7, 3);
        return (a || b || c || d);
    }

    private boolean compactUp() {
        boolean a = compactTileRowOrColumn(0, 1, 2, 3);
        boolean b = compactTileRowOrColumn(4, 5, 6, 7);
        boolean c = compactTileRowOrColumn(8, 9, 10, 11);
        boolean d = compactTileRowOrColumn(12, 13, 14, 15);
        return (a || b || c || d);
    }

    private boolean compactDown() {
        boolean a = compactTileRowOrColumn(3, 2, 1, 0);
        boolean b = compactTileRowOrColumn(7, 6, 5, 4);
        boolean c = compactTileRowOrColumn(11, 10, 9, 8);
        boolean d = compactTileRowOrColumn(15, 14, 13, 12);
        return (a || b || c || d);
    }

    private boolean actionMoveLeft() {
        boolean a = slideLeft();
        boolean b = compactLeft();
        boolean c = slideLeft();
        return (a || b || c);
    }

    private boolean actionMoveRight() {
        boolean a = slideRight();
        boolean b = compactRight();
        boolean c = slideRight();
        return (a || b || c);
    }

    private boolean actionMoveUp() {
        boolean a = slideUp();
        boolean b = compactUp();
        boolean c = slideUp();
        return (a || b || c);
    }

    private boolean actionMoveDown() {
        boolean a = slideDown();
        boolean b = compactDown();
        boolean c = slideDown();
        return (a || b || c);
    }


    // Central game move trigger
    // Central game move trigger
    public boolean actionMove(Moves move) {

        this.createNewTransitions();
        if (!hasMovesRemaining()) return false;

        boolean result = false;
        switch (move) {
            case left:
                result = actionMoveLeft();  break;
            case right:
                result = actionMoveRight(); break;
            case up:
                result = actionMoveUp();    break;
            case down:
                result = actionMoveDown();  break;
        }

        if (result) addNewTile();
        return result;
    }


    @Override
    public String toString() {
        return "TwoZeroFourEight:class\n" +
                "--------------------\n" +
                "|" + tiles[0] + "|" + tiles[4] + "|" + tiles[8] + "|" + tiles[12] + "|\n" +
                "|" + tiles[1] + "|" + tiles[5] + "|" + tiles[9] + "|" + tiles[13] + "|\n" +
                "|" + tiles[2] + "|" + tiles[6] + "|" + tiles[10] + "|" + tiles[14] + "|\n" +
                "|" + tiles[3] + "|" + tiles[7] + "|" + tiles[11] + "|" + tiles[15] + "|\n" +
                "--------------------\n";
    }


    class Transition implements Serializable {

        actions type;
        int val, newLocation, oldLocation = -1;

        Transition(actions action, int val, int newLocation, int oldLocation) {
            type = action;
            this.val = val;
            this.newLocation = newLocation;
            this.oldLocation = oldLocation;
        }

        Transition(actions action, int val, int newLocation) {
            type = action;
            this.val = val;
            this.newLocation = newLocation;
        }
    }
}
