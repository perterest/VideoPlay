package com.charles.videoplay.fragment;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.videoplay.R;
import com.charles.videoplay.adapter.TabAdapter;
import com.charles.videoplay.base.BaseFragment;
import com.charles.videoplay.entity.VedioDetails;
import com.charles.videoplay.entity.VideoDetailsList;
import com.charles.videoplay.http.AppException;
import com.charles.videoplay.listener.ResponseListener;
import com.charles.videoplay.net.IndexRequest;
import com.charles.videoplay.recyclerview.LayoutManager.ChStaggeredGridLayoutManager;
import com.charles.videoplay.recyclerview.Listener.LoadDataListener;
import com.charles.videoplay.recyclerview.View.PullRefreshRecycleView;
import com.charles.videoplay.util.JsonParser;
import com.charles.videoplay.util.Logger;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Charles on 2016/10/9.
 */

public class TabFragments extends BaseFragment implements LoadDataListener {

    @Bind(R.id.rvTabView)
    PullRefreshRecycleView recycleView;
    
    private String title;
    private int vid;
    private List<VedioDetails> mDatas = new ArrayList<>();
    private TabAdapter adapter;
    private int mPage;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = getArguments().getString("title");
            vid = getArguments().getInt("vid");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_tab, container, false);
        ButterKnife.bind(this,inflate);
        initData();
        return inflate;
    }

    private void initData(){
        recycleView.setLoadDataListener(this);
        adapter = new TabAdapter(getBaseActivity(),mDatas);
        ChStaggeredGridLayoutManager manager = new ChStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recycleView.setLayoutManager(manager);
        recycleView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        mPage = 0;
        try {
            initDataVideos(mPage);
        } catch (Exception e) {
        }
    }

    @Override
    public void onLoadMore() {
        mPage = mPage + 1 ;
        try {
            initDataVideos(mPage);
        } catch (Exception e) {
        }
    }

    @Override
    protected void fetchObjectData() {
        recycleView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recycleView.forceRefresh();
            }
        }, 200);
    }

    private void initDataVideos(final int page) throws Exception {
        IndexRequest.getVideoListOfType(getBaseActivity(), page, vid, new ResponseListener<String>() {
            @Override
            public void onSuccess(String s) {
                recycleView.refreshComplete();
                if(page > 0){
                    recycleView.loadMoreComplete();
                }
                VideoDetailsList vedioDetails = JsonParser.deserializeByJson(s, VideoDetailsList.class);
                if (vedioDetails.getErrcode() == 0) {
                    List<VedioDetails> datas = vedioDetails.getData();
                    if(datas != null && datas.size() > 0 ){
                        if(mPage == 0){
                            mDatas.clear();
                        }
                        mDatas.addAll(datas);
                        adapter.notifyItemRangeRemoved(mDatas.size(),mDatas.size()+datas.size());
                    }else {
                        if (mPage == 0) {
                            datas.clear();
                            adapter.notifyDataSetChanged();
                        }
                        recycleView.setLoadMoreEnable(false);
                    }
                }
            }

            @Override
            public void onFailure(AppException e) {
                recycleView.refreshComplete();
                recycleView.loadMoreComplete();
            }
        });
    }

    public static TabFragments newInstance(String title , int vid)
    {
        TabFragments tabFragments = new TabFragments();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putInt("vid",vid);
        tabFragments.setArguments(bundle);
        return tabFragments;
    }
}
