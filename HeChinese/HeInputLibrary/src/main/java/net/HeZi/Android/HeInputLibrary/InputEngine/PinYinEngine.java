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
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class PinYinEngine extends Engine {

	public PinYinEngine() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getPinYinPromptStr(char danZi, SQLiteDatabase hemaDatabase)
	{
		Cursor cursor = hemaDatabase.rawQuery("select PinYin, ShengDiao " +
				"from HanZi_PinYin where HanZi=? ", new String [] {String.valueOf(danZi)});
		
		if(cursor.getCount()==0)
		{
			cursor.close();
			return "";
		}
		cursor.moveToFirst();
		String str = cursor.getString(0)+cursor.getInt(1);
		if(!cursor.isLast())
		{
			cursor.moveToNext();
			str += " | "+cursor.getString(0)+cursor.getInt(1);
		}

		cursor.close();
		return str;
	}
	
	public ArrayList<ZiCiObject> generateCandidates(Setting setting, TypingState ts, SQLiteDatabase hemaDatabase)
	{
		ArrayList<ZiCiObject> resultZiCiObjArr = new ArrayList<ZiCiObject>();

		//Cursor cursor =  null;
		String searchStr = "";
		switch(ts.engCharArrayLen)
		{
			case 0:
			//Log.d("Debug","Location PinYinEngine 0");
			if(ts.engCharShuMa>=1 && ts.engCharShuMa<=5) {
				if (ts.engCharShuMa >= 1 && ts.engCharShuMa <= 5) {
					resultZiCiObjArr.add(new ZiCiObject("1- F1E2T3J4Z5", 1, 0, 0, 0, 0,0));
					resultZiCiObjArr.add(new ZiCiObject("2- B1A2D3P4R5", 2, 0, 0, 0, 0,0));
					resultZiCiObjArr.add(new ZiCiObject("3- L1I2N3H4K5M6", 3, 0, 0, 0, 0,0));
					resultZiCiObjArr.add(new ZiCiObject("4- C1O2S3G4Q5", 4, 0, 0, 0, 0,0));
					resultZiCiObjArr.add(new ZiCiObject("5- V1U2W3Y4X5", 5, 0, 0, 0, 0,0));
				}
			}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:	
			case 6:
			case 7: {
				searchStr = new String(ts.engCharArray);
				searchStr = searchStr.substring(0, ts.engCharArrayLen);

				// Do not need query pinyin, since need to use getPinYinPromptStr(..) to get danZi' one or two pinYIn
				Cursor c = hemaDatabase.rawQuery("select HanZiString as ZiCi " + 		//, PinYin as PromptMa " +
						"from PinYin_HanZi where PinYin like ? order by PinYin", new String[]{searchStr + "%"});

				if (c != null) {

					c.moveToFirst();
					String danZiStr = "";
					//String engCharArray = "";
					while(!c.isAfterLast()) {

						danZiStr = c.getString(0);
						for(int i=0; i<danZiStr.length();i++) {
							resultZiCiObjArr.add(new ZiCiObject(String.valueOf(danZiStr.charAt(i)), 0, 0, 0, 0, 0,0));
						}
						c.moveToNext();
					}
					c.close();
				}
			}
				break;
				
			default:
				break;
		}

		return resultZiCiObjArr;
	}
	
	//PinYin_HanZi table record: one PinYin to many HanZi list
	private Cursor generateDanZiPinYinCursor(Cursor cursor)
	{
		MatrixCursor extras = new MatrixCursor(new String[] { "ZiCi", "PromptMa" });
		cursor.moveToFirst();
		String danZiStr = "";
		String engCharArray = "";
		while(!cursor.isAfterLast())
		{
			danZiStr = cursor.getString(0);
			engCharArray = cursor.getString(1);
			
			for(int i=0; i<danZiStr.length();i++)
			{
				extras.addRow(new Object[] { danZiStr.charAt(i), engCharArray});
			}
			
			cursor.moveToNext();
		}
		return extras;
	}
}
