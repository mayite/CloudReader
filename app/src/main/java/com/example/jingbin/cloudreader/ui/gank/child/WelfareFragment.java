package com.example.jingbin.cloudreader.ui.gank.child;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.adapter.WelfareAdapter;
import com.example.jingbin.cloudreader.base.BaseFragment;
import com.example.jingbin.cloudreader.bean.GankIoDataBean;
import com.example.jingbin.cloudreader.databinding.FragmentWelfareBinding;
import com.example.jingbin.cloudreader.utils.DebugUtil;
import com.example.jingbin.cloudreader.view.viewbigimage.ViewBigImageActivity;
import com.example.jingbin.cloudreader.viewmodel.gank.WelfareViewModel;
import com.example.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.jingbin.library.ByRecyclerView;

/**
 * 福利
 *
 * @author jingbin
 */
public class WelfareFragment extends BaseFragment<WelfareViewModel, FragmentWelfareBinding> {

    private static final String TAG = "WelfareFragment";
    private WelfareAdapter mWelfareAdapter;
    private boolean isPrepared = false;
    private boolean isFirst = true;
    private ArrayList<String> imgList = new ArrayList<>();
    private ArrayList<String> imgTitleList = new ArrayList<>();

    @Override
    public int setContent() {
        return R.layout.fragment_welfare;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initRecycleView();
        isPrepared = true;
    }

    @Override
    protected void loadData() {
        if (!mIsVisible || !isPrepared || !isFirst) {
            return;
        }
        loadWelfareData();
    }

    private void initRecycleView() {
        mWelfareAdapter = new WelfareAdapter();
        //构造器中，第一个参数表示列数或者行数，第二个参数表示滑动方向,瀑布流
        bindingView.xrvWelfare.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        bindingView.xrvWelfare.setHasFixedSize(true);
        bindingView.xrvWelfare.setItemAnimator(null);
        bindingView.xrvWelfare.setAdapter(mWelfareAdapter);
        bindingView.xrvWelfare.setOnLoadMoreListener(new ByRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                int page = viewModel.getPage();
                page++;
                viewModel.setPage(page);
                loadWelfareData();
            }
        });
        bindingView.xrvWelfare.setOnItemClickListener(new ByRecyclerView.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                ViewBigImageActivity.startImageList(getContext(), position, imgList, imgTitleList);
            }
        });
        viewModel.getImageAndTitle().observe(this, new Observer<ArrayList<ArrayList<String>>>() {
            @Override
            public void onChanged(@Nullable ArrayList<ArrayList<String>> arrayLists) {
                if (arrayLists != null && arrayLists.size() == 2) {
                    imgList.addAll(arrayLists.get(0));
                    imgTitleList.addAll(arrayLists.get(1));
                }
            }
        });
    }

    private void loadWelfareData() {
        viewModel.loadWelfareData().observe(this, new Observer<GankIoDataBean>() {
            @Override
            public void onChanged(@Nullable GankIoDataBean bean) {
                if (bean != null && bean.getResults() != null && bean.getResults().size() > 0) {
                    // +1 是因为本身的rv带有一个刷新头布局
                    if (viewModel.getPage() == 1) {
                        showContentView();
                        mWelfareAdapter.clear();
                        mWelfareAdapter.setNewData(bean.getResults());
                    } else {
                        mWelfareAdapter.addData(bean.getResults());
                    }
                    bindingView.xrvWelfare.loadMoreComplete();

                    if (isFirst) {
                        isFirst = false;
                    }
                } else {
                    bindingView.xrvWelfare.loadMoreComplete();
                    if (mWelfareAdapter.getItemCount() == 0) {
                        showError();
                    } else {
                        bindingView.xrvWelfare.loadMoreEnd();
                    }
                }
            }
        });
    }

    @Override
    protected void onRefresh() {
        loadWelfareData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.onDestroy();
    }
}
