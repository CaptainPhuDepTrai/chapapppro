package com.trabajo.carlos.sender.holder;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBChatDialogHolder {

    private static QBChatDialogHolder instance;
    private HashMap<String, QBChatDialog> qbChatDialogHashMap;

    public static synchronized QBChatDialogHolder getInstance() {
        QBChatDialogHolder qbChatDialogHolder;
        synchronized (QBMensajeHolder.class) {
            if (instance == null)
                instance = new QBChatDialogHolder();
        }
        qbChatDialogHolder = instance;
        return qbChatDialogHolder;
    }

    private QBChatDialogHolder() {
        this.qbChatDialogHashMap = new HashMap<>();
    }

    public void putDialogs(List<QBChatDialog> dialogs) {
        for (QBChatDialog qbChatDialog : dialogs)
            putDialog(qbChatDialog);
    }

    public void putDialog(QBChatDialog qbChatDialog) {
        this.qbChatDialogHashMap.put(qbChatDialog.getDialogId(), qbChatDialog);
    }

    private QBChatDialog getChatDialogById(String dialogId) {
        return qbChatDialogHashMap.get(dialogId);
    }

    public List<QBChatDialog> getChatDialogsById(List<String> dialogsId) {
        List<QBChatDialog> chatDialogs = new ArrayList<>();
        for (String id : dialogsId) {
            QBChatDialog chatDialog = getChatDialogById(id);
            if (chatDialog != null)
                chatDialogs.add(chatDialog);
        }
        return chatDialogs;
    }

    public ArrayList<QBChatDialog> getAllChatDialogs() {
        ArrayList<QBChatDialog> qbChat = new ArrayList<>();
        for (String key : qbChatDialogHashMap.keySet())
            qbChat.add(qbChatDialogHashMap.get(key));
        return qbChat;
    }

    public void borrarConversacion(String id) {
        qbChatDialogHashMap.remove(id);
    }

}
