package cn.airburg.emo.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 自己实现的ViewPager适配器类
 * Created by u17 on 2014/12/30.
 */
public class SelfPageAdapter extends PagerAdapter {

    private List<View> mViewList;

    public SelfPageAdapter(List<View> viewList) {
        this.mViewList = viewList ;
    }

    @Override
    public int getCount() {
        return null == this.mViewList ? 0 : this.mViewList.size() ;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object ;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)   {
        container.removeView(mViewList.get(position));//删除页卡
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {  // 这个方法用来实例化页卡
        container.addView(mViewList.get(position), 0); // 添加页卡
        return mViewList.get(position);
    }
}
