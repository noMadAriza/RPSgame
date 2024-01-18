package com.RPS.game;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.RPS.main.MainActivity;
import com.RPS.firebaselogin.R;

public class GameActivity extends AppCompatActivity {

    GameLogic myGame;
    final int cntPlayer = 4;
    int screenHeight;

    Drawable darkSquare;
    Drawable lightSquare;
    Drawable darkPaper_blue;
    Drawable darkRock_blue ;
    Drawable darkScissors_blue;
    Drawable lightPaper_blue;
    Drawable lightRock_blue ;
    Drawable lightScissors_blue;
    Drawable darkPaper_red;
    Drawable darkRock_red ;
    Drawable darkScissors_red;
    Drawable lightPaper_red;
    Drawable lightRock_red ;
    Drawable lightScissors_red;
    Drawable lightTrap;
    Drawable darkTrap;
    Drawable darkFlag_blue;
    Drawable lightFlag_blue;
    Drawable darkFlag_red;
    Drawable lightFlag_red;
    Drawable lightquestion_red;
    Drawable darkquestion_red;
    Drawable lightquestion_blue;
    Drawable darkquestion_blue;
    // highlight drawables
    Drawable highlightEmptySquare;
    Drawable highLight_rock_red;
    Drawable highLight_rock_blue;
    Drawable highLight_scissors_red;
    Drawable highLight_scissors_blue;
    Drawable highLight_paper_red;
    Drawable highLight_paper_blue;
    Drawable highLight_hole;
    Drawable highLight_flag_red;
    Drawable highLight_flag_blue;
    Drawable highLight_question_red;
    Drawable highLight_question_blue;

    FrameLayout transparentSheet;
    ImageView rockMenuChoose;
    ImageView paperMenuChoose;
    ImageView scissorsMenuChoose;
    LinearLayout warMenu;
    TableLayout board;
    TextView textPrompt;
    TextView turnPrompt;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        board = findViewById(R.id.board);
        darkFlag_blue = getDrawable(R.drawable.dark_blue_flag);
        lightFlag_blue = getDrawable(R.drawable.light_blue_flag);
        darkFlag_red = getDrawable(R.drawable.dark_red_flag);
        lightFlag_red = getDrawable(R.drawable.light_red_flag);
        lightquestion_red = getDrawable(R.drawable.white_red_question);
        darkquestion_red = getDrawable(R.drawable.dark_red_question);
        lightquestion_blue = getDrawable(R.drawable.white_blue_question);
        darkquestion_blue = getDrawable(R.drawable.dark_blue_question);
        darkPaper_blue = getDrawable(R.drawable.dark_blue_paper);
        darkRock_blue = getDrawable(R.drawable.dark_blue_rock);
        darkScissors_blue = getDrawable(R.drawable.dark_blue_scissors);
        lightPaper_blue = getDrawable(R.drawable.white_blue_paper);
        lightRock_blue = getDrawable(R.drawable.white_blue_rock);
        lightScissors_blue = getDrawable(R.drawable.white_blue_scissors);
        darkPaper_red = getDrawable(R.drawable.dark_red_paper);
        darkRock_red = getDrawable(R.drawable.dark_red_rock);
        darkScissors_red = getDrawable(R.drawable.dark_red_scissors);
        lightPaper_red = getDrawable(R.drawable.white_red_paper);
        lightRock_red = getDrawable(R.drawable.white_red_rock);
        lightScissors_red = getDrawable(R.drawable.white_red_scissors);
        lightSquare = getDrawable(R.drawable.lightsquare);
        darkSquare = getDrawable(R.drawable.darksquare);
        darkSquare = getDrawable(R.drawable.darksquare);
        lightSquare = getDrawable(R.drawable.lightsquare);
        lightTrap = getDrawable(R.drawable.hole_light);
        darkTrap = getDrawable(R.drawable.hole_dark);
        // highlight drawables
        highlightEmptySquare = getDrawable(R.drawable.highlight_empty_square);
        highLight_flag_blue = getDrawable(R.drawable.highlight_blue_flag);
        highLight_flag_red = getDrawable(R.drawable.highlight_red_flag);
        highLight_question_red = getDrawable(R.drawable.highlight_red_question);
        highLight_question_blue = getDrawable(R.drawable.highlight_blue_question);
        highLight_paper_blue = getDrawable(R.drawable.highlight_blue_paper);
        highLight_paper_red = getDrawable(R.drawable.highlight_red_paper);
        highLight_rock_blue = getDrawable(R.drawable.highlight_blue_rock);
        highLight_rock_red = getDrawable(R.drawable.highlight_red_rock);
        highLight_scissors_blue = getDrawable(R.drawable.highlight_blue_scissors);
        highLight_scissors_red = getDrawable(R.drawable.highlight_red_scissors);
        highLight_hole = getDrawable(R.drawable.hole_highlight);

