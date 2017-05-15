package com.trabajo.carlos.sender.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.squareup.picasso.Picasso;
import com.trabajo.carlos.sender.R;
import com.trabajo.carlos.sender.holder.QBUnreadMensajeHolder;

import java.util.ArrayList;

public class ChatDialogsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogsAdapter(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog, null);

            TextView txvTitulo, txvMensaje;
            final ImageView img, imgUnread;

            txvMensaje = (TextView) view.findViewById(R.id.txvListaChat);
            txvTitulo = (TextView) view.findViewById(R.id.txvTitulo);
            img = (ImageView) view.findViewById(R.id.imgIconoChatDialog);
            imgUnread = (ImageView) view.findViewById(R.id.imgIconoUnread);

            txvMensaje.setText(qbChatDialogs.get(position).getLastMessage());
            txvTitulo.setText(qbChatDialogs.get(position).getName());

            //Color random para las fotos de la lista
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int randomColor = generator.getRandomColor();

            if (qbChatDialogs.get(position).getPhoto().equals("null")) {

                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                        .endConfig()
                        .round();

                //Cogemos el primer caracter en mayusculas del texto de la lista para mostrarlo en el ImageView
                TextDrawable drawable = builder.build(txvTitulo.getText().toString().substring(0, 1).toUpperCase(), randomColor);
                img.setImageDrawable(drawable);

            } else {

                //Nos descargamos el bitmap del servidor y lo establecemos en la conversacion
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(position).getPhoto())).performAsync(new QBEntityCallback<QBFile>() {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        String fileURL = qbFile.getPublicUrl();
                        Picasso.with(context)
                                .load(fileURL)
                                .resize(50, 50)
                                .centerCrop()
                                .into(img);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR_IMAGE", "" + e.getMessage());
                    }
                });

            }

            //Establecer contador mensajes no leidos
            TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig()
                    .endConfig()
                    .round();
            int contador_unread = QBUnreadMensajeHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if (contador_unread > 0) {
                TextDrawable unread_drawable = unreadBuilder.build("" + contador_unread, Color.RED);
                imgUnread.setImageDrawable(unread_drawable);
            }

        }
        return view;

    }

}
