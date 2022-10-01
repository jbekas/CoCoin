package com.jbekas.cocoin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbekas.cocoin.R;
import com.jbekas.cocoin.fragment.ExpenseReportsFragment;
import com.jbekas.cocoin.util.CoCoinUtil;

import java.util.ArrayList;

public class ReportTagAdapter extends BaseAdapter {

    private CoCoinUtil coCoinUtil;
    private ArrayList<double[]> tagExpense;

    public ReportTagAdapter(
            CoCoinUtil coCoinUtil,
            ArrayList<double[]> tagExpense
    ) {
        this.coCoinUtil = coCoinUtil;
        this.tagExpense = tagExpense;
    }

    @Override
    public int getCount() {
        return min(tagExpense.size() - 1, ExpenseReportsFragment.MAX_TAG_EXPENSE);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_tag, null);

        ImageView icon = (ImageView)convertView.findViewById(R.id.icon);
        TextView name = (TextView)convertView.findViewById(R.id.tag_name);
        TextView expense = (TextView)convertView.findViewById(R.id.tag_expense);
        TextView records = (TextView)convertView.findViewById(R.id.tag_sum);

//        name.setTypeface(coCoinUtil.typefaceLatoLight);
//        expense.setTypeface(coCoinUtil.typefaceLatoLight);
//        records.setTypeface(coCoinUtil.typefaceLatoLight);

        icon.setImageDrawable(coCoinUtil.getTagIconDrawable((int)tagExpense.get(position + 1)[2]));
        name.setText(coCoinUtil.getTagName((int)tagExpense.get(position + 1)[2]) + coCoinUtil.getPurePercentString(tagExpense.get(position + 1)[1] * 100));
        expense.setText(coCoinUtil.getInMoney((int)tagExpense.get(position + 1)[0]));
        records.setText(coCoinUtil.getInRecords((int)tagExpense.get(position + 1)[3]));

        return convertView;
    }

    private int min(int a, int b) {
        return (a < b ? a : b);
    }
}
