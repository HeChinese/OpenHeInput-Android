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
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class HeEnglishEngine extends Engine {

	public HeEnglishEngine() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ArrayList<ZiCiObject> generateCandidates(Setting setting, TypingState ts, SQLiteDatabase hemaDatabase)
	{
		//Log.d("HeEnglishEngine","GenerateCandidates---------");
		ArrayList<ZiCiObject> englishWordObjArray = new ArrayList<ZiCiObject>();

		String searchStr = "";
		switch(ts.engCharArrayLen)
		{
			case 0:
				//Log.d("Debug","Location HeEnglishEngine 0");
				if(ts.engCharShuMa>=1 && ts.engCharShuMa<=5)
				{
					englishWordObjArray.add(new ZiCiObject("1- F1E2T3J4Z5",1,0,0,0,0,0));
					englishWordObjArray.add(new ZiCiObject("2- B1A2D3P4R5",2,0,0,0,0,0));
					englishWordObjArray.add(new ZiCiObject("3- L1I2N3H4K5M6",3,0,0,0,0,0));
					englishWordObjArray.add(new ZiCiObject("4- C1O2S3G4Q5",4,0,0,0,0,0));
					englishWordObjArray.add(new ZiCiObject("5- V1U2W3Y4X5",5,0,0,0,0,0));
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

				Cursor c = hemaDatabase.rawQuery("select word as ZiCi, HeMaOrder as PromptMa " +
						"from English_Word where word like ? order by HeMaOrder limit 100", new String[]{searchStr + "%"});

				if (c != null) {

					c.moveToFirst();
					while(!c.isAfterLast()) {
						englishWordObjArray.add(new ZiCiObject(c.getString(0),c.getInt(1),0,0,0,0,0));
						c.moveToNext();
					}
					c.close();
				}
			}
			break;

			default:
				break;
		}

		return englishWordObjArray;
	}
}
