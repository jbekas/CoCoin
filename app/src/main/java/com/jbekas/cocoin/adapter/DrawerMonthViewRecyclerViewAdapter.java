package com.jbekas.cocoin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jbekas.cocoin.R;
import com.jbekas.cocoin.activity.CoCoinApplication;
import com.jbekas.cocoin.model.RecordManager;
import com.jbekas.cocoin.util.CoCoinUtil;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 伟平 on 2015/10/20.
 */

public class DrawerMonthViewRecyclerViewAdapter
        extends RecyclerView.Adapter<DrawerMonthViewRecyclerViewAdapter.DMVviewHolder> {

    private ArrayList<Double> expenses;
    private ArrayList<Integer> records;
    private ArrayList<Integer> months;
    private ArrayList<Integer> years;

    private Context mContext;

    OnItemClickListener onItemClickListener;

    public DrawerMonthViewRecyclerViewAdapter(Context context) {
        mContext = context;
        expenses = new ArrayList<>();
        records = new ArrayList<>();
        months = new ArrayList<>();
        years = new ArrayList<>();

        if (RecordManager.getInstance(CoCoinApplication.getAppContext()).RECORDS.size() != 0) {

            int currentYear = RecordManager.RECORDS.
                    get(RecordManager.RECORDS.size() - 1).getCalendar().get(Calendar.YEAR);
            int currentMonth = RecordManager.RECORDS.
                    get(RecordManager.RECORDS.size() - 1).getCalendar().get(Calendar.MONTH);
            double currentMonthSum = 0;
            int currentMonthRecord = 0;

            for (int i = RecordManager.RECORDS.size() - 1; i >= 0; i--) {
                if (RecordManager.RECORDS.get(i).getCalendar().get(Calendar.YEAR) == currentYear
                        && RecordManager.RECORDS.get(i).
                        getCalendar().get(Calendar.MONTH) == currentMonth) {
                    currentMonthSum += RecordManager.RECORDS.get(i).getMoney();
                    currentMonthRecord++;
                } else {
                    expenses.add(currentMonthSum);
                    records.add(currentMonthRecord);
                    years.add(currentYear);
                    months.add(currentMonth);
                    currentMonthSum = RecordManager.RECORDS.get(i).getMoney();
                    currentMonthRecord = 1;
                    currentYear = RecordManager.RECORDS.get(i).getCalendar().get(Calendar.YEAR);
                    currentMonth = RecordManager.RECORDS.get(i).getCalendar().get(Calendar.MONTH);
                }
            }
            expenses.add(currentMonthSum);
            records.add(currentMonthRecord);
            years.add(currentYear);
            months.add(currentMonth);

        }


    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    @Override
    public DMVviewHolder
        onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month_view_drawer, parent, false);
        return new DMVviewHolder(view) {
        };

    }

    @Override
    public void onBindViewHolder(final DMVviewHolder holder, final int position) {
        holder.month.setText(CoCoinUtil.GetMonthShort(months.get(position) + 1));
//        holder.month.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);

        holder.year.setText(years.get(position) + "");
//        holder.year.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);

        holder.sum.setText(CoCoinUtil.getInstance().GetInRecords(records.get(position)));
//        holder.sum.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);

        holder.money.setText(CoCoinUtil.getInstance().GetInMoney((int) (double) (expenses.get(position))));
//        holder.money.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
    }

    public class DMVviewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
        @BindView(R.id.month)
        TextView month;
        @BindView(R.id.year)
        TextView year;
        @BindView(R.id.money)
        TextView money;
        @BindView(R.id.sum)
        TextView sum;

        DMVviewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

}
