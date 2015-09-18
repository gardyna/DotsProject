package com.ru.dots.dotsproj;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class BoardView extends View {
    private boolean m_moving;
    private int m_cell_height;
    private int m_cell_width;
    private RectF m_rect = new RectF();
    private Paint m_paint = new Paint();
    private List<Point> m_cellPath = new ArrayList<Point>();
    private Path m_path = new Path();
    private Paint m_paintPath = new Paint();
    private List<Integer> m_colors = new ArrayList<Integer>(); // Halda utan um litina
    private List<ArrayList<Integer>> m_points = new ArrayList<ArrayList<Integer>>(); // two dim array to hold point colors
    ValueAnimator animator = new ValueAnimator();
    private ToneGenerator m_toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 50);
    private List<Integer> m_tones = new ArrayList<Integer>();

    private int NUM_CELL;
    private RectF m_circle = new RectF();
    private Paint m_paint_circle = new Paint();
    private Integer m_score = 0;
    private Integer m_movesLeft = 30;
    private Integer m_finalScore = 0;
    private SoundPlayer m_sound = new SoundPlayer();

    private Vibrator m_vibrator;
    private Boolean m_use_vibrator = false;
    private Boolean m_use_sound = false;

    SharedPreferences sp;

    private ListView m_listView;
    ArrayList<Record> m_data = new ArrayList<Record>();
    RecordAdapter m_highscoreRecords;
    String m_recordName;

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorArray();
        m_paint.setColor(Color.BLACK);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        m_paint.setStrokeWidth(2);
        m_paint.setAntiAlias(true);

        m_paint_circle.setColor(m_colors.get(0));
        m_paint_circle.setStyle(Paint.Style.FILL_AND_STROKE);
        m_paint_circle.setAntiAlias(true);

        m_paintPath.setColor(Color.RED);
        m_paintPath.setStrokeWidth(10.0f);
        m_paintPath.setStyle(Paint.Style.STROKE);
        m_paintPath.setAntiAlias(true);

        //Sækja úr preference hvað grid-ið á að vera stórt
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        NUM_CELL = Integer.parseInt(sp.getString(SettingsActivity.DOTSCOUNT, "6"));
        m_vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        m_use_vibrator = sp.getBoolean("vibrate", false);
        m_use_sound = sp.getBoolean("sound", false);

        //sitthvor skráin miðað við mismunadi punktafjölda
        if (NUM_CELL == 6)
        {
            m_recordName = "records6.ser";
        } else {
            m_recordName = "records9.ser";
        }

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_hiscore, (ViewGroup)getParent(),false);
        readRecords();
        m_listView = (ListView) rowView.findViewById(R.id.records);
        m_highscoreRecords = new RecordAdapter(this.getContext(), m_data);
        m_listView.setAdapter(m_highscoreRecords);

        // create points
        //initialize colors
        Random r = new Random();
        for (int i = 0; i < NUM_CELL; ++i){
            System.out.println(i);
            m_points.add(new ArrayList<Integer>());
            for (int j = 0; j < NUM_CELL; ++j){
                System.out.println(j);
                m_points.get(i).add(j, m_colors.get(r.nextInt(m_colors.size())));
            }
        }
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
        m_cell_width = boardWidth / NUM_CELL;
        m_cell_height = boardHeight / NUM_CELL;

        m_circle.set(0, 0, m_cell_width, m_cell_height);
        m_circle.offset(getPaddingLeft(), getPaddingTop());
        m_circle.inset(m_cell_width * 0.1f, m_cell_height * 0.1f);
    }

    @Override
    protected void onDraw(Canvas canvas ) {
        for (int row = 0; row < NUM_CELL; ++row){
            for (int col = 0; col < NUM_CELL; ++col){
                int x = col * m_cell_width;
                int y =  row * m_cell_height;
                m_rect.set(x, y, x + m_cell_width, y + m_cell_height);
                m_rect.offset(getPaddingLeft(), getPaddingTop());
                m_rect.inset(m_cell_width * 0.1f, m_cell_height * 0.1f);
                m_paint.setColor(m_points.get(col).get(row));
                canvas.drawOval(m_rect, m_paint);
            }
        }

        if (!m_cellPath.isEmpty()){
            m_path.reset();
            Point point = m_cellPath.get(0);
            m_path.moveTo(colToX(point.x) + m_cell_width/2, rowToY(point.y) + m_cell_height/2);
            m_rect.set(colToX(point.x),rowToY(point.y),
                    colToX(point.x)+ m_cell_width, rowToY(point.y)+ m_cell_height);
            m_rect.inset(m_cell_width * 0.01f, m_cell_height * 0.01f);
            m_paint.setColor(m_points.get(point.x).get(point.y));
            m_paint.setAlpha(50);
            canvas.drawOval(m_rect, m_paint);
            for (int i = 1; i < m_cellPath.size(); ++i){
                point = m_cellPath.get(i);
                m_path.lineTo(colToX(point.x) + m_cell_width/2, rowToY(point.y) + m_cell_height /2);
                m_rect.set(colToX(point.x),rowToY(point.y),
                            colToX(point.x)+ m_cell_width, rowToY(point.y)+ m_cell_height);
                m_rect.inset(m_cell_width * 0.01f, m_cell_height * 0.01f);
                m_paint.setColor(m_points.get(point.x).get(point.y));
                m_paint.setAlpha(50);
                canvas.drawOval(m_rect, m_paint);
            }
            canvas.drawPath(m_path, m_paintPath);
        }
        View p = (View)getParent();
        TextView score = (TextView)p.findViewById(R.id.currScore);
        TextView movesLeft = (TextView)p.findViewById(R.id.movesLeft);
        movesLeft.setText("Moves: " + m_movesLeft.toString());
        score.setText("Score: " + m_score.toString());
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
            m_moving = true;
            m_cellPath.add(new Point(xToCol(x), yToRow(y)));
            m_paintPath.setColor(m_points.get(xToCol(x)).get(yToRow(y)));
            if(m_use_sound){ m_sound.playTone(100); }
        }else if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (m_moving){
                if (!m_cellPath.isEmpty()){
                    int col = xToCol(x);
                    int row = yToRow(y);
                    Point last = m_cellPath.get(m_cellPath.size() - 1);
                    Point nextToLast = new Point();
                    if (m_cellPath.size() > 1)
                    {
                        nextToLast = m_cellPath.get(m_cellPath.size() -2);
                    }
                    int dx = Math.abs(col - last.x);
                    int dy = Math.abs(row - last.y);
                    System.out.print(col);
                    if ((nextToLast != null) && (col == nextToLast.x) && (row == nextToLast.y)) {
                        m_cellPath.remove(m_cellPath.size() - 1);
                        if(m_use_sound){ m_sound.playTone(100 * m_cellPath.size()); }
                    } else if ((col != last.x || row != last.y)
                            && m_points.get(last.x).get(last.y).compareTo(m_points.get(col).get(row)) == 0
                            && (Math.abs(col - last.x) == 1 || Math.abs(row - last.y) == 1)
                            && dx <= 1 && dy <= 1 && dx != dy
                            && (m_cellPath.get(m_cellPath.size()-1).x != col
                            || m_cellPath.get(m_cellPath.size()-1).y != row)) {
                        m_cellPath.add(new Point(col, row));
                        if(m_use_sound){ m_sound.playTone(100 * m_cellPath.size()); }
                    }
                }
                m_circle.offsetTo(x, y);
                invalidate();
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (m_cellPath.size()<=1) {
                m_cellPath.clear();
            } else {
                m_movesLeft--;
                m_moving = false;
                snapToGrid(m_circle);
                // count scores here
                Point curr = new Point();
                Random r = new Random();
                for (int col = NUM_CELL-1; col >= 0; --col){
                    int removed = 0;
                    for (int row = NUM_CELL-1; row >= 0; --row){
                        curr.x = col;
                        curr.y = row;
                        if (m_cellPath.contains(curr)){
                            m_points.get(col).remove(row);
                            removed++;
                            m_score++;
                        }
                    }
                    for (int i = 0; i<removed; ++i){
                        m_points.get(col).add(0, m_colors.get(r.nextInt(m_colors.size() - 1)));
                    }
                }
                m_cellPath.clear();
                if (m_vibrator != null) {
                    try {
                        if (m_use_vibrator)
                            m_vibrator.vibrate(200);
                    } catch (Exception e) {

                    }
                }
                invalidate();
                if (m_movesLeft == 0) {
                    Date dags = new Date();
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

                    m_data.add(new Record(df.format(dags), m_score, "#1"));
                    m_highscoreRecords.notifyDataSetChanged();
                    writeRecords();

                    m_finalScore = m_score;

                    //í staðinn fyrir "this"
                    Activity activityThis = (Activity) getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    TextView myMsg = new TextView(activityThis);
                    myMsg.setText("Game over \nScore: " + String.valueOf(m_finalScore));
                    myMsg.setGravity(Gravity.CENTER);
                    myMsg.setTextColor(Color.parseColor("#0277bd"));
                    myMsg.setTextSize(50);

                    builder.setView(myMsg);
                    builder.setPositiveButton("Play", new Dialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent i = new Intent(getContext(), GameActivity.class);
                            getContext().startActivity(i);
                        }
                    });
                    builder.setNegativeButton("Home", new Dialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent i = new Intent(getContext(), HomeActivity.class);
                            getContext().startActivity(i);
                        }

                    });
                    AlertDialog a = builder.create();
                    a.show();
                    Button bn = a.getButton(DialogInterface.BUTTON_NEGATIVE);
                    bn.setTextColor(Color.parseColor("#0277bd"));
                    Button bp = a.getButton(DialogInterface.BUTTON_POSITIVE);
                    bp.setTextColor(Color.parseColor("#0277bd"));
                }
            }
        }

        return true;
    }

    // Er með array sem heldur utan um litina
    private void setColorArray(){
        m_colors.add(Color.parseColor("#ffeb3b")); //YELLOW
        m_colors.add(Color.parseColor("#f44336")); //RED
        m_colors.add(Color.parseColor("#8bc34a")); //GREEN
        m_colors.add(Color.parseColor("#304ffe")); //BLUE
        m_colors.add(Color.parseColor("#5e35b1")); //Purple
    }

    void writeRecords( ) {
        try {
            //FileOutputStream fos = new FileOutputStream( "records.ser" );
            FileOutputStream fos = getContext().openFileOutput(m_recordName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject( m_data );
            oos.close();
            fos.close();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    void readRecords() {
        try {

            FileInputStream fis = getContext().openFileInput(m_recordName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Record> records = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
            m_data.clear();
            for ( Record rec: records ) {
                m_data.add( rec );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
