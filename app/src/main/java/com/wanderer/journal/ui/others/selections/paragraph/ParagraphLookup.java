package com.wanderer.journal.ui.others.selections.paragraph;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphPagingAdapter;

public class ParagraphLookup extends ItemDetailsLookup<Long> {
    private final RecyclerView recyclerView;

    public ParagraphLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public @Nullable ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof ParagraphPagingAdapter.ParagraphViewHolder) {
                return ((ParagraphPagingAdapter.ParagraphViewHolder) holder).getItemDetails();
            } else if (holder instanceof ParagraphPagingAdapter.DateSeparatorViewHolder) {
                return ((ParagraphPagingAdapter.DateSeparatorViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
