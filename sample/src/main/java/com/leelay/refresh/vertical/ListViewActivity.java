package com.leelay.refresh.vertical;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListViewActivity extends BaseActivity {

    private List<ImageView> mViews;
    private List<String> mList;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        initData();
        initView();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView){
                    String text = ((TextView) view).getText().toString().trim();
                    Class target = null;
                    try {
                        target = Class.forName("com.leelay.refresh.vertical." + text + "Activity");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (target != null) {
                        Intent intent = new Intent(ListViewActivity.this, target);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ListViewActivity.this, ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    private void initView() {
        ViewPager viewPager = new ViewPager(this);
        viewPager.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, dp2px(200)));
        mViews = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.android1);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mViews.add(imageView);
        }
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView imageView = mViews.get(position);
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViews.get(position));
            }
        });
        mListView = (ListView) findViewById(R.id.listView);
        mListView.addHeaderView(viewPager);
        mListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mList));
    }

    private void initData() {
        mList = new ArrayList<>();
        String[] array1 = getResources().getStringArray(R.array.activity_array);
        mList.addAll(Arrays.asList(array1));
        String[] array = getResources().getStringArray(R.array.language_array);
        mList.addAll(Arrays.asList(array));
    }

}
