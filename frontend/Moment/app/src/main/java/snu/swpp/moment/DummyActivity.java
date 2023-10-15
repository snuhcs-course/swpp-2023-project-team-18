package snu.swpp.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.security.GeneralSecurityException;

import snu.swpp.moment.data.AuthenticationRepository;
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
        try {
            authenticationRepository = AuthenticationRepository.getInstance(getApplicationContext());
            System.out.println("#DEBUG: go home");
            authenticationRepository.logout();
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("#DEBUG: logout button clicked");
                    authenticationRepository.logout();
                    Intent logoutIntent= new Intent(DummyActivity.this, EntryActivity.class);
                    startActivity(logoutIntent);
                }
            });
        } catch (GeneralSecurityException e) {
            System.out.println("#DEBUG: General");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("#DEBUG: IO");
            throw new RuntimeException(e);
        }
    }
}