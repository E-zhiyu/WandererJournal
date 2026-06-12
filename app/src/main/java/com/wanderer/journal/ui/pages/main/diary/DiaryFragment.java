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
import com.wanderer.journal.auxiliary.interfaces.RecyclerViewScrollListener;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.composite.DiaryWithSummaryUiModel;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.databinding.FragmentDiaryBinding;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.ui.pages.DiaryReadActivity;
import com.wanderer.journal.ui.pages.WriteActivity;

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
        //添加 FAB
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFab);
        binding.addFab.setOnClickListener(view -> {
            Intent skip2DiaryContent = new Intent(requireContext(), WriteActivity.class);
            startActivity(skip2DiaryContent);
        });
        binding.addFab.setOnLongClickListener(view -> {
            DateTimePickerHelper.selectDate(
                    LocalDate.now(),
                    getParentFragmentManager(),
                    selection -> {
                        LocalDate date = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String dateStr = date.format(formatter);

                        //跳转到写日记界面并传递选择的日期
                        Intent skip2DiaryContent = new Intent(requireContext(), WriteActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(KeyStrings.WRITE_DIARY_DATE.getS(), dateStr);
                        skip2DiaryContent.putExtras(bundle);
                        startActivity(skip2DiaryContent);
                    }
            );
            return true;
        });

        //日期跳转按钮
        binding.dateSkipBtn.setOnClickListener(view -> DateTimePickerHelper.selectDate(
                null,
                getParentFragmentManager(),
                "选择跳转到的日期",
                selection -> {
                    LocalDate selectedDate = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                    DiaryDao dao = DiaryDatabase.getInstance(requireContext()).diaryDao();
                    disposable.add(dao.getDiaryCountBeforeDateSingle(selectedDate)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    count -> scrollToTargetPosition(count, selectedDate),
                                    e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                            )
                    );
                }
        ));

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
     * 跳转到指定位置
     *
     * @param targetPosition 目标下标，实际为大于目标日期的日记数量
     * @param targetDate     希望跳转到的日期
     */
    private void scrollToTargetPosition(int targetPosition, LocalDate targetDate) {
        //判断位置是否在有效范围内
        RecyclerView.Adapter<?> adapter = binding.diaryRecycler.getAdapter();
        if (adapter == null || targetPosition >= adapter.getItemCount()) {
            targetPosition -= 1;    //防止超出范围
            Toast.makeText(requireContext(), "已跳转至最早的日记", Toast.LENGTH_SHORT).show();
        } else if (targetPosition == 0) {
            Toast.makeText(requireContext(), "已跳转至最晚的日记", Toast.LENGTH_SHORT).show();
        } else {
            //判断跳转到的位置是否是目标日期
            if (adapter instanceof DiaryAdapter) {
                DiaryWithSummaryUiModel diaryModel = ((DiaryAdapter) adapter).getCurrentList().get(targetPosition);
                LocalDate exactDate = diaryModel.getDiary().getDiaryDate();
                if (!exactDate.isEqual(targetDate)) {
                    Toast.makeText(requireContext(), "未找到日记，已跳转至相邻日记", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //获取布局管理器
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.diaryRecycler.getLayoutManager();

        //滚动列表视图
        AppearanceAnimationHelper.scrollRecycler(
                binding.diaryRecycler,
                layoutManager,
                targetPosition,
                15,
                0,
                new RecyclerViewScrollListener() {
                    @Override
                    public void onSucceed() {
                    }

                    @Override
                    public void onFailed(String errMessage) {
                        Toast.makeText(requireContext(), errMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
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
                .setPositiveButton("确定", (dialogInterface, i) -> disposable.add(DiaryService.deleteDiaryAndParagraphMedias(diary, requireContext())
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
                ))
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