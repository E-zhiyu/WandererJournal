package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.databinding.BottomSheetParagraphEditBinding;

public class ParagraphContentModifySheet extends BaseBottomSheetDialogFragment {
    private BottomSheetParagraphEditBinding binding;    //绑定的XML布局
    private final String originContent;                 //原始内容
    private final SendListener sendListener;            //发送监听

    public interface SendListener {
        /**
         * 新内容发送回调
         *
         * @param newContent 文本框中的新内容
         */
        void send(String newContent);
    }

    public ParagraphContentModifySheet(SendListener sendListener, String originContent) {
        this.originContent = originContent;
        this.sendListener = sendListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetParagraphEditBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //原始文本文本框
        binding.originText.setText(originContent);

        //文本框内容
        binding.contentInputEditText.setText(originContent);

        //发送按钮
        binding.sendBtn.setOnClickListener(view -> {
            dismiss();

            String newContent = String.valueOf(binding.contentInputEditText.getText());
            sendListener.send(newContent);
        });
    }
}
