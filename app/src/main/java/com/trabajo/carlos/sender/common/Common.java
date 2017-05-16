package com.trabajo.carlos.sender.common;

import com.quickblox.users.model.QBUser;
import com.trabajo.carlos.sender.holder.QBUsuariosHolder;

import java.util.List;

public class Common {

    public static final String DIALOG_EXTRA = "Dialogs";

    public static final String UPDATE_DIALOG_EXTRA = "ChatDialogs";
    public static final String UPDATE_MODE = "mode";
    public static final String UPDATE_ADD_MODE = "add";
    public static final String UPDATE_REMOVE_MODE = "remove";
    public static final int SELECCIONAR_FOTO = 7171;

    public static String createChatDialogName(List<Integer> qbUsuarios) {
        List<QBUser> qbUsers1 = QBUsuariosHolder.getInstance().getUsersById(qbUsuarios);

        // The name of the dialog will be the name of all the users in the list, if the length of the name is greater than 30 we put a "..." at the end

        StringBuilder nombre = new StringBuilder();
        for (QBUser user : qbUsers1)
            nombre.append(user.getFullName()).append(" ");
        if (nombre.length() > 30)
            nombre = nombre.replace(30, nombre.length() - 1, "...");
        return nombre.toString();
    }


    // Check if string is empty or null
    public static boolean isNuloOVacioString(String content) {
        return (!(content != null && !content.trim().isEmpty()));
    }

}
