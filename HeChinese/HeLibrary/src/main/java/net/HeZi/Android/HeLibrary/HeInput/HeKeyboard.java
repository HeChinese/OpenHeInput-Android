/*
  * Copyright (c) 2015 Guilin Ouyang. All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package net.HeZi.Android.HeLibrary.HeInput;

import net.HeZi.Android.HeLibrary.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.SparseArray;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class HeKeyboard extends Keyboard {

    public static final int KEYCODE_ABC_N_BACK = -11;
    public static final int KEYCODE_123_N_BACK = -12;

    private Key mCancelKey;
    private Key mModeKey;

    private Key m123AndBackKey;
    private Key mAbcAndBackKey;

    private Key mEnterKey;
    private Key mSpaceKey;
    /**
     * Stores the current state of the mode change key. Its width will be dynamically updated to
     * match the region of {@link #mModeChangeKey} when {@link #mModeChangeKey} becomes invisible.
     */
    private Key mModeChangeKey;
    /**
     * Stores the current state of the language switch key (a.k.a. globe key). This should be
     * visible while {@link InputMethodManager#shouldOfferSwitchingToNextInputMethod(IBinder)}
     * returns true. When this key becomes invisible, its width will be shrunk to zero.
     */
    private Key mLanguageSwitchKey;
    /**
     * Stores the size and other information of {@link #mModeChangeKey} when
     * {@link #mLanguageSwitchKey} is visible. This should be immutable and will be used only as a
     * reference size when the visibility of {@link #mLanguageSwitchKey} is changed.
     */
    private Key mSavedModeChangeKey;
    /**
     * Stores the size and other information of {@link #mLanguageSwitchKey} when it is visible.
     * This should be immutable and will be used only as a reference size when the visibility of
     * {@link #mLanguageSwitchKey} is changed.
     */
    //private Key mSavedLanguageSwitchKey;

    public final SparseArray<String> chinesePuncArray = new SparseArray<String>();

	public HeKeyboard(Context context, int xmlLayoutResId) {
		super(context, xmlLayoutResId);
		//setKeyboard(context.getResources(),true);
        populateChinesePunctureArray();
	}

	public HeKeyboard(Context context, int layoutTemplateResId,
			CharSequence characters, int columns, int horizontalPadding) {
		super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        populateChinesePunctureArray();
	}

	public HeKeyboard(Context context, int xmlLayoutResId, int modeId, int width,
			int height) {
		super(context, xmlLayoutResId, modeId, width, height);
        populateChinesePunctureArray();
	}

	public HeKeyboard(Context context, int xmlLayoutResId, int modeId) {
		super(context, xmlLayoutResId, modeId);
        populateChinesePunctureArray();
	}

    private void populateChinesePunctureArray() {

        chinesePuncArray.put(32, "　");
        chinesePuncArray.put(33, "！");
        chinesePuncArray.put(34, "“");
        chinesePuncArray.put(36, "￥");
        //chinesePuncArray.put(40, "（");
        chinesePuncArray.put(41, "）");
        chinesePuncArray.put(44, "，");
        chinesePuncArray.put(46, "。");
        chinesePuncArray.put(47, "、");
        chinesePuncArray.put(58, "：");
        chinesePuncArray.put(59, "；");
        chinesePuncArray.put(63, "？");
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
                                   XmlResourceParser parser) {
        Key key = new HeInputKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        }
        else if (key.codes[0] == 32) //' ')
        {
            mSpaceKey = key;
        } else if (key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE) {
            mModeChangeKey = key;
            mSavedModeChangeKey = new HeInputKey(res, parent, x, y, parser);
        }
        /*
        else if (key.codes[0] == HeKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            mLanguageSwitchKey = key;
            mSavedLanguageSwitchKey = new HeInputKey(res, parent, x, y, parser);
        }
        //*/
        else if (key.codes[0] == -3) {
            mCancelKey = key;
        } else if (key.codes[0] == 0) {
            mModeKey = key;
        } else if (key.codes[0] == -11) {
            mAbcAndBackKey = key;
        } else if (key.codes[0] == -12) {
            m123AndBackKey = key;
        }

        return key;
    }

    public boolean isControlKey(int primaryCode)
    {
        boolean bControlKey = false;
        switch(primaryCode)
        {
            case -1:	//Shift Key
            case -2:	//Mode Key
            case -3:	//Cancel Key, Keyboard.KEYCODE_CANCEL(Not right) it is  KEYCODE_ESCAPE
            case -5:	//Delete Key
            case -11:   //ABC and Back
            case -12:	//123 and Back Switch to symbol keyboard
            case -13:   //pinyin key
                //case -15:	//back to previous keyboard;
            case 38:	//Up arrow Key
            case 40:	//Down arrow key
            case 37:	//Left arrow Key
            case 39:	//right arrow key
            case 10:	//return Key
            case 32:	//space key  KeyEvent.KEYCODE_SPACE = 62
                bControlKey = true;
                break;
            default:
                break;
        }
        return bControlKey;
    }

    public boolean isPunctureKey(int primaryCode){

        boolean bPunctureKey = false;
        switch(primaryCode){
            case 32:    // space
            case 33:    // !
            case 34:    // "  &quot;
            case 36:    // $
            //case 40:    // (  it is use by down arrow key
            case 41:    // )
            case 44:    // ,
            case 46:    // .
            case 47:    // /
            case 58:    //:
            case 59:    //;
            case 63:    //?

            //case 35:    // #
            //case 43:    // +
            //case 45:    // -
            //case 61:    // =
            //case 64:    // @
            //case 92:    // \
            //case 95:    // _
                bPunctureKey = true;
                break;
            default:
                break;
        }
        return bPunctureKey;
    }

    public boolean isHeShuMaKey(int primaryCode)
    {
        boolean bHeShuMaKey = false;

        /*
            1. 48...57 for: 0123..9
            2. 97... 122 for: abc...xyz
         */

        if (primaryCode >= 48 && primaryCode <= 57 || primaryCode >= 97 && primaryCode <= 122) {
            bHeShuMaKey = true;
        }
        return bHeShuMaKey;
    }

    public int convertHeShuMaKey2ShuMa(int shuMaKeyCode) {

        int shuMa = -1;
        if (shuMaKeyCode >= 48 && shuMaKeyCode <= 57) {
            return shuMaKeyCode - 48;
        }

        switch (shuMaKeyCode) {
            case 102: //F
               shuMa = 11; break;
            case 101: //E
                shuMa = 12; break;
            case 116: //T
                shuMa = 13; break;
            case 106: //J
                shuMa = 14; break;
            case 122: //Z
                shuMa = 15; break;

            case 98: //B
                shuMa = 21; break;
            case 97: //A
                shuMa = 22; break;
            case 100: //D
                shuMa = 23; break;
            case 112: //P
                shuMa = 24; break;
            case 114: //R
                shuMa = 25; break;

            case 108: //L
                shuMa = 31; break;
            case 105: //I
                shuMa = 32; break;
            case 110: //N
                shuMa = 33; break;
            case 104: //H
                shuMa = 34; break;
            case 107: //K
                shuMa = 35; break;
            case 109: //M
                shuMa = 36; break;

            case 99: //C
                shuMa = 41; break;
            case 111: //O
                shuMa = 42; break;
            case 115: //S
                shuMa = 43; break;
            case 103: //G
                shuMa = 44; break;
            case 113: //Q
                shuMa = 45; break;

            case 118: //V
                shuMa = 51; break;
            case 117: //U
                shuMa = 52; break;
            case 119: //W
                shuMa = 53; break;
            case 121: //Y
                shuMa = 54; break;
            case 120: //X
                shuMa = 55; break;

            default:
                break;
        }

        return shuMa;
    }

    /**
     * Dynamically change the visibility of the language switch key (a.k.a. globe key).
     * @param visible True if the language switch key should be visible.
     */
    /*
    public void setLanguageSwitchKeyVisibility(boolean visible) {
        if (visible) {
            // The language switch key should be visible. Restore the size of the mode change key
            // and language switch key using the saved layout.
            mModeChangeKey.width = mSavedModeChangeKey.width;
            mModeChangeKey.x = mSavedModeChangeKey.x;
            mLanguageSwitchKey.width = mSavedLanguageSwitchKey.width;
            mLanguageSwitchKey.icon = mSavedLanguageSwitchKey.icon;
            mLanguageSwitchKey.iconPreview = mSavedLanguageSwitchKey.iconPreview;
        } else {
            // The language switch key should be hidden. Change the width of the mode change key
            // to fill the space of the language key so that the user will not see any strange gap.
            mModeChangeKey.width = mSavedModeChangeKey.width + mSavedLanguageSwitchKey.width;
            mLanguageSwitchKey.width = 0;
            mLanguageSwitchKey.icon = null;
            mLanguageSwitchKey.iconPreview = null;
        }
    }
    //*/

    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    public void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }
        
        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_go_key);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_next_key);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = res.getDrawable(R.mipmap.sym_keyboard_search);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_send_key);
                break;
            default:
                mEnterKey.icon = res.getDrawable(R.mipmap.sym_keyboard_return);
                mEnterKey.label = null;
                break;
        }
    }
	
    public void setSpaceIcon(final Drawable icon) {
        if (mSpaceKey != null) {
            mSpaceKey.icon = icon;
        }
    }
	
    static class HeInputKey extends Keyboard.Key {
        
        public HeInputKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }
        
        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard. 
         */
        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
    }
}
