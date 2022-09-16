package com.jbekas.cocoin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.jbekas.cocoin.R;
import com.jbekas.cocoin.model.CoCoinRecord;
import com.jbekas.cocoin.model.RecordManager;
import com.jbekas.cocoin.util.CoCoinUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 伟平 on 2015/11/1.
 */
public class RecordCheckDialogRecyclerViewAdapter extends RecyclerView.Adapter<RecordCheckDialogRecyclerViewAdapter.RCDviewHolder> {

    private OnItemClickListener onItemClickListener;

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List<CoCoinRecord> coCoinRecords;

    public RecordCheckDialogRecyclerViewAdapter(Context context, List<CoCoinRecord> list) {
        coCoinRecords = new ArrayList<>();
        coCoinRecords = list;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public RecordCheckDialogRecyclerViewAdapter(Context context, List<CoCoinRecord> list, OnItemClickListener onItemClickListener) {
        coCoinRecords = new ArrayList<>();
        coCoinRecords = list;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RCDviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RCDviewHolder(mLayoutInflater.inflate(R.layout.record_check_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RCDviewHolder holder, int position) {
        holder.imageView.setImageResource(
                CoCoinUtil.GetTagIcon(coCoinRecords.get(position).getTag()));
        holder.date.setText(coCoinRecords.get(position).getCalendarString());
//        holder.date.setTypeface(CoCoinUtil.typefaceLatoLight);
//        holder.money.setTypeface(CoCoinUtil.typefaceLatoLight);
        holder.money.setText(String.valueOf((int) coCoinRecords.get(position).getMoney()));
        holder.money.setTextColor(
                CoCoinUtil.GetTagColorResource(RecordManager.TAGS.get(coCoinRecords.get(position).getTag()).getId()));
        holder.index.setText((position + 1) + "");
//        holder.index.setTypeface(CoCoinUtil.typefaceLatoLight);
        holder.remark.setText(coCoinRecords.get(position).getRemark());
//        holder.remark.setTypeface(CoCoinUtil.typefaceLatoLight);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (coCoinRecords == null) {
            return 0;
        }
        return coCoinRecords.size();
    }

    public class RCDviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.image_view)
        ImageView imageView;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.remark)
        TextView remark;
        @BindView(R.id.money)
        TextView money;
        @BindView(R.id.index)
        TextView index;
        @BindView(R.id.material_ripple_layout)
        MaterialRippleLayout layout;

        RCDviewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
//            onItemClickListener.onItemClick(v, getPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }
}