package com.wanderer.journal.ui.others.selections.paragraph;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphUiModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphPagingAdapter;

import java.util.List;
import java.util.Objects;

public class ParagraphKeyProvider extends ItemKeyProvider<Long> {
    private final ParagraphPagingAdapter adapter;

    public ParagraphKeyProvider(ParagraphPagingAdapter adapter) {
        super(SCOPE_MAPPED);
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        // 从 PagingDataAdapter 的快照缓存中安全获取指定位置的实体 ID
        if (position >= 0 && position < adapter.getItemCount()) {
            ParagraphUiModel item = adapter.peek(position); // 使用 peek 不会触发懒加载
            if ((item instanceof ParagraphUiModel.Item)) {
                return ((ParagraphUiModel.Item) item).model.getParagraph().getParagraphId(); // 返回你的唯一 ID
            } else if ((item instanceof ParagraphUiModel.Separator)) {
                Long timeMillis = DateTimeConverter.fromLocalDate(((ParagraphUiModel.Separator) item).date);

                //永远返回负数时间戳
                if (timeMillis != null && timeMillis > 0) {
                    return -timeMillis;
                } else {
                    return timeMillis;
                }
            }
        }
        return null;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        // 反向查找：根据 ID 找到当前的 Position
        List<ParagraphUiModel> snapshot = adapter.snapshot().getItems();
        for (int i = 0; i < snapshot.size(); i++) {
            ParagraphUiModel item = snapshot.get(i);
            if (key >= 0 &&
                    (item instanceof ParagraphUiModel.Item) &&
                    ((ParagraphUiModel.Item) item).model.getParagraph().getParagraphId() == key
            ) {
                return i;
            } else if (key < 0 &&
                    (item instanceof ParagraphUiModel.Separator) &&
                    Objects.equals(DateTimeConverter.fromLocalDate(((ParagraphUiModel.Separator) item).date), key)
            ) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
}
