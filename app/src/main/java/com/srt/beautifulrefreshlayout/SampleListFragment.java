package com.srt.beautifulrefreshlayout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;

import com.srt.refresh.BeautifulRefreshLayout;

public class SampleListFragment extends Fragment implements BeautifulRefreshLayout.BuautifulRefreshListener {

    BeautifulRefreshLayout mBeautifulRefreshLayout;
    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        mBeautifulRefreshLayout = (BeautifulRefreshLayout) v.findViewById(R.id.refresh);
        mBeautifulRefreshLayout.setLoadMoreEnable(true);
        mBeautifulRefreshLayout.setBuautifulRefreshListener(this);

        RecyclerView rv = (RecyclerView) v.findViewById(R.id.recyclerview);
        setupRecyclerView(rv);
        return v;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private List<String> getRandomSublist(String[] array, int amount) {
        ArrayList<String> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array[random.nextInt(array.length)]);
        }
        return list;
    }

    @Override
    public void onRefresh(BeautifulRefreshLayout refreshLayout) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBeautifulRefreshLayout.finishRefreshing();
            }
        }, 1000);
    }

    /**
     * 加载更多中
     *
     * @param refreshLayout
     */
    @Override
    public void onLoadMore(BeautifulRefreshLayout refreshLayout) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                        Toast.makeText(getActivity(), "加载结束", Toast.LENGTH_SHORT).show();
                mBeautifulRefreshLayout.finishLoadMore();
            }
        }, 3000);
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {


        public static class ViewHolder extends RecyclerView.ViewHolder {

            public final ImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.avatar);
            }


        }

        public SimpleStringRecyclerViewAdapter(Context context) {
            super();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (position == 0) {
                holder.mImageView.setImageResource(R.drawable.bb);
            } else if (position == 1) {
                holder.mImageView.setImageResource(R.drawable.cc);
            }

        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
