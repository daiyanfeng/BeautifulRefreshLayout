package com.srt.refresh;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by 戴延枫 on 2017/8/1.
 */
public class RefreshLayout extends FrameLayout {
    private Context mContext;
    //波浪的高度
    protected float mWaveHeight;

    //头部的高度
    protected float mHeadHeight;

    //footer的高度
    protected float mFooterHeight;

    //子控件
    private View mChildView;

    //头部layout
    protected FrameLayout mHeadLayout;
    //floatView
    protected GifView mFloatView;
    //loadingView
    protected ImageView mLoadingView;
    //loadingView动画
    private Animation mLoadingAnim;
    //footerView
    protected FrameLayout mFooterLayout;
    //footerLoadingView
    protected ImageView mFooterLoadingView;

    //刷新的状态
    protected boolean isRefreshing;

    //触摸获得Y的位置
    private float mTouchY;

    //当前Y的位置
    private float mCurrentY;

    //loadmore的状态
    protected boolean isLoadMore;

    /**
     * 控件的状态
     * 1.下拉刷新
     * 2.加载更多
     */
    private int pullState;

    /**
     * 上拉加载更多是否可用
     * true 可用
     * false 不可用
     */
    private boolean loadMoreEnable;

    //动画的变化率
    private DecelerateInterpolator decelerateInterpolator;

    //自动收回headView的动画
    public ObjectAnimator pullReleasingOA;


    public RefreshLayout(Context context) {
        this(context, null, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = context;
        //使用isInEditMode解决可视化编辑器无法识别自定义控件的问题
        if (isInEditMode()) {
            return;
        }

        if (getChildCount() > 1) {
            throw new RuntimeException("只能拥有一个子控件哦");
        }

        //在动画开始的地方快然后慢;
        decelerateInterpolator = new DecelerateInterpolator(10);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //获得子控件
        mChildView = getChildAt(0);

        //添加头部
        FrameLayout headViewLayout = new FrameLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams.gravity = Gravity.TOP;
        headViewLayout.setLayoutParams(layoutParams);
        mHeadLayout = headViewLayout;
        this.addView(mHeadLayout);

        if (loadMoreEnable) {
            //添加footer
            FrameLayout footerViewLayout = new FrameLayout(getContext());
            LayoutParams footerlayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mFooterHeight);
            footerlayoutParams.gravity = Gravity.BOTTOM;
            footerlayoutParams.bottomMargin = -(int) mFooterHeight;
            footerViewLayout.setLayoutParams(footerlayoutParams);

            mFooterLayout = footerViewLayout;
            this.addView(mFooterLayout);
        }
//        mChildView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

    }

    //由于animate().setUpdateListener必须在API 19以上才能使用，故使用ObjectAnimator代替
    private void setChildViewTransLationY(float... values) {
        pullReleasingOA = ObjectAnimator.ofFloat(mChildView, View.TRANSLATION_Y, values);
        pullReleasingOA.setInterpolator(new DecelerateInterpolator());
        pullReleasingOA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (pullState == 1) {
                    int height = (int) mChildView.getTranslationY();//获得mChildView当前y的位置
                    mHeadLayout.getLayoutParams().height = height;
                    mHeadLayout.requestLayout();//重绘
                } else if (pullState == 2) {
                    if (null != mFooterLayout) {
                        mFooterLayout.setTranslationY(mChildView.getTranslationY());
                    }
                }
            }
        });
        pullReleasingOA.start();
    }

    /**
     * 当用户松开手后，但是还没有出发刷新时，已经显示的head自动收回的动画
     *
     * @param values
     */
    private void setPullReleasingTransLationY(float... values) {
        pullReleasingOA = ObjectAnimator.ofFloat(mChildView, View.TRANSLATION_Y, values);
        pullReleasingOA.setDuration(500);
        pullReleasingOA.setInterpolator(new DecelerateInterpolator());
        pullReleasingOA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) mChildView.getTranslationY();//获得mChildView当前y的位置
                mHeadLayout.getLayoutParams().height = height;
                mHeadLayout.requestLayout();//重绘
                if (pullStateListener != null) {
                    pullStateListener.onPullReleasing(RefreshLayout.this, height);
                }
            }
        });
        pullReleasingOA.start();
    }

    /**
     * 当用户松开手后，但是还没有触发加载更多时，已经显示的footer自动收回的动画
     *
     * @param values
     */
    private void setloadMoreReleasingTransLationY(float... values) {
        pullReleasingOA = ObjectAnimator.ofFloat(mChildView, View.TRANSLATION_Y, values);
        pullReleasingOA.setDuration(500);
        pullReleasingOA.setInterpolator(new DecelerateInterpolator());
        pullReleasingOA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = -(int) mChildView.getTranslationY();//获得mChildView当前y的位置
//                mFooterLayout.getLayoutParams().height = height;
//                mFooterLayout.requestLayout();//重绘
                if (pullStateListener != null) {
                    pullStateListener.onLoadMorePullReleasing(RefreshLayout.this, height);
                }
            }
        });
        pullReleasingOA.start();
    }

    private void setFloatViewTransLationY(float... values) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(mFloatView, View.TRANSLATION_Y, values);
