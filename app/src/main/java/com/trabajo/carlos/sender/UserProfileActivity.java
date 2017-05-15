package com.trabajo.carlos.sender;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;
import com.trabajo.carlos.sender.common.Common;
import com.trabajo.carlos.sender.holder.QBUsuariosHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    EditText edtOldPass, edtNewPass, edtFullName, edtEmail, edtTelefono;
    Button btnUpdate, btnCancelarUpdate;
    ImageView imgUserAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Perfil");
        setSupportActionBar(toolbar);

        btnCancelarUpdate = (Button) findViewById(R.id.btnCancelarUpdate);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtFullName = (EditText) findViewById(R.id.edtFullName);
        edtNewPass = (EditText) findViewById(R.id.edtNewPass);
        edtOldPass = (EditText) findViewById(R.id.edtOldPass);
        edtTelefono = (EditText) findViewById(R.id.edtTelefono);
        imgUserAvatar = (ImageView) findViewById(R.id.imgUsuarioAvatar);

        btnUpdate.setOnClickListener(this);
        btnCancelarUpdate.setOnClickListener(this);
        imgUserAvatar.setOnClickListener(this);

        //Cargamos el perfil del usuario desde webservices
        loadUserProfile();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_update_log_out:
                logOut();

                break;
            default:

                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUpdate:
                String newPass = edtNewPass.getText().toString();
                String oldPass = edtOldPass.getText().toString();
                String fullName = edtFullName.getText().toString();
                String email = edtEmail.getText().toString();
                String telefono = edtTelefono.getText().toString();

                //Cogemos el id del usuario que esta logeado
                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());
                //Comprobamos que los eddittext no sean nulos ni vacios
                if (!Common.isNuloOVacioString(oldPass))
                    user.setOldPassword(oldPass);
                if (!Common.isNuloOVacioString(newPass))
                    user.setPassword(newPass);
                if (!Common.isNuloOVacioString(fullName))
                    user.setFullName(fullName);
                if (!Common.isNuloOVacioString(email))
                    user.setEmail(email);
                if (!Common.isNuloOVacioString(telefono))
                    user.setPhone(telefono);

                final ProgressDialog mDialog = new ProgressDialog(UserProfileActivity.this);
                mDialog.setMessage("Espere...");
                mDialog.show();

                //Actualizamos el usuario
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "Usuario: " + qbUser.getLogin() + " actualizado", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfileActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                break;
            case R.id.btnCancelarUpdate:
                finish();

                break;
            case R.id.imgUsuarioAvatar:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.SELECCIONAR_FOTO);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Common.SELECCIONAR_FOTO) {
                Uri imagenSeleccionadaUri = data.getData();

                final ProgressDialog mDialog = new ProgressDialog(UserProfileActivity.this);
                mDialog.setMessage("Subiendo foto...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                //Actualizar el avatar del usuario
                try {

                    InputStream in = getContentResolver().openInputStream(imagenSeleccionadaUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    File file = new File(Environment.getExternalStorageDirectory() + "/myimage.png");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bos.toByteArray());
                    fos.flush();
                    fos.close();

                    //Cogemos el tamaño del archivo
                    final int imageSizeKb = (int) file.length() / 1024;
                    if (imageSizeKb >= (1024 * 100)) {
                        Toast.makeText(this, "Tamaño incorrecto", Toast.LENGTH_SHORT).show();
                    }

                    //Subimos la foto al servidor
                    QBContent.uploadFileTask(file, true, null).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            //Set avatar para el usuario
                            QBUser user = new QBUser();
                            user.setId(QBChatService.getInstance().getUser().getId());
                            user.setFileId(Integer.parseInt(qbFile.getId().toString()));

                            //Actualizamos el usuario
                            QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser qbUser, Bundle bundle) {
                                    mDialog.dismiss();
                                    imgUserAvatar.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void logOut() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "Te has desconectado!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                        //Borramos todas las actividades previas
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void loadUserProfile() {

        //Cargamos el avatar
        QBUsers.getUser(QBChatService.getInstance().getUser().getId()).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                //Guardamos en cache
                QBUsuariosHolder.getInstance().putUser(qbUser);
                if (qbUser.getFileId() != null) {
                    int imagenPerfilId = qbUser.getFileId();

                    QBContent.getFile(imagenPerfilId).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {

                            String fileUrl = qbFile.getPublicUrl();
                            Picasso.with(getBaseContext())
                                    .load(fileUrl)
                                    .into(imgUserAvatar);

                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
                }
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        QBUser currentUser = QBChatService.getInstance().getUser();
        String fullName = currentUser.getFullName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhone();

        edtEmail.setText(email);
        edtFullName.setText(fullName);
        edtTelefono.setText(phone);

    }

}
