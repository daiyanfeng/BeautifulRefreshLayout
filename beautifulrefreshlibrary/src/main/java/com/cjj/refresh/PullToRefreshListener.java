package com.cjj.refresh;

import android.support.v7.widget.RecyclerView;

/**
 * Created by cjj on 2015/8/4.
 * 刷新回调接口
 */
public interface PullToRefreshListener {
    /**
     * 刷新中。。。
     * @param refreshLayout
     */
    void onRefresh(RefreshLayout refreshLayout);

//    /**
//     * 加载更多中
//     * @param recyclerView
//     * @param newState
//     * @param lastVisibleItem
//     */
//    void onLoadMore(RecyclerView recyclerView, int newState, int lastVisibleItem);
}
