package com.wanderer.journal.ui.pages.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.wanderer.journal.R;
import com.wanderer.journal.databinding.ActivityMainBinding;
import com.wanderer.journal.ui.others.adapters.FragmentPagerAdapter;
import com.wanderer.journal.ui.pages.main.diary.DiaryFragment;
import com.wanderer.journal.ui.pages.main.home.HomeFragment;
import com.wanderer.journal.ui.pages.main.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;    //绑定的XML布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        initViews();

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.viewPager2, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //翻页视图
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new DiaryFragment());
        fragmentList.add(new HomeFragment());
        fragmentList.add(new SettingsFragment());
        initViewPager2(fragmentList);
        binding.viewPager2.setCurrentItem(1, false);

        //底部导航栏
        binding.bottomNavi.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_diary) {
                binding.viewPager2.setCurrentItem(0);
                return true;
            } else if (id == R.id.nav_home) {
                binding.viewPager2.setCurrentItem(1);
                return true;
            } else if (id == R.id.nav_settings) {
                binding.viewPager2.setCurrentItem(2);
                return true;
            }

            return false;
        });
    }

    /**
     * 初始化翻页视图
     *
     * @param fragmentList 页面Fragment列表
     */
    private void initViewPager2(List<Fragment> fragmentList) {
        ViewPager2 viewPager2 = binding.viewPager2;
        FragmentPagerAdapter viewPagerAdapter = new FragmentPagerAdapter(this, fragmentList);
        viewPager2.setAdapter(viewPagerAdapter);

        //ViewPager 页面切换监听
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 更新底部导航栏选中状态
                binding.bottomNavi.getMenu().getItem(position).setChecked(true);
            }
        });
        viewPager2.setOffscreenPageLimit(2);    //设置保留邻近Fragment
    }
}