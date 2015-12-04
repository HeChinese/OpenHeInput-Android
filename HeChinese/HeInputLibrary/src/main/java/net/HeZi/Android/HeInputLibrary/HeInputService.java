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

package net.HeZi.Android.HeInputLibrary;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.HeZi.Android.HeInputLibrary.HeInput_DataServer.OnDataServerListener;
import net.HeZi.Android.HeLibrary.HeInput.HeKeyboard;
import net.HeZi.Android.HeLibrary.HeInput.HeKeyboardView;
import net.HeZi.Android.HeLibrary.HeInput.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class HeInputService extends InputMethodService
implements KeyboardView.OnKeyboardActionListener, CandidateListView.CandidateItemInteractionListener
{    	
	static final boolean DEBUG = false;
    private HeInput_DataServer dataServer;

    @Override
	public void onDestroy() {
		// TODO Auto-generated method stub
    	//engineDBHelper.close();
    	dataServer.clearState();
    	setCandidatesViewShown(false);
    	mInputView.closing();
    	super.onDestroy();
	}

	/**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on 
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    private InputMethodManager mInputMethodManager;
    private HeKeyboardView mInputView;
    private CandidateListView mCandidateListView;
    public TextView typedMaView;
    private TextView pinYinPromptView;
    private TextView pageIndicatorView;
    private CompletionInfo[] mCompletions;
    
    //private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private boolean wantChinesePuncture = true;
    private boolean bPreviousPunctuation = false;

    private boolean doubleQuoteOpen = false; //34
    private boolean singleBracketOpen = false; //41, actually 41 is ")" close bracket

    private HeKeyboard heKeyboard_4x6;
    private HeKeyboard heKeyboard_6x7;

    private HeKeyboard mSymbolsKeyboard;
    private HeKeyboard mSymbolsShiftedKeyboard;
    private HeKeyboard mQwertyKeyboard;
    
    private HeKeyboard mCurKeyboard;  
    private HeKeyboard mPreKeyboard;
    private String mWordSeparators;
    
    private SharedPreferences sharedPreferences;
    
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    //Step 1
    //The IME Lifecycle
    //http://developer.android.com/guide/topics/text/creating-input-method.html
    @Override public void onCreate() {
        super.onCreate();
        //Log.d("","OnCreate.....1");
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
        
        openOrCreateSharedPreferencesFile();

        dataServer = new HeInput_DataServer(this, getSettingFromSharedPreference());
        
        dataServer.setOnDataServerListener(dataServerListener);  
        dataServer.setMaxItemsOfPage(getResources().getInteger(R.integer.page_max_candidate));
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    //Step 2
    //The IME Lifecycle
    //http://developer.android.com/guide/topics/text/creating-input-method.html
    @Override public void onInitializeInterface() 
    {
        Log.d("", "onInitializeInterface.....2");

        if (heKeyboard_4x6 != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        else
        {
	        heKeyboard_4x6 = new HeKeyboard(this, R.xml.keyboard_4x6);
	        heKeyboard_6x7 = new HeKeyboard(this, R.xml.keyboard_6x7); //R.xml.keyboard_6x6_horizontal
	        //englishKeyboard_6x6 = new HeKeyboard(this, R.xml.english_keyboard_6x6);
	        //heEnglishKeyboard_6x6 = new HeKeyboard(this, R.xml.he_english_keyboard_6x6);

            mQwertyKeyboard = new HeKeyboard(this, R.xml.qwerty);
	        mSymbolsKeyboard = new HeKeyboard(this, R.xml.symbols);
	        mSymbolsShiftedKeyboard = new HeKeyboard(this, R.xml.symbols_shift);
        }
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    //Step 4
    //The IME Lifecycle
    //http://developer.android.com/guide/topics/text/creating-input-method.html
    @Override public View onCreateInputView() {
        //Log.d("","onCreateInputView.....4");

        mInputView = (HeKeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        mInputView.setOnKeyboardActionListener(this);
        setHeKeyboard(heKeyboard_4x6);
        return mInputView;
    }

    // CandidateListView Interface
    // When user typed candidate item, implement the input
    @Override
    public void onItemInteraction(int itemIndexOnCurrentPage)
    {
        if(dataServer.isMenuShow()) {
                handleCharAndNumber(dataServer.getItemPromptMaByIndex(itemIndexOnCurrentPage),null);
        }
        else if (dataServer.isInputable()) {
            String ziCiStr = dataServer.getItemZiCiStrByIndex(itemIndexOnCurrentPage);
            heCommitText(ziCiStr, true, true);
            dataServer.clearState();
            updateCandidates();
        }
    }

    private void setHeKeyboard(HeKeyboard nextKeyboard) {
        /*
        final boolean shouldSupportLanguageSwitchKey =
                mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
        nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        //*/

        mInputView.setKeyboard(nextKeyboard);
        //mInputView.setBackgroundColor(Color.CYAN);
        //mInputView.setBackground(getResources().getDrawable(R.drawable.he_keyboard_background));
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    //Step 5
    @Override public View onCreateCandidatesView() {

        //Log.d("","onCreateCandidatesView.....5");
        LinearLayout mainLayout =
                (LinearLayout) getLayoutInflater().inflate(R.layout.candidate_view_layout, null);

        mCandidateListView = (CandidateListView) mainLayout.findViewById(R.id.customListView);

        typedMaView = (TextView) mainLayout.findViewById(R.id.typedMa);
        pinYinPromptView = (TextView)mainLayout.findViewById(R.id.pinYinPrompt);
        pageIndicatorView = (TextView)mainLayout.findViewById(R.id.pageIndicator);

        mCandidateListView.setService(this);
        setCandidatesViewShown(false);
        return mainLayout;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    //Step 3
    @Override 
    public void onStartInput(EditorInfo attribute, boolean restarting) 
    {
        super.onStartInput(attribute, restarting);
        
        //Log.d("","onStartInput.....3");

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        //mComposing.setLength(0);
        
        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
            dataServer.clearState();
        }
        
        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        
        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) 
        {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
            	//mPreKeyboard = mCurKeyboard;
                mCurKeyboard = mSymbolsKeyboard;
                break;
                
            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                break;
                
            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
				mPredictionOn = true;
		    	dataServer.clearState();
       			switch(dataServer.setting.currentKeyMode)
    			{
                    case HeMa_Simplified_Mode:
                    case HeMa_Traditional_Mode:
                        if(dataServer.setting.bHeMaModeNumpad) {
    							mCurKeyboard = heKeyboard_4x6;
    					}
    					else {
    							mCurKeyboard = heKeyboard_6x7;
    					}
    					break;
    				case PinYinMode:
    					if(dataServer.setting.bPinYinModeNumpad) {
    							mCurKeyboard = heKeyboard_4x6;
    					}
    					else {
    							mCurKeyboard = mQwertyKeyboard;//heEnglishKeyboard_6x6;
    					}
    					break;
    				case HeEnglishMode:
    					if(dataServer.setting.bHeEnglishModeNumpad) {
    							mCurKeyboard = heKeyboard_4x6;
    					}
    					else {
    							mCurKeyboard = mQwertyKeyboard;//heEnglishKeyboard_6x6;
    					}
    					break;
    				case EnglishMode:
    					mPredictionOn = false;
    						mCurKeyboard = mQwertyKeyboard;// englishKeyboard_6x6;
    					break;
    				default:
    					break;
    			}
                
                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                    mCurKeyboard = mQwertyKeyboard;//englishKeyboard_6x6;
                }
                
                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                    mCurKeyboard = mQwertyKeyboard;//englishKeyboard_6x6;
                }
                
                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCurKeyboard = mQwertyKeyboard;//englishKeyboard_6x6;
                    mCompletionOn = isFullscreenMode();
                }
                
                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;
                
            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = heKeyboard_4x6;
                updateShiftKeyState(attribute);
        }
        
        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();
        
        // Clear current composing text and candidates.
        //mComposing.setLength(0);
        dataServer.clearState();
        
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    //Step 6
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting)
    {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        //Log.d("","onStartInputView.....6");

        setHeKeyboard(mCurKeyboard);
        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        mInputView.setSubtypeOnSpaceKey(subtype);

        if (subtype.getExtraValueOf("subTypeName").contains("heMa_simplified")) {
            //ToDo: set input method to Chinese simplified
            dataServer.setting.systemKeyMode = Setting.InputMode.HeMa_Simplified_Mode;
        }
        else if (subtype.getExtraValueOf("subTypeName").contains("heMa_traditional")){
            //ToDo: set input method to Chinese traditional
            dataServer.setting.systemKeyMode = Setting.InputMode.HeMa_Traditional_Mode;
        }
        dataServer.setting.currentKeyMode = dataServer.setting.systemKeyMode;
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) 
    {
        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (/*mComposing.length() > 0 && */(newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) 
        {
            //mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                //setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            //setSuggestions(stringList, true, true);
        }
    }
    
    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }
        
        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }
        /*
        if (mComposing.length() > 0) 
        {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }
        //*/
        onKey(c, null);
        
        return true;
    }
    
    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
   
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;
                
            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                /*
            	if (mComposing.length() > 0) 
                {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                //*/
                break;
                
            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) 
                {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) 
                    {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped()
    {
    	//Log.d("CommitTyped function->", "SelectedItemIndex: "+typingState.selectedIndex);
    	String ziCiStr = dataServer.getSelectedZiCiStr();
    	
    	if(ziCiStr.length() > 0)
    	{
       		heCommitText(ziCiStr,true,true);
    		dataServer.clearState();
            updateCandidates();
    	}
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null 
                && mInputView != null && mQwertyKeyboard /*englishKeyboard_6x6*/ == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }
    
    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    heCommitText(String.valueOf((char) keyCode),false,false);
                }
                break;
        }
    }

    //Implementation of KeyboardViewListener
    public void onKey(int primaryCode, int[] keyCodes)
    {
        if(mCurKeyboard.isControlKey(primaryCode))
        {
            handleControlKey(primaryCode);
        }
        /*
        else if (primaryCode == HeKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
            return;
        } //*/
        //*
        else if (!mPredictionOn && isWordSeparator(primaryCode)) {
            // Handle separator
            //if (mComposing.length() > 0) {
            //    commitTyped(getCurrentInputConnection());
            //}
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        }
        //*/
        else if (!mPredictionOn) {
            //sendKey(primaryCode);
            //updateShiftKeyState(getCurrentInputEditorInfo());

            if(mCapsLock)
            {
                getCurrentInputConnection().commitText(String.valueOf((char) (primaryCode-32)), 1);
                checkToggleCapsLock();
                mInputView.setShifted(mCapsLock || !mInputView.isShifted());
            }
            else
            {
                getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
            }
        }
        else if(mCurKeyboard.isHeShuMaKey(primaryCode)){
            handleCharAndNumber(mCurKeyboard.convertHeShuMaKey2ShuMa(primaryCode), keyCodes);
        }
        else if (mCurKeyboard.isPunctureKey(primaryCode)) {
            handlePunctureKey(primaryCode);
        }
    }

    private void handlePunctureKey(int primaryCode) {

        // not chinese puncture
        if(!wantChinesePuncture) {
            if (primaryCode == 41) {
                if(!singleBracketOpen) {
                    heCommitText(String.valueOf((char) 40), false, true);
                } else {
                    heCommitText(String.valueOf((char) 41), false, true);
                }
                singleBracketOpen = !singleBracketOpen;
            }
            else
                heCommitText(String.valueOf((char) primaryCode), false, true);
            return;
        }

        // Chinese puncture input
        String puncStr = "";
        if(primaryCode == 34) { // doubleQuate
            if(!doubleQuoteOpen) {
                puncStr = "“";
            } else {
                puncStr = "”";
            }
            doubleQuoteOpen = !doubleQuoteOpen;
        }
        else if(primaryCode == 41) { // singleBracket
            if(!singleBracketOpen) {
                puncStr = "（";//mCurKeyboard.chinesePuncArray.get(primaryCode);
            } else {
                puncStr = "）";
            }
            singleBracketOpen = !singleBracketOpen;
        }
        else
            puncStr = mCurKeyboard.chinesePuncArray.get(primaryCode);

        heCommitText(puncStr, true, true);
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        
        /*
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        //*/
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() 
    {
    	if(mCandidateListView != null)
    	{
    		updateTypedMa();
    		updatePinYinPrompt();
    		updatePageIndicator();
            mCandidateListView.printListViewPage(dataServer.onePageRows, dataServer.getItemIndex());
    		if(dataServer.numOfCand>0)
              {
    			setCandidatesViewShown(true);
              }
    	}
    }
    
    public void setSuggestions(List<String> suggestions, boolean completions,
            boolean typedWordValid) 
    {
    	mCandidateListView.printListViewPage(dataServer.onePageRows, dataServer.getItemIndex());
        setCandidatesViewShown(true);
        
        if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
    }
    
    private void handleBackspace() {
    	//When numOfCand>0 it could be empty list for keep the listview exist.
    	if(dataServer.onePageRows.size()>0)
    	{
    		if(dataServer.typingCharAndNumber(100)) //in android -2 is backspace, in TypingState 100 is backspace
    		{
    			updateCandidates();
    		}
    	}
    	else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
            if(bPreviousPunctuation) {
                wantChinesePuncture = !wantChinesePuncture;
            }
    	}
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() 
    {
        if (mInputView == null) {
            return;
        }
        
        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard /*englishKeyboard_6x6*/ == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            setHeKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            setHeKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
    }

    private void heCommitText(String text, boolean bChinese, boolean bPuncture) {
        bPreviousPunctuation = bPuncture;
        wantChinesePuncture = bChinese;
        getCurrentInputConnection().commitText(text, 1);
    }

    private void handleCharAndNumber(int shuMa, int[] keyCodes) {
    	
    	if(dataServer.isMenuShow())
    	{
    		if(dataServer.typingCharAndNumber(shuMa))
            {
            		updateCandidates();
        	}
    	}
    	else if(dataServer.typingCharAndNumber(shuMa))
        {
        		updateCandidates();
    	}
    }

    private void handleClose() {
        //commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void handleLanguageSwitch() {
        Log.d("Debug","handleLanguageSwitch.");
        mInputMethodManager.switchToNextInputMethod(getToken(), true); // false:  onlyCurrentIme
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 < now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }
    
    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            //
            //if (mCandidateView != null) {
                //mCandidateView.clear();
            //}
            updateShiftKeyState(getCurrentInputEditorInfo());
        } 
        /*
        else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
        //*/
    }
    
    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }
    
    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }
    
    public void onPress(int primaryCode) {
    }
    
    public void onRelease(int primaryCode) {
    }
    
    //Handle Control key
    private void handleControlKey(int primaryCode)
    {
    	switch(primaryCode)
    	{
    		case -1:	//Shift Key
    			handleShift();
    			break;
    		case -2:	//Mode Key  Keyboard.KEYCODE_MODE_CHANGE
    			if(dataServer.isMenuShow())
    			{
    				dataServer.clearState();
    				setCandidatesViewShown(false);
    			}
    			else if(dataServer.typingCharAndNumber(-2))
                {
                		updateCandidates();
                }   
    			break;
    		case -3:	//Cancel Key, Keyboard.KEYCODE_ESCAPE,
    			if(dataServer.numOfCand>0)
        		{
        			dataServer.clearState();
        			if(mCandidateListView != null)
        			{
        				setCandidatesViewShown(false);
        			}
        		}
        		else
        		{
        			setCandidatesViewShown(false);
        			handleClose();
                    //let application to handle keyboad show/hide
                    //It is used in CustomTextView of HeBook application.
                    keyDownUp(KeyEvent.KEYCODE_ESCAPE);
        		}
    			break;
    		case -5:	//Delete Key
    			handleBackspace();
    			break;
            case -11:		//ABC keyboard or Back to previous keyboard
            {
                if (mCurKeyboard == mQwertyKeyboard) {
                    mCurKeyboard = heKeyboard_4x6;
                    dataServer.setting.currentKeyMode = dataServer.setting.systemKeyMode;
                    mPredictionOn = true;
                }
                else
                {
                    mCurKeyboard = mQwertyKeyboard;
                    dataServer.setting.currentKeyMode = Setting.InputMode.EnglishMode;
                    mPredictionOn = false;
                }
                setHeKeyboard(mCurKeyboard);
                dataServer.clearState();
                setCandidatesViewShown(false);
            }
            break;
            case -12:	//Number keyboard and back to previous keyboard
            {
                //Switch to Number keyboard
                if (mCurKeyboard == mQwertyKeyboard || mCurKeyboard == heKeyboard_4x6) {
                    mPredictionOn = false;
                    mPreKeyboard = mCurKeyboard;
                    mCurKeyboard = mSymbolsKeyboard;
                } else if (mCurKeyboard == mSymbolsKeyboard) {
                    if (mPreKeyboard == mQwertyKeyboard) {
                        mCurKeyboard = mQwertyKeyboard;
                        mPredictionOn = false;
                    } else {
                        mCurKeyboard = heKeyboard_4x6;
                        mPredictionOn = true;
                    }
                } else if (mCurKeyboard == mSymbolsShiftedKeyboard) {
                    mCurKeyboard = mSymbolsKeyboard;
                    mPredictionOn = false;
                }
                setHeKeyboard(mCurKeyboard);
                dataServer.clearState();
                setCandidatesViewShown(false);
            }
            break;
            case -13:	//PinYin key
            {
                //Switch to PinYin Mode and English keyboard
                if (mCurKeyboard != mQwertyKeyboard) {
                    mCurKeyboard = mQwertyKeyboard;
                    dataServer.setting.currentKeyMode = Setting.InputMode.PinYinMode;
                    mPredictionOn = true;
                }
                setHeKeyboard(mCurKeyboard);
                setCandidatesViewShown(false);
                dataServer.clearState();
            }
            break;
       	case 38:	//Up arrow Key
    			if(dataServer.changeItemIndexBy(-1))
        		{
        			mCandidateListView.setItemChecked(dataServer.itemIndex, true);
        			updatePinYinPrompt();
        		}
    			else if(dataServer.numOfCand>0) //but list page does not change
    			{
    				;
    			}
    			else //numOfCand == 0
    			{
    				keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
    			}
    			break;
    		case 40:	//Down arrow key
    			if(dataServer.changeItemIndexBy(1))
        		{
        			mCandidateListView.setItemChecked(dataServer.itemIndex, true);
        			updatePinYinPrompt();
        		}
    			else if(dataServer.numOfCand>0) //but list page does not change
    			{
    				;
    			}
    			else //numOfCand == 0
    			{
    				keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
    			}
                break;
    		case 37:	//Left arrow Key
    			if(dataServer.changePageIndexBy(-1)) //list page changed
        		{
        			mCandidateListView.printListViewPage(dataServer.onePageRows, dataServer.getItemIndex());
        			pageIndicatorView.setText(dataServer.getPageIndicatorStr());
        			updatePinYinPrompt();
        		}
    			else if(dataServer.numOfCand>0) //but list page does not change
    			{
    				;
    			}
    			else //numOfCand == 0
    			{
    				keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
    			}
    			break;
    		case 39:	//right arrow key
    			if(dataServer.changePageIndexBy(1))
        		{
        			mCandidateListView.printListViewPage(dataServer.onePageRows,dataServer.getItemIndex());
        			pageIndicatorView.setText(dataServer.getPageIndicatorStr());
        			updatePinYinPrompt();
        		}
    			else if(dataServer.numOfCand>0) //but list page does not change
    			{
    				;
    			}
    			else //numOfCand == 0
    			{
    				keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
    			}
    			break;
    		case 10:	//return Key
    			if(dataServer.isMenuShow())
    	    	{
    	    		handleCharAndNumber(dataServer.getSelectedZiCiPromptMa(),null);
    	     	}
    			else if(dataServer.isInputable())
    			{
        			commitTyped();
    			}
        		else
        		{
        			keyDownUp(KeyEvent.KEYCODE_ENTER);
        		}
    			break;
    		case 32:	//space key  KeyEvent.KEYCODE_SPACE = 62
    			if(dataServer.isMenuShow())
    	    	{
                    handleCharAndNumber(dataServer.getSelectedZiCiPromptMa(),null);
    	     	}
    			else if(dataServer.isInputable())
    			{
        			commitTyped();
    			}
        		else
        		{
        			//getCurrentInputConnection().commitText(" ", 1);
                    if(wantChinesePuncture)
                        heCommitText("　",true,true);
                    else
                        heCommitText(" ",false,true);
        		}
    			break;
    		case -100:	//HeKeyboardView.KEYCODE_OPTIONS
    			break;
    		default:
    			break;
    	}
    }
    
    private void updateTypedMa()
    {
    	if(typedMaView != null)
    	{
    		typedMaView.setText(dataServer.getTypedStr());
    	}
    }

    private void updatePinYinPrompt()
    {
    	if(pinYinPromptView != null)
    	{
    		pinYinPromptView.setText(dataServer.pinYinPromptStr);
    	}
    }

    private void updatePageIndicator()
    {
    	if(pageIndicatorView != null)
    	{
    		pageIndicatorView.setText(dataServer.getPageIndicatorStr());
    	}
    }

    protected void openOrCreateSharedPreferencesFile()
	{
     	/**
		* "net.HeZi.Android.SharedPreferenceFile" is shared by user session and all HeChinese apps will share it,
		* each user space will have one of this file created.
		* "***This data will persist across user sessions (even if your application is killed)***"
		* That means only when user is deleted from device, 
		* then net.HeZi.Android.SharedPreferenceFile will be removed
		* "HC_USER_ID" is the ID to identifier this file, and will saved in HeZi.net web server.
		* HC_USER_ID is unique for each user, since each device can have more users.
		* HC_USER_ID is not changeable.
		*/ 
    	
       	//Can't use getPreferences(); since it is service application
    	sharedPreferences = getSharedPreferences("net.HeZi.Android.SharedPreferenceFile",MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();

 		String uniqueUserID = sharedPreferences.getString("HC_USER_ID", null);
 		//That means sharedPreferences file does not exist
 		if (uniqueUserID == null) {
            uniqueUserID = UUID.randomUUID().toString();
            editor.putString("HC_USER_ID", uniqueUserID);
            String android_ID = Secure.getString(getContentResolver(), Secure.ANDROID_ID); 
    		editor.putString("Device_UDID", android_ID);
            //editor.commit();
        }

		//Initial sharedPreferences for HeInput, Only first time runs for this user
		if(!sharedPreferences.contains("HeInput_Simplified_Chinese"))
		{
			editor.putBoolean("HeInput_Simplified_Chinese", true);
			editor.putBoolean("HeInput_Normal_ZiKu", true);
			editor.putBoolean("HeInput_PinYin_Prompt", true);
			editor.putBoolean("HeInput_LianXiang", false);
			
			editor.putBoolean("HeInput_HeMaModeNumpad", true);
			editor.putBoolean("HeInput_PinYinModeNumpad", true);
			editor.putBoolean("HeInput_HeEnglishModeNumpad", true);
			
			//editor.commit();
		}
		editor.commit();
	}
    
    private Setting getSettingFromSharedPreference()
    {
    	Setting setting = new Setting();
    	
        if (sharedPreferences.getBoolean("HeInput_Simplified_Chinese", true)) {
            setting.systemKeyMode = Setting.InputMode.HeMa_Simplified_Mode;
        }
        else {
            setting.systemKeyMode = Setting.InputMode.HeMa_Traditional_Mode;
        }
        //setting.systemKeyMode = Setting.InputMode.HeMa_Traditional_Mode;
        setting.currentKeyMode = setting.systemKeyMode;

    	setting.bNormalZiKu = sharedPreferences.getBoolean("HeInput_Normal_ZiKu", true);
    	setting.bPinYinPrompt = sharedPreferences.getBoolean("HeInput_PinYin_Prompt", true);
    	setting.bLianXiang = sharedPreferences.getBoolean("HeInput_LianXiang", false);		
    	
    	setting.bHeMaModeNumpad = sharedPreferences.getBoolean("HeInput_HeMaModeNumpad", true);		
    	setting.bPinYinModeNumpad = sharedPreferences.getBoolean("HeInput_PinYinModeNumpad", true);		
    	setting.bHeEnglishModeNumpad = sharedPreferences.getBoolean("HeInput_HeEnglishModeNumpad", true);		

    	return setting;
    }
    
    protected OnDataServerListener dataServerListener = new OnDataServerListener() 
    {
		@Override
		public void keyboardChange(Setting.InputMode inputMode)
		{
			switch(inputMode)
			{
				case HeMa_Simplified_Mode:
                case HeMa_Traditional_Mode:
					mPredictionOn = true;
					if(dataServer.setting.bHeMaModeNumpad)
					{
						if(mCurKeyboard != heKeyboard_4x6)
						{
					    	dataServer.clearState();
							mCurKeyboard = heKeyboard_4x6;
							setHeKeyboard(mCurKeyboard);
						}
					}
					else
					{
						if(mCurKeyboard != heKeyboard_6x7)
						{
					    	dataServer.clearState();
							mCurKeyboard = heKeyboard_6x7;
							setHeKeyboard(mCurKeyboard);
						}						
					}
					break;
				case PinYinMode:
					mPredictionOn = true;
					if(dataServer.setting.bPinYinModeNumpad)
					{
						if(mCurKeyboard != heKeyboard_4x6)
						{
					    	dataServer.clearState();
							mCurKeyboard = heKeyboard_4x6;
							setHeKeyboard(mCurKeyboard);
						}
					}
					else
					{
						if(mCurKeyboard != mQwertyKeyboard /*englishKeyboard_6x6*/)
						{
					    	dataServer.clearState();
							mCurKeyboard = mQwertyKeyboard /*englishKeyboard_6x6*/;
							setHeKeyboard(mCurKeyboard);
						}						
					}
					break;
				case HeEnglishMode:
					mPredictionOn = true;
					if(dataServer.setting.bHeEnglishModeNumpad)
					{
						if(mCurKeyboard != heKeyboard_4x6)
						{
					    	dataServer.clearState();
							mCurKeyboard = heKeyboard_4x6;
							setHeKeyboard(mCurKeyboard);
						}
					}
					else
					{
						if(mCurKeyboard != mQwertyKeyboard /*englishKeyboard_6x6*/)
						{
					    	dataServer.clearState();
							mCurKeyboard = mQwertyKeyboard /*englishKeyboard_6x6*/;
							setHeKeyboard(mCurKeyboard);
						}						
					}
					break;
				case NumberMode:
					mPredictionOn = false;
					if(mCurKeyboard != heKeyboard_4x6)
					{
				    	dataServer.clearState();
						mCurKeyboard = heKeyboard_4x6;
						setHeKeyboard(mCurKeyboard);
					}
					break;
				case EnglishMode:
					mPredictionOn = false;
					if(mCurKeyboard != mQwertyKeyboard /*englishKeyboard_6x6*/)
					{
				    	dataServer.clearState();
						mCurKeyboard = mQwertyKeyboard /*englishKeyboard_6x6*/;
						setHeKeyboard(mCurKeyboard);
					}
					break;
				default:
					break;
			}
	    	setCandidatesViewShown(false);
			//Log.d("Trace..","menu 52 selected.....2....... ");			
		}

		@Override
		public void saveSharedPreferences() 
		{
			SharedPreferences.Editor editor = sharedPreferences.edit();
						
            if (dataServer.setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode) {
                editor.putBoolean("HeInput_Simplified_Chinese", true);
            }

			editor.putBoolean("HeInput_Normal_ZiKu", dataServer.setting.bNormalZiKu);
			editor.putBoolean("HeInput_PinYin_Prompt", dataServer.setting.bPinYinPrompt);
			editor.putBoolean("HeInput_LianXiang", dataServer.setting.bLianXiang);
			
			editor.putBoolean("HeInput_HeMaModeNumpad", dataServer.setting.bHeMaModeNumpad);
			editor.putBoolean("HeInput_PinYinModeNumpad", dataServer.setting.bPinYinModeNumpad);
			editor.putBoolean("HeInput_HeEnglishModeNumpad", dataServer.setting.bHeEnglishModeNumpad);

			editor.commit();
		}

		@Override
		public void commitString(String typedString) 
		{
			if(typedString.length() > 0)
	    	{
				//getCurrentInputConnection().commitText(typedString, 1);
                heCommitText(typedString,true,true);
	    		dataServer.clearState();
	    		//setCandidatesViewShown(false);
	    	}    	
		}

		@Override
		public void updateTypedMaView() {
			// TODO Auto-generated method stub
			updateTypedMa();
		}

		@Override
		public void changeSelection(int byNum) {
			// TODO Auto-generated method stub
			onKey(-63, null); //arror down key
		}
    };
}
