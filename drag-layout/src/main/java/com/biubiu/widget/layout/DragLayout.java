package com.biubiu.widget.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 一个可以拖拽的layout，RelativeLayout。
 * 可以设置拖拽范围{@link DragLayout#setDragLimited(int)}；
 * 也可以设置是否允许拖拽{@link DragLayout#setDragEnable(boolean)}。
 */
public class DragLayout extends RelativeLayout {

    public interface OnDragListener {
        void onDragStart(View view);

        void onDragEnd(View view);
    }

    /**
     * DragLayout 的拖拽范围。
     * INSIDE_PARENT = 0x000001;
     * HORIZONTAL = 0x000010;
     * VERTICAL = 0x000100;
     * WITHOUT_LIMITED = 0x100000;
     * <p>
     * INSIDE_PARENT|HORIZONTAL|VERTICAL = 0x000111;//在父布局内，水平、垂直方向均可移动，等同于 INSIDE_PARENT
     * INSIDE_PARENT|HORIZONTAL = 0x000011;//在父布局内，且只能水平方向移动
     * INSIDE_PARENT|VERTICAL = 0x000101;//在父布局内，且只能垂直方向移动
     * INSIDE_PARENT = 0x000001;//在父布局内，水平、垂直方向均可移动
     * <p>
     * WITHOUT_LIMITED|HORIZONTAL|VERTICAL = 0x100110;//水平、垂直方向均可移动，等同于 WITHOUT_LIMITED
     * WITHOUT_LIMITED|HORIZONTAL = 0x100010;//只能水平方向移动，等同于 HORIZONTAL
     * WITHOUT_LIMITED|VERTICAL = 0x100100;//只能垂直方向移动，等同于 VERTICAL
     * WITHOUT_LIMITED = 0x100000;//无限制
     */
    @IntDef({DragLimited.INSIDE_PARENT,
            DragLimited.WITHOUT_LIMITED,
            DragLimited.HORIZONTAL,
            DragLimited.VERTICAL,
            (DragLimited.INSIDE_PARENT | DragLimited.HORIZONTAL | DragLimited.VERTICAL),
            (DragLimited.INSIDE_PARENT | DragLimited.HORIZONTAL),
            (DragLimited.INSIDE_PARENT | DragLimited.VERTICAL),
            (DragLimited.WITHOUT_LIMITED | DragLimited.HORIZONTAL | DragLimited.VERTICAL),
            (DragLimited.WITHOUT_LIMITED | DragLimited.HORIZONTAL),
            (DragLimited.WITHOUT_LIMITED | DragLimited.VERTICAL),
            (DragLimited.HORIZONTAL | DragLimited.VERTICAL)})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DragLimited {

        int INSIDE_PARENT = 1;// 不能超过其父布局

        int HORIZONTAL = 1 << 1;//水平方向移动

        int VERTICAL = 1 << 2;//垂直方向移动

        int WITHOUT_LIMITED = 1 << 3;// 无限制
    }

    public final int DRAG_LIMITED_ILLEGAL = DragLimited.INSIDE_PARENT | DragLimited.WITHOUT_LIMITED;

    private int dragLimited = DragLimited.INSIDE_PARENT;
    private boolean dragEnable;

    private int touchSlop;

    private float downX;
    private float downY;
    private float moveX;
    private float moveY;

    private OnDragListener onDragListener;

    public DragLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setDragLimited(DragLimited.INSIDE_PARENT);
        setDragEnable(true);
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }

    /**
     * 是否允许 drag
     *
     * @param dragEnable true or false
     */
    public void setDragEnable(boolean dragEnable) {
        this.dragEnable = dragEnable;
    }

    public boolean isDragEnable() {
        return dragEnable;
    }

    /**
     * 设置 Drag 的范围
     * 支持类似于 INSIDE_PARENT | ALIGN_TOP 的这种“或”操作
     *
     * @param limited 合法输入参见{@link DragLimited}
     */
    public void setDragLimited(int limited) {
        if ((limited & DRAG_LIMITED_ILLEGAL) == DRAG_LIMITED_ILLEGAL) {
            throw new RuntimeException("检测到 setDragLimited() 输入了非法的类型！不能同时应用 INSIDE_PARENT 和 WITHOUT_LIMITED");
        }
        dragLimited = limited;
    }


    //drag的状态，用于事件通知
    private int dragState = DRAG_END;
    private final static int DRAG_START = 1;
    private final static int DRAG_END = 2;


    private List<MotionEvent> motionEventsCache = new ArrayList<>();
    private boolean isProcessingCache = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //如果不允许拖拽，那就直接交给child处理
        //如果正在处理Cache的Event，则返回false，直接交给child处理
        if (!dragEnable || isProcessingCache) {
            return false;
        }
        //记录 motionEvent cache
        motionEventsCache.add(MotionEvent.obtain(event));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = moveX = event.getRawX();
                downY = moveY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                moveX = event.getRawX();
                moveY = event.getRawY();
                if (dragState == DRAG_END) {
                    //抬手的瞬间，如果发现并没有触发drag事件的话，则将cache住的所有event抛给下层处理
                    isProcessingCache = true;
                    for (MotionEvent motionEvent : motionEventsCache) {
                        dispatchTouchEvent(motionEvent);
                    }
                    isProcessingCache = false;
                    //清除cache信息
                    motionEventsCache.clear();
                    return false;
                }
                motionEventsCache.clear();
                break;

        }
        return true;//如果没有触发拖动事件，就返回false，把event交给child处理
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!dragEnable || isProcessingCache) {
            return false;
        }
        // 进入onTouchEvent就表明onInterceptTouchEvent返回的true，此时并没触发ActionUp。
        // 因此需要在平移和回调的时候判断状态信息
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float curX = event.getRawX();
                float curY = event.getRawY();

                float dX = curX - moveX;
                float dY = curY - moveY;
                moveX = curX;
                moveY = curY;

                if (dragState == DRAG_END) {
                    //超过最小滑动距离，则认定为滑动事件
                    if (Math.abs(moveX - downX) > touchSlop || Math.abs(moveY - downY) > touchSlop) {
                        dragState = DRAG_START;
                        if (onDragListener != null) {
                            onDragListener.onDragStart(this);
                        }
                    }
                }

                if (dragState == DRAG_START) {
                    setTargetX(dX);
                    setTargetY(dY);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                moveX = event.getRawX();
                moveY = event.getRawY();
                if (dragState == DRAG_START) {
                    dragState = DRAG_END;
                    if (onDragListener != null) {
                        onDragListener.onDragEnd(this);
                    }
                    return true;
                }
                break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onInterceptTouchEvent(ev)) {
            //直接拦截自己处理
            return onTouchEvent(ev);
        } else {
            // 此处的事件都是cache事件，因为parent认为DragLayout已经处理过正常的事件了，
            // 所以touch事件的处理只能让child响应，如果child需要执行，那就执行，如果不需要执行，也就结束了。
            // 目前不支持child处理长按事件。
            // 理论上，只有当前的dispatchTouchEvent不return，才能保证child不处理的时候将事件抛到上层，但是那样很难处理。
            return super.dispatchTouchEvent(ev);
        }
    }

    private void setTargetX(float dX) {
        if (isDragHorizontal()) {
            setX(getTargetX(dX));
        }
    }

    private void setTargetY(float dY) {
        if (isDragVertical()) {
            setY(getTargetY(dY));
        }
    }

    private float getTargetX(float dX) {
        float x = getX() + dX;
        if (isDragInsideParent()) {
            float maxRight = getMaxRightInsideParent();
            float minLeft = getMinLeftInsideParent();
            x = x > maxRight ? maxRight : x;
            x = x < minLeft ? minLeft : x;
        }
        return x;
    }

    private float getTargetY(float dY) {
        float y = getY() + dY;
        if (isDragInsideParent()) {
            float maxBottom = getMaxBottomInsideParent();
            float minTop = getMinTopInsideParent();
            y = y > maxBottom ? maxBottom : y;
            y = y < minTop ? minTop : y;
        }
        return y;
    }

    private boolean isDragInsideParent() {
        return (dragLimited & DragLimited.INSIDE_PARENT) == DragLimited.INSIDE_PARENT;
    }

    /*能否水平方向拖拽*/
    private boolean isDragHorizontal() {
        //有 Vertical 没有 Horizontal，就是不行的，其他的都行
        return (dragLimited & DragLimited.VERTICAL) != DragLimited.VERTICAL ||
                (dragLimited & DragLimited.HORIZONTAL) == DragLimited.HORIZONTAL;
    }

    /*能否垂直方向拖拽*/
    private boolean isDragVertical() {
        //有 Horizontal 没有 Vertical，就是不行的，其他的都行
        return (dragLimited & DragLimited.HORIZONTAL) != DragLimited.HORIZONTAL ||
                (dragLimited & DragLimited.VERTICAL) == DragLimited.VERTICAL;
    }

    private int getMinTopInsideParent() {
        if (getParent() instanceof View) {
            return ((View) getParent()).getPaddingTop();
        }
        return 0;
    }

    private int getMaxBottomInsideParent() {
        if (getParent() instanceof View) {
            int parentHeight = ((View) getParent()).getHeight();
            int paddingBottom = ((View) getParent()).getPaddingBottom();
            int height = getHeight();
            return parentHeight - paddingBottom - height;
        }
        return 0;
    }

    private int getMinLeftInsideParent() {
        if (getParent() instanceof View) {
            return ((View) getParent()).getPaddingLeft();
        }
        return 0;
    }

    private int getMaxRightInsideParent() {
        if (getParent() instanceof View) {
            int parentWidth = ((View) getParent()).getWidth();
            int paddingRight = ((View) getParent()).getPaddingRight();
            int width = getWidth();
            return parentWidth - paddingRight - width;
        }
        return 0;
    }
}