//        oa.setDuration(500);
        oa.setInterpolator(new AccelerateInterpolator());
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

            }
        });
        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //替换飞翔的火箭gif
                mFloatView.setMovieResource(R.drawable.pencil_fly_gif);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        oa.start();
    }

    /**
     * 拦截事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isRefreshing || isLoadMore) return true;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = ev.getY();
                mCurrentY = mTouchY;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - mTouchY;
                Log.e("dyf", "onInterceptTouchEvent: " + canChildScrollUp());
                if (dy > 0 && !canChildScrollUp()) {
                    pullState = 1;
                    return true;
                } else if (loadMoreEnable && dy < 0 && !canChildScrollDown()) {
                    pullState = 2;
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 响应事件
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (isRefreshing || isLoadMore) {
            return super.onTouchEvent(e);
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mCurrentY = e.getY();

                float dy = mCurrentY - mTouchY;
                if (pullState == 1) { //下拉刷新
                    dy = Math.max(0, dy);
                    dy = Math.min(mWaveHeight + mHeadHeight, dy);

                    if (mChildView != null) {
//                    float offsetY = decelerateInterpolator.getInterpolation(dy / (mWaveHeight + mHeadHeight)) * dy;
                        float offsetY = dy;
                        mChildView.setTranslationY(offsetY);
                        mHeadLayout.getLayoutParams().height = (int) offsetY;
                        mHeadLayout.requestLayout();

                        if (pullStateListener != null) {
                            pullStateListener.onPulling(RefreshLayout.this, offsetY);
                        }
                    }
                } else if (pullState == 2) { // 加载更多
                    dy = Math.min(0, dy);
                    dy = Math.max(-mFooterHeight, dy);

                    if (mChildView != null) {
                        float offsetY = dy;
                        mChildView.setTranslationY(offsetY);
                        if (pullStateListener != null) {
                            pullStateListener.onLoadMorePulling(RefreshLayout.this, -offsetY);
                        }
                    }
                }

                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mChildView != null) {
                    if (pullState == 1) { //下拉刷新
                        if (mChildView.getTranslationY() >= mHeadHeight + mWaveHeight) {
                            setFloatViewTransLationY(-mFloatView.getHeight());
                            setChildViewTransLationY(mHeadHeight);
                            isRefreshing = true;
                            isLoadMore = false;
                            if (pullToRefreshPullingListener != null) {
                                pullToRefreshPullingListener.onRefresh(RefreshLayout.this);
                            }
                        } else {
                            setPullReleasingTransLationY(0);
                        }
                    } else if (pullState == 2) { // 加载更多
                        if (-mChildView.getTranslationY() >= mFooterHeight) { //因为是负数
                            isRefreshing = false;
                            isLoadMore = true;
                            if (pullToRefreshPullingListener != null) {
                                pullToRefreshPullingListener.onLoadMore(RefreshLayout.this);
                            }
                        } else {
                            setloadMoreReleasingTransLationY(0);
                        }
                    }


                }
                return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * 用来判断顶部是否可以滚动
     *
     * @return boolean
     */
    public boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mChildView, -1) || mChildView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mChildView, -1);
        }
    }

    /**
     * 判断底部是否可以滚动
     *
     * @return
     */
    public boolean canChildScrollDown() {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mChildView, 1) || mChildView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mChildView, 1);
        }
    }

    /**
     * loadMore结束
     */
    public void finishLoadMore() {
        hiddenFooterLoadingView();
        if (mChildView != null) {
            setChildViewTransLationY(0);
        }
        isLoadMore = false;
    }

    /**
     * 设置下拉监听
     */
    private PullToRefreshListener pullToRefreshPullingListener;

    public void setPullToRefreshListener(PullToRefreshListener pullToRefreshPullingListener) {
        this.pullToRefreshPullingListener = pullToRefreshPullingListener;
    }

    /**
     * 设置wave监听
     */
    private PullStateListener pullStateListener;

    public void setPullStateListener(PullStateListener pullStateListener) {
        this.pullStateListener = pullStateListener;
    }

    /**
     * 刷新结束
     */
    public void finishRefreshing() {
        hiddenLoadingView();
        if (mChildView != null) {
//            mChildView.animate().translationY(0).start();
            setChildViewTransLationY(0);
        }
        isRefreshing = false;
    }

    /**
     * 设置头部View
     *
     * @param headerView
     */
    public void setHeaderView(final View headerView) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeadLayout.addView(headerView);
            }
        });
    }

    /**
     * 设置floatView
     */
    public void setFloatView(GifView floatView) {
        mFloatView = floatView;
    }

    /**
     * 设置footerView
     *
     * @param footerView
     */
    public void setFooterView(final View footerView) {
        post(new Runnable() {
            @Override
            public void run() {
                if (loadMoreEnable) {
                    mFooterLayout.addView(footerView);
                }
//                mFooterLayout.setY(mFooterLayout.getY() + mFooterHeight);
            }
        });
    }

    /**
     * 设置loadingView
     */
    public void setLoadingView(ImageView loadingView) {
        mLoadingView = loadingView;
        mLoadingAnim = AnimationUtils.loadAnimation(mContext, R.anim.lodingview_progress);
        LinearInterpolator lin = new LinearInterpolator();
        mLoadingAnim.setInterpolator(lin);
    }

    /**
     * 显示laodingview
     */
    public void showLoadingView() {
        if (mLoadingView != null && mLoadingAnim != null) {
            mLoadingView.setVisibility(View.VISIBLE);
            mLoadingView.clearAnimation();
            mLoadingView.startAnimation(mLoadingAnim);
        }
    }

    /**
     * 隐藏LoadingView
     */
    public void hiddenLoadingView() {
        if (mLoadingView != null && mLoadingAnim != null) {
            mLoadingView.clearAnimation();
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置footerloadingView
     */
    public void setFooterLoadingView(ImageView footerLoadingView) {
        mFooterLoadingView = footerLoadingView;
        mLoadingAnim = AnimationUtils.loadAnimation(mContext, R.anim.lodingview_progress);
        LinearInterpolator lin = new LinearInterpolator();
        mLoadingAnim.setInterpolator(lin);
    }

    /**
     * 显示设置footerloadingView
     */
    public void showFooterLoadingView() {
        if (mFooterLoadingView != null && mLoadingAnim != null) {
//            mFooterLoadingView.setVisibility(View.VISIBLE);
            mFooterLoadingView.clearAnimation();
            mFooterLoadingView.startAnimation(mLoadingAnim);
        }
    }

    /**
     * 隐藏设置footerloadingView
     */
    public void hiddenFooterLoadingView() {
        if (mFooterLoadingView != null && mLoadingAnim != null) {
            mFooterLoadingView.clearAnimation();
//            mFooterLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置wave的下拉高度
     *
     * @param waveHeight
     */
    public void setWaveHeight(float waveHeight) {
        this.mWaveHeight = waveHeight;
    }

    /**
     * 设置下拉头的高度
     *
     * @param headHeight
     */
    public void setHeaderHeight(float headHeight) {
        this.mHeadHeight = headHeight;
    }

    /**
     * 设置footer的高度
     *
     * @param footerHeight
     */
    public void setFooterHeight(float footerHeight) {
        this.mFooterHeight = footerHeight;
    }

    /**
     * 上拉加载更多是否可用
     * true 可用
     * false 不可用
     */
    public void setLoadMoreEnable(boolean loadMoreEnable) {
        this.loadMoreEnable = loadMoreEnable;
    }
}
