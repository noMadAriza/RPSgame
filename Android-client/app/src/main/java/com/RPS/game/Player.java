package com.RPS.game;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public abstract class Player {
    public enum Players {
        EMPTY_CELL,
        ROCK,
        SCISSORS,
        PAPER,
        FLAG,
        TRAP,
        UNKNOWN
    }
    private Players type;
    protected int row;
    protected int column;
    protected GameLogic.Color mycolor;
    protected  Boolean visible = false;

    public Player(Players type, GameLogic.Color color, GameLogic myGame , int row, int column){
        this.type = type;
        myGame.getGamePlayers()[row][column] = this;
        this.row = row;
        this.column = column;
        this.mycolor = color;
    }
    /* gets a Drawable and imageView and ste imageview object to have Drawable */
    public void setImage(Drawable draw,ImageView image) {
        image.setImageDrawable(draw);
    }

    public Players getType() {
        return type;
    }

    public GameLogic.Color getMycolor() {
        return mycolor;
    }
    public boolean isLight(){
        if(((row + column) % 2 ) == 0)
            return true;
        else
            return false;
    }
    protected void setType(Players type) {
        this.type = type;
    }
    public void setVisible(Boolean bool){
        this.visible = bool;
    }
    public Boolean getVisible(){
        return visible;
    }
}
