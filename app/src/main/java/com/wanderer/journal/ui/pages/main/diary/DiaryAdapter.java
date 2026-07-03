package com.wanderer.journal.ui.pages.main.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.auxiliary.classes.text.RoleRefTextRule;
import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnLongClickListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.ViewHolderListener;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.DiaryWithSummaryUiModel;
import com.wanderer.journal.databinding.ViewHolderDiaryListBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.text.ParagraphTextConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DiaryAdapter extends ListAdapter<DiaryWithSummaryUiModel, DiaryAdapter.ViewHolderDiary> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");
    private final AdapterOnClickListener<DiaryEntity> clickListener;
    private final AdapterOnLongClickListener<DiaryEntity> longClickListener;
    private static final DiffUtil.ItemCallback<DiaryWithSummaryUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {

        @Override
        public boolean areItemsTheSame(@NonNull DiaryWithSummaryUiModel oldItem, @NonNull DiaryWithSummaryUiModel newItem) {
            return oldItem.getDiary().getDiaryId() == newItem.getDiary().getDiaryId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryWithSummaryUiModel oldItem, @NonNull DiaryWithSummaryUiModel newItem) {
            DiaryEntity oldDiary = oldItem.getDiary();
            DiaryEntity newDiary = newItem.getDiary();

            return oldDiary.getDiaryDate().isEqual(newDiary.getDiaryDate()) &&
                    oldItem.getParagraphCount() == newItem.getParagraphCount() &&
                    oldItem.getParagraphFragment().equals(newItem.getParagraphFragment());
        }
    };

    public DiaryAdapter(
            AdapterOnClickListener<DiaryEntity> clickListener,
            AdapterOnLongClickListener<DiaryEntity> longClickListener
    ) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        //注册数据变更监听器，用于自动更新圆角
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                // 如果在顶部插入了数据，通知原先的第一项（现在的第 itemCount 项）更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(itemCount);
                }

                // 如果在末尾追加了数据，通知原先的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(positionStart - 1);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                // 如果在顶部删除了数据，通知现在的第一项更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(0);
                }

                // 如果在末尾删除了数据，通知现在的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(getItemCount() - 1);
                }
            }
        });
    }

    public static class ViewHolderDiary extends RecyclerView.ViewHolder {
        ViewHolderDiaryListBinding binding;

        public ViewHolderDiary(@NonNull ViewHolderDiaryListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view ->
                    listener.onClick(getBindingAdapterPosition(), binding.getRoot())
            );

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    @NonNull
    @Override
    public ViewHolderDiary onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderDiaryListBinding binding = ViewHolderDiaryListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolderDiary(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int position, View anchor) {
                        DiaryEntity diary = getItem(position).getDiary();
                        clickListener.onClick(diary, anchor);
                    }

                    @Override
                    public void onLongClick(int position, View view) {
                        DiaryEntity diary = getItem(position).getDiary();
                        longClickListener.onLongClick(diary, view);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDiary holder, int position) {
        DiaryWithSummaryUiModel diaryWithSummaryUiModel = getItem(position);
        Context context = holder.itemView.getContext();

        //日期
        LocalDate date = diaryWithSummaryUiModel.getDiary().getDiaryDate();
        holder.binding.dateText.setText(date.format(DATE_TIME_FORMATTER));

        //片段摘要
        String paragraphFragment = diaryWithSummaryUiModel.getParagraphFragment();
        CharSequence richText = ParagraphTextConverter.hierarchic(
                context,
                null,
                paragraphFragment,
                new RoleRefTextRule() {
                    @Override
                    public void onClick(String clickData) {
                    }
                }
        );
        holder.binding.contentPreviewText.setText(richText);

        //片段数量
        int paragraphCount = diaryWithSummaryUiModel.getParagraphCount();
        String tip = "×" + paragraphCount;
        holder.binding.paragraphCountText.setText(tip);

        //设置圆角
        AppearanceHelper.setRecyclerItemRadius(holder.binding.getRoot(), getItemCount(), holder.getBindingAdapterPosition());
    }
}
