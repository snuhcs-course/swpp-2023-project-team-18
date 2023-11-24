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
        navController = Navigation.findNavController(this,
            R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        // Set info button size
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
            } else if (destination.getId() == R.id.StatView) {
                setToolbarTitle("돌아보기");
            } else if (destination.getId() == R.id.SearchView) {
                setToolbarTitle("찾아보기");
            } else if (destination.getId() == R.id.UserInfoView) {
                setToolbarTitle("내 정보");
            } else if (destination.getId() == R.id.LogoutView) {
                setToolbarTitle("로그아웃");
            }
            // MonthView는 fragment 안에서 별도로 설정
        });


        // 도움 버튼 : frontend/P8
        Button infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 여기에서 팝업을 띄우거나 원하는 동작을 수행합니다.
                showHelpPopup();
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
        getSupportActionBar().setHomeButtonEnabled(show);
        getSupportActionBar().setDisplayHomeAsUpEnabled(show);
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

    /*
    private void showInformationPopup() {
        // 현재 NavController의 목적지 ID를 기반으로 정보를 표시합니다.
        int currentDestinationId = navController.getCurrentDestination().getId();
        String infoMessage = "";

        if (currentDestinationId == R.id.WriteView) {
            infoMessage = "하루쓰기";
        } else if (currentDestinationId == R.id.MonthView) {
            infoMessage = "한달보기";
        } else if (currentDestinationId == R.id.StatView) {
            infoMessage = "돌아보기";
        } else if (currentDestinationId == R.id.SearchView) {
            infoMessage = "찾아보기";
        } else if (currentDestinationId == R.id.UserInfoView) {
            infoMessage = "내 정보";
        } else if (currentDestinationId == R.id.LogoutView) {
            infoMessage = "로그아웃";
        } else {
            infoMessage = "알 수 없는 뷰";
        }

        showHelpPopup(infoMessage);
    }
    */
    // Help Popup
    private void showHelpPopup() {

        // 현재 NavController의 목적지 ID를 기반으로 정보를 표시합니다.
        int currentDestinationId = navController.getCurrentDestination().getId();
        String infoMessage = "";


        if (currentDestinationId == R.id.WriteView) {
            infoMessage = "하루쓰기";
        } else if (currentDestinationId == R.id.MonthView) {
            infoMessage = "한달보기";
        } else if (currentDestinationId == R.id.StatView) {
            infoMessage = "돌아보기";
        } else if (currentDestinationId == R.id.SearchView) {
            infoMessage = "찾아보기";
        } else if (currentDestinationId == R.id.UserInfoView) {
            infoMessage = "내 정보";
        } else if (currentDestinationId == R.id.LogoutView) {
            infoMessage = "로그아웃";
        } else {
            infoMessage = "알 수 없는 뷰";
        }




        // Dialog 인스턴스를 생성
        Dialog helpDialog = new Dialog(this);
        helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 볼드처리할 단어와 TextView ID를 매핑
        HashMap<Integer, String> textViewMap = new HashMap<>();
        // 커스텀 레이아웃을 사용하여 Dialog를 설정
        if(infoMessage.equals("하루쓰기")) {
            helpDialog.setContentView(R.layout.user_guide_writeview);


            textViewMap.put(R.id.user_guide_writeview_explanation1, "하루쓰기");
            textViewMap.put(R.id.user_guide_writeview_explanation2, "넛지");
            textViewMap.put(R.id.user_guide_writeview_explanation3, "새 모먼트 추가하기");
            textViewMap.put(R.id.user_guide_writeview_explanation4, "하루 마무리하기");
            textViewMap.put(R.id.user_guide_writeview_explanation5, "AI에게 부탁하기");

            // 반복문을 통해 각 TextView에 볼드처리 적용
            for(Map.Entry<Integer, String> entry : textViewMap.entrySet()) {
                TextView textView = helpDialog.findViewById(entry.getKey());
                String boldUnderlineWord = entry.getValue();
                applyBoldUnderlineSpan(textView, boldUnderlineWord);
            }
        }
        else if(infoMessage.equals("돌아보기")){
            helpDialog.setContentView(R.layout.user_guide_statview);

            ImageView hashCloudGif = (ImageView) helpDialog.findViewById(R.id.user_guide_statview_image3);
            if (hashCloudGif != null) {
                Glide.with(this).load(R.drawable.gif_user_guide_hashcloud).into(hashCloudGif);
            }

        }

        else if(infoMessage.equals("한달보기")){
            helpDialog.setContentView(R.layout.user_guide_monthview);

        }
        else if(infoMessage.equals("찾아보기")){
            helpDialog.setContentView(R.layout.user_guide_searchview);
        }
        else
            helpDialog.setContentView(R.layout.dialog_help);
        // 도움말 텍스트를 설정합니다.
        //TextView helpText = helpDialog.findViewById(R.id.tv_help_text);
        //helpText.setText(infoMessage);


/*
        // 커스텀 레이아웃을 Dialog에 설정합니다.
        if(infoMessage.equals("하루쓰기"))
            helpDialog.setContentView(R.layout.user_guide_writeview); // 혹은 infoMessage에 따른 다른 레이아웃 설정
        else if(infoMessage.equals("한달보기"))
            helpDialog.setContentView(R.layout.user_guide_statview);
        else
            helpDialog.setContentView(R.layout.dialog_help); // 혹은 infoMessage에 따른 다른 레이아웃 설정
*/
        setHelpDialogSize(helpDialog);


        // Dialog가 표시되기 전에 애니메이션을 설정합니다.
        helpDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;


        // "닫기" 버튼에 클릭 리스너를 설정합니다.
        Button closeButton = helpDialog.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss(); // Dialog를 닫습니다.
            }
        });
        closeButton.setActivated(true);
        // Dialog를 화면에 표시
        helpDialog.show();

        // Dialog 컨텐츠에 애니메이션을 적용합니다.
        View dialogContent = helpDialog.findViewById(android.R.id.content);
        if (dialogContent != null) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            dialogContent.startAnimation(fadeInAnimation);
        }
    }


    /*
    // 볼드처리를 적용하는 메소드 정의
    private void applyBoldSpan(TextView textView, String boldWord) {
        if (textView != null) {
            String text = textView.getText().toString();
            int start = text.indexOf(boldWord);
            if (start != -1) {
                int end = start + boldWord.length();
                SpannableStringBuilder spannable = new SpannableStringBuilder(text);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                textView.setText(spannable);
            }
        }
    }
    */


    // 텍스트에 볼드와 밑줄 치는 함수
    private void applyBoldUnderlineSpan(TextView textView, String boldUnderlinePart) {
        if (textView != null && boldUnderlinePart != null) {
            String text = textView.getText().toString();
            int start = text.indexOf(boldUnderlinePart);
            if (start != -1) {
                int end = start + boldUnderlinePart.length();
                SpannableStringBuilder spannable = new SpannableStringBuilder(text);
                // 볼드 스팬 적용
                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                // 밑줄 스팬 적용
                spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                textView.setText(spannable);
            }
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