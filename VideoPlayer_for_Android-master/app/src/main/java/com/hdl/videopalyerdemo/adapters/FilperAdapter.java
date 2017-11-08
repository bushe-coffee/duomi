package com.hdl.videopalyerdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.hdl.videopalyerdemo.R;
import com.hdl.videopalyerdemo.models.CommendModel;

import java.util.List;


public class FilperAdapter extends BaseAdapter {

    private Context mContext;
    private List<CommendModel> mDatas;
    private int mType = 0;

    public FilperAdapter(Context context) {
        this.mContext = context;
    }

    public void setDatas(List<CommendModel> datas, int type) {
        this.mDatas = datas;
        this.mType = type;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View parent, ViewGroup viewGroup) {
        ItemHolder holder;
        if (parent == null && mContext != null) {
            View view = null;
            if (mType == 4) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_filper_taobao, viewGroup, false);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_filper_layout, viewGroup, false);
            }
            
            holder = new ItemHolder(view);
            parent = view;
            parent.setTag(holder);
        } else {
            assert parent != null;
            holder = (ItemHolder) parent.getTag();
        }

        CommendModel model = mDatas.get(position);
        if (model != null) {
            if (mType != 4) {
                holder.title.setText(model.getDisplay_title().trim());
                holder.content.setText(model.getDetailed_description().trim());
                if (model.getDetailed_image_url().length() > 8) {
//                    ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), holder.image);
                }
            } else {
                holder.taobaoComment1.setText(model.getComment1());
                holder.taobaoComment2.setText(model.getComment2());
                holder.taobaoComment3.setText(model.getComment3());
                holder.taobaoTitle.setText(model.getDisplay_title());
                holder.taobaoPrice.setText("￥" + model.getDetailed_description());
                if (model.getDetailed_image_url().length() > 8) {
                    //ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), holder.taobaoImage);
                }
            }
        }

        return parent;
    }

    private class ItemHolder {
        TextView title;
        ImageView image;
        TextView content;

        TextView taobaoTitle;
        TextView taobaoPrice;
        ImageView taobaoImage;
        TextView taobaoComment1;
        TextView taobaoComment2;
        TextView taobaoComment3;

        ItemHolder(View view) {
            if (view != null) {
                if (mType != 4) {
                    title = (TextView) view.findViewById(R.id.item_filper_name);
                    image = (ImageView) view.findViewById(R.id.item_filper_image);
                    content = (TextView) view.findViewById(R.id.item_filper_content);
                } else {
                    taobaoTitle = (TextView) view.findViewById(R.id.item_filper_taobao_name);
                    taobaoPrice = (TextView) view.findViewById(R.id.item_filper_taobao_price);
                    taobaoImage = (ImageView) view.findViewById(R.id.item_filper_taobao_image);
                    taobaoComment1 = (TextView) view.findViewById(R.id.item_filper_taobao_comment1);
                    taobaoComment2 = (TextView) view.findViewById(R.id.item_filper_taobao_comment2);
                    taobaoComment3 = (TextView) view.findViewById(R.id.item_filper_taobao_comment3);
                }
            }
        }
    }
}
