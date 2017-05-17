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
import com.trabajo.carlos.sender.adapters.UserlistAdapter;
import com.trabajo.carlos.sender.common.Common;
import com.trabajo.carlos.sender.holder.QBUsuariosHolder;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity implements View.OnClickListener {

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
                        Toast.makeText(ListUsersActivity.this, "Select a friend to chat", Toast.LENGTH_SHORT).show();

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

                        //We call the services

                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ListUsersActivity.this, "User successfully added", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(ListUsersActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                        //We call the services
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ListUsersActivity.this, "User eject successfully ", Toast.LENGTH_SHORT).show();
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

        btnCrearChat.setText("Eject User");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                List<Integer> occupantsId = qbChatDialog.getOccupants();
                List<QBUser> listUsuariosYaEnElGrupo = QBUsuariosHolder.getInstance().getUsersById(occupantsId);
                ArrayList<QBUser> usuarios = new ArrayList<QBUser>();
                usuarios.addAll(listUsuariosYaEnElGrupo);

                UserlistAdapter adapter = new UserlistAdapter(getBaseContext(), usuarios);
                lsvUsuarios.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                userAdd = usuarios;

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListUsersActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void cargarListaUsuariosDisponibles() {

        btnCrearChat.setText("\n" + "Add User");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                ArrayList<QBUser> listadoUsuarios = QBUsuariosHolder.getInstance().getAllUsers();

                //We take all the info of the Occupants
                List<Integer> occupantsId = qbChatDialog.getOccupants();
                List<QBUser> listUsuariosYaEnElGrupo = QBUsuariosHolder.getInstance().getUsersById(occupantsId);

                //Delete all users who are already in the group
                for (QBUser user : listUsuariosYaEnElGrupo)
                    listadoUsuarios.remove(user);
                if (listadoUsuarios.size() > 0) {
                    UserlistAdapter adapter = new UserlistAdapter(getBaseContext(), listadoUsuarios);
                    lsvUsuarios.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    userAdd = listadoUsuarios;
                }

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListUsersActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                //We load the list with the users
                UserlistAdapter adapter = new UserlistAdapter(getBaseContext(), qbUserWithoutCurrent);
                lsvUsuarios.setAdapter(adapter);
                //We notify you when you change

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialogo = new ProgressDialog(ListUsersActivity.this);
        mDialogo.setMessage("Waited\n...");
        mDialogo.setCanceledOnTouchOutside(false);
        mDialogo.show();

        int contadorEleccion = lsvUsuarios.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();

        //We walk all users, if a user has been selected we create a dialog with that user
        for (int i = 0; i < contadorEleccion; i++) {
            if (checkedItemPositions.get(i)) {
                QBUser usuario = (QBUser) lsvUsuarios.getItemAtPosition(i);
                occupantIdsList.add(usuario.getId());
            }
        }

        //Create chat dialog
        QBChatDialog dialogo = new QBChatDialog();
        dialogo.setName(Common.createChatDialogName(occupantIdsList));
        dialogo.setType(QBDialogType.GROUP);
        dialogo.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialogo).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialogo.dismiss();
                Toast.makeText(ListUsersActivity.this, "Dialog created correctly", Toast.LENGTH_SHORT).show();

                //Send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());

                //We take all the ids of the occupants of the group
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
        final ProgressDialog mDialogo = new ProgressDialog(ListUsersActivity.this);
        mDialogo.setMessage("Wait...");
        mDialogo.setCanceledOnTouchOutside(false);
        mDialogo.show();

        int contadorEleccion = lsvUsuarios.getCount();

        //We walk all users, if a user has been selected we create a dialog with that user
        for (int i = 0; i < contadorEleccion; i++) {
            if (checkedItemPositions.get(i)) {
                final QBUser usuario = (QBUser) lsvUsuarios.getItemAtPosition(i);

                QBChatDialog dialogo = DialogUtils.buildPrivateDialog(usuario.getId());

                QBRestChatService.createChatDialog(dialogo).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialogo.dismiss();
                        Toast.makeText(ListUsersActivity.this, "Private chat dialog created correctly", Toast.LENGTH_SHORT).show();

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
