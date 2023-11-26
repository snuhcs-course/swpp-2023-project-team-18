package snu.swpp.moment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import snu.swpp.moment.databinding.ActivityMainBinding;
import snu.swpp.moment.utils.AnimationProvider;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
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
            } else if(destination.getId() == R.id.MonthView){
                infoButton.setVisibility(View.VISIBLE);
                // 그외에 MonthView는 fragment 안에서 별도로 설정
            } else if (destination.getId() == R.id.UserInfoView) {
                setToolbarTitle("내 정보");
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
        Dialog helpDialog = createHelpDialog(currentDestinationId);

        setupDialogContentView(helpDialog, currentDestinationId);
        applyTextStyles(helpDialog);
        setupCloseButton(helpDialog);
        animateDialogContent(helpDialog);
        setHelpDialogSize(helpDialog);

        helpDialog.show();
    }

    private Dialog createHelpDialog(int currentDestinationId) {
        Dialog helpDialog = new Dialog(this);
        helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return helpDialog;
    }

    // Note : switch-case로 하면 R.id.XXX 가 컴파일타임에 상수로 결정되지 않아서 문제가 생기는 것 같음
    private void setupDialogContentView(Dialog dialog, int destinationId) {
        if (destinationId == R.id.WriteView) {
            dialog.setContentView(R.layout.user_guide_writeview);
        } else if (destinationId == R.id.MonthView) {
            dialog.setContentView(R.layout.user_guide_monthview);
        } else if (destinationId == R.id.StatView) {
            dialog.setContentView(R.layout.user_guide_statview);
            setupStatViewDialog(dialog);
        } else if (destinationId == R.id.SearchView) {
            dialog.setContentView(R.layout.user_guide_searchview);
        }
    }

    private void setupStatViewDialog(Dialog dialog) {
        ImageView hashCloudGif = dialog.findViewById(R.id.user_guide_statview_image3);
        if (hashCloudGif != null) {
            Glide.with(this).load(R.drawable.gif_user_guide_hashcloud).into(hashCloudGif);
        }
    }

    private void applyTextStyles(Dialog dialog) {
        HashMap<Integer, String> textViewMap = getBoldTextHashMap();
        for (Map.Entry<Integer, String> entry : textViewMap.entrySet()) {
            TextView textView = dialog.findViewById(entry.getKey());
            if (textView != null) {
                applyBoldUnderlineSpan(textView, entry.getValue());
            }
        }
    }

    private HashMap<Integer, String> getBoldTextHashMap() {
        HashMap<Integer, String> textViewMap = new HashMap<>();

        // WirteView
        textViewMap.put(R.id.user_guide_writeview_explanation1, "하루쓰기");
        textViewMap.put(R.id.user_guide_writeview_explanation2, "넛지");
        textViewMap.put(R.id.user_guide_writeview_explanation3, "새 모먼트 추가하기");
        textViewMap.put(R.id.user_guide_writeview_explanation4, "하루 마무리하기");
        textViewMap.put(R.id.user_guide_writeview_explanation5, "AI에게 부탁하기");

        // MonthView
        textViewMap.put(R.id.user_guide_monthview_explanation1, "한달보기");

        // StatView
        textViewMap.put(R.id.user_guide_statview_explanation1, "돌아보기");

        // SearchView
        textViewMap.put(R.id.user_guide_searchview_explanation1, "찾아보기");

        return textViewMap;
    }

    private void applyBoldUnderlineSpan(TextView textView, String boldUnderlinePart) {
        String text = textView.getText().toString();
        int start = text.indexOf(boldUnderlinePart);
        if (start != -1) {
            int end = start + boldUnderlinePart.length();
            SpannableStringBuilder spannable = new SpannableStringBuilder(text);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            textView.setText(spannable);
        }
    }

    private void setupCloseButton(Dialog dialog) {
        Button closeButton = dialog.findViewById(R.id.btn_close);
        if (closeButton != null) {
            closeButton.setActivated(true);
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private void animateDialogContent(Dialog dialog) {
        View dialogContent = dialog.findViewById(android.R.id.content);
        if (dialogContent != null) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            dialogContent.startAnimation(fadeInAnimation);
        }
    }

    private void setHelpDialogSize(Dialog helpDialog){
        // Dialog 사이즈 설정
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = helpDialog.getWindow();
        if (window != null) {
            lp.copyFrom(window.getAttributes());
            // Dialog의 너비와 높이를 화면의 80%로 설정
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int dialogWidth = (int)(displayMetrics.widthPixels * 0.9);
            int dialogHeight = (int)(displayMetrics.heightPixels * 0.8);
            lp.width = dialogWidth;
            lp.height = dialogHeight;
            window.setAttributes(lp);
        }
    }
}