package com.mycompany.myapp;

import android.app.*;
import android.content.DialogInterface;
import android.os.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity 
{
    private GameView gameView;
    private Button startButton;
    private TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        gameView = (GameView) findViewById(R.id.gameView);
        startButton = (Button) findViewById(R.id.startButton);
        scoreText = (TextView) findViewById(R.id.scoreText);

        // 初始显示最高分
        scoreText.setText(getString(R.string.score_format, 0, gameView.getHighScore()));

        gameView.setOnGameListener(new GameView.OnGameListener() {
            @Override
            public void onScoreChanged(int score) {
                scoreText.setText(getString(R.string.score_format, score, gameView.getHighScore()));
            }

            @Override
            public void onGameOver(final int score) {
                scoreText.setText(getString(R.string.score_format, score, gameView.getHighScore()));
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("游戏结束")
                    .setMessage(getString(R.string.game_over_message, score))
                    .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGame();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
                startButton.setText(R.string.new_game);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private void startGame() {
        gameView.startGame();
        startButton.setText(R.string.new_game);
        scoreText.setText(getString(R.string.score_format, 0, gameView.getHighScore()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resumeGame();
    }
}
