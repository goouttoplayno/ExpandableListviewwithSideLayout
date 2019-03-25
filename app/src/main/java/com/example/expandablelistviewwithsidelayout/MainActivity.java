package com.example.expandablelistviewwithsidelayout;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ExpandableListView expandableListView;
    private ArrayList<Integer> groups;
    private ArrayList<ArrayList<MyContent>> childs;
//    private String[] groups = {"A", "B", "C"};
//    private String[][] childs = {{"A1", "A2", "A3", "A4"}, {"A1", "A2", "A3", "A4"}, {"A1", "A2", "A3", "A4"}};
    private Set<SlideLayout> sets = new HashSet();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        expandableListView = (ExpandableListView)findViewById(R.id.expandableListView);
        groups = new ArrayList<>();
        childs = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            groups.add(i);
            ArrayList<MyContent> al = new ArrayList<>();
            for (int j = 0; j < 10 ;j++){
                al.add(new MyContent("Content" + j));
            }
            childs.add(al);
        }
        MyExpandableListView myExpandableListView = new MyExpandableListView(this, childs);
        expandableListView.setAdapter(myExpandableListView);
    }

    class MyExpandableListView extends BaseExpandableListAdapter {
        private Context context;
        private ArrayList<ArrayList<MyContent>> childs;
        public MyExpandableListView(Context context, ArrayList<ArrayList<MyContent>> childs){
            this.context = context;
            this.childs = childs;
        }
        //返回一级列表的个数
        @Override
        public int getGroupCount() {
            return groups.size();
        }
        //返回二级列表的个数
        @Override
        public int getChildrenCount(int groupPosition) {//groupPosition表示第几个一级列表
            Log.d("smyhvae", "-->" + groupPosition);
            return childs.get(groupPosition).size();
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            super.unregisterDataSetObserver(observer);
        }

        //返回一级列表的单个item
        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        //返回二级列表中的单个item
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childs.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        //每个item的id是否是固定
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.item_group, null);
            }
            TextView tv_group = (TextView)convertView.findViewById(R.id.tv_group);
            tv_group.setText(groups.get(groupPosition) + "");
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.i("touchevent", event.getAction() + "");
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            for (SlideLayout s: sets){
                                s.closeMenu();
                                sets.remove(s);
                            }
                            break;
                    }
                    return false;
                }
            });
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                convertView = getLayoutInflater().from(context).inflate(R.layout.item_slide, null);
                viewHolder = new ViewHolder();
                viewHolder.contentView = (TextView)convertView.findViewById(R.id.content);
                viewHolder.menuView = (TextView)convertView.findViewById(R.id.menu);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.contentView.setText(childs.get(groupPosition).get(childPosition).getContent());
            final MyContent myContent = childs.get(groupPosition).get(childPosition);
            viewHolder.menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SlideLayout slideLayout = (SlideLayout)v.getParent();
                    slideLayout.closeMenu();
                    childs.get(groupPosition).remove(myContent);
                    notifyDataSetChanged();
                }
            });
            SlideLayout slideLayout = (SlideLayout)convertView;
            slideLayout.setOnStateChangeListener(new MyOnStateChangeListener());
            return convertView;
        }

        //二级列表中的item是否能被选中，可以返回true
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public SlideLayout slideLayout = null;
        class MyOnStateChangeListener implements SlideLayout.OnStateChangeListener{
            //滑动后每次手势抬起保证只有一个item是open状态，加入sets集合中
            @Override
            public void onOpen(SlideLayout layout) {
                slideLayout = layout;
                if(sets.size() > 0){
                    for (SlideLayout s : sets){
                        s.closeMenu();
                        sets.remove(s);
                    }
                }
                sets.add(layout);
            }

            @Override
            public void onMove(SlideLayout layout) {
                if (slideLayout != null && slideLayout != layout){
                    slideLayout.closeMenu();
                }
            }

            @Override
            public void onClose(SlideLayout layout) {
                if (sets.size() > 0){
                    sets.remove(layout);
                }
                if (slideLayout == layout){
                    slideLayout = null;
                }
            }
        }
    }
    static class ViewHolder{
        public TextView contentView;
        public TextView menuView;
    }
}
