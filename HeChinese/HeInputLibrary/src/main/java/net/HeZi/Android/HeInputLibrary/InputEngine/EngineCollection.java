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

package net.HeZi.Android.HeInputLibrary.InputEngine;

import net.HeZi.Android.HeLibrary.HeBase.ZiCiObject;
import net.HeZi.Android.HeLibrary.HeInput.Setting;
import net.HeZi.Android.HeLibrary.HeInput.TypingState;

import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class EngineCollection 
{
	private HeMaEngine heMaEngine;
	private PinYinEngine pinYinEngine;
	private HeEnglishEngine heEnglishEngine;
	//private HanZi2InforEngine inforEngine;
	private MenuEngine menuEngine;
	
	//hemaDatabase is passed in from HeInput_DataServer
	//it is open already and will keep open during input active
	private SQLiteDatabase hemaDatabase;
	private ArrayList<ZiCiObject> menuDictionary;

	// used for return from all kinds of engine.
	public ArrayList<ZiCiObject> resultZiCiObjArray = new ArrayList<ZiCiObject>();

	public EngineCollection(SQLiteDatabase db, ArrayList<ZiCiObject> menuDic)
	{
		heMaEngine = new HeMaEngine();
		pinYinEngine = new PinYinEngine();
		heEnglishEngine = new HeEnglishEngine();
		//inforEngine = new HanZi2InforEngine();
		menuEngine = new MenuEngine();

		hemaDatabase = db; 	//already opened
		menuDictionary = menuDic;
	}

	public void setMenuDictionary(ArrayList<ZiCiObject> menuDic)
	{
		menuDictionary = menuDic;
	}
	
	public String getDanZiCode(char danZi)
	{
		return heMaEngine.getDanZiCode(danZi, hemaDatabase);
	}

	public String getPinYinPromptStr(char danZi)
	{
		return pinYinEngine.getPinYinPromptStr(danZi, hemaDatabase);
	}

	public boolean generateCandidates(Setting setting, TypingState typingState)
	{
		ArrayList<ZiCiObject> ziCiObjArr = new ArrayList<ZiCiObject>();
		//Log.d("Debug","------EngineCollection generateCandidates------");
		if(typingState.maShu >= 2 && (typingState.ma1 == 0 || typingState.ma1 == -2))
		{
			ziCiObjArr =menuEngine.generateCandidates(setting, typingState, menuDictionary);
		}		
		else
		{
			switch(setting.currentKeyMode)
			{
				case EnglishMode:
				case NumberMode:
					break;
				case HeMa_Simplified_Mode:
                case HeMa_Traditional_Mode:
				{
					ziCiObjArr = heMaEngine.generateCandidates(setting, typingState, hemaDatabase);
				}
				break;
				case PinYinMode:
				{
					//Log.d("Debug","Location EngineCollection.........");
					ziCiObjArr = pinYinEngine.generateCandidates(setting, typingState, hemaDatabase);
				}
					break;
				case HeEnglishMode:
				{
					//Log.d("Debug","Location EngineCollection...4 HeEnglishMode:......");
					ziCiObjArr = heEnglishEngine.generateCandidates(setting, typingState, hemaDatabase);
				}
					break;
				default:
					break;
			}
		}

		if (ziCiObjArr.size()>0) {
			resultZiCiObjArray.clear();
			resultZiCiObjArray = (ArrayList<ZiCiObject>)ziCiObjArr.clone();
			return true;
		}
		else {
			return false;
		}
	}
}
