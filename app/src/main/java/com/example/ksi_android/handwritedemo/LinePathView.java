package com.example.ksi_android.handwritedemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


/**
 * 绘制Path的View 可用于签名
 * 
 * @author terry
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class LinePathView extends View {

	private Context mContext;

    /**
     * 笔画X坐标起点
     */
    private float mX;
    /**
     *笔画Y坐标起点
     */
    private float mY;
    /**
     * 手写画笔
     */
	private final Paint mGesturePaint = new Paint();
    /**
     * 背景画笔
     */
    private final Path mPath = new Path();
    /**
     *背景画布
     */
    private Canvas cacheCanvas;
    /**
     *背景Bitmap缓存
     */
    private Bitmap cachebBitmap;
    /**
     *是否已经签名
     */
    private boolean isTouched = false;



    /**
     *画笔宽度 px；
     */
    private int mPaintWidth=10;
    /**
     *背景色
     */
    private  int mBackColor= Color.WHITE;
    /**
     *前景色
     */
    private int mPenColor=Color.BLACK;
    /**
     * 用于计算颜色相似度 仅比较颜色的RGB
     */
    private final static int COLORMAX=(int) Math.sqrt(255*255+255*255+255*255);

	public LinePathView(Context context) {
		super(context);
		init(context);
	}

	public LinePathView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init(context);
	}

	public LinePathView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context,attrs,defStyleAttr);
		init(context);
	}

	public void init(Context context) {
		this.mContext = context;
		mGesturePaint.setAntiAlias(true);
		mGesturePaint.setStyle(Style.STROKE);
		mGesturePaint.setStrokeWidth(mPaintWidth);
		mGesturePaint.setColor(mPenColor);
		cachebBitmap = Bitmap.createBitmap(ScreenUtils.getScreenWidth(mContext), ScreenUtils.getScreenHeight(mContext), Config.ARGB_8888);
		cacheCanvas = new Canvas(cachebBitmap);
		cacheCanvas.drawColor(mBackColor);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			isTouched = true;
			touchMove(event);
			break;
		case MotionEvent.ACTION_UP:
			cacheCanvas.drawPath(mPath, mGesturePaint);
			mPath.reset();
			break;
		}
		// 更新绘制
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawBitmap(cachebBitmap, 0, 0, mGesturePaint);
		// 通过画布绘制多点形成的图形
		canvas.drawPath(mPath, mGesturePaint);
	}

	// 手指点下屏幕时调用
	private void touchDown(MotionEvent event) {

		// mPath.rewind();
		// 重置绘制路线，即隐藏之前绘制的轨迹
		mPath.reset();
		float x = event.getX();
		float y = event.getY();

		mX = x;
		mY = y;
		// mPath绘制的绘制起点
		mPath.moveTo(x, y);
	}

	// 手指在屏幕上滑动时调用
	private void touchMove(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();

		final float previousX = mX;
		final float previousY = mY;

		final float dx = Math.abs(x - previousX);
		final float dy = Math.abs(y - previousY);

		// 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
		if (dx >= 3 || dy >= 3) {
			// 设置贝塞尔曲线的操作点为起点和终点的一半
			float cX = (x + previousX) / 2;
			float cY = (y + previousY) / 2;

			// 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
			mPath.quadTo(previousX, previousY, cX, cY);

			// 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
			mX = x;
			mY = y;
		}
	}

	/**
	 * 清除画板
	 */
	public void clear() {
		if (cacheCanvas != null) {
			isTouched = false;
			mGesturePaint.setColor(mPenColor);
			cacheCanvas.drawPaint(mGesturePaint);
			mGesturePaint.setColor(mPenColor);
			cacheCanvas.drawColor(mBackColor);
			invalidate();
		}
	}

	/**
	 * 保存画板
	 * @param path 保存到路劲
	 * @param clearBlank 是否清楚空白区域
     * @param BACKGAUGE 边缘空白区域
	 * @param istran 是否背景翻转为透明
     * @param similarity 背景颜色比对相似度 越高保留的前景色越多。
     */
	public void save(String path,boolean clearBlank,int BACKGAUGE,boolean istran,double similarity )
	{
		setDrawingCacheEnabled(true);
		buildDrawingCache();
		Bitmap bitmap = getDrawingCache();
		//BitmapUtil.createScaledBitmapByHeight(srcBitmap, 300);//  压缩图片 ksicore的工具
		if (clearBlank)
		{
			bitmap= clearBlank(bitmap,BACKGAUGE);
		}
		if(istran)
		{
			bitmap=bitmapTransport(bitmap,similarity);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] buffer = bos.toByteArray();
		if (buffer != null) {
			try {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				OutputStream outputStream = new FileOutputStream(file);
				outputStream.write(buffer);
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				setDrawingCacheEnabled(false);
			}
		}
	}

    /**
     * 比较两个颜色是否相同
     * @param c1  颜色1
     * @param c2  颜色2
     * @param similarity  相似度多少认为一样
     * @return
     */
    private  boolean isSameColor(int c1,int c2,double similarity)
    {
        if (similarity>=1||similarity<0)
        {
            return true;
        }else if (similarity==0)
        {
            return false;
        }else
        {
            int r=Color.red(c1)-Color.red(c2);
            int g=Color.green(c1)-Color.green(c2);
            int b=Color.blue(c1)-Color.blue(c2);
            double _similarity=1-Math.sqrt(r*r+g*g+b*b)/COLORMAX;
            if (similarity>_similarity)
            {
                return false;
            }else
            {
                return true;
            }
        }
    }


	/**
	 * 背景转透明
	 * @param bitmap
	 * @return
     */
	private Bitmap bitmapTransport(Bitmap bitmap,double similarity){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] argb = new int[width*height];
		bitmap.getPixels(argb, 0, width, 0, 0,width, height);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
			{
				int index = y * width + x;
				if(isSameColor(mBackColor,argb[index],similarity)){
					argb[index] = 0x00000000 ;
				}
			}
		return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
	}



	/**
	 * 清楚边界空白。
	 * @param bp
	 * @return
     */
	private Bitmap clearBlank(Bitmap bp, int BACKGAUGE)
	{
		int HEIGHT=bp.getHeight();
		int WIDTH=bp.getWidth();
		int top=0,left=0,right=0,bottom=0;
		int[] pixs=new int[WIDTH];
		boolean isStop;
		for (int y=0;y<HEIGHT;y++)
		{
			bp.getPixels(pixs,0,WIDTH,0,y,WIDTH,1);
			isStop=false;
			for (int pix:pixs)
			{
				if (pix!=mBackColor)
				{
					top=y;
					isStop=true;
					break;
				}
			}
			if (isStop)
			{
				break;
			}
		}
        for (int y=HEIGHT-1;y>=0;y--)
		{
			bp.getPixels(pixs,0,WIDTH,0,y,WIDTH,1);
			isStop=false;
			for (int pix:pixs)
			{
				if (pix!=mBackColor)
				{
					bottom=y;
					isStop=true;
					break;
				}
			}
			if (isStop)
			{
				break;
			}
		}
		pixs=new int[HEIGHT];
		for (int x=0;x<WIDTH;x++)
		{
			bp.getPixels(pixs,0,1,x,0,1,HEIGHT);
			isStop=false;
			for (int pix:pixs)
			{
				if (pix!=mBackColor)
				{
					left=x;
					isStop=true;
					break;
				}
			}
			if (isStop)
			{
				break;
			}
		}
		for (int x=WIDTH-1;x>0;x--)
		{
			bp.getPixels(pixs,0,1,x,0,1,HEIGHT);
			isStop=false;
			for (int pix:pixs)
			{
				if (pix!=mBackColor)
				{
					right=x;
					isStop=true;
					break;
				}
			}
			if (isStop)
			{
				break;
			}
		}
        if (BACKGAUGE<0)
        {
            BACKGAUGE=0;
        }
		left=left-BACKGAUGE>0?left-BACKGAUGE:0;
		top=top-BACKGAUGE>0?top-BACKGAUGE:0;
		right=right+BACKGAUGE>WIDTH-1?WIDTH-1:right+BACKGAUGE;
		bottom=bottom+BACKGAUGE>HEIGHT-1?HEIGHT-1:bottom+BACKGAUGE;
		return Bitmap.createBitmap(bp,left,top,right-left,bottom-top);
	}

    /**
     * 设置画笔宽度 默认宽度为10px
     * @param mPaintWidth
     */
    public void setmPaintWidth(int mPaintWidth) {
        mPaintWidth=mPaintWidth>0?mPaintWidth:10;
        this.mPaintWidth = mPaintWidth;
        mGesturePaint.setStrokeWidth(mPaintWidth);

    }

    /**
     * 设置画布背景色
     * @param mBackColor
     */
    public void setmBackColor(int mBackColor) {
        this.mBackColor = mBackColor;
        cacheCanvas.drawColor(mBackColor);
    }

    /**
     * 设置画笔颜色
     * @param mPenColor
     */
    public void setmPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        mGesturePaint.setColor(mPenColor);
    }

	public boolean getTouched(){
		return isTouched;
	}
}
