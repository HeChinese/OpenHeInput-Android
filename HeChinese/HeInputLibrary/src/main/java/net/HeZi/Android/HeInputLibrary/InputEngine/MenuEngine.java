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
import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.ArrayList;

public class MenuEngine extends Engine {

	public MenuEngine() {
	}

	//This function is similar as engineCollect.typingCharAndNumber()
	public ArrayList<ZiCiObject> generateCandidates(Setting setting, TypingState typingState, ArrayList<ZiCiObject> menuDic)
	{
		//MatrixCursor tempCursor = new MatrixCursor(new String[] { "ZiCi", "PromptMa" });
		ArrayList<ZiCiObject> menuObjArr = new ArrayList<ZiCiObject>();
		int totalCand = menuDic.size();
		ZiCiObject ziCiObjT = null;

		switch(typingState.maShu)
		{
		 	case 2:
				if(typingState.ma1 == 0)
				{
					for(int i = 0; i<totalCand; i++)
					{
						menuObjArr.add(ZiCiObject.copy(menuDic.get(i)));
					}
				}
				else if(typingState.ma1 == -2) //mode
				{
					for(int i = 0; i<totalCand; i++)
					{
						if(menuDic.get(i).ma1 != 0)
						{
							menuObjArr.add(ZiCiObject.copy(menuDic.get(i)));
						}
					}
				}			 
			 break;
		 	case 3:
		 	{
				if(typingState.ma1 == 0)
				{
					for(int i = 0; i<totalCand; i++)
					{
						if(menuDic.get(i).ma2/10 == typingState.ma2)
						{
							ZiCiObject ziCiObj = ZiCiObject.copy(menuDic.get(i));
							ziCiObj.promptShuMa = ziCiObj.ma2%10;
							menuObjArr.add(ziCiObj);
						}
					}
				}
				else if(typingState.ma1 == -2) //mode
				{
					for(int i = 0; i<totalCand; i++)
					{
						ziCiObjT = menuDic.get(i);
						if (ziCiObjT.ma1 != 0 && ziCiObjT.ma2/10 == typingState.ma2)
						//if(cursor.getInt(1) != 0 && cursor.getInt(2)/10 == typingState.ma2)
						{
							ZiCiObject ziCiObj = ZiCiObject.copy(menuDic.get(i));
							ziCiObj.promptShuMa = ziCiObj.ma2%10;
							menuObjArr.add(ziCiObj);
							//tempCursor.addRow(new Object[]{cursor.getString(0), cursor.getInt(2)%10});
						}
					}
				}			 		 		
		 	}
		 	break;
		 	case 4:
		 	default:
		 		break;		
		}

		return menuObjArr;
	}
}