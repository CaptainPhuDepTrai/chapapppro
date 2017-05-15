package com.trabajo.carlos.sender.holder;

import android.os.Bundle;

public class QBUnreadMensajeHolder {
    private static QBUnreadMensajeHolder instance;
    private Bundle bundle;

    public static synchronized QBUnreadMensajeHolder getInstance() {
        QBUnreadMensajeHolder qbUnreadMensajeHolder;
        synchronized (QBUnreadMensajeHolder.class) {
            if (instance == null)
                instance = new QBUnreadMensajeHolder();
            qbUnreadMensajeHolder = instance;
        }
        return qbUnreadMensajeHolder;
    }

    private QBUnreadMensajeHolder() {
        bundle = new Bundle();
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public int getUnreadMensajeByDialogId(String id) {
        return this.bundle.getInt(id);
    }

}
