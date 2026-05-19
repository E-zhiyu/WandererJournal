package com.wanderer.journal.ui.pages.main.diary;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.databinding.FragmentDiaryBinding;
import com.wanderer.journal.enums.KeyStrings;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.ui.pages.read.DiaryReadActivity;
import com.wanderer.journal.ui.pages.write.WriteActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryFragment extends Fragment {
    private FragmentDiaryBinding binding;   //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiaryBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //添加FAB
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFab);
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.diaryRecycler, binding.addFab);
        binding.addFab.setOnClickListener(view -> {
            Intent skip2DiaryContent = new Intent(requireContext(), WriteActivity.class);
            startActivity(skip2DiaryContent);
        });

        //日期跳转FAB
        AppearanceAnimationHelper.attachMorphAnimation(binding.dateSkipFab);
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.diaryRecycler, binding.dateSkipFab);
        binding.dateSkipFab.setOnClickListener(view -> showDateScrollDatePicker());

        //日记列表
        DiaryAdapter adapter = new DiaryAdapter(
                diary -> {
                    Intent skip2Read = new Intent(requireContext(), DiaryReadActivity.class);
                    Bundle bundle = new Bundle();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String date = diary.getDiaryDate().format(formatter);
                    bundle.putString(KeyStrings.INIT_DATE.getS(), date);

                    skip2Read.putExtras(bundle);
                    startActivity(skip2Read);
                },
                this::showDiaryPopupMenu
        );
        binding.diaryRecycler.setAdapter(adapter);
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        disposable.add(db.diaryDao().getAllDiariesFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        diaryList -> {
                            adapter.submitList(diaryList);
                            if (diaryList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }
                        },
                        e -> {
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                            Log.e(LogTags.DIARY_FRAGMENT.n(), "日记数据库读取失败");
                        }
                )
        );
    }

    /**
     * 跳转到指定日期的日记
     */
    private void showDateScrollDatePicker() {
        DateTimePickerHelper.selectDate(
                null,
                getParentFragmentManager(),
                selection -> {
                    LocalDate selectedDate = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                    DiaryDao dao = DiaryDatabase.getInstance(requireContext()).diaryDao();
                    disposable.add(dao.getDiaryCountSingle(selectedDate)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    this::scrollToTargetPosition,
                                    e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                            )
                    );
                }
        );
    }

    /**
     * 跳转到指定位置
     *
     * @param targetPosition 目标下标
     */
    private void scrollToTargetPosition(int targetPosition) {
        //判断位置是否在有效范围内
        RecyclerView.Adapter<?> adapter = binding.diaryRecycler.getAdapter();
        if (adapter == null || targetPosition >= adapter.getItemCount()) {
            Toast.makeText(requireContext(), "所选日期已超过最早的日期", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取布局管理器
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.diaryRecycler.getLayoutManager();
        if (layoutManager == null) {
            Toast.makeText(requireContext(), "无法获取布局管理器", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前屏幕上第一个完全可见的条目位置
        int firstVisiblePos = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisiblePos = layoutManager.findLastVisibleItemPosition();
        if (firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION) {
            //处理没有可见视图的情况
            return;
        } else if (targetPosition >= firstVisiblePos && targetPosition <= lastVisiblePos) {
            //处理不需要滚动的情况
            RecyclerView.ViewHolder viewHolder = binding.diaryRecycler.findViewHolderForAdapterPosition(targetPosition);
            if (viewHolder != null) {
                AppearanceAnimationHelper.blink(viewHolder.itemView);
            }
            return;
        }

        //根据距离远近采用不同的滚动方式
        int distance = Math.abs(targetPosition - firstVisiblePos);
        final int DISTANCE_THRESHOLD = 20;
        if (distance > DISTANCE_THRESHOLD) {
            //瞬间滚动
            layoutManager.scrollToPositionWithOffset(targetPosition, 0);

            //播放闪烁动画
            binding.diaryRecycler.post(() -> {
                RecyclerView.ViewHolder viewHolder = binding.diaryRecycler.findViewHolderForAdapterPosition(targetPosition);
                if (viewHolder != null) {
                    AppearanceAnimationHelper.blink(viewHolder.itemView);
                }
            });
        } else {
            //添加滚动监听器并平滑滚动
            binding.diaryRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    // 当滚动完全停止 (IDLE) 时再闪烁
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        RecyclerView.ViewHolder viewHolder = binding.diaryRecycler.findViewHolderForAdapterPosition(targetPosition);
                        if (viewHolder != null) {
                            AppearanceAnimationHelper.blink(viewHolder.itemView);
                        }
                        recyclerView.removeOnScrollListener(this);  //移除滚动监听器防止用户滚动时触发闪烁
                    }
                }
            });
            binding.diaryRecycler.smoothScrollToPosition(targetPosition);
        }
    }

    /**
     * 删除日记
     *
     * @param diary 待删除的日记实例
     */
    private void deleteDiary(DiaryEntity diary) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_diary)
                .setMessage("此操作将删除该日记的所有内容，确认继续吗？")
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
                    DiaryDao dao = db.diaryDao();

                    disposable.add(dao.deleteDiaryCompletable(diary)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                                        Toast.makeText(requireContext(), "日记删除成功", Toast.LENGTH_SHORT).show();
                                        Log.i(LogTags.DIARY_FRAGMENT.n(), "日记删除成功");
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(requireContext(), throwable);
                                        Log.e(LogTags.DIARY_FRAGMENT.n(), "日记删除失败");
                                    })
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示日记日期选择器
     *
     * @param diary 需要修改日期的日记
     */
    private void showDiaryDatePicker(@NonNull DiaryEntity diary) {
        DateTimePickerHelper.selectDate(
                diary.getDiaryDate(),
                getParentFragmentManager(),
                selection -> {
                    LocalDate targetDate = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                    DiaryDao diaryDao = DiaryDatabase.getInstance(requireContext()).diaryDao();
                    disposable.add(diaryDao.getDiarySingleByDate(targetDate)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(optional -> {
                                if (optional.isPresent()) {
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle(R.string.modify_date)
                                            .setMessage("该日期已有日记，是否覆盖该日记？")
                                            .setPositiveButton("覆盖", (dialogInterface, i) ->
                                                    modifyDiaryDate(diary, targetDate)
                                            )
                                            .setNegativeButton("取消", null)
                                            .show();
                                } else {
                                    modifyDiaryDate(diary, targetDate);
                                }
                            })
                    );
                }
        );
    }

    /**
     * 修改日记的日期
     *
     * @param diary      待修改日期的日记
     * @param targetDate 修改后的日期
     */
    private void modifyDiaryDate(@NonNull DiaryEntity diary, LocalDate targetDate) {
        disposable.add(DiaryService.updateDiaryDate(diary.getDiaryId(), targetDate, requireContext())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Toast.makeText(requireContext(), "日期更新完毕", Toast.LENGTH_SHORT).show(),
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }

    /**
     * 显示日记编辑下拉菜单
     *
     * @param diary 需要修改的日记
     * @param view  下拉菜单绑定的视图
     */
    private void showDiaryPopupMenu(DiaryEntity diary, View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_diary_edit, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_diary) {
                deleteDiary(diary);
                return true;
            } else if (item.getItemId() == R.id.action_modify_date) {
                showDiaryDatePicker(diary);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}