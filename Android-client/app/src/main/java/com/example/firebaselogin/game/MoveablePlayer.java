package com.example.firebaselogin.game;

import android.widget.ImageView;

public class MoveablePlayer extends Player {
    public enum Direction {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }
    private Players type;
    private boolean isLightSquare;
    GameLogic myGame;
    public MoveablePlayer(GameLogic myGame, Players type, GameLogic.Color color, int row, int column){
        super(type,color,myGame,row,column);
        this.myGame = myGame;
        isLightSquare = ((row + column) % 2) == 0;
    }
    /* gets state of the board and moves the player in the cell provided to direction provided if possible return true, else false */
    public boolean move(Player[][] gamePlayers, ImageView[][] imageCells,Direction direction){
        /* the move is legal */
            ImageView myImage = imageCells[row][column];
            myImage.setOnClickListener(null); //cancels the clickabillity of the previous cell
            gamePlayers[row][column] = null;
            isLightSquare = !isLightSquare;
            switch (direction) {
                case LEFT:
                    gamePlayers[row][column - 1] = this;
                    column--;
                    break;
                case RIGHT:
                    gamePlayers[row][column + 1] = this;
                    column++;
                    break;
                case FORWARD:
                    gamePlayers[row - 1][column] = this;
                    row--;
                    break;
                case BACKWARD:
                    gamePlayers[row + 1][column] = this;
                    row++;
                    break;
            }
            return true;
    }
}