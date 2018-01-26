package com.example.guo.touchloadingview_master;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by guo on 2018/1/26.
 */

public class TouchLoadingView extends View {
    private int mPadding;
    private int mMaxWidth;
    private int mProgressWidth;
    private int mSpacing;
    private int mPrimaryColor;
    private int mProgressColor;
    private RectF mRectFProgress;
    private Paint mPaint;
    private int mRadius;
    private int mInnerRadius;
    private float mCenterX;
    private float mCenterY;
    private boolean pressing;
    private int DEFAUT_RADIUS;
    private ObjectAnimator upAnimator;
    private ObjectAnimator pressAnimator;

    private long DEFAUT_PRESS_TIME = 2000L;//默认按下的完成时间
    private int DEFAUT_ANGLE = -90;
    private int mCurrentAngle = 0;
    private ObjectAnimator progressAnimator;
    private LinearInterpolator linearInterpolator;

    private String mPressStr = "长按";
    private String mDefaultStr = "结束";

    private boolean isOnceCompleted = false;
    private int mTextSize;
    private int mTextColor;
    private Rect mRectText;
    private Paint mPaintText;
    private float mCenterTextY;
    private OnPressCompletedListener mOnPressCompletedListener;

    public TouchLoadingView(Context context) {
        this(context, null);
    }

    public TouchLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    /**
     * 初始化
     */
    private void init() {
        //初始化最大width
        mMaxWidth = dp2px(100);
        //设定进度条的宽度大小
        mProgressWidth = dp2px(10);
        //缝隙的宽度
        mSpacing = dp2px(2);
        //主颜色
        mPrimaryColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        //进度条颜色
        mProgressColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        //字体颜色
        mTextColor = Color.WHITE;
        //字体大小
        mTextSize = sp2px(20);
        //定义进度条的rect
        mRectFProgress = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        //文字的rect
        mRectText = new Rect();

        linearInterpolator = new LinearInterpolator();

        mPaintText = new Paint();
        mPaintText.setColor(mTextColor);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(mTextSize);
        mPaintText.setStyle(Paint.Style.STROKE);
        mPaintText.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingRight()),
                Math.max(getPaddingTop(), getPaddingBottom())
        );
        setPadding(mPadding, mPadding, mPadding, mPadding);
        int width = resolveSize(mMaxWidth, widthMeasureSpec);


        mInnerRadius = mRadius - mProgressWidth - mSpacing;
        setMeasuredDimension(width, width);

        //圆的半径
        mRadius = (width - mPadding * 2) / 2;
        DEFAUT_RADIUS = mRadius;

        //中心点
        mCenterX = mCenterY = getMeasuredWidth() / 2f;
        mRectFProgress.set(
                getPaddingLeft() + mProgressWidth / 2,
                getPaddingTop() + mProgressWidth / 2,
                getMeasuredWidth() - getPaddingRight() - mProgressWidth / 2,
                getMeasuredWidth() - getPaddingBottom() - mProgressWidth / 2
        );

        //文字中心点
//        mPaintText.getTextBounds(mDefaultStr, 0, mDefaultStr.length(), mRectText);
//        mCenterTextY = mCenterY + mRectText.height() / 2;
        //修改文字的中心点
        Paint.FontMetrics fontMetrics = mPaintText.getFontMetrics();
        mCenterTextY = mCenterY - (fontMetrics.bottom + fontMetrics.top)/2 ;


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //防止不被调用
        setWillNotDraw(false);

        mPaint.setColor(mPrimaryColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);

        if (pressing && !isOnceCompleted) {//如果正在按着,,首先显示一个圆环,用来表示进度条
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mProgressWidth);
            canvas.drawCircle(mCenterX, mCenterY, DEFAUT_RADIUS - mProgressWidth / 2, mPaint);
        }
        //画进度条
        if (pressing && !isOnceCompleted) {
            mPaint.setColor(mPrimaryColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mProgressWidth);
            canvas.drawArc(mRectFProgress, DEFAUT_ANGLE, mCurrentAngle, false, mPaint);
        }

        //文字
        if (pressing && !isOnceCompleted) {
            //长按
            canvas.drawText(mPressStr, mCenterX, mCenterTextY, mPaintText);
        } else {
            canvas.drawText(mDefaultStr, mCenterX, mCenterTextY, mPaintText);
        }



    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isOnceCompleted = false;
                pressing = true;
                post(pressRunnable);
                return true;
//            case MotionEvent.ACTION_MOVE:
//                pressing = true;
//                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressing = false;
                post(upRunnable);
                return true;
        }


        return super.onTouchEvent(event);

    }

    /**
     * 按下的runnable
     */
    private Runnable pressRunnable = new Runnable() {
        @Override
        public void run() {
            if (pressing) {
                pressView();

            }
        }
    };




    /**
     * 抬起的runnable
     */
    private Runnable upRunnable = new Runnable() {
        @Override
        public void run() {
            if (progressAnimator != null) {
                if (progressAnimator.isRunning()) {
                    progressAnimator.cancel();
                }
                mCurrentAngle = 0;
                postInvalidate();
            }
            if (!pressing && !isOnceCompleted) {
                restoreView();
            }
        }
    };


    /**
     * 按下动画
     */
    private void pressView() {
        if (pressAnimator == null) {
            pressAnimator = ObjectAnimator.ofInt(TouchLoadingView.this, "mRadius", mRadius, mInnerRadius);
            pressAnimator.setDuration(100).setInterpolator(linearInterpolator);
            pressAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //开始执行进度条的动画
                    progressView();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        pressAnimator.start();
    }

    /**
     * 还原动画
     */
    private void restoreView() {
        if (upAnimator == null) {
            upAnimator = ObjectAnimator.ofInt(TouchLoadingView.this, "mRadius", getMRadius(), DEFAUT_RADIUS);
            upAnimator.setDuration(100).setInterpolator(linearInterpolator);

        }
        upAnimator.start();
    }


    /**
     * 进度条动画
     */
    private void progressView() {
        if (progressAnimator == null) {
            progressAnimator = ObjectAnimator.ofInt(TouchLoadingView.this, "mCurrentAngle", 0, 360);
            progressAnimator.setDuration(DEFAUT_PRESS_TIME).setInterpolator(linearInterpolator);
            progressAnimator.addListener(progressListener);
        }
        progressAnimator.start();
    }

    Animator.AnimatorListener progressListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isOnceCompleted = true;
            restoreView();
            if (mOnPressCompletedListener != null && mCurrentAngle == 360) {
                mOnPressCompletedListener.onPressCompleted();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


    public void setMRadius(int mRadius) {
        this.mRadius = mRadius;
        postInvalidate();
    }


    public int getMCurrentAngle() {
        return mCurrentAngle;
    }

    public void setMCurrentAngle(int mCurrentAngle) {
        this.mCurrentAngle = mCurrentAngle;
        postInvalidate();
    }

    public int getMRadius() {
        return mRadius;
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }


    /**
     * 回调
     */
    public interface OnPressCompletedListener {
        void onPressCompleted();

    }


    public void setOnPressCompletedListener(OnPressCompletedListener onPressCompletedListener) {
        mOnPressCompletedListener = onPressCompletedListener;
    }
}
