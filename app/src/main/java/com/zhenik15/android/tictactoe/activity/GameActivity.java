package com.zhenik15.android.tictactoe.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenik15.android.tictactoe.R;
import com.zhenik15.android.tictactoe.model.Board;
import com.zhenik15.android.tictactoe.model.Player;
import com.zhenik15.android.tictactoe.model.PlayerStatsService;
import com.zhenik15.android.tictactoe.model.util.ApplicationPojoFactory;
import com.zhenik15.android.tictactoe.model.util.GameStatusCode;
import com.zhenik15.android.tictactoe.model.util.GameSymbol;

/**
 *  Class represent game
 *  implements OnClickListener interface,
 *  for bottom button navigation bar
 *
 *  Game loop in method getGameClickListener
 *
 * */
public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "GameActivity:> ";

    // Views
    private TextView scorePlayer1;
    private TextView scorePlayer2;
    private ViewGroup navigation;

    private RelativeLayout player1Info;
    private RelativeLayout player2Info;
    private Drawable marker;

    // Models
    private Board gameBoard;
    private Player player1;
    private Player player2;
    private PlayerStatsService playerStatsService;

    // Game
    private int turnCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initPlayers();
        initBoard();
        initNavigation();
        navigation.setVisibility(View.INVISIBLE);
        setListenerNavigationButtons();
        initInfos();
        resetGame();
        playerStatsService = ApplicationPojoFactory.getPlayerStatsServiceInstance(getApplicationContext());

    }

    // http://stackoverflow.com/questions/15686555/display-back-button-on-action-bar
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * SET & INIT  methods
     */
    private void initInfos() {
        player1Info = (RelativeLayout) findViewById(R.id.game_player1_info);
        player2Info = (RelativeLayout) findViewById(R.id.game_player2_info);
        marker = getResources().getDrawable(R.drawable.turn_border, getTheme());
//        changeMarker();
//        player1Info.setBackground(getResources().getDrawable(R.drawable.turn_border, getTheme()));
//        player1Info.setBackground(null);
    }

    private void initPlayers() {
        player1 = (Player) getIntent().getSerializableExtra(MainActivity.PLAYER1);
        player2 = (Player) getIntent().getSerializableExtra(MainActivity.PLAYER2);

        ((TextView) findViewById(R.id.game_player1_name)).setText(player1.getName());
        ((TextView) findViewById(R.id.game_player2_name)).setText(player2.getName());

        scorePlayer1 = (TextView) findViewById(R.id.game_player1_score);
        scorePlayer2 = (TextView) findViewById(R.id.game_player2_score);
        updateScore();

    }

    private void setScore(TextView scoreView, Player player) {
        scoreView.setText("Score: " + player.getScore());
    }

    private void updateScore() {
        setScore(scorePlayer1, player1);
        setScore(scorePlayer2, player2);
    }

    private void initNavigation() {
        navigation = (ViewGroup) findViewById(R.id.game_nav_panel);
    }

    private void initBoard() {
        TableLayout table = (TableLayout) findViewById(R.id.game_board);
        gameBoard = ApplicationPojoFactory.getBoardInstance(table);
    }

    public void setCellListeners() {
        TableLayout table = gameBoard.getTableLayout();
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);
                // render empty template
                cell.setText("");
                cell.setOnClickListener(getGameClickListener(i, j, cell));
                Log.i(TAG, "set up listener for " + cell.getText() + i + j);
            }
        }
    }

    private void unsetCellListeners() {
        for (int i = 0; i < gameBoard.getTableLayout().getChildCount(); i++) {
            TableRow row = (TableRow) gameBoard.getTableLayout().getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);
                cell.setOnClickListener(null);
                Log.i(TAG, "unset listener for " + cell.getText() + i + j);
            }
        }
    }

    /**
     *  returning LISTENER CONTAINS MAIN GAME LOOP
     */
    //http://stackoverflow.com/questions/10614696/how-to-pass-parameters-to-onclicklistener/10614751
    public View.OnClickListener getGameClickListener(final int i, final int j, final TextView cell) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                char turnSymbol = nextTurn();

                if (gameBoard.isCellAvailable(i, j)) {
//                    Log.i(TAG, "|turn=|"+turnSymbol+"|");
                    gameBoard.getTable()[i][j] = turnSymbol;
//                    Log.i(TAG,": click cell "+ i+j + " : "+gameBoard[i][j]);
                    if (turnSymbol == GameSymbol.X)
                        cell.setTextColor(Color.parseColor("#ffcc0000"));
                    if (turnSymbol == GameSymbol.O)
                        cell.setTextColor(Color.parseColor("#ff99cc00"));
                    cell.setText(String.valueOf(turnSymbol));

                    Log.i(TAG, "turn BEFORE step - " + turnCounter);
                    turnCounter++;
                    changeMarker();
                    Log.i(TAG, "turn AFTER step - " + turnCounter);
                }

                switch (checkGameState(turnSymbol)) {
                    case GameStatusCode.DRAW:
                        Toast.makeText(getApplicationContext(), "DRAW", Toast.LENGTH_SHORT).show();
                        stopGame(GameStatusCode.DRAW);
                        break;
                    case GameStatusCode.X_WIN:
                        Toast.makeText(getApplicationContext(), "X___WIN", Toast.LENGTH_SHORT).show();
                        stopGame(GameStatusCode.X_WIN);
                        break;
                    case GameStatusCode.O_WIN:
                        Toast.makeText(getApplicationContext(), "O___WIN", Toast.LENGTH_SHORT).show();
                        stopGame(GameStatusCode.O_WIN);
                        break;
                    case GameStatusCode.CONTINUE:
                        break;
                }
            }
        };
    }

    /**
     * NAVIGATION BAR
     */

    private void setListenerNavigationButtons() {
        for (int i = 0; i < navigation.getChildCount(); i++) {
            navigation.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_btn_new_game:
                resetGame();
                navigation.setVisibility(View.INVISIBLE);
                break;
            case R.id.game_btn_exit:
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                break;
            case R.id.game_btn_save:
                Player winner = getPlayerWithHighScore();
                if (winner != null) {
                    playerStatsService.appendToFile(winner, PlayerStatsService.FILENAME);
                    playerStatsService.optimizeFile(PlayerStatsService.FILENAME);
                    Log.i(TAG, "save to file");
                    Toast.makeText(getApplicationContext(), winner.getName() + " - saved!!!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * GAME LOGIC
     */

    public void resetGame() {
        turnCounter = 0;
        changeMarker();
        Log.i(TAG, "turnCounter INIT - " + turnCounter);
        gameBoard.resetGameBoard();
        setCellListeners();
    }

    /**
     * Mark user which turn is now
     */
    private void changeMarker() {
        if (turnCounter % 2 == 0) {
            player2Info.setBackground(marker);
            player1Info.setBackground(null);
        } else {
            player1Info.setBackground(marker);
            player2Info.setBackground(null);
        }

    }


    private char nextTurn() {
        if (turnCounter % 2 != 0) return GameSymbol.O;
        return GameSymbol.X;
    }

    private int checkGameState(char turn) {

        // RAW check
        if (checkRows(turn))
            return getWinner(turn);

        // COLUMN check
        if (checkColumns(turn))
            return getWinner(turn);

        // DIAGONALS check
        if (checkDiagonals(turn))
            return getWinner(turn);

        // DRAW
        if (turnCounter == 9)
            return GameStatusCode.DRAW;

        // CONTINUE
        return GameStatusCode.CONTINUE;
    }


    private int getWinner(char turn) {
        if (turn == GameSymbol.X) return GameStatusCode.X_WIN;
        else return GameStatusCode.O_WIN;
    }

    private boolean checkRows(char turn) {
        for (int j = 0; j < 3; j++) {
            int symbolCounter = 0;
            for (int i = 0; i < 3; i++) {
                if (gameBoard.getTable()[j][i] == turn)
                    symbolCounter++;
            }
            if (symbolCounter == 3)
                return true;
        }
        return false;
    }

    private boolean checkColumns(char turn) {
        for (int j = 0; j < 3; j++) {
            int symbolCounter = 0;
            for (int i = 0; i < 3; i++) {
                if (gameBoard.getTable()[i][j] == turn)
                    symbolCounter++;
            }
            if (symbolCounter == 3)
                return true;
        }
        return false;
    }

    private boolean checkDiagonals(char turn) {
        int count1 = 0;
        int count2 = 0;
        for (int i = 0; i < 3; i++) {
            if (gameBoard.getTable()[i][i] == turn)
                count1++;
        }

        for (int i = 0; i < 3; i++) {
            if (gameBoard.getTable()[i][3 - 1 - i] == turn)
                count2++;
        }
        return (count1 == 3 || count2 == 3);
    }

    private void stopGame(int statusCode) {
        unsetCellListeners();
        switch (statusCode) {
            case GameStatusCode.DRAW:
                navigation.setVisibility(View.VISIBLE);
                break;
            case GameStatusCode.O_WIN:
                player1.win();
                updateScore();
                navigation.setVisibility(View.VISIBLE);
                break;
            case GameStatusCode.X_WIN:
                player2.win();
                updateScore();
                navigation.setVisibility(View.VISIBLE);
                break;
        }
    }


    public Player getPlayerWithHighScore() {
        if (player1.getScore() > player2.getScore()) return player1;
        else if (player1.getScore() == player2.getScore()) return null;
        else return player2;
    }
}