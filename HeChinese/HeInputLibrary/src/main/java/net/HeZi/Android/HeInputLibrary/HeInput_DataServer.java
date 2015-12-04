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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.HeZi.Android.HeLibrary.HeBase.HeSQLiteOpenHelper;
import net.HeZi.Android.HeLibrary.HeBase.ZiCiObject;
import net.HeZi.Android.HeLibrary.HeInput.Setting;
import net.HeZi.Android.HeLibrary.HeInput.TypingState;
import net.HeZi.Android.HeInputLibrary.InputEngine.EngineCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HeInput_DataServer
{
	public Setting setting;
	private TypingState typingState;
	
	private HeSQLiteOpenHelper heInput_dBHelper;
	private EngineCollection engineCollection;
	private Context context;
	// We will keep Database open during input system active
	private SQLiteDatabase hemaDatabase;
	
	public int numOfCand;
    public int itemIndex;

	private int maxItemsOfPage;
    private int numOfItemsInCurrentPage;
    private int pageIndex;
    public TypingState.TypeSessionState typeSessionState;
    public String pinYinPromptStr;
    
    private String typedString;
    
    public interface OnDataServerListener {
		void keyboardChange(Setting.InputMode inputMode);
		void saveSharedPreferences();
		void commitString(String typedString);
		void updateTypedMaView();
		void changeSelection(int byNum);
	}

	private OnDataServerListener dataServerListener;
	
	public void setOnDataServerListener(OnDataServerListener listener) {
		dataServerListener = listener;
	}

    public List<HashMap<String, String>> onePageRows = new ArrayList<HashMap<String, String>>();

	@Override
	protected void finalize() throws Throwable 
	{
		Log.d("HeInput_DataServer","heMa database closed...........");
		if (hemaDatabase.isOpen())
			hemaDatabase.close();

		heInput_dBHelper.close();
		super.finalize();
	}

	public HeInput_DataServer(Context cxt, Setting set) 
	{
		context = cxt;
		numOfCand = 0;
		itemIndex = 0;
		pageIndex = 0;

 		setting = set;
 		typingState = new TypingState();

 		ApplicationInfo ai = null;
 		int db_version = 1;
		String mydb_file_name;// = "hema_db.sqlite";
 		
 		try 
 		{
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
 		} 
 		catch (NameNotFoundException e) 
 		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

 	    db_version = Integer.parseInt(ai.metaData.get("hema_db_version").toString());
 	    mydb_file_name = ai.metaData.get("hema_db_name").toString();
 		
 		heInput_dBHelper = new HeSQLiteOpenHelper(context,mydb_file_name, db_version);
	    try 
	    {
	    	heInput_dBHelper.createDatabase();
	 	} 
	    catch (IOException ioe) 
	    {
	 		throw new Error("Unable to create database");
	 	}

		try
		{
			heInput_dBHelper.openDatabase();
			heInput_dBHelper.close();
			hemaDatabase = heInput_dBHelper.getReadableDatabase();
			Log.d("HeInput_DataServer","HeInput Database opened..........");
		}
		catch (SQLException mSQLException)
		{
			Log.e("Open Database", "open >>" + mSQLException.toString());
			throw new Error("Unable to open database");
		}

	 	if(hemaDatabase != null)
	 	{
	 		setting.bHealthy = true;
			engineCollection = new EngineCollection(hemaDatabase, generateMenuDictionary());
		}
	 	else
	 	{
	 		setting.bHealthy = false;
	 	}
	 	
	 	//Java is always pass-by-value.
	 	//The difficult thing can be to understand that Java passes objects as references 
	 	//and those references are passed by value.
	}
		
	public boolean typing4Modes(int typedShuMa)
	{
		boolean bRet = false;
		if(isMenuShow()) {
			bRet =typingState.typeShuMa(typedShuMa);
		}
		else {
			switch(setting.currentKeyMode) {

				case HeMa_Traditional_Mode:
				case HeMa_Simplified_Mode:
					bRet =typingState.typeShuMa(typedShuMa);
					break;
				case PinYinMode:
				case HeEnglishMode:
					bRet =typingState.typeEngCharShuMa(typedShuMa);	
					break;
				case NumberMode:
				case EnglishMode:
					//When mode key 
					break;
				default:
					break;
			}
		}
		return bRet;
	}

	// return true, typed shuMa is valid, and produced valid rowsList
	public boolean typingCharAndNumber(int typedShuMa)
	{
		//Log.d("Debug","Location 0 M1:"+typingState.ma1+", MaShu="+typingState.maShu+",TypedMa: "+typedShuMa);

		// Continuing Typing
		if (typingState.maShu == 8 && typingState.isValidEngCharShuMa(typedShuMa))
		{
			//Continuing typing
			if( (setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode || setting.currentKeyMode == Setting.InputMode.HeMa_Traditional_Mode))
			{
				if(typedShuMa == 0) //move to next selection, good for 14 0 0 0 0
				{
					dataServerListener.changeSelection(1);
					return false;
				}
				else {
					dataServerListener.commitString(getSelectedZiCiStr());
				}
			}
		}

		if(typedShuMa == -2) //Mode menu
		{
			typingState.clearState();
			typingState.typeShuMa(typedShuMa);
		}
		else if(typedShuMa == 0 && (setting.currentKeyMode == Setting.InputMode.HeEnglishMode || setting.currentKeyMode == Setting.InputMode.PinYinMode)) {
			if(numOfCand > 0) {
				dataServerListener.changeSelection(1);
			}
			else {
				typingState.clearState();
				typingState.typeShuMa(0);
			}
		}
		else {
			if(!typing4Modes(typedShuMa))
			{
				return false; //don't need update candidateListView
			}
		}

		if((typingState.ma1 == 0 || typingState.ma1 == -2) && typingState.maShu == 4)
		{
			if(menuSelected())
			{
				clearState();
				return true;
			}
			else
			{
				typingState.typeShuMa(100); //typeback
				return false;
			}				
		}

		//Backspace to empty
		if(typingState.maShu==0 && typingState.engCharArrayLen == 0 && typingState.engCharShuMa == 0)
		{
			clearState();
			return true;
		}			
		
		//Log.d("Debug","Location DataServer location 2");
		if(engineCollection.generateCandidates(setting, typingState))
		{
			//Log.d("Debug","Location DataServer location 3");
			itemIndex = 0;
			pageIndex = 0;
			numOfCand = engineCollection.resultZiCiObjArray.size();
			getNumOfItemsOfCurrentPage();
			getOnePageList();
			typeSessionState = typingState.typeSessionState;
			
			//When HeEnglish or PinYin and typed first Shuma 
			if(!isInputable())
			{
				if(typingState.engCharShuMa <= maxItemsOfPage)
				{
					itemIndex = typingState.engCharShuMa-1;
				}
				else
				{
					changePageIndexBy(1);
					itemIndex = typingState.engCharShuMa - maxItemsOfPage -1;
				}				
			}
			
			return true;
		}
		else
		{
			//means did not get resultZiCiObjArr, need do typeBack.
			//however this typeback is inside dataServer, do not need to call typedCharAndNumber, and do not need to update candidateView and UI
			if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode || setting.currentKeyMode == Setting.InputMode.HeMa_Traditional_Mode)
			{
				  if(typingState.maShu <=5)
				  {
						//41 15 does not have danzi, but should not typeback, since we need to type CiZu
						dataServerListener.updateTypedMaView();
						return false;
				  }
				  else if(typedShuMa == 0) //when 7 43 0, should change selection
				{
						dataServerListener.changeSelection(1);
						//return false;
				}
			}
						
			typing4Modes(100); //typeback
			return false;
		}
	}
	
	public int getItemIndex()
	{
		return itemIndex;
	}
	
	public boolean isInputNumber()
	{
		return (setting.currentKeyMode==Setting.InputMode.NumberMode);
	}

	public boolean isMenuShow()
	{
		return (typingState.maShu>=2 && (typingState.ma1==0 || typingState.ma1 == -2));			
	}

	//This function need to use resource to get localized string
	//So must stay in DataServer class.
	private ArrayList<ZiCiObject> generateMenuDictionary()
	{
		//MatrixCursor cursor = new MatrixCursor(new String[] { "ZiCi", "M1", "PromptMa" });
		ArrayList<ZiCiObject> menuDic = new ArrayList<ZiCiObject>();

		menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_repeat), 0, 0, 0, 0, 0, 0));
		menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_punctuation), 11, 0, 11, 0, 0, 0));
		menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_math_symbol), 12, 0, 12, 0, 0, 0));
		menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_number), 13, 0, 13, 0, 0, 0));
		menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_english_char), 14, 0, 14, 0, 0, 0));

		if(setting.currentKeyMode != Setting.InputMode.HeMa_Simplified_Mode)
		{
			menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_he_chinese_input), 21, -2, 21, 0, 0, 0));
		}

		if(setting.currentKeyMode != Setting.InputMode.PinYinMode)
		{
			menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_pinyin_input), 22, -2, 22, 0, 0, 0));
		}

		if(setting.currentKeyMode != Setting.InputMode.HeEnglishMode)
		{
			menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_he_english_input), 25, -2, 25, 0, 0, 0));
		}
		
		if(setting.currentKeyMode != Setting.InputMode.EnglishMode)
		{
			menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_english_input), 31, -2, 31, 0, 0, 0));
		}
		
		if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode)
		{
			menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_use_traditional), 41, -2, 41, 0, 0, 0));
		}
		
		if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode)
		{
			if(setting.bNormalZiKu)			
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_use_big_collection), 42, -2, 42, 0, 0, 0));
			}
			else
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_use_small_collection), 42, -2, 42, 0, 0, 0));
				//cursor.addRow(new Object[] { context.getResources().getString(R.string.menu_use_small_collection),-2, 42});
			}
		}
		
		if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode || setting.currentKeyMode == Setting.InputMode.PinYinMode)
		{
			if(setting.bPinYinPrompt)
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_turn_off_pinyin_prompt), 43, -2, 43, 0, 0, 0));
			}
			else
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_turn_on_pinyin_prompt), 43, -2, 43, 0, 0, 0));
			}
		}				
		/*
		if(setting.bSystemLianXiangOn)
		{
		}
		else
		{

		}
		//*/

		if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode)
		{
			if(setting.bHeMaModeNumpad)
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_big_number_keyboard), 44, -2, 44, 0, 0, 0));
			}
			else
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_numpad), 44, -2, 44, 0, 0, 0));
			}
		}
		else if(setting.currentKeyMode == Setting.InputMode.PinYinMode)
		{
			if(setting.bPinYinModeNumpad)
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_big_char_keyboard), 44, -2, 44, 0, 0, 0));
			}
			else
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_numpad), 44, -2, 44, 0, 0, 0));
			}
		}
		else if(setting.currentKeyMode == Setting.InputMode.HeEnglishMode)
		{
			if(setting.bHeEnglishModeNumpad)
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_big_char_keyboard), 44, -2, 44, 0, 0, 0));
			}
			else
			{
				menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_numpad), 44, -2, 44, 0, 0, 0));
			}
		}

		menuDic.add(new ZiCiObject(context.getResources().getString(R.string.menu_save_setting), 55, -2, 55, 0, 0, 0));
		return menuDic;
	}
		
	private boolean menuSelected()
    {
		boolean bRet = false; //get the selection
		//ma1 == 0 or ma1 == -2
		switch(typingState.ma2)
		{
			case 0:
				//repeat typedString
				dataServerListener.commitString(typedString);
				bRet = true;
				break;
			case 11:
				typingState.ma1 = 6;
				typingState.maShu = 2;
				bRet = true;
				break;
			case 12:
				typingState.ma1 = 7;
				typingState.maShu = 2;
				bRet = true;
				break;
			case 13:
				typingState.ma1 = 8;
				typingState.maShu = 2;
				bRet = true;
				break;
			case 14:
				typingState.ma1 = 9;
				typingState.maShu = 2;
				bRet = true;
				break;
			//-------------------
			case 21:
					setting.currentKeyMode = Setting.InputMode.HeMa_Simplified_Mode;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				bRet = true;
				break;
			case 22:
					setting.currentKeyMode = Setting.InputMode.PinYinMode;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				bRet = true;
				break;
			case 23:
				//setting.currentKeyMode = InputMode.PinYinMode;
				//bRet = true;
				break;
			case 24:
					setting.currentKeyMode = Setting.InputMode.NumberMode;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				bRet = true;
				break;
			
			case 25:
					setting.currentKeyMode = Setting.InputMode.HeEnglishMode;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				bRet = true;
				break;
				
			case 31: //English Mode
					setting.currentKeyMode = Setting.InputMode.EnglishMode;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				//Log.d("Trace..","menu 31 selected.....");
				bRet = true;
				break;
			case 41:
				setting.currentKeyMode = Setting.InputMode.HeMa_Simplified_Mode;
				engineCollection.setMenuDictionary(generateMenuDictionary());
				//mySettingListener.sharedPreferenceChanged("HeInput_Simplified_Chinese");
				bRet = true;
				break;
			case 42:
				setting.bNormalZiKu = !setting.bNormalZiKu;
				engineCollection.setMenuDictionary(generateMenuDictionary());
				//mySettingListener.sharedPreferenceChanged("HeInput_Normal_ZiKu");
				bRet = true;
				break;
			case 43:
				setting.bPinYinPrompt = !setting.bPinYinPrompt;
				engineCollection.setMenuDictionary(generateMenuDictionary());
				//mySettingListener.sharedPreferenceChanged("HeInput_PinYin_Prompt");
				bRet = true;
				break;
			case 44:
				if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode)
				{
					setting.bHeMaModeNumpad = !setting.bHeMaModeNumpad;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				}
				else if(setting.currentKeyMode == Setting.InputMode.PinYinMode)
				{
					setting.bPinYinModeNumpad = !setting.bPinYinModeNumpad;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				}				
				else if(setting.currentKeyMode == Setting.InputMode.HeEnglishMode)
				{
					setting.bHeEnglishModeNumpad = !setting.bHeEnglishModeNumpad;
					dataServerListener.keyboardChange(setting.currentKeyMode);
					engineCollection.setMenuDictionary(generateMenuDictionary());
				}				
				break;
			case 55:
				dataServerListener.saveSharedPreferences();
				bRet = true;
				break;
			default:
				break;				
		}
				
		return bRet;    	
    }
	
	public boolean changeItemIndexBy(int num)
	{
		int itemIndexBeforeChange = itemIndex;
		
		if(numOfItemsInCurrentPage<=1)
			return false;
		
		if(num==1)
		{
			itemIndex++;
			//next page is empty, then go to first page
			if(itemIndex == numOfItemsInCurrentPage)
			{
				itemIndex=0;
			}
		}
		else
		{
			itemIndex--;
			//if previous page is empty, then go to last page
			if(itemIndex == -1)
			{
				itemIndex = numOfItemsInCurrentPage-1;
			}			
		}
		
		
		if(itemIndex != itemIndexBeforeChange)
		{
			if((setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode || setting.currentKeyMode == Setting.InputMode.PinYinMode) && !isMenuShow())
			{
				//HashMap<String, String> map = onePageRows.get(itemIndex);
				char danZi = onePageRows.get(itemIndex).get("ZiCi").charAt(0);
				pinYinPromptStr = getPinYinPromptString(danZi);
			}
				
			if(!isInputable())
			{
				typingState.engCharShuMa = pageIndex * maxItemsOfPage + itemIndex+1;
				
				dataServerListener.updateTypedMaView();
			}
			return true;
		}
		else
			return false;

	}

	public boolean changePageIndexBy(int num)
	{
		if(numOfCand <= 0)
		{
			return false;
		}
		
		int pageIndexBeforeChange = pageIndex;
		
		if(num == 1)
		{
			pageIndex++;
			//next page is empty, then go to first page
			if(pageIndex*maxItemsOfPage >= numOfCand)
			{
				pageIndex=0;
			}
		}
		else if( num == -1)
		{
			pageIndex--;
			//if previous page is empty, then go to last page
			if(pageIndex == -1)
			{
				pageIndex = numOfCand/maxItemsOfPage-1; //when numOfCand = 1
				
				if(pageIndex<0)
					pageIndex = 0;
			}			
		}
		
		if(pageIndex != pageIndexBeforeChange)
		{
			itemIndex = 0;
			getNumOfItemsOfCurrentPage();
			getOnePageList();
			
			if(!isInputable())
			{
				typingState.engCharShuMa = pageIndex * maxItemsOfPage + itemIndex+1;
				
				dataServerListener.updateTypedMaView();
			}
			
			return true;
		}
		else
			return false;
	}

	private void getNumOfItemsOfCurrentPage()
	{
		int numOfItemLeft = numOfCand - pageIndex*maxItemsOfPage;
		
		numOfItemsInCurrentPage = (numOfItemLeft>=maxItemsOfPage)? maxItemsOfPage:numOfItemLeft;
	}
	
	private void getOnePageList()
	{
		if(numOfCand == 0)
		{
			return;
		}
		
		if(isMenuShow())
		{
			getOnePageRows_with_ShuMa();
		}
		else
		{
			switch(setting.currentKeyMode)
			{
				case HeEnglishMode:
				case HeMa_Simplified_Mode:
				case HeMa_Traditional_Mode:
					getOnePageRows_with_ShuMa();
					break;		
				case PinYinMode:
					getOnePageRows_with_danZiCode();
					break;
				case EnglishMode:
				case NumberMode:
					//Only Mode menu
					break;
				default:
					break;
			}
		}
	}

	private void getOnePageRows_with_danZiCode()
	{
		int currIndex = pageIndex*maxItemsOfPage+itemIndex;
		onePageRows.clear();
		String danZi="";

		for(int i = 0; i < numOfItemsInCurrentPage; i++)
		{
			danZi = engineCollection.resultZiCiObjArray.get(currIndex).ziCi;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ZiCi", danZi);
			if(typingState.maShu <= 3 && typingState.engCharArrayLen == 0 && (typingState.ma1 == 0 || typingState.ma1 == -2)) {
				map.put("PromptMa", "");
			}
			else {
				map.put("PromptMa", engineCollection.getDanZiCode(danZi.charAt(0)));
			}
			onePageRows.add(map);
			currIndex++;
			if(currIndex == numOfCand) {
				break;
			}
		}
	 
		if(!isMenuShow())
		{
			danZi = onePageRows.get(0).get("ZiCi");
			pinYinPromptStr = getPinYinPromptString(danZi.charAt(0));
		}
	 }

	private void getOnePageRows_with_ShuMa()
	{
		int currIndex = pageIndex*maxItemsOfPage+itemIndex;
		ZiCiObject ziCiObj = new ZiCiObject();
		onePageRows.clear();
		//List<HashMap<String, String>> onePageListLocal = new ArrayList<HashMap<String, String>>();
		for(int i = 0; i < numOfItemsInCurrentPage; i++)
		{
			HashMap<String, String> map = new HashMap<String, String>();
			ziCiObj = engineCollection.resultZiCiObjArray.get(currIndex);
			map.put("ZiCi", ziCiObj.ziCi);
			map.put("PromptMa", ""+ziCiObj.promptShuMa);
			onePageRows.add(map);
			currIndex++;
			if(currIndex == numOfCand) {
				break;
			}
		}	
	 
		if(!isMenuShow())
		{
			String danZi = onePageRows.get(0).get("ZiCi");
			pinYinPromptStr = getPinYinPromptString(danZi.charAt(0));
		}
	 }

	public boolean isInputable()
	{
		if(typingState.engCharArrayLen==0 
				&& typingState.engCharShuMa>0 && typingState.engCharShuMa<=5
				&& typingState.maShu == 0)
		{
			return false;
		}
		else if(numOfCand == 0)
		{
			return false;
		}
		return true;
	}
	
	private String getPinYinPromptString(char c)
	{
		if(setting.bPinYinPrompt)
		{
			if(typingState.engCharArrayLen>=1 && setting.currentKeyMode == Setting.InputMode.PinYinMode)
			{
				return engineCollection.getPinYinPromptStr(c);
			}
			else if(typingState.maShu <= 3  &&  (typingState.ma1 == 0 || typingState.ma1 == -2))
			{
				return "";
			}
			else
				return engineCollection.getPinYinPromptStr(c);
		}
		else
			return "";
	}
	
	public void clearState()
	{
		itemIndex = 0;
		pageIndex = 0;
		numOfCand = 0;
		numOfItemsInCurrentPage = 0;
		onePageRows.clear();
		typingState.clearState();
		pinYinPromptStr = "";
		return;
	}
	   
    public String getPageIndicatorStr()
    {
    	int totalPages = numOfCand/maxItemsOfPage;
    	
    	if(numOfCand%maxItemsOfPage>0)
    		totalPages++;
    	
    	if(pageIndex==0 && totalPages>1)
    	{
    		return "1/"+ totalPages;
    	}
    	else if(pageIndex==0)
    	{
    		return "1/"+ totalPages;
    	}
    	else if((pageIndex+1)*maxItemsOfPage>=numOfCand)
    	{
    		return ""+(pageIndex+1) +"/"+totalPages;
    	}
    	else
    	{
    		return ""+(pageIndex+1) +"/"+totalPages;
    	}    	
    }
	
    public int getSelectedZiCiPromptMa()
    {
    	return Integer.valueOf(onePageRows.get(itemIndex).get("PromptMa"));
    }

	public int getItemPromptMaByIndex(int idx)
	{
		return Integer.valueOf(onePageRows.get(idx).get("PromptMa"));
	}


	public String getItemZiCiStrByIndex(int idx)
	{
		//Log.d("Debug","DataServer getSelectedZiCiStr..ItemIndex:"+itemIndex);

		if(numOfCand == 0)
		{
			return "";
		}

		typedString = onePageRows.get(idx).get("ZiCi").trim();

		return typedString;
	}

	public String getSelectedZiCiStr()
	{
		//Log.d("Debug","DataServer getSelectedZiCiStr..ItemIndex:"+itemIndex);
		if(numOfCand == 0)
		{
			return "";
		}
		
		typedString = onePageRows.get(itemIndex).get("ZiCi").trim();
		return typedString;
	}

    public String getTypedStr()
    {
    	String str="";
    	if(isMenuShow())
    	{
    		str = typingState.getTypedShuMaStr();
    	}
    	else
    	{
	    	switch(setting.currentKeyMode)
	    	{
	    		case EnglishMode:
	    		case NumberMode:
	    			break;
				case HeMa_Traditional_Mode:
				case HeMa_Simplified_Mode:
	    			str = typingState.getTypedShuMaStr();
	    			break;
	    		case PinYinMode:
	    		case HeEnglishMode:
	    			str = typingState.getTypedEngCharArray();
	    			break;
	    		default:
	    			break;
	    	}
    	}
    	return str;
    }

	public void setMaxItemsOfPage(int maxItemsOfPage) {
		this.maxItemsOfPage = maxItemsOfPage;
	}
}