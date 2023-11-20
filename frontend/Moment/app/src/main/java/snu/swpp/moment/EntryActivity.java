package snu.swpp.moment;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class EntryActivity extends AppCompatActivity {

    private AuthenticationRepository authenticationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        authenticationRepository = AuthenticationRepository.getInstance(
            getApplicationContext());
        if (!authenticationRepository.isLoggedIn()) {
            System.out.println("#DEBUG: not logged in");
            Intent entryIntent = new Intent(EntryActivity.this, LoginRegisterActivity.class);
            startActivity(entryIntent);
        } else {
            System.out.println("#DEBUG: logged in");
            Intent entryIntent = new Intent(EntryActivity.this, MainActivity.class);
            startActivity(entryIntent);
        }
    }
}