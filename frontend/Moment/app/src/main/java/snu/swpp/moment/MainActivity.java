package snu.swpp.moment;

import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import snu.swpp.moment.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    //private AuthenticationRepository authenticationRepository;

    // Toolbar text view - 날짜넣기위해
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //followied line is to set each fragment's label as null string not to show in app bar
        setTitle("");

        // from now on, we can handle app bar title through followed line
        //toolbarTitle = findViewById(R.id.text_title);
        toolbarTitle = binding.appBarMain.textTitle;

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.WriteView, R.id.MonthView, R.id.StatView, R.id.SearchView, R.id.UserInfoView,
            R.id.LogoutView)
            .setOpenableLayout(drawer)
            .build();
        NavController navController = Navigation.findNavController(this,
            R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Wirte View의 타이틀이 항상 날짜로 나오도록 NavController의 타이틀 업데이트 비활성화:
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        System.out.println("#Debug Mainactivity date ok");
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.WriteView) {
                setToolbarTitle(currentDate);
            } else if (destination.getId() == R.id.MonthView) {
                // MonthViewFragment에서 설정해줌
            } else if (destination.getId() == R.id.StatView) {
                setToolbarTitle("돌아보기");
            } else if (destination.getId() == R.id.SearchView) {
                setToolbarTitle("찾아보기");
            } else if (destination.getId() == R.id.UserInfoView) {
                setToolbarTitle("내 정보");
            } else if (destination.getId() == R.id.LogoutView) {
                setToolbarTitle("로그아웃");
            }
        });

        // MainActivity (frament 왔다갔다하는) 에서 뒤로가기 버튼이 눌린경우 로그인 화면으로 돌아가지 않도록
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
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
        toolbarTitle.setText(title);
    }
}