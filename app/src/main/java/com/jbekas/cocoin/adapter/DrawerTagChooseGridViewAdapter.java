package com.jbekas.cocoin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbekas.cocoin.R;
import com.jbekas.cocoin.db.RecordManager;
import com.jbekas.cocoin.util.CoCoinUtil;

public class DrawerTagChooseGridViewAdapter extends BaseAdapter {

    private CoCoinUtil coCoinUtil;
    private LayoutInflater inflater;

    public DrawerTagChooseGridViewAdapter(
            Context context,
            CoCoinUtil coCoinUtil) {
        this.inflater = LayoutInflater.from(context);
        this.coCoinUtil = coCoinUtil;
    }

    @Override
    public int getCount() {
        return RecordManager.TAGS.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.item_tag_choose, null);
            holder.tagImage = (ImageView) convertView.findViewById(R.id.tag_image);
            holder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tagImage.setImageResource(
                coCoinUtil.getTagIcon(RecordManager.TAGS.get(position).getId()));
        holder.tagName.setText(coCoinUtil.getTagName(RecordManager.TAGS.get(position).getId()));
//        holder.tagName.setTypeface(CoCoinUtil.GetTypeface());

        return convertView;
    }

    private class ViewHolder {
        ImageView tagImage;
        TextView tagName;
    }
}
