package snu.swpp.moment;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import snu.swpp.moment.databinding.ActivityMainBinding;
import snu.swpp.moment.utils.userguide_dialog.CustomDialog;
import snu.swpp.moment.utils.userguide_dialog.CustomDialogFactory;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;
    private TextView toolbarTitle;
    // 도움 버튼
    private Button infoButton;
    private final MutableLiveData<LocalDate> writeDestinationDate = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "created");
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set each fragment's label as null string not to show in app bar
        setTitle("");

        toolbarTitle = binding.appBarMain.textTitle;
        infoButton = findViewById(R.id.info_button);
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.WriteView, R.id.MonthView, R.id.StatView, R.id.SearchView, R.id.UserInfoView)
            .setOpenableLayout(drawer)
            .build();
        navController = Navigation.findNavController(this,
            R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // View가 완전히 생성된 후에 크기를 가져옵니다.
        toolbarTitle.post(new Runnable() {
            @Override
            public void run() {
                // AppBar의 높이
                int appBarHeight = toolbarTitle.getHeight();
                // AppBar 높이의 80% 계산
                int buttonSize = (int) (appBarHeight);
                // 버튼 참조
                Button infoButton = findViewById(R.id.info_button);

                // 버튼 크기 설정
                ViewGroup.LayoutParams params = infoButton.getLayoutParams();
                params.width = buttonSize; // 너비를 AppBar 높이의 80%로 설정
                params.height = buttonSize; // 높이를 AppBar 높이의 80%로 설정
                infoButton.setLayoutParams(params);
            }
        });

        //Wirte View의 타이틀이 항상 날짜로 나오도록 NavController의 타이틀 업데이트 비활성화:
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd. E", Locale.KOREA);//다른 탭 갔다 왔을 때 요일이 잠시 영어로 보이는 문제 해결 위해 Locale을 직접 지정
        String currentDate = sdf.format(new Date());
        System.out.println("#Debug Mainactivity date ok");

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.WriteView) {
                setToolbarTitle(currentDate);
                infoButton.setVisibility(View.VISIBLE);
            } else if (destination.getId() == R.id.StatView) {
                setToolbarTitle("돌아보기");
                infoButton.setVisibility(View.VISIBLE);
            } else if (destination.getId() == R.id.SearchView) {
                setToolbarTitle("찾아보기");
                infoButton.setVisibility(View.VISIBLE);
            } else if (destination.getId() == R.id.MonthView) {
                infoButton.setVisibility(View.VISIBLE);
                // 그외에 MonthView는 fragment 안에서 별도로 설정
            } else if (destination.getId() == R.id.UserInfoView) {
                setToolbarTitle("내 정보");
                infoButton.setVisibility(View.GONE);
            }
        });

        // MainActivity에서 뒤로가기 버튼이 눌린 경우 로그인 화면으로 돌아가지 않도록
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                boolean popped = navController.popBackStack();
                if (!popped) {
                    finishAffinity();
                    System.exit(0);
                }
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelpPopup();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // StatView가 시작될 때 preload를 호출하면 앱이 해당 뷰로 이동할 준비를 할 수 있습니다.
        if (navController.getCurrentDestination().getId() == R.id.StatView) {
            Glide.with(this)
                    .load(R.drawable.user_guide_hashcloud_gif)
                    .preload();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
            R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
            || super.onSupportNavigateUp();
    }

    public void setToolbarTitle(String title) {
        if (toolbarTitle == null) {
            return;
        }
        toolbarTitle.setText(title);
    }

    public void showHamburgerButton(boolean show) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setHomeButtonEnabled(show);
        actionBar.setDisplayHomeAsUpEnabled(show);
    }

    public void navigateToWriteViewPage(LocalDate date) {
        Log.d("MainActivity", "navigateToWriteViewPage: " + date);
        navController.navigate(R.id.WriteView);
        writeDestinationDate.setValue(date);
    }

    public void observeWriteDestinationDate(Observer<LocalDate> observer) {
        Log.d("MainActivity", "observer for writeDestinationDate set");
        writeDestinationDate.observe(this, observer);
    }

    public void unobserveWriteDestinationDate() {
        Log.d("MainActivity", "observer for writeDestinationDate removed");
        writeDestinationDate.removeObservers(this);
    }

    public void resetWriteDestinationDate() {
        writeDestinationDate.setValue(null);
    }

    // Help Popup
    private void showHelpPopup() {
        int currentDestinationId = navController.getCurrentDestination().getId();
        CustomDialog helpDialog = CustomDialogFactory.createDialog(this, currentDestinationId);
        helpDialog.show();
    }
}