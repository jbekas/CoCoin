package com.jbekas.cocoin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.jbekas.cocoin.R;
import com.jbekas.cocoin.db.RecordManager;
import com.jbekas.cocoin.model.Tag;
import com.jbekas.cocoin.util.CoCoinUtil;

import java.util.ArrayList;

public class TagDraggableItemAdapter
        extends RecyclerView.Adapter<TagDraggableItemAdapter.MyViewHolder>
        implements DraggableItemAdapter<TagDraggableItemAdapter.MyViewHolder> {

    private static final String TAG = "TagDraggableItemAdapter";

    private CoCoinUtil coCoinUtil;
    private ArrayList<Tag> tags;
    private boolean changed;

    // NOTE: Make accessible with short name
    private interface Draggable extends DraggableItemConstants {
    }

    public static class MyViewHolder extends AbstractDraggableItemViewHolder {
        public FrameLayout mContainer;
        public ImageView tagImage;
        public TextView tagName;

        public MyViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            tagImage = (ImageView)v.findViewById(R.id.tag_image);
            tagName = (TextView)v.findViewById(R.id.tag_name);
        }
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public static String getTAG() {

        return TAG;
    }

    public TagDraggableItemAdapter(CoCoinUtil coCoinUtil) {

        this.coCoinUtil = coCoinUtil;

        tags = new ArrayList<>();

        for (int i = 2; i < RecordManager.TAGS.size(); i++) {
            Tag tag = RecordManager.TAGS.get(i);
            tag.setDragId(i);
            tags.add(tag);
        }

        changed = false;


        // DraggableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return tags.get(position).getDragId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.tag_grid_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();

        holder.tagImage.setImageResource(coCoinUtil.getTagIcon(tags.get(position).getId()));
        holder.tagName.setText(coCoinUtil.getTagName(tags.get(position).getId()));
//        holder.tagName.setTypeface(CoCoinUtil.typefaceLatoLight);

        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
            int bgResId;

            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.
                coCoinUtil.clearState(holder.mContainer.getForeground());
            } else if ((dragState & Draggable.STATE_FLAG_DRAGGING) != 0) {
                bgResId = R.drawable.bg_item_normal_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }
    }

    @Override
    public int getItemCount() {
        return RecordManager.TAGS.size() - 2;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {

        if (fromPosition == toPosition) {
            return;
        }

        changed = true;

        Tag tempTag = tags.remove(fromPosition);
        tags.add(toPosition, tempTag);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
        return true;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }
}
