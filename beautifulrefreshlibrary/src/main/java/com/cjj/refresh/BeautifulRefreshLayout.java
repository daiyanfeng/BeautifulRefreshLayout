package com.cjj.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;


/**
 * Created by cjj on 2015/8/4.
 */
public class BeautifulRefreshLayout extends RefreshLayout {
    private float waveHeight = 200;
    private float headHeight = 120;
    private final int SHAKE_TIME = 1000;//wave震动时间

    private View mHeadView;
    private WaveView mWaveView;
    private TextView mTv_tip;

    public BeautifulRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public BeautifulRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautifulRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化
     */
    private void init(AttributeSet attrs) {
        /**
         * attrs  需要在xml设置什么属性  自己自定义吧  啊哈哈
         */

        /**
         * 初始化headView
         */
        mHeadView = LayoutInflater.from(getContext()).inflate(R.layout.view_head, null);
        mWaveView = (WaveView) mHeadView.findViewById(R.id.draweeView);
        mTv_tip = (TextView) mHeadView.findViewById(R.id.tv_tip);
        /**
         * 设置波浪的高度
         */
        setWaveHeight(DensityUtil.dip2px(getContext(), waveHeight));
        /**
         * 设置headView的高度
         */
        setHeaderHeight(DensityUtil.dip2px(getContext(), headHeight));
        /**
         * 设置headView
         */
        setHeaderView(mHeadView);
        /**
         * 监听波浪变化监听
         */
        setPullWaveListener(new PullWaveListener() {
            @Override
            public void onPulling(RefreshLayout refreshLayout, float fraction) {
                float headW = DensityUtil.dip2px(getContext(), waveHeight);
                mWaveView.setHeadHeight((int) (DensityUtil.dip2px(getContext(), headHeight) * limitValue(1, fraction)));
                mWaveView.setWaveHeight((int) (headW * Math.max(0, fraction - 1)));

                mWaveView.invalidate();

                if (DensityUtil.dip2px(getContext(), headHeight) > (int) (DensityUtil.dip2px(getContext(), headHeight) * limitValue(1, fraction))) {
                    mTv_tip.setText("下拉刷新");
                } else {
                    mTv_tip.setText("松开刷新");
                }


            }

            @Override
            public void onPullReleasing(RefreshLayout refreshLayout, float fraction) {
                if (!refreshLayout.isRefreshing) {

                }
            }
        });

        /**
         * 松开后的监听
         */
        setPullToRefreshListener(new PullToRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                mTv_tip.setText("刷新中...");
                mWaveView.setHeadHeight((int) (DensityUtil.dip2px(getContext(), headHeight)));
                ValueAnimator animator = ValueAnimator.ofInt(mWaveView.getWaveHeight(), 0, -300, 0, -100, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Log.i("anim", "value--->" + (int) animation.getAnimatedValue());
                        mWaveView.setWaveHeight((int) animation.getAnimatedValue());
                        mWaveView.invalidate();

                    }
                });
                animator.setInterpolator(new BounceInterpolator());
                animator.setDuration(SHAKE_TIME);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (listener != null) {
                            listener.onRefresh(BeautifulRefreshLayout.this);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

//                refreshLayout.postDelayed(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                if (listener != null) {
//                                    listener.onRefresh(BeautifulRefreshLayout.this);
//                                }
//                            }
//                        }, SHAKE_TIME);
            }
        });
    }


    public void shakeAnim(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0, 2, 0, -2, 0);
        animator.setDuration(100);
        animator.setRepeatCount(-1);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.start();
    }


    /**
     * 限定值
     *
     * @param a
     * @param b
     * @return
     */
    public float limitValue(float a, float b) {
        float valve = 0;
        final float min = Math.min(a, b);
        final float max = Math.max(a, b);
        valve = valve > min ? valve : min;
        valve = valve < max ? valve : max;
        return valve;
    }

    public interface BuautifulRefreshListener {
        void onRefresh(BeautifulRefreshLayout refreshLayout);
    }

    private BuautifulRefreshListener listener;

    public void setBuautifulRefreshListener(BuautifulRefreshListener listener) {
        this.listener = listener;
    }
}
