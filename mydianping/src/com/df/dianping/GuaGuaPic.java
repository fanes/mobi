package com.df.dianping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class GuaGuaPic extends View
{

	/**
	 * ����������Paint,���û���ָ����Path
	 */
	private Paint mOutterPaint = new Paint();
	/**
	 * ��¼�û����Ƶ�Path
	 */
	private Path mPath = new Path();
	/**
	 * �ڴ��д�����Canvas
	 */
	private Canvas mCanvas;
	/**
	 * mCanvas��������������
	 */
	private Bitmap mBitmap;
	private Bitmap mBackBitmap;

	private int mLastX;
	private int mLastY;

	public GuaGuaPic(Context context)
	{
		this(context, null);
	}

	public GuaGuaPic(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public GuaGuaPic(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		mPath = new Path();

	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
		  
        int width = getMeasuredWidth();  
        int height = getMeasuredHeight();  
        // ��ʼ��bitmap  
        mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);  
        mCanvas = new Canvas(mBitmap);  
        
        //
        mBackBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.back);
        
//        setUpOutPaint();  
     // ���û���
     	mOutterPaint.setColor(Color.RED);
     	mOutterPaint.setAntiAlias(true);
     	mOutterPaint.setDither(true);
     	mOutterPaint.setStyle(Paint.Style.STROKE);
     	mOutterPaint.setStrokeJoin(Paint.Join.ROUND); // Բ��
     	mOutterPaint.setStrokeCap(Paint.Cap.ROUND); // Բ��
     	// ���û��ʿ���
     	mOutterPaint.setStrokeWidth(20);
        
        //������ĳ�  
        mCanvas.drawColor(Color.parseColor("#c0c0c0"));  
	}
	
	

	@Override
//	protected void onDraw(Canvas canvas)
//	{
//		drawPath();
//		canvas.drawBitmap(mBitmap, 0, 0, null);
//
//	}
	
	protected void onDraw(Canvas canvas)  
	{  
	    canvas.drawBitmap(mBackBitmap, 0, 0, null);  
	    drawPath();  
	    canvas.drawBitmap(mBitmap, 0, 0, null);  
	}

	/**
	 * ��������
	 */
//	private void drawPath()
//	{
//		mCanvas.drawPath(mPath, mOutterPaint);
//	}
	
	private void drawPath()  
	{
		mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		mCanvas.drawPath(mPath, mOutterPaint);  
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (action)
		{
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;
			mPath.moveTo(mLastX, mLastY);
			break;
		case MotionEvent.ACTION_MOVE:

			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);

			if (dx > 3 || dy > 3)
				mPath.lineTo(x, y);

			mLastX = x;
			mLastY = y;
			break;
		}

		invalidate();
		return true;
	}

}