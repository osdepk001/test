package com.example.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGameView extends View implements Runnable {
    // 方向常量
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_DOWN = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;

    // 游戏状态
    public enum GameState {
        READY, RUNNING, PAUSED, GAME_OVER
    }

    private GameState gameState = GameState.READY;

    // 网格参数
    private static final int GRID_COLS = 20;
    private static final int GRID_ROWS = 30;
    private static final long TICK_INTERVAL = 200; // 毫秒

    // 颜色
    private static final int COLOR_BG = Color.rgb(0, 50, 0);
    private static final int COLOR_GRID = Color.rgb(0, 80, 0);
    private static final int COLOR_SNAKE_HEAD = Color.rgb(0, 200, 0);
    private static final int COLOR_SNAKE_BODY = Color.rgb(34, 139, 34);
    private static final int COLOR_FOOD = Color.RED;
    private static final int COLOR_WALL = Color.rgb(139, 90, 43);

    // 游戏数据
    private LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private int currentDirection = DIRECTION_RIGHT;
    private int nextDirection = DIRECTION_RIGHT;
    private int score = 0;
    private Random random = new Random();
    private Thread gameThread;
    private boolean isRunning = false;

    // 绘制相关
    private Paint paint;
    private float cellWidth;
    private float cellHeight;
    private int viewWidth;
    private int viewHeight;

    // 事件监听
    private OnGameEventListener listener;

    public interface OnGameEventListener {
        void onScoreChanged(int newScore);
        void onGameOver();
    }

    public void setOnGameEventListener(OnGameEventListener listener) {
        this.listener = listener;
    }

    // 构造函数
    public SnakeGameView(Context context) {
        super(context);
        init();
    }

    public SnakeGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnakeGameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        cellWidth = (float) w / GRID_COLS;
        cellHeight = (float) h / GRID_ROWS;
        initGame();
    }

    private void initGame() {
        snake.clear();
        // 初始化蛇：长度3，水平放置
        int startX = GRID_COLS / 2;
        int startY = GRID_ROWS / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        currentDirection = DIRECTION_RIGHT;
        nextDirection = DIRECTION_RIGHT;
        score = 0;
        spawnFood();
        gameState = GameState.READY;
        if (listener != null) {
            listener.onScoreChanged(score);
        }
    }

    private void spawnFood() {
        Point newFood;
        do {
            int x = random.nextInt(GRID_COLS);
            int y = random.nextInt(GRID_ROWS);
            newFood = new Point(x, y);
        } while (snake.contains(newFood));
        food = newFood;
    }

    public void setDirection(int direction) {
        if (!isValidDirectionChange(direction)) {
            return;
        }
        nextDirection = direction;
    }

    private boolean isValidDirectionChange(int newDir) {
        // 不允许180度转向
        if ((currentDirection == DIRECTION_UP && newDir == DIRECTION_DOWN) ||
                (currentDirection == DIRECTION_DOWN && newDir == DIRECTION_UP) ||
                (currentDirection == DIRECTION_LEFT && newDir == DIRECTION_RIGHT) ||
                (currentDirection == DIRECTION_RIGHT && newDir == DIRECTION_LEFT)) {
            return false;
        }
        return true;
    }

    public void startGame() {
        if (gameState == GameState.READY || gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
            if (!isRunning) {
                isRunning = true;
                gameThread = new Thread(this);
                gameThread.start();
            }
        }
    }

    public void pauseGame() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
        }
    }

    public void restartGame() {
        initGame();
        gameState = GameState.RUNNING;
        if (!isRunning) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (gameState == GameState.RUNNING) {
                updateGame();
                postInvalidate(); // 刷新UI
                try {
                    Thread.sleep(TICK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // 如果游戏未运行，暂停线程
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateGame() {
        // 应用新方向
        currentDirection = nextDirection;

        // 计算新蛇头位置
        Point head = snake.getFirst();
        Point newHead = new Point(head.x, head.y);
        switch (currentDirection) {
            case DIRECTION_UP:
                newHead.y--;
                break;
            case DIRECTION_DOWN:
                newHead.y++;
                break;
            case DIRECTION_LEFT:
                newHead.x--;
                break;
            case DIRECTION_RIGHT:
                newHead.x++;
                break;
        }

        // 检测是否吃到食物
        boolean ateFood = newHead.equals(food);

        // 移动蛇
        snake.addFirst(newHead);
        if (!ateFood) {
            snake.removeLast();
        }

        // 碰撞检测
        // 墙壁碰撞
        if (newHead.x < 0 || newHead.x >= GRID_COLS || newHead.y < 0 || newHead.y >= GRID_ROWS) {
            gameOver();
            return;
        }

        // 自身碰撞（除了蛇尾可能被移除，所以需要检查新蛇头是否与蛇身（去掉蛇尾后）重合）
        if (!ateFood) {
            // 没吃到食物，蛇尾已被移除，检查新蛇头是否在蛇身中
            if (snake.subList(1, snake.size()).contains(newHead)) {
                gameOver();
                return;
            }
        } else {
            // 吃到了食物，蛇尾未被移除，检查新蛇头是否在蛇身中（除新蛇头自己）
            if (snake.subList(1, snake.size()).contains(newHead)) {
                gameOver();
                return;
            }
        }

        // 处理食物
        if (ateFood) {
            score++;
            if (listener != null) {
                listener.onScoreChanged(score);
            }
            spawnFood();
        }
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        if (listener != null) {
            listener.onGameOver();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算单元格大小（重新计算以保持准确）
        cellWidth = (float) viewWidth / GRID_COLS;
        cellHeight = (float) viewHeight / GRID_ROWS;

        // 绘制背景
        paint.setColor(COLOR_BG);
        canvas.drawRect(0, 0, viewWidth, viewHeight, paint);

        // 绘制网格线
        paint.setColor(COLOR_GRID);
        paint.setStrokeWidth(1);
        for (int i = 0; i <= GRID_COLS; i++) {
            float x = i * cellWidth;
            canvas.drawLine(x, 0, x, viewHeight, paint);
        }
        for (int i = 0; i <= GRID_ROWS; i++) {
            float y = i * cellHeight;
            canvas.drawLine(0, y, viewWidth, y, paint);
        }

        // 绘制蛇
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            float left = p.x * cellWidth + 2;
            float top = p.y * cellHeight + 2;
            float right = (p.x + 1) * cellWidth - 2;
            float bottom = (p.y + 1) * cellHeight - 2;

            if (i == 0) {
                // 蛇头
                paint.setColor(COLOR_SNAKE_HEAD);
                canvas.drawRoundRect(new RectF(left, top, right, bottom), 8, 8, paint);

                // 画蛇眼
                paint.setColor(Color.WHITE);
                float eyeRadius = Math.min(cellWidth, cellHeight) * 0.1f;
                float centerX = left + (right - left) / 2;
                float centerY = top + (bottom - top) / 2;
                float offsetX = (right - left) * 0.2f;
                float offsetY = (bottom - top) * 0.2f;

                switch (currentDirection) {
                    case DIRECTION_UP:
                        canvas.drawCircle(centerX - offsetX, centerY - offsetY, eyeRadius, paint);
                        canvas.drawCircle(centerX + offsetX, centerY - offsetY, eyeRadius, paint);
                        break;
                    case DIRECTION_DOWN:
                        canvas.drawCircle(centerX - offsetX, centerY + offsetY, eyeRadius, paint);
                        canvas.drawCircle(centerX + offsetX, centerY + offsetY, eyeRadius, paint);
                        break;
                    case DIRECTION_LEFT:
                        canvas.drawCircle(centerX - offsetX, centerY - offsetY, eyeRadius, paint);
                        canvas.drawCircle(centerX - offsetX, centerY + offsetY, eyeRadius, paint);
                        break;
                    case DIRECTION_RIGHT:
                        canvas.drawCircle(centerX + offsetX, centerY - offsetY, eyeRadius, paint);
                        canvas.drawCircle(centerX + offsetX, centerY + offsetY, eyeRadius, paint);
                        break;
                }
            } else {
                // 蛇身
                paint.setColor(COLOR_SNAKE_BODY);
                canvas.drawRoundRect(new RectF(left, top, right, bottom), 5, 5, paint);
            }
        }

        // 绘制食物
        if (food != null) {
            float left = food.x * cellWidth + 4;
            float top = food.y * cellHeight + 4;
            float right = (food.x + 1) * cellWidth - 4;
            float bottom = (food.y + 1) * cellHeight - 4;
            paint.setColor(COLOR_FOOD);
            canvas.drawOval(new RectF(left, top, right, bottom), paint);

            // 食物高光
            paint.setColor(Color.argb(100, 255, 255, 255));
            float highLightRadius = Math.min(cellWidth, cellHeight) * 0.15f;
            canvas.drawCircle(left + (right - left) * 0.3f, top + (bottom - top) * 0.3f, highLightRadius, paint);
        }

        // 显示游戏状态
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);

        if (gameState == GameState.READY) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(40);
            canvas.drawText("点击屏幕开始游戏", viewWidth / 2, viewHeight / 2, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameState == GameState.READY) {
                startGame();
                return true;
            } else if (gameState == GameState.PAUSED) {
                startGame();
                return true;
            } else if (gameState == GameState.GAME_OVER) {
                // 游戏结束时点击无效，使用按钮重新开始
                return true;
            }
        }
        // 支持滑动控制（可选）
        return super.onTouchEvent(event);
    }

    // 内部类：点
    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }

    // 清理线程
    public void cleanup() {
        isRunning = false;
        if (gameThread != null) {
            gameThread.interrupt();
            gameThread = null;
        }
    }
}
