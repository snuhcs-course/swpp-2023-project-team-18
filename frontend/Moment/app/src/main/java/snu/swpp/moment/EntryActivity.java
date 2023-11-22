package snu.swpp.moment;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import snu.swpp.moment.data.factory.AuthenticationRepositoryFactory;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class EntryActivity extends AppCompatActivity {

    private AuthenticationRepositoryFactory authenticationRepositoryFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        authenticationRepositoryFactory = new AuthenticationRepositoryFactory(
            getApplicationContext());
        AuthenticationRepository authenticationRepository = authenticationRepositoryFactory.getRepository();
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