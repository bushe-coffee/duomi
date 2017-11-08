package com.hdl.videopalyerdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdl.videopalyerdemo.R;
import com.hdl.videopalyerdemo.models.ProductModel;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;


public class ProductFilperAdapter extends BaseAdapter {

    private Context mContext;
    private List<ProductModel> mDatas;

    public ProductFilperAdapter(Context context) {
        this.mContext = context;
    }

    public void setDatas(List<ProductModel> product_list) {
        this.mDatas = product_list;
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
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_filper_taobao2, viewGroup, false);

            holder = new ItemHolder(view);
            parent = view;
            parent.setTag(holder);
        } else {
            assert parent != null;
            holder = (ItemHolder) parent.getTag();
        }

        ProductModel model = mDatas.get(position);
        if (model != null) {

            holder.taobaoTitle.setText(model.getProduct_name());
            holder.taobaoPrice.setText("￥ " + model.getPrice());
            if (model.getProduct_image().length() > 8) {
                ImageLoader.getInstance().displayImage(model.getProduct_image(), holder.taobaoImage);
            }
        }

        return parent;
    }

    private class ItemHolder {

        TextView taobaoTitle;
        TextView taobaoPrice;
        ImageView taobaoImage;

        ItemHolder(View view) {
            if (view != null) {
                taobaoTitle = (TextView) view.findViewById(R.id.item_filper_taobao_name2);
                taobaoPrice = (TextView) view.findViewById(R.id.item_filper_taobao_price2);
                taobaoImage = (ImageView) view.findViewById(R.id.item_filper_taobao_image2);
            }
        }
    }
}
