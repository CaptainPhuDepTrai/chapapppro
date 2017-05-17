package com.trabajo.carlos.AppChat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.trabajo.carlos.AppChat.adapters.ChatDialogsAdapter;
import com.trabajo.carlos.AppChat.common.Common;
import com.trabajo.carlos.AppChat.holder.QBChatDialogHolder;
import com.trabajo.carlos.AppChat.holder.QBUnreadMessageHolder;
import com.trabajo.carlos.AppChat.holder.QBUserHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener, QBSystemMessageListener, QBChatDialogMessageListener {

    FloatingActionButton fabAddUser;
    ListView lsvChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sender");
        setSupportActionBar(toolbar);

        fabAddUser = (FloatingActionButton) findViewById(R.id.fabAddUser);
        lsvChats = (ListView) findViewById(R.id.lsvChats);

        registerForContextMenu(lsvChats);

        fabAddUser.setOnClickListener(this);
        lsvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog) lsvChats.getAdapter().getItem(position);
                Intent intent = new Intent(ConversationActivity.this, ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                startActivity(intent);
            }
        });

        crearChatSesion();

        cargarChats();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.context_delete_dialog:
                borrarConversacion(info.position);
                break;
        }

        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_dialog_menu_user:
                showUserProfile();

                break;
            default:

                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarChats();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fabAddUser:

                Intent intent = new Intent(ConversationActivity.this, ListUsersActivity.class);
                startActivity(intent);

                break;

        }

    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        //We put the dialogues in cache

        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //We cache it
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogsAdapter adapters = new ChatDialogsAdapter(getBaseContext(), adapterSource);
                lsvChats.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR", "" + e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        cargarChats();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }

    private void showUserProfile() {
        Intent intent = new Intent(ConversationActivity.this, UserProfileActivity.class);
        startActivity(intent);
    }

    private void cargarChats() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        //We take all the chats there are
        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                //We put all the dialogues in cache
                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

                //Config unread
                Set<String> setIds = new HashSet<>();
                for (QBChatDialog chatDialog : qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                //Take the unread message
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle()).performAsync(new QBEntityCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer, Bundle bundle) {
                        //Caching
                        QBUnreadMessageHolder.getInstance().setBundle(bundle);

                        //Refresh the list
                        ChatDialogsAdapter adapter = new ChatDialogsAdapter(getBaseContext(), QBChatDialogHolder.getInstance().getAllChatDialogs());
                        lsvChats.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    private void crearChatSesion() {
        final ProgressDialog mDialogo = new ProgressDialog(ConversationActivity.this);
        mDialogo.setMessage("Loading...");
        mDialogo.setCanceledOnTouchOutside(false);
        mDialogo.show();

        //We collect the user and password of the MainActivity
        String user, password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        //We load all users and cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUserHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        final QBUser qbUser = new QBUser(user, password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                //We take the id of the user
                qbUser.setId(qbSession.getUserId());

                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                //We login

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialogo.dismiss();

                        //We add a listener for the received system message
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ConversationActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ConversationActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void borrarConversacion(int index) {

        final QBChatDialog chatDialog = (QBChatDialog) lsvChats.getAdapter().getItem(index);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {

                //We delete this cache conversation and refresh the listview
                QBChatDialogHolder.getInstance().borrarConversacion(chatDialog.getDialogId());
                ChatDialogsAdapter adapter = new ChatDialogsAdapter(getBaseContext(), QBChatDialogHolder.getInstance().getAllChatDialogs());
                lsvChats.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

}
