package com.trabajo.carlos.sender;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String APP_ID = "56914";
    static final String AUTH_KEY = "wGsqaFNMLmbNzPQ";
    static final String AUTH_SECRET = "KWHwJUUN67AfXFh";
    static final String ACCOUNT_KEY = "cq5SMjXYz2NizfXMBrCy";

    static final int REQUEST_CODE = 1000;

    Button btnLogin;
    TextView txvSignup;
    EditText edtUser, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //requestRuntimePermission();

        btnLogin = (Button) findViewById(R.id.btnLogin);
        txvSignup = (TextView) findViewById(R.id.txvRegistrar);
        edtUser = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        btnLogin.setOnClickListener(this);
        txvSignup.setOnClickListener(this);

        inicializqarFramework();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getBaseContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), "Permission Denied", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnLogin:
                final String user = edtUser.getText().toString();
                final String password = edtPassword.getText().toString();

                //We create the instance to create a new user and pass the user and password
                QBUser qbUser = new QBUser(user, password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "\n" + "Login successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish(); //Closes login activity after logging in
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                break;

            case R.id.txvRegistrar:
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));

                break;

        }

    }

    private void inicializqarFramework() {

        //Initialize the service
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);

    }

}
