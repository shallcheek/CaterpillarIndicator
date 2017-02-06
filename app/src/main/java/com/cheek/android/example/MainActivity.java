package com.cheek.android.example;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.chaek.android.widget.CaterpillarIndicator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout activityMain;
    private CaterpillarIndicator titleBar;
    private ViewPager viewpage;

    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        titleBar = (CaterpillarIndicator) findViewById(R.id.title_bar);
        viewpage = (ViewPager) findViewById(R.id.viewpage);

        fragmentList.add(new BaseFragment());
        fragmentList.add(new BaseFragment());
        fragmentList.add(new BaseFragment());
        fragmentList.add(new BaseFragment());
        BaseFragmentAdapter adapter = new BaseFragmentAdapter(getSupportFragmentManager());
        viewpage.setAdapter(adapter);
        viewpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                titleBar.setTextColorSelected(getResources().getColor(R.color.colorPrimary));
            }
        });
        List<CaterpillarIndicator.TitleInfo> titleInfos = new ArrayList<>();
        titleInfos.add(new CaterpillarIndicator.TitleInfo("热门"));
        titleInfos.add(new CaterpillarIndicator.TitleInfo("榜单"));
        titleInfos.add(new CaterpillarIndicator.TitleInfo("视频"));
        titleInfos.add(new CaterpillarIndicator.TitleInfo("头条"));
        titleBar.init(0, titleInfos, viewpage);

        titleBar.setFooterLineHeight(3);
        titleBar.setItemLineWidth(40);

        titleBar.setTextSizeSelected(18);

    }

    private class BaseFragmentAdapter extends FragmentStatePagerAdapter {

        public BaseFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList != null ? fragmentList.size() : 0;
        }
    }
}
