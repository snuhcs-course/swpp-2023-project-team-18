package snu.swpp.moment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.ui.register.RegisterActivity;

public class LoginRegisterActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        System.out.println("#DEBUG loginregister start");
        // Initialize the loginButton by referencing the XML layout
        loginButton = findViewById(R.id.main_login);
        registerButton = findViewById(R.id.main_register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginRegisterActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Login register activity, back press action
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
                System.exit(0);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

    }
}
