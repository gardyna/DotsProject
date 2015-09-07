package com.ru.dots.dotsproj;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.List;

public class BoardView extends View {
    private boolean m_moving;
    private int m_cell_height;
    private int m_cell_width;
    private Rect m_rect = new Rect();
    private Paint m_paint = new Paint();
    private List<Point> m_cellPath = new ArrayList<Point>();
    private Path m_path = new Path();
    private Paint m_paintPath = new Paint();

    ValueAnimator animator = new ValueAnimator();

    private final int NUM_CELL = 6;
    private RectF m_circle = new RectF();
    private Paint m_paint_circle = new Paint();

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_paint.setColor(Color.BLACK);
        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setStrokeWidth(2);
        m_paint.setAntiAlias(true);

        m_paint_circle.setColor(Color.RED);
        m_paint_circle.setStyle(Paint.Style.FILL_AND_STROKE);
        m_paint_circle.setAntiAlias(true);

        m_paintPath.setColor(Color.RED);
        m_paintPath.setStrokeWidth(10.0f);
        m_paintPath.setStyle(Paint.Style.STROKE);
        m_paintPath.setAntiAlias(true);
    }



    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width  = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int size = Math.min(width, height);
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged( int xNew, int yNew, int xOld, int yOld ) {
        int   boardWidth = (xNew - getPaddingLeft() - getPaddingRight());
        int   boardHeight = (yNew - getPaddingTop() - getPaddingBottom());
        //m_rect.set(0, 0, boardWidth, boardHeight );
        //m_rect.offset( getPaddingLeft(), getPaddingTop());
        m_cell_width = boardWidth / NUM_CELL;
        m_cell_height = boardHeight / NUM_CELL;

        m_circle.set(0, 0, m_cell_width, m_cell_height);
        m_circle.offset(getPaddingLeft(), getPaddingTop());
        m_circle.inset(m_cell_width * 0.1f, m_cell_height * 0.1f);
    }

    @Override
    protected void onDraw(Canvas canvas ) {
        canvas.drawRect(m_rect, m_paint);
        for (int row = 0; row < NUM_CELL; ++row){
            for (int col = 0; col < NUM_CELL; ++col){
                int x = col * m_cell_width;
                int y =  row * m_cell_height;
                m_rect.set(x, y, x + m_cell_width, y + m_cell_height);
                m_rect.offset(getPaddingLeft(), getPaddingTop());
                canvas.drawRect(m_rect, m_paint);
                canvas.drawOval(m_circle, m_paint_circle);
            }
        }

        if (!m_cellPath.isEmpty()){
            m_path.reset();
            Point point = m_cellPath.get(0);
            m_path.moveTo(colToX(point.x) + m_cell_width/2, rowToY(point.y) + m_cell_height/2);
            for (int i = 1; i < m_cellPath.size(); ++i){
                point = m_cellPath.get(i);
                m_path.lineTo(colToX(point.x) + m_cell_width/2, rowToY(point.y) + m_cell_height /2);
            }
            canvas.drawPath(m_path, m_paintPath);
        }


        canvas.drawOval(m_circle, m_paint_circle);
    }

    private int xToCol(int x){
        return (x - getPaddingLeft()) / m_cell_width;
    }

    private int yToRow(int y){
        return (y - getPaddingTop()) / m_cell_height;
    }

    private int colToX(int x){
        return x * m_cell_width + getPaddingLeft();
    }

    private int rowToY(int y){
        return y * m_cell_height + getPaddingTop();
    }

    void snapToGrid( RectF circle){
        int col = xToCol((int)circle.left);
        int row = yToRow((int) circle.top);
        int x = colToX(col) + (int)(m_cell_width - circle.width()) / 2;
        int y = rowToY(row) + (int)(m_cell_height - circle.height()) / 2;
        circle.offsetTo(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();

        if (x < getPaddingLeft() || y < getPaddingTop()){
            return true;
        }

        int xMax = getPaddingLeft() + m_cell_width * NUM_CELL;
        int yMax = getPaddingTop() + m_cell_height * NUM_CELL;
        x = Math.max(getPaddingLeft(), Math.min(x, xMax - (int) m_circle.width()));
        y = Math.max(getPaddingTop(), Math.min(y, yMax - (int) m_circle.height()));

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (m_circle.contains(x, y)){
                m_moving = true;
                m_cellPath.add(new Point(xToCol(x), yToRow(y)));
            }else {
                animateMovement(m_circle.left, m_circle.top,
                        x - m_circle.width() / 2, y - m_circle.height() / 2);
                invalidate();
            }
        }else if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (m_moving){
                if (!m_cellPath.isEmpty()){
                    int col = xToCol(x);
                    int row = yToRow(y);
                    Point last = m_cellPath.get(m_cellPath.size()-1);
                    if (col != last.x || row != last.y){
                        m_cellPath.add(new Point(col, row));
                    }
                }
                m_circle.offsetTo(x, y);
                invalidate();
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP) {
            m_moving = false;
            snapToGrid(m_circle);
            m_cellPath.clear();
            invalidate();
        }

        return true;
    }

    private void animateMovement(final float xs, final float ys, final float xt, final float yt){
        animator.removeAllUpdateListeners();
        animator.setDuration(2000);
        animator.setFloatValues(0.0f, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void  onAnimationUpdate(ValueAnimator animation){
                float ratio = (float) animation.getAnimatedValue();
                float x = (float)( (1.0 - ratio) * xs + ratio * xt);
                float y = (float)((1.0 - ratio) * ys + ratio * yt);
                m_circle.offsetTo(x, y);
                invalidate();
            }
        });

        animator.start();
    }
}
