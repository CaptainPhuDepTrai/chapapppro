package com.trabajo.carlos.sender;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.trabajo.carlos.sender.adapters.ListadoUsuariosAdapter;
import com.trabajo.carlos.sender.common.Common;
import com.trabajo.carlos.sender.holder.QBUsuariosHolder;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListadoUsuariosActivity extends AppCompatActivity implements View.OnClickListener {

    ListView lsvUsuarios;
    Button btnCrearChat;

    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_usuarios);

        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);

        btnCrearChat = (Button) findViewById(R.id.btnCrearChat);
        lsvUsuarios = (ListView) findViewById(R.id.lsvUsuarios);

        lsvUsuarios.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnCrearChat.setOnClickListener(this);

        if (mode == null && qbChatDialog == null)
            recogerTodosLosUsuarios();
        else {
            if (mode.equals(Common.UPDATE_ADD_MODE))
                cargarListaUsuariosDisponibles();
            else if (mode.equals(Common.UPDATE_REMOVE_MODE))
                cargarListaUsuariosEnGrupo();

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnCrearChat:

                if (mode == null) {

                    if (lsvUsuarios.getCheckedItemPositions().size() == 1)
                        createPrivateChat(lsvUsuarios.getCheckedItemPositions());
                    else if (lsvUsuarios.getCheckedItemPositions().size() > 1)
                        createGroupChat(lsvUsuarios.getCheckedItemPositions());
                    else
                        Toast.makeText(ListadoUsuariosActivity.this, "Selecciona un amigo para chatear", Toast.LENGTH_SHORT).show();

                } else if (mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog != null) {

                    if (userAdd.size() > 0) {

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();

                        int contadorEleccion = lsvUsuarios.getCount();
                        SparseBooleanArray checkItemPositions = lsvUsuarios.getCheckedItemPositions();
                        for (int i = 0; i < contadorEleccion; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser usuario = (QBUser) lsvUsuarios.getItemAtPosition(i);
                                requestBuilder.addUsers(usuario);
                            }
                        }

                        //LLamamos a los servicios
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ListadoUsuariosActivity.this, "Usuario Añadido correctamente", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(ListadoUsuariosActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                } else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog != null) {

                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();

                        int contadorEleccion = lsvUsuarios.getCount();
                        SparseBooleanArray checkItemPositions = lsvUsuarios.getCheckedItemPositions();

                        for (int i = 0; i < contadorEleccion; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser usuario = (QBUser) lsvUsuarios.getItemAtPosition(i);
                                requestBuilder.removeUsers(usuario);
                            }
                        }

                        //LLamamos a los servicios
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ListadoUsuariosActivity.this, "Usuario expulsado correctamente", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });

                    }

                }

                break;

        }
    }

    private void cargarListaUsuariosEnGrupo() {

        btnCrearChat.setText("Expulsar Usuario");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                List<Integer> occupantsId = qbChatDialog.getOccupants();
                List<QBUser> listUsuariosYaEnElGrupo = QBUsuariosHolder.getInstance().getUsersById(occupantsId);
                ArrayList<QBUser> usuarios = new ArrayList<QBUser>();
                usuarios.addAll(listUsuariosYaEnElGrupo);

                ListadoUsuariosAdapter adapter = new ListadoUsuariosAdapter(getBaseContext(), usuarios);
                lsvUsuarios.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                userAdd = usuarios;

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListadoUsuariosActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void cargarListaUsuariosDisponibles() {

        btnCrearChat.setText("Añadir Usuario");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                ArrayList<QBUser> listadoUsuarios = QBUsuariosHolder.getInstance().getAllUsers();

                //Cogemos toda la info de los Occupants
                List<Integer> occupantsId = qbChatDialog.getOccupants();
                List<QBUser> listUsuariosYaEnElGrupo = QBUsuariosHolder.getInstance().getUsersById(occupantsId);

                //Borrar todos los usuarios que ya estan en el grupo
                for (QBUser user : listUsuariosYaEnElGrupo)
                    listadoUsuarios.remove(user);
                if (listadoUsuarios.size() > 0) {
                    ListadoUsuariosAdapter adapter = new ListadoUsuariosAdapter(getBaseContext(), listadoUsuarios);
                    lsvUsuarios.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    userAdd = listadoUsuarios;
                }

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListadoUsuariosActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void recogerTodosLosUsuarios() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<>();
                for (QBUser user : qbUsers) {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                        qbUserWithoutCurrent.add(user);
                }

                //Cargamos la lista con los usuarios
                ListadoUsuariosAdapter adapter = new ListadoUsuariosAdapter(getBaseContext(), qbUserWithoutCurrent);
                lsvUsuarios.setAdapter(adapter);
                //notificamos cuando cambia
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialogo = new ProgressDialog(ListadoUsuariosActivity.this);
        mDialogo.setMessage("Espere...");
        mDialogo.setCanceledOnTouchOutside(false);
        mDialogo.show();

        int contadorEleccion = lsvUsuarios.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();

        //Recorremos todos los usuarios, si un usuario ha sido seleccionado creamos un dialogo con dicho usuario
        for (int i = 0; i < contadorEleccion; i++) {
            if (checkedItemPositions.get(i)) {
                QBUser usuario = (QBUser) lsvUsuarios.getItemAtPosition(i);
                occupantIdsList.add(usuario.getId());
            }
        }

        //Creamos chat dialogo
        QBChatDialog dialogo = new QBChatDialog();
        dialogo.setName(Common.createChatDialogName(occupantIdsList));
        dialogo.setType(QBDialogType.GROUP);
        dialogo.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialogo).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialogo.dismiss();
                Toast.makeText(ListadoUsuariosActivity.this, "Dialogo del chat creado correctamente", Toast.LENGTH_SHORT).show();

                //Send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());

                //Cogemos todos los ids de los ocupantes del grupo
                for (int i = 0; i < qbChatDialog.getOccupants().size(); i++) {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {

                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);

                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }

                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialogo = new ProgressDialog(ListadoUsuariosActivity.this);
        mDialogo.setMessage("Espere...");
        mDialogo.setCanceledOnTouchOutside(false);
        mDialogo.show();

        int contadorEleccion = lsvUsuarios.getCount();

        //Recorremos todos los usuarios, si un usuario ha sido seleccionado creamos un dialogo con dicho usuario
        for (int i = 0; i < contadorEleccion; i++) {
            if (checkedItemPositions.get(i)) {
                final QBUser usuario = (QBUser) lsvUsuarios.getItemAtPosition(i);

                QBChatDialog dialogo = DialogUtils.buildPrivateDialog(usuario.getId());

                QBRestChatService.createChatDialog(dialogo).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialogo.dismiss();
                        Toast.makeText(ListadoUsuariosActivity.this, "Dialogo del chat privado creado correctamente", Toast.LENGTH_SHORT).show();

                        //Send system message to recipient id user
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(usuario.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {

                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);

                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage());
                    }
                });
            }
        }
    }

}
