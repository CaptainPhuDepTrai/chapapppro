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

    static final String APP_ID = "55736";
    static final String AUTH_KEY = "fxZeB6RJSNhwpNn";
    static final String AUTH_SECRET = "UsKyxNXtbzfVwux";
    static final String ACCOUNT_KEY = "sspxhTd8dTFFwhuifqzC";

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

    /*private void requestRuntimePermission() {

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE);

        }

    }*/

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

                //Creamos la instancia para crear un nuevo usuario y le pasamos el usuario y la contraseña
                QBUser qbUser = new QBUser(user, password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Logeado correctamente", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, ConversacionActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish(); //Cierra la actividad del ligin despues de logearse
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

        //Inicializamos el servicio
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);

    }

}
