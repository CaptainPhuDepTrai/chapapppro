package com.trabajo.carlos.sender.holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBMensajeHolder {

    private static QBMensajeHolder instance;

    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QBMensajeHolder getInstance() {
        QBMensajeHolder qbMensajeHolder;

        synchronized (QBMensajeHolder.class) {
            if (instance == null)
                instance = new QBMensajeHolder();
            qbMensajeHolder = instance;
        }
        return qbMensajeHolder;
    }

    private QBMensajeHolder() {
        this.qbChatMessageArray = new HashMap<>();
    }

    public void putMensajes(String dialogId, ArrayList<QBChatMessage> qbChatMessages) {
        this.qbChatMessageArray.put(dialogId, qbChatMessages);
    }

    public void putMensaje(String dialogId, QBChatMessage qbChatMessage) {
        List<QBChatMessage> lstResultado = (List) this.qbChatMessageArray.get(dialogId);
        lstResultado.add(qbChatMessage);

        ArrayList<QBChatMessage> lstAdded = new ArrayList(lstResultado.size());
        lstAdded.addAll(lstResultado);
        putMensajes(dialogId, lstAdded);
    }

    public ArrayList<QBChatMessage> getChatMensajesByDialogId(String dialogId) {
        return this.qbChatMessageArray.get(dialogId);
    }

}
