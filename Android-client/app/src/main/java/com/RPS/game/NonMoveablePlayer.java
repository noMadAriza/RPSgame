package com.RPS.game;

public class NonMoveablePlayer extends Player {
    public NonMoveablePlayer(GameLogic myGame, Players type, GameLogic.Color color, int row, int column){
        super(type,color,myGame,row,column);
    }
}