package com.jbekas.cocoin.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jbekas.cocoin.R;
import com.jbekas.cocoin.activity.CoCoinApplication;
import com.jbekas.cocoin.db.RecordManager;
import com.jbekas.cocoin.model.SettingManager;
import com.jbekas.cocoin.util.CoCoinUtil;
import com.rengwuxian.materialedittext.MaterialEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditRecordFragment extends Fragment {

    @Inject
    protected CoCoinUtil coCoinUtil;

    private int fragmentPosition;
    private int tagId = -1;

    public MaterialEditText editView;
    public MaterialEditText remarkEditView;

    public ImageView tagImage;
    public TextView tagName;

    private View mView;

    Activity activity;

    static public EditRecordFragment newInstance(int position) {
        EditRecordFragment fragment = new EditRecordFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.edit_money_fragment, container, false);

        fragmentPosition = getArguments().getInt("position");
        remarkEditView = (MaterialEditText) mView.findViewById(R.id.remark);
        editView = (MaterialEditText) mView.findViewById(R.id.money);
        tagImage = (ImageView) mView.findViewById(R.id.tag_image);
        tagName = (TextView) mView.findViewById(R.id.tag_name);
//        tagName.setTypeface(CoCoinUtil.typefaceLatoLight);

        if (fragmentPosition == 0) {
//            editView.setTypeface(CoCoinUtil.typefaceLatoHairline);
            editView.setText("" + (int) RecordManager.RECORDS.get(coCoinUtil.editRecordPosition).getMoney());
            editView.requestFocus();
            editView.setHelperText(coCoinUtil.FLOATINGLABELS[editView.getText().toString().length()]);

            tagId = RecordManager.RECORDS.get(coCoinUtil.editRecordPosition).getTag();
            tagName.setText(coCoinUtil.getTagName(tagId));
            tagImage.setImageResource(coCoinUtil.getTagIcon(tagId));

            remarkEditView.setVisibility(View.GONE);
        } else {
//            remarkEditView.setTypeface(CoCoinUtil.GetTypeface());

            remarkEditView.setText(RecordManager.RECORDS.get(coCoinUtil.editRecordPosition).getRemark());
            int pos = remarkEditView.getText().length();
            remarkEditView.setSelection(pos);

            editView.setVisibility(View.GONE);
        }

        boolean shouldChange
                = SettingManager.getInstance().getIsMonthLimit()
                && SettingManager.getInstance().getIsColorRemind()
                && RecordManager.getCurrentMonthExpense()
                >= SettingManager.getInstance().getMonthWarning();

        setEditColor(shouldChange);

        return mView;
    }

    public interface OnTagItemSelectedListener {
        void onTagItemPicked(int position);
    }

    public void updateTags() {

    }

    public int getTagId() {
        return tagId;
    }

    public void setTag(int p) {
        tagId = RecordManager.TAGS.get(p).getId();
        tagName.setText(coCoinUtil.getTagName(RecordManager.TAGS.get(p).getId()));
        tagImage.setImageResource(coCoinUtil.getTagIcon(RecordManager.TAGS.get(p).getId()));
    }

    public String getNumberText() {
        return editView.getText().toString();
    }

    public void setNumberText(String string) {
        editView.setText(string);
    }

    public String getHelpText() {
        return editView.getHelperText();
    }

    public void setHelpText(String string) {
        editView.setHelperText(string);
    }

    public void editRequestFocus() {
        if (fragmentPosition == 0) {
            editView.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    CoCoinApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        } else {
            remarkEditView.requestFocus();
            InputMethodManager keyboard = (InputMethodManager)
                    CoCoinApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(remarkEditView, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    public void setEditColor(boolean shouldChange) {
        if (shouldChange) {
            editView.setTextColor(SettingManager.getInstance().getRemindColor());
            editView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            editView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
            remarkEditView.setTextColor(SettingManager.getInstance().getRemindColor());
            remarkEditView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            remarkEditView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
        } else {
            editView.setTextColor(coCoinUtil.MY_BLUE);
            editView.setPrimaryColor(coCoinUtil.MY_BLUE);
            editView.setHelperTextColor(coCoinUtil.MY_BLUE);
            remarkEditView.setTextColor(coCoinUtil.MY_BLUE);
            remarkEditView.setPrimaryColor(coCoinUtil.MY_BLUE);
            remarkEditView.setHelperTextColor(coCoinUtil.MY_BLUE);
        }
    }

    public void setTagName(String name) {
        tagName.setText(name);
    }

    public void setTagImage(int resource) {
        tagImage.setImageResource(resource);
    }

    public String getRemark() {
        return remarkEditView.getText().toString();
    }

    public void setRemark(String string) {
        remarkEditView.setText(string);
    }

}