        transparentSheet = findViewById(R.id.transparentSheet);
        rockMenuChoose = findViewById(R.id.rockMenuChoose);
        paperMenuChoose = findViewById(R.id.paperMenuChoose);
        scissorsMenuChoose = findViewById(R.id.scissorsMenuChoose);
        textPrompt = findViewById(R.id.textPrompt);
        turnPrompt = findViewById(R.id.turnPrompt);
        warMenu = findViewById(R.id.warMenu);

        new Thread(() -> {
            int added = 0;
            String text;
            while(textPrompt.getVisibility() == View.VISIBLE){
                try {
                    text = null;
                    if(added >= 3){
                        text = textPrompt.getText().toString().substring(0,textPrompt.length()-3);
                        added = -1;
                    }
                    String finalText = text;
                    runOnUiThread(() -> {
                        if(finalText != null)
                            textPrompt.setText(finalText);
                        else
                            textPrompt.append(".");
                    });
                    added++;
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        myGame = new GameLogic(this,intent.getIntExtra("lobbyID", 0), (GameLogic.Color) intent.getSerializableExtra("color"));

        new Thread(() -> {
            setWarMenuImages();
        }).start();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.widthPixels;
    }

    /* builds the initial game UI */
    public void initialGameState(ImageView[][] cellsImage) {
        runOnUiThread(() -> {
            int squareHeight = (int) Math.floor(screenHeight/ (double) cellsImage.length);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,squareHeight);
            layoutParams.width = 0;
            layoutParams.weight = 1;
            // Loop through each row and column to create the cells
            for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
                TableRow tableRow = new TableRow(this);
                for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                    ImageView imageView = new ImageView(this);
                    if(((col + row) % 2) == 0) { // it is a dark square
                        imageView.setImageDrawable(lightSquare);
                    }
                    else { // it is a dark square
                        imageView.setImageDrawable(darkSquare);
                    }
                    // Set the maximum width and height of the image view
                    imageView.setMaxWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, squareHeight, getResources().getDisplayMetrics()));
                    imageView.setMaxHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, squareHeight, getResources().getDisplayMetrics()));

                    // Set the layout parameters for the image view
                    imageView.setLayoutParams(layoutParams);
                    // Add the image view to the current row
                    tableRow.addView(imageView);
                    // Add the image view to the cells array
                    cellsImage[row][col] = imageView;
                }
                // Add the current row to the table layout
                board.addView(tableRow,row);
            }
            startGame(cellsImage);
        });
    }

    /*starting game  enables the player to choose where to place his flag and trap*/
    private void startGame(ImageView[][] cellsImage){
        runOnUiThread(() -> {
            textPrompt.setText("choose where to place your flag ");
        });
        for (int row = cellsImage.length - 2; row < cellsImage.length; row++) { // 2 rows for selecting flag and Trap
            for (int column = 0; column < cellsImage[0].length; column++) {
                ImageView image = cellsImage[row][column];
                image.setOnClickListener(view -> {
                    int[] p;
                    p = GameLogic.findObjectPlace(board, image);
                    int cnt = myGame.getCnt();
                    /* placing the flag */
                    if(cnt == 0) {
                        runOnUiThread(() -> {
                            new NonMoveablePlayer(myGame, Player.Players.FLAG, myGame.getColor(), p[0],p[1]);
                            setImage(myGame.getCellsImage()[p[0]][p[1]], Player.Players.FLAG,myGame.getColor(),isLight(p[0],p[1]),false);
                            myGame.getCellsImage()[p[0]][p[1]].setOnClickListener(null);
                            textPrompt.setText("choose where to place your trap ");
                        });
                        myGame.addCnt();
                    }
                    // placing the hole
                    else{
                        new NonMoveablePlayer(myGame, Player.Players.TRAP, myGame.getColor(), p[0],p[1]);
                        runOnUiThread(() -> {
                            setImage(myGame.getCellsImage()[p[0]][p[1]], Player.Players.TRAP,myGame.getColor(),isLight(p[0],p[1]),false);
                            removeClickable(cellsImage); //no more clickable squares for putting starting objects
                            textPrompt.setVisibility(View.INVISIBLE);
                        });
                        new Thread(() -> {
                            shuffle(cellsImage, myGame.getGamePlayers());
                            myGame.addCnt();
                        }).start();
                    }
                });
            }
        }
    }

    /* gets the images and matrix of players of the game and assign to the last 2 rows players and its images randomly */
    private void shuffle(ImageView[][] imageCells,Player[][] gamePlayers){
        runOnUiThread(() -> {
            int[] playersCnt = {cntPlayer,cntPlayer,cntPlayer};
            int rand;
            int playersPlaced = 2;
            for (int row = imageCells.length - 2; row < imageCells.length; row++) { //  filling up 2rows
                for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
                    do
                        rand = getRandomArbitrary(0, playersCnt.length - 1);
                    while(playersCnt[rand] <= 0 && playersPlaced < 2 * GameConstants.BOARD_SIZE); // get a random number until you get a needed type of player
                    Player.Players playerType = Player.Players.values()[rand + 1];
                    if(gamePlayers[row][column] == null){ //if the placement is not used for any other player
                        MoveablePlayer player = new MoveablePlayer(myGame,playerType, myGame.getColor(),row,column);
                        setImage(myGame.getCellsImage()[row][column],playerType,myGame.getColor(),isLight(row,column),false);
                        myGame.makeClickable(player);
                        playersCnt[rand]--;
                        playersPlaced++;
                    }
                }
            }
            myGame.updateServer(); // updates the server of the new locations of objects
        });
    }

    /* get a random int between min and max */
    private int getRandomArbitrary(int min, int max) {
        return (int) Math.round(Math.random() * (max - min) + min);
    }

    /* gets a 2-dimensional ImageView array, and size of the matrix. removes the clickable function each ImageView has has */
    private void removeClickable(ImageView[][] cellsImage){
        for (int i = 0; i < cellsImage.length; i++) {
            for (int j = 0; j < cellsImage.length; j++) {
                cellsImage[i][j].setOnClickListener(null);
            }
        }
    }

    // gets Player matrix and updates UI accordingly
    public void updateUI(Player[][] players) {
        ImageView[][] imageCells = myGame.getCellsImage();
        Player.Players type;
        GameLogic.Color color;
        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
                color = null;
                type = Player.Players.EMPTY_CELL;
                Boolean light = isLight(row,column);
                if(players[row][column] != null) { // not empty
                    type = players[row][column].getType();
                    color = players[row][column].getMycolor();
                }
                setImage(imageCells[row][column], type, color ,light,false);
            }
        }
    }   //can be considered to change UI of only enemy for less complexity time

    // gets image and player data and attaches the right Drawable to it
    private void setImage(ImageView image, Player.Players type, GameLogic.Color color, Boolean light,boolean highLight) {
        runOnUiThread(() -> {
            if (color == GameLogic.Color.RED) {
                switch (type) {
                    case ROCK:
                        if(highLight)
                            image.setImageDrawable(highLight_rock_red);
                        else if (light)
                            image.setImageDrawable(lightRock_red);
                        else
                            image.setImageDrawable(darkRock_red);
                        break;
                    case PAPER:
                        if(highLight)
                            image.setImageDrawable(highLight_paper_red);
                        else if (light)
                            image.setImageDrawable(lightPaper_red);
                        else
                            image.setImageDrawable(darkPaper_red);
                        break;
                    case SCISSORS:
                        if(highLight)
                            image.setImageDrawable(highLight_scissors_red);
                        else if (light)
                            image.setImageDrawable(lightScissors_red);
                        else
                            image.setImageDrawable(darkScissors_red);
                        break;
                    case FLAG:
                        if(highLight)
                            image.setImageDrawable(highLight_flag_red);
                        else if (light)
                            image.setImageDrawable(lightFlag_red);
                        else
                            image.setImageDrawable(darkFlag_red);
                        break;
                    case TRAP:
                        if(highLight)
                            image.setImageDrawable(highLight_hole);
                        else if (light)
                            image.setImageDrawable(lightTrap);
                        else
                            image.setImageDrawable(darkTrap);
                        break;
                    case UNKNOWN:
                        if(highLight)
                            image.setImageDrawable(highLight_question_red);
                        else if (light)
                            image.setImageDrawable(lightquestion_red);
                        else
                            image.setImageDrawable(darkquestion_red);
                        break;
                }
            }
            else if (color == GameLogic.Color.BLUE) {
                switch (type) {
                    case ROCK:
                        if(highLight)
                            image.setImageDrawable(highLight_rock_blue);
                        else if (light)
                            image.setImageDrawable(lightRock_blue);
                        else
                            image.setImageDrawable(darkRock_blue);
                        break;
                    case PAPER:
                        if(highLight)
                            image.setImageDrawable(highLight_paper_blue);
                        else if (light)
                            image.setImageDrawable(lightPaper_blue);
                        else
                            image.setImageDrawable(darkPaper_blue);
                        break;
                    case SCISSORS:
                        if(highLight)
                            image.setImageDrawable(highLight_scissors_blue);
                        else if (light)
                            image.setImageDrawable(lightScissors_blue);
                        else
                            image.setImageDrawable(darkScissors_blue);
                        break;
                    case FLAG:
                        if(highLight)
                            image.setImageDrawable(highLight_flag_blue);
                        else if (light)
                            image.setImageDrawable(lightFlag_blue);
                        else
                            image.setImageDrawable(darkFlag_blue);
                        break;
                    case TRAP:
                        if(highLight)
                            image.setImageDrawable(highLight_hole);
                        else if (light)
                            image.setImageDrawable(lightTrap);
                        else
                            image.setImageDrawable(darkTrap);
                        break;
                    case UNKNOWN:
                        if(highLight)
                            image.setImageDrawable(highLight_question_blue);
                        else if (light)
                            image.setImageDrawable(lightquestion_blue);
                        else
                            image.setImageDrawable(darkquestion_blue);
                        break;
                }
            } else {   //it is an empty cell
                if(highLight)
                    image.setImageDrawable(highlightEmptySquare);
                else if (light)
                    image.setImageDrawable(lightSquare);
                else
                    image.setImageDrawable(darkSquare);
            }
            if(myGame.getCnt() >= 2 && color != myGame.getColor())
                image.setOnClickListener(null);
        });
    }

    // determines if a cell is light
    private Boolean isLight(int row,int column){
        if((row + column) % 2 == 0)
            return true;
        return false;
    }

    public void showMenu() {
        System.out.println("showing menu!");
        runOnUiThread(() ->{
            transparentSheet.setVisibility(View.VISIBLE);
            warMenu.setVisibility(View.VISIBLE);
            rockMenuChoose.setOnClickListener(view -> {
                clickWarMenu(Player.Players.ROCK);
            });
            paperMenuChoose.setOnClickListener(view -> {
                clickWarMenu(Player.Players.PAPER);
            });
            scissorsMenuChoose.setOnClickListener(view -> {
                clickWarMenu(Player.Players.SCISSORS);
            });
        });
    }
    // clicked on the war-menu
    private void clickWarMenu(Player.Players type){
        myGame.sendMenuChoose(type);
        hideMenu();
    }

    //hides the war menu
    public void hideMenu(){
        runOnUiThread(() -> {
            transparentSheet.setVisibility(View.INVISIBLE);
            warMenu.setVisibility(View.INVISIBLE);
            rockMenuChoose.setOnClickListener(null);
            scissorsMenuChoose.setOnClickListener(null);
            paperMenuChoose.setOnClickListener(null);
        });

    }

    // sets appropriate images for war menu
    private void setWarMenuImages(){
        if(myGame.getColor() == GameLogic.Color.BLUE){
            rockMenuChoose.setImageDrawable(darkRock_blue);
            paperMenuChoose.setImageDrawable(darkPaper_blue);
            scissorsMenuChoose.setImageDrawable(darkScissors_blue);
        }
        else{
            rockMenuChoose.setImageDrawable(darkRock_red);
            paperMenuChoose.setImageDrawable(darkPaper_red);
            scissorsMenuChoose.setImageDrawable(darkScissors_red);
        }
    }
    //gets input of whether lost or won. and shows an appropriate msg. returns to main Activity
    public void finishGame(boolean winner) {
        StringBuilder sb = new StringBuilder("you just");
        if(winner)
            sb.append(" won!");
        else
            sb.append(" lost!");
        runOnUiThread(() -> {
            Toast.makeText(this,sb.toString(),Toast.LENGTH_LONG).show();
        });
        myGame = null;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            sleep(500);
        }catch(Exception e){ e.printStackTrace(); }
        this.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        myGame.win(false);
                        GameActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    /* highlights the cell in order to make the player understand possible moves if boolean true, if false the other way around */
    public void highlight(ImageView cell, int row, int column, Player player, boolean bool){
        if(player == null || player.getType() == Player.Players.EMPTY_CELL){ // cell is empty
            setImage(cell, Player.Players.EMPTY_CELL,null,isLight(row,column),bool);
        }
        else{
            if(player.getMycolor() != myGame.getColor())
                setImage(cell,player.getType(),player.getMycolor(),isLight(row,column),bool);
        }

    }

    //shows it's player's turn on the screen
    public void showTurn(boolean turn) {
        runOnUiThread(() -> {
            if(turn)
                turnPrompt.setVisibility(View.VISIBLE);
            else
                turnPrompt.setVisibility(View.INVISIBLE);
        });
    }
}