package com.trabajo.carlos.AppChat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnRegistro;
    TextView txvCancelar;
    EditText edtUser, edtPassword, edtNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnRegistro = (Button) findViewById(R.id.btnRegistro);
        txvCancelar = (TextView) findViewById(R.id.txvCancelar);
        edtUser = (EditText) findViewById(R.id.edtRegistroUsername);
        edtPassword = (EditText) findViewById(R.id.edtRegistroPassword);
        edtNombre = (EditText) findViewById(R.id.edtNombre);

        btnRegistro.setOnClickListener(this);
        txvCancelar.setOnClickListener(this);

        registroSesion();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnRegistro:
                String user = edtUser.getText().toString();
                String nombre = edtNombre.getText().toString();
                String password = edtPassword.getText().toString();

                //We create the instance to create a new user and pass the user and password
                QBUser qbUser = new QBUser(user, password);

                //We establish the full name
                qbUser.setFullName(nombre);

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                break;

            case R.id.txvCancelar:
                finish();

                break;

        }

    }

    private void registroSesion() {

        //We create the session
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });

    }

}
