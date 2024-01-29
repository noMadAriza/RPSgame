package com.RPS.game;

import android.app.Activity;
import android.view.View;

public class Timer extends Thread{

    private int time;
    private boolean bool = true;
    private GameActivity activity;
    private GameLogic logic;

    public Timer(GameLogic logic,GameActivity activity, int time){
        this.time = time;
        this.activity = activity;
        this.logic = logic;
    }
    @Override
    public void run() {
        while(time >= 0 && bool){
            try {
                activity.setTime(time);
                sleep(1000);
                time--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(bool) {
            if(activity.warMenu.getVisibility() == View.VISIBLE) //out of time while in war
                activity.expiredWar();
            else
                logic.updateServer(); // normal out of time
        }
    }
    public void stopTimer(){
        bool = false;
    }
}