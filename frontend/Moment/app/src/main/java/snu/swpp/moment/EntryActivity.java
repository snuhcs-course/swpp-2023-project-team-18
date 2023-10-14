package snu.swpp.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;

import snu.swpp.moment.data.AuthenticationRepository;
import snu.swpp.moment.ui.login.LoginActivity;

public class EntryActivity extends AppCompatActivity {
    private AuthenticationRepository authenticationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        try {
            authenticationRepository = AuthenticationRepository.getInstance(getApplicationContext());
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, "개발자에게 연락하세요", Toast.LENGTH_SHORT);
        } catch (IOException e) {
            Toast.makeText(this, "개발자에게 연락하세요", Toast.LENGTH_SHORT);
        }

        if (!authenticationRepository.isLoggedIn()) {
            System.out.println("#DEBUG: not logged in");
            Intent entryIntent = new Intent(EntryActivity.this, LoginActivity.class);
            startActivity(entryIntent);
        } else {
            System.out.println("#DEBUG: logged in");
            Intent entryIntent = new Intent(EntryActivity.this, DummyActivity.class);
            startActivity(entryIntent);
        }
    }
}