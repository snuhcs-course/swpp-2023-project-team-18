package snu.swpp.moment;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        loginButton = findViewById(R.id.main_login);  // make sure the ID matches the button ID in your XML layout
        registerButton = findViewById(R.id.main_register);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginRegisterActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        // If you have other actions for registerButton or other UI elements, initialize and set them up here.
    }
}
