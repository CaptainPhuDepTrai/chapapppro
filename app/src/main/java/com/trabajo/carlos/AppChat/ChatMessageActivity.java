package com.trabajo.carlos.AppChat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bhargavms.dotloader.DotLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;
import com.trabajo.carlos.AppChat.adapters.MessageChatAdapter;
import com.trabajo.carlos.AppChat.common.Common;
import com.trabajo.carlos.AppChat.holder.QBMessageHolder;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChatMessageActivity extends AppCompatActivity implements View.OnClickListener, QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView lsvListaMensajes;
    EditText edtMensaje, edtNombreGrupo;
    ImageButton imgbEnviar, imgbEmoji;

    MessageChatAdapter adapter;

    Toolbar toolbar;

    //Update user online
    ImageView imgvContadorOnline, imgAvatar;
    TextView txvContadorOnline;

    //Variables for updating / deleting messages
    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMensaje;

    //Variables typing ...
    DotLoader dotLoader;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        toolbar = (Toolbar) findViewById(R.id.toolbar3);

        iniciarVistas();
        iniciarConversaciones();
        recogerMensaje();

        imgbEnviar.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);

        //Add context menu
        registerForContextMenu(lsvListaMensajes);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.message_chat_group_menu, menu);

        return true;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.message_chat_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //Let's take the index context menu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId()) {
            case R.id.message_chat_update:
                actualizarMensaje();

                break;
            case R.id.message_chat_delete:
                borrarMensaje();

                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_group_edit_name:
                editarNombreGrupo();

                break;
            case R.id.chat_group_add_user:
                addUsuario();

                break;
            case R.id.chat_group_remove_user:
                borrarUsuario();

                break;
        }

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == Common.SELECCIONAR_FOTO) {
                //We take the URI of the selected image, convert it to a file and upload it to the serve
                Uri imagenSeleccionadaUri = data.getData();

                final ProgressDialog mDialog = new ProgressDialog(ChatMessageActivity.this);
                mDialog.setMessage("Setting image ...");
                mDialog.setCancelable(false);
                mDialog.show();

                try {

                    //Convert URI to File

                    InputStream inputStream = getContentResolver().openInputStream(imagenSeleccionadaUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    File file = new File(Environment.getExternalStorageDirectory() + "/image.png");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    int imageSizeKb = (int) file.length() / 1024;
                    if (imageSizeKb >= (1024 * 100)) {
                        Toast.makeText(this, "\n" + "Incorrect size", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Upload the image
                    QBContent.uploadFileTask(file, true, null).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {

                            qbChatDialog.setPhoto(qbFile.getId().toString());

                            //We update the conversation
                            QBRequestUpdateBuilder requestUpdateBuilder = new QBDialogRequestBuilder();
                            QBRestChatService.updateGroupChatDialog(qbChatDialog, requestUpdateBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                                    mDialog.dismiss();
                                    imgAvatar.setImageBitmap(bitmap);

                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.imgbEnviar:

                if (!isEditMode) {
                    QBChatMessage mensajeChat = new QBChatMessage();
                    mensajeChat.setBody(edtMensaje.getText().toString());
                    mensajeChat.setSenderId(QBChatService.getInstance().getUser().getId());
                    mensajeChat.setSaveToHistory(true);

                    try {
                        //We send the message
                        qbChatDialog.sendMessage(mensajeChat);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                    //We fixed the bug that private chat does not display messages
                    if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                        //Cached Message
                        QBMessageHolder.getInstance().putMensaje(qbChatDialog.getDialogId(), mensajeChat);
                        ArrayList<QBChatMessage> mensajes = QBMessageHolder.getInstance().getChatMensajesByDialogId(mensajeChat.getDialogId());

                        //We set the adapter from the list
                        adapter = new MessageChatAdapter(getBaseContext(), mensajes);
                        lsvListaMensajes.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    //Clean EdditText
                    edtMensaje.setText("");
                    edtMensaje.setFocusable(true);
                } else {
                    final ProgressDialog actualizarDialog = new ProgressDialog(ChatMessageActivity.this);
                    actualizarDialog.setMessage("Updating...");
                    actualizarDialog.show();

                    QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                    messageUpdateBuilder.updateText(edtMensaje.getText().toString()).markDelivered().markRead();
                    QBRestChatService.updateMessage(editMensaje.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder).performAsync(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            //Recharge messages from the server
                            recogerMensaje();
                            isEditMode = false;
                            actualizarDialog.dismiss();

                            //Reset the edittext
                            edtMensaje.setText("");
                            edtMensaje.setFocusable(true);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.e("ERROR", e.getMessage());
                        }
                    });
                }


                break;
            case R.id.imgvDialogAvatar:
                Intent seleccionarImagen = new Intent();
                seleccionarImagen.setType("image*//*");
                seleccionarImagen.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(seleccionarImagen, "\n" + "Select Image"), Common.SELECCIONAR_FOTO);

                break;

        }
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //Cached Message
        QBMessageHolder.getInstance().putMensaje(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> mensajes = QBMessageHolder.getInstance().getChatMensajesByDialogId(qbChatMessage.getDialogId());

        //We set the adapter from the list
        adapter = new MessageChatAdapter(getBaseContext(), mensajes);
        lsvListaMensajes.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR", e.getMessage());
    }

    private void borrarUsuario() {

        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_REMOVE_MODE);
        startActivity(intent);

    }

    private void addUsuario() {

        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_ADD_MODE);
        startActivity(intent);

    }

    private void editarNombreGrupo() {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.edit_group_dialog_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        edtNombreGrupo = (EditText) view.findViewById(R.id.edtNombreGrupo);

        //Set Dialog Message
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //We establish a new name
                        qbChatDialog.setName(edtNombreGrupo.getText().toString());

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ChatMessageActivity.this, "Updated group name", Toast.LENGTH_SHORT).show();
                                toolbar.setTitle(qbChatDialog.getName());
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.cancel();

                    }
                });

        //We create alert
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void borrarMensaje() {
        final ProgressDialog borrarDialog = new ProgressDialog(ChatMessageActivity.this);
        borrarDialog.setMessage("Erasing...");
        borrarDialog.show();

        //We put the message for edittext
        editMensaje = QBMessageHolder.getInstance().getChatMensajesByDialogId(qbChatDialog.getDialogId()).get(contextMenuIndexClicked);

        QBRestChatService.deleteMessage(editMensaje.getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                recogerMensaje();
                borrarDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ChatMessageActivity.this, "You do not have permission to delete this message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarMensaje() {

        //We put the message for edittext
        editMensaje = QBMessageHolder.getInstance().getChatMensajesByDialogId(qbChatDialog.getDialogId()).get(contextMenuIndexClicked);
        edtMensaje.setText(editMensaje.getBody());
        isEditMode = true;

    }

    /**
     * Method that loads the messages that are sent and receiving in the list
     */
    private void recogerMensaje() {

        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        //The limit is 500 messages
        messageGetBuilder.setLimit(500);

        if (qbChatDialog != null) {
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //We cache messages
                    QBMessageHolder.getInstance().putMensajes(qbChatDialog.getDialogId(), qbChatMessages);

                    adapter = new MessageChatAdapter(getBaseContext(), qbChatMessages);
                    lsvListaMensajes.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }

    }

    private void iniciarConversaciones() {

        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.DIALOG_EXTRA);

        if (qbChatDialog.getPhoto() == null && !qbChatDialog.getPhoto().equals("null")) {
            QBContent.getFile(Integer.parseInt(qbChatDialog.getPhoto())).performAsync(new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle bundle) {
                    String fileURL = qbFile.getPublicUrl();
                    Picasso.with(getBaseContext())
                            .load(fileURL)
                            .resize(50, 50)
                            .centerCrop()
                            .into(imgAvatar);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR_IMAGE", "" + e.getMessage());
                }
            });
        }

        qbChatDialog.initForChat(QBChatService.getInstance());

        //Record the listener of the arriving message
        QBIncomingMessagesManager incomingMensaje = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMensaje.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //Add typing listener
        mostrarEscribiendoConversacion(qbChatDialog);

        //We enable you to join a group chat
        if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP) {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR", e.getMessage());
                }
            });

        }

        QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {

                if (Objects.equals(dialogId, qbChatDialog.getDialogId())) {
                    QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                            //We take the user online
                            try {

                                Collection<Integer> listaOnline = qbChatDialog.getOnlineUsers();

                                TextDrawable.IBuilder builder = TextDrawable.builder()
                                        .beginConfig()
                                        .withBorder(4)
                                        .endConfig()
                                        .round();

                                TextDrawable online = builder.build("", Color.RED);
                                imgvContadorOnline.setImageDrawable(online);

                                txvContadorOnline.setText(String.format("%d/%d online", listaOnline.size(), qbChatDialog.getOccupants().size()));

                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
                }

            }
        };

        qbChatDialog.addParticipantListener(participantListener);

        qbChatDialog.addMessageListener(this);

        //Put title for the toolbar
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);

    }

    /**
     *
     Listener for when someone is typing
     * @param qbChatDialog
     */
    private void mostrarEscribiendoConversacion(QBChatDialog qbChatDialog) {

        QBChatDialogTypingListener typingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String dialogId, Integer integer) {

                if (dotLoader.getVisibility() != View.VISIBLE)
                    dotLoader.setVisibility(View.VISIBLE);

            }

            @Override
            public void processUserStopTyping(String dialogId, Integer integer) {

                if (dotLoader.getVisibility() != View.INVISIBLE)
                    dotLoader.setVisibility(View.INVISIBLE);

            }
        };

        qbChatDialog.addIsTypingListener(typingListener);

    }

    private void iniciarVistas() {
        lsvListaMensajes = (ListView) findViewById(R.id.lsvListaMensajes);
        imgbEnviar = (ImageButton) findViewById(R.id.imgbEnviar);
        edtMensaje = (EditText) findViewById(R.id.edtMensaje);
        imgvContadorOnline = (ImageView) findViewById(R.id.imgvContadorOnline);
        imgAvatar = (ImageView) findViewById(R.id.imgvDialogAvatar);
        txvContadorOnline = (TextView) findViewById(R.id.txvContadorOnline);
        dotLoader = (DotLoader) findViewById(R.id.dot_loader);

        edtMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                try {

                    qbChatDialog.sendIsTypingNotification();

                } catch (XMPPException | SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {

                    qbChatDialog.sendStopTypingNotification();

                } catch (XMPPException | SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
