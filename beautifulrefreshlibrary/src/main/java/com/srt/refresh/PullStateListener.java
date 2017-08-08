package com.srt.refresh;

/**
 * Created by 戴延枫 on 2017/8/3.
 * 下拉状态回调监听
 */
public interface PullStateListener {
    /**
     * 下拉中
     * @param refreshLayout
     * @param offsetY
     */
    void onPulling(RefreshLayout refreshLayout, float offsetY);

    /**
     * 下拉松开
     * @param refreshLayout
     * @param offsetY
     */
    void onPullReleasing(RefreshLayout refreshLayout, float offsetY);

    /**
     * 加载更多的上拉中回调
     * @param refreshLayout
     * @param offsetY
     */
    void onLoadMorePulling(RefreshLayout refreshLayout, float offsetY);
    /**
     * 加载更多的上拉松开回调
     * @param refreshLayout
     * @param offsetY
     */
    void onLoadMorePullReleasing(RefreshLayout refreshLayout, float offsetY);

}
