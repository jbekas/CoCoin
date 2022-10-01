package com.jbekas.cocoin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.jbekas.cocoin.databinding.RecordCheckItemBinding;
import com.jbekas.cocoin.db.RecordManager;
import com.jbekas.cocoin.model.CoCoinRecord;
import com.jbekas.cocoin.util.CoCoinUtil;

import java.util.List;

public class RecordCheckDialogRecyclerViewAdapter extends RecyclerView.Adapter<RecordCheckDialogRecyclerViewAdapter.RCDviewHolder> {

    private OnItemClickListener onItemClickListener;

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final CoCoinUtil coCoinUtil;
    private List<CoCoinRecord> coCoinRecords;

    public RecordCheckDialogRecyclerViewAdapter(
            Context context,
            CoCoinUtil coCoinUtil,
            List<CoCoinRecord> list,
            OnItemClickListener onItemClickListener
    ) {
        coCoinRecords = list;
        mContext = context;
        this.coCoinUtil = coCoinUtil;
        mLayoutInflater = LayoutInflater.from(context);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RCDviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecordCheckItemBinding binding = RecordCheckItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RCDviewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RCDviewHolder holder, int position) {
        holder.binding.imageView.setImageResource(
                coCoinUtil.getTagIcon(coCoinRecords.get(holder.getAdapterPosition()).getTag()));
        holder.binding.date.setText(coCoinRecords.get(holder.getAdapterPosition()).getCalendarString(coCoinUtil));
        holder.binding.money.setText(String.valueOf((int) coCoinRecords.get(holder.getAdapterPosition()).getMoney()));
        holder.binding.money.setTextColor(
                coCoinUtil.getTagColorResource(RecordManager.TAGS.get(coCoinRecords.get(holder.getAdapterPosition()).getTag()).getId()));
        holder.binding.index.setText((holder.getAdapterPosition() + 1) + "");
        holder.binding.remark.setText(coCoinRecords.get(holder.getAdapterPosition()).getRemark());

        holder.binding.materialRippleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, holder.getAdapterPosition());
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

    public static class RCDviewHolder extends RecyclerView.ViewHolder {

        public RecordCheckItemBinding binding;

        RCDviewHolder(RecordCheckItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }
}