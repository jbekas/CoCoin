package com.jbekas.cocoin.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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
public class EditRemarkFragment extends Fragment {

    @Inject
    protected CoCoinUtil coCoinUtil;

    private int fragmentPosition;
    private int tagId = -1;

    public MaterialEditText editView;

    private View mView;

    Activity activity;

    static public EditRemarkFragment newInstance(int position, int type) {
        EditRemarkFragment fragment = new EditRemarkFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("type", type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.edit_remark_fragment, container, false);
        editView = (MaterialEditText)mView.findViewById(R.id.remark);

        if (getArguments().getInt("type") == CoCoinFragmentManager.MAIN_ACTIVITY_FRAGMENT) {
            CoCoinFragmentManager.mainActivityEditRemarkFragment = this;
        } else if (getArguments().getInt("type") == CoCoinFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT) {
            CoCoinFragmentManager.editRecordActivityEditRemarkFragment = this;
        }

        boolean shouldChange
                = SettingManager.getInstance().getIsMonthLimit()
                && SettingManager.getInstance().getIsColorRemind()
                && RecordManager.getCurrentMonthExpense()
                >= SettingManager.getInstance().getMonthWarning();

        setEditColor(shouldChange);

        if (getArguments().getInt("type") == CoCoinFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT
                && coCoinUtil.editRecordPosition != -1) {
            CoCoinFragmentManager.editRecordActivityEditRemarkFragment
                    .setRemark(RecordManager.SELECTED_RECORDS.get(coCoinUtil.editRecordPosition).getRemark());
            CoCoinFragmentManager.editRecordActivityEditRemarkFragment.setLastSelection();
        }

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

    public void setLastSelection() {
        editView.setSelection(editView.getText().length());
    }

    public void editRequestFocus() {
        editView.requestFocus();
        InputMethodManager keyboard = (InputMethodManager)
                CoCoinApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editView, InputMethodManager.SHOW_IMPLICIT);
    }

    public void setEditColor(boolean shouldChange) {
        if (shouldChange) {
            editView.setTextColor(SettingManager.getInstance().getRemindColor());
            editView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            editView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
        } else {
            editView.setTextColor(coCoinUtil.MY_BLUE);
            editView.setPrimaryColor(coCoinUtil.MY_BLUE);
            editView.setHelperTextColor(coCoinUtil.MY_BLUE);
        }
    }

    public String getRemark() {
        return editView.getText().toString();
    }

    public void setRemark(String string) {
        editView.setText(string);
    }

}
