package com.example.snakegame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SnakeGameView gameView;
    private TextView scoreText;
    private TextView gameOverText;
    private Button restartButton;
    private Button upButton, downButton, leftButton, rightButton;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        scoreText = findViewById(R.id.scoreText);
        gameOverText = findViewById(R.id.gameOverText);
        restartButton = findViewById(R.id.restartButton);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        // 设置方向按钮点击监听
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.setDirection(SnakeGameView.DIRECTION_UP);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.setDirection(SnakeGameView.DIRECTION_DOWN);
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.setDirection(SnakeGameView.DIRECTION_LEFT);
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.setDirection(SnakeGameView.DIRECTION_RIGHT);
            }
        });

        // 重新开始按钮
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.restartGame();
                score = 0;
                updateScore();
                gameOverText.setVisibility(View.GONE);
                restartButton.setVisibility(View.GONE);
            }
        });

        // 设置游戏事件监听
        gameView.setOnGameEventListener(new SnakeGameView.OnGameEventListener() {
            @Override
            public void onScoreChanged(int newScore) {
                score = newScore;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateScore();
                    }
                });
            }

            @Override
            public void onGameOver() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameOverText.setVisibility(View.VISIBLE);
                        restartButton.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "游戏结束！得分: " + score, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateScore() {
        scoreText.setText("得分: " + score);
    }
}
