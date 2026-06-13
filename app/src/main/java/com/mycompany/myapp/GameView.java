package com.mycompany.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.*;
import android.util.AttributeSet;
import android.view.*;

import java.util.*;

public class GameView extends View {
    // 游戏状态
    public static final int READY = 0;
    public static final int RUNNING = 1;
    public static final int GAME_OVER = 2;

    private int gameState = READY;

    // 网格行列数
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 30;

    // 方向
    public enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;

    // 蛇
    private List<Point> snake = new ArrayList<>();
    private Point food;

    // 定时器
    private Handler handler = new Handler();
    private long moveDelay = 200; // 200ms移动一次
    private Runnable moveRunnable;

    // 界面尺寸
    private int cellSize;
    private int viewWidth, viewHeight;
    private int offsetX, offsetY;

    // 画笔
    private Paint gridPaint, snakePaint, foodPaint, textPaint, backgroundPaint;

    // 分数监听
    private OnGameListener listener;
    private int score = 0;
    private int highScore = 0;

    // 滑动起点
    private float touchStartX, touchStartY;

    public interface OnGameListener {
        void onScoreChanged(int score);
        void onGameOver(int score);
    }

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        gridPaint = new Paint();
        gridPaint.setColor(0xFFCCCCCC);
        gridPaint.setStyle(Paint.Style.STROKE);

        snakePaint = new Paint();
        snakePaint.setColor(0xFF4CAF50);
        snakePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        foodPaint = new Paint();
        foodPaint.setColor(0xFFFF5722);
        foodPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFF5F5F5);

        setFocusable(true);
        setFocusableInTouchMode(true);

        loadHighScore(context);
    }

    public void setOnGameListener(OnGameListener listener) {
        this.listener = listener;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    private void loadHighScore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);
    }

    private void saveHighScore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE);
        prefs.edit().putInt("high_score", highScore).apply();
    }

    public void startGame() {
        if (gameState == RUNNING) return;

        // 初始化蛇（在中间偏左位置，长度3）
        snake.clear();
        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        // 初始方向
        currentDirection = Direction.RIGHT;
        nextDirection = Direction.RIGHT;

        // 放置食物
        placeFood();

        score = 0;
        if (listener != null) listener.onScoreChanged(score);

        gameState = RUNNING;

        // 启动移动循环
        if (moveRunnable == null) {
            moveRunnable = new Runnable() {
                @Override
                public void run() {
                    if (gameState == RUNNING) {
                        moveSnake();
                        invalidate();
                        handler.postDelayed(this, moveDelay);
                    }
                }
            };
        }
        handler.removeCallbacks(moveRunnable);
        handler.postDelayed(moveRunnable, moveDelay);

        invalidate();
    }

    private void moveSnake() {
        // 更新方向
        currentDirection = nextDirection;

        // 头部移动后位置
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);
        switch (currentDirection) {
            case UP:    newHead.y--; break;
            case DOWN:  newHead.y++; break;
            case LEFT:  newHead.x--; break;
            case RIGHT: newHead.x++; break;
        }

        // 碰撞检测：撞墙
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH || newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            gameOver();
            return;
        }

        // 碰撞检测：撞到自己
        for (Point body : snake) {
            if (body.equals(newHead)) {
                gameOver();
                return;
            }
        }

        // 添加新头部
        snake.add(0, newHead);

        // 判断是否吃到食物
        if (food != null && newHead.equals(food)) {
            score++;
            if (score > highScore) {
                highScore = score;
                saveHighScore(getContext());
            }
            if (listener != null) listener.onScoreChanged(score);
            placeFood();
            // 不移除尾部（蛇变长）
        } else {
            // 移除尾部
            snake.remove(snake.size() - 1);
        }
    }

    private void placeFood() {
        Random rand = new Random();
        int x, y;
        boolean onSnake;
        do {
            x = rand.nextInt(GRID_WIDTH);
            y = rand.nextInt(GRID_HEIGHT);
            onSnake = false;
            for (Point p : snake) {
                if (p.x == x && p.y == y) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);
        food = new Point(x, y);
    }

    private void gameOver() {
        gameState = GAME_OVER;
        handler.removeCallbacks(moveRunnable);
        if (listener != null) {
            listener.onGameOver(score);
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        // 计算单元格大小，保持宽高比适配
        cellSize = Math.min(w / GRID_WIDTH, h / GRID_HEIGHT);
        // 居中偏移
        offsetX = (w - cellSize * GRID_WIDTH) / 2;
        offsetY = (h - cellSize * GRID_HEIGHT) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        canvas.drawRect(0, 0, viewWidth, viewHeight, backgroundPaint);

        // 绘制网格
        for (int i = 0; i <= GRID_WIDTH; i++) {
            float x = offsetX + i * cellSize;
            canvas.drawLine(x, offsetY, x, offsetY + GRID_HEIGHT * cellSize, gridPaint);
        }
        for (int j = 0; j <= GRID_HEIGHT; j++) {
            float y = offsetY + j * cellSize;
            canvas.drawLine(offsetX, y, offsetX + GRID_WIDTH * cellSize, y, gridPaint);
        }

        // 绘制食物
        if (food != null) {
            float fx = offsetX + food.x * cellSize + cellSize / 2f;
            float fy = offsetY + food.y * cellSize + cellSize / 2f;
            canvas.drawCircle(fx, fy, cellSize / 2f - 4, foodPaint);
        }

        // 绘制蛇
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            float left = offsetX + p.x * cellSize + 2;
            float top = offsetY + p.y * cellSize + 2;
            float right = offsetX + (p.x + 1) * cellSize - 2;
            float bottom = offsetY + (p.y + 1) * cellSize - 2;
            canvas.drawRect(left, top, right, bottom, snakePaint);
        }

        // 游戏状态文字
        if (gameState == READY) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("点击“开始游戏”按钮", viewWidth / 2f, viewHeight / 2f, textPaint);
        } else if (gameState == GAME_OVER) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("游戏结束！", viewWidth / 2f, viewHeight / 2f, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameState != RUNNING) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0 && currentDirection != Direction.LEFT) {
                        nextDirection = Direction.RIGHT;
                    } else if (dx < 0 && currentDirection != Direction.RIGHT) {
                        nextDirection = Direction.LEFT;
                    }
                } else {
                    if (dy > 0 && currentDirection != Direction.UP) {
                        nextDirection = Direction.DOWN;
                    } else if (dy < 0 && currentDirection != Direction.DOWN) {
                        nextDirection = Direction.UP;
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    // 可选的暂停/重置
    public void pauseGame() {
        if (gameState == RUNNING) {
            handler.removeCallbacks(moveRunnable);
        }
    }

    public void resumeGame() {
        if (gameState == RUNNING) {
            handler.postDelayed(moveRunnable, moveDelay);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(moveRunnable);
    }
}
