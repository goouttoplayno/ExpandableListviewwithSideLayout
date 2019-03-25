package com.example.expandablelistviewwithsidelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SlideLayout extends FrameLayout {

    private static final String TAG = "SlideLayout";
    private View contentView;
    private View menuView;
    private int viewHeight;
    private int contentWidth;
    private int menuWidth;

    private Scroller scroller;

    public SlideLayout(Context context, AttributeSet attrs){
        super(context, attrs);
        scroller = new Scroller(context);
    }

    //布局文件加载完成时调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate");
        contentView = findViewById(R.id.content);
        menuView = findViewById(R.id.menu);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure");
        viewHeight = menuView.getMeasuredHeight();
        contentWidth = contentView.getMeasuredWidth();
        menuWidth = menuView.getMeasuredWidth();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG, "onLayout");
        menuView.layout(contentWidth, 0, contentWidth + menuWidth, viewHeight);
    }

    private float startX;
    private float startY;

    private float downX;
    private float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float endX = event.getX();
                float endY = event.getY();
                //计算偏移量
                float distanceX = endX - startX;
                int toScrollX = (int)(getScrollX() - distanceX);
                if(toScrollX < 0){
                    Log.i(TAG, ""+ toScrollX);
                    toScrollX = 0;
                }
                if(toScrollX > menuWidth){
                    Log.i(TAG, ""+ toScrollX);
                    toScrollX = menuWidth;
                }
                System.out.println("toScroll-->"+toScrollX+"-->"+getScrollX());
                scrollTo(toScrollX, getScrollY());

                startX = event.getX();

                float dx = Math.abs(event.getX() - downX);
                float dy = Math.abs(event.getY() - downY);
                if (dx > dy && dx > 0){
                    //事件反拦截，使父ListView的事件传递到自身SlideLayout
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "action_up");
                if(getScrollX() > menuWidth / 2){
                    openMenu();
                }else {
                    closeMenu();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = startX = ev.getX();
                downY = startY = ev.getY();
                if (onStateChangeListener != null){
                    onStateChangeListener.onMove(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX()-downX);
                float dy = Math.abs(ev.getY()-downY);
                if (dx > dy && dx > 0){
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "intercepttouchevent actionup");
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void openMenu(){

        int dx = menuWidth - getScrollX();
        scroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY());
        invalidate();
        if (onStateChangeListener != null){
            onStateChangeListener.onOpen(this);
        }
    }

    public void closeMenu(){
        int dx = 0 - getScrollX();
        scroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY());
        invalidate();
        if (onStateChangeListener != null){
            onStateChangeListener.onClose(this);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    public interface OnStateChangeListener{
        void onOpen(SlideLayout slideLayout);
        void onMove(SlideLayout slideLayout);
        void onClose(SlideLayout slideLayout);
    }
    public OnStateChangeListener onStateChangeListener;
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener){
        this.onStateChangeListener = onStateChangeListener;
    }

}
