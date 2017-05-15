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

        //El nombre del dialogo sera el nombre de todos los usuarios en la lista, si la longitud del nombre es mayor a 30  ponemos un "..." al final
        StringBuilder nombre = new StringBuilder();
        for (QBUser user : qbUsers1)
            nombre.append(user.getFullName()).append(" ");
        if (nombre.length() > 30)
            nombre = nombre.replace(30, nombre.length() - 1, "...");
        return nombre.toString();
    }

    //Comprobamos si el string esta vacio o es nulo
    public static boolean isNuloOVacioString(String content) {
        return (!(content != null && !content.trim().isEmpty()));
    }

}
