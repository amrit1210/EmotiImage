package com.google.android.gms.samples.vision.face.facetracker.settings;

import com.google.android.gms.samples.vision.face.facetracker.EmotionEnums;

/**
 * Created by amritkaur on 17-04-2017.
 */

public class Emoticons {
    public static final int NEUTRAL_FACE = 0x1f610;
    public static final int SMILING_FACE_WITH_OPEN_MOUTH = 0x1f603;
    public static final int PENSIVE_FACE = 0x1f614;
    public static final int WINKING_FACE = 0x1f609;
    public static final int GRINNING_FACE = 0x1f601;
    public static final int POUT_FACE = 0x1f617;
    public static final int HUSHED_FACE = 0x1F62F;
    public static final int PERSERVERING_FACE = 0x1F623;


    public static EmotionEnums getMood(int position) {
        EmotionEnums mood = EmotionEnums.values()[position];
        return mood;
    }

    public static String getEmoticon(EmotionEnums mood) {
        int unicode = -1;
        switch (mood) {
            /*case NONE:
                unicode = 0;
                break;*/

            case NEUTRAL:
                unicode = NEUTRAL_FACE;
                break;

            case HAPPY:
                unicode = SMILING_FACE_WITH_OPEN_MOUTH;
                break;

            case SAD:
                unicode =  PENSIVE_FACE;
                break;

            case WINKING:
                unicode =  WINKING_FACE;
                break;

            case GRINNING:
                unicode =  GRINNING_FACE;
                break;

            case POUT:
                unicode = POUT_FACE;
                break;

            case BLINKING:
                unicode = PERSERVERING_FACE;
                break;

            /*case SURPRISED:
                unicode = HUSHED_FACE;
                break;*/
        }
        return  getEmojiByUnicode(unicode);
    }

    public static String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

}
