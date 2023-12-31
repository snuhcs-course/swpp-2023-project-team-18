package snu.swpp.moment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.ActivityDummyBinding;

public class DummyActivity extends AppCompatActivity {

    private ActivityDummyBinding binding;
    private AuthenticationRepository authenticationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        binding = ActivityDummyBinding.inflate(getLayoutInflater());

        //CAUTION : Button is clicked but nothing happen because of this. you should put below line
        setContentView(binding.getRoot());

        final Button logoutButton = binding.logoutButton;

        authenticationRepository = AuthenticationRepository.getInstance(
            getApplicationContext());
        System.out.println("#DEBUG: go home");
        logoutButton.setOnClickListener(v -> {
            System.out.println("#DEBUG: logout button clicked");
            authenticationRepository.logout();
            Intent logoutIntent = new Intent(DummyActivity.this, EntryActivity.class);
            startActivity(logoutIntent);
        });
    }
}