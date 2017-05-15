package com.trabajo.carlos.sender.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.trabajo.carlos.sender.R;
import com.trabajo.carlos.sender.holder.QBUsuariosHolder;

import java.util.ArrayList;

public class MensajeChatAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    public MensajeChatAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //Comprobamos si el mensaje ha sido enviado por el usuario actual
            if (qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId())) {
                view = inflater.inflate(R.layout.list_mensaje_enviado, null);

                BubbleTextView btxvMensaje = (BubbleTextView) view.findViewById(R.id.btxvMensaje);
                btxvMensaje.setText(qbChatMessages.get(position).getBody());
            } else {
                view = inflater.inflate(R.layout.list_mensaje_recibido, null);

                BubbleTextView btxvMensaje = (BubbleTextView) view.findViewById(R.id.btxvMensaje);
                btxvMensaje.setText(qbChatMessages.get(position).getBody());

                TextView txvUsuarioMensaje = (TextView) view.findViewById(R.id.txvUsuarioMensaje);
                txvUsuarioMensaje.setText(QBUsuariosHolder.getInstance().getUserById(qbChatMessages.get(position).getSenderId()).getFullName());
            }
        }
        return view;
    }

}
