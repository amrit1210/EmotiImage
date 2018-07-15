package com.google.android.gms.samples.vision.face.facetracker.settings;

/**
 * Created by amritkaur on 17-04-2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.samples.vision.face.facetracker.EmotionEnums;
import com.google.android.gms.samples.vision.face.facetracker.R;

public class CustomAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public CustomAdapter(Context context) {
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return EmotionEnums.values().length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.custom_emoticon_item, null);
            holder.emoticonName = (TextView) view.findViewById(R.id.emoticonName);
            holder.emoticon = (TextView) view.findViewById(R.id.emoticon);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        EmotionEnums mood = Emoticons.getMood(position);
        String emojiUnicode = Emoticons.getEmoticon(mood);
        holder.emoticonName.setText(mood.toString() + " ");
        if (emojiUnicode != "0") {
            holder.emoticon.setText(emojiUnicode);
        }
        return view;
    }
}

class ViewHolder {
    TextView emoticonName;
    TextView emoticon;
}