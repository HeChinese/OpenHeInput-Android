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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class HeMaEngine extends Engine{

	private static final int HEMAORDER_1MAZISPECIAL = 0; 
	private static final int HEMAORDER_1MAZI = 1;
	private static final int HEMAORDER_2MAZI = 2;
	private static final int HEMAORDER_3SHUMAZI = 3;
	private static final int HEMAORDER_3MAZI = 4;
	private static final int HEMAORDER_4MAZI = 5;
	private static final int HEMAORDER_CIZU = 5;
	private static final int HEMAORDER_AFTERCIZU = 6;
	private static final int HEMAORDER_RONGCUOMA = 7;
	private static final int HEMAORDER_GBK = 8;	//more than 7600 hanzi
	private static final int HEMAORDER_ENGCHAR = 9;

	ArrayList<ZiCiObject> danZiResult4ShuMa4 = new ArrayList<ZiCiObject>();
	ArrayList<ZiCiObject> ciZuResult4ShuMa6 = new ArrayList<ZiCiObject>();
	ArrayList<ZiCiObject> resultZiCiObjArr = new ArrayList<ZiCiObject>();

	//keep for another project using this old library function
	public HeMaEngine(Context cxt) {
	}
	
	public HeMaEngine() {
		super();
	}
	
	public String getDanZiCode(char danZi, SQLiteDatabase hemaDatabase)
	{
		Cursor cursor = hemaDatabase.rawQuery("select M1, M2, M3 " +
				"from HanZi where HanZi=? " ,
				new String [] {String.valueOf(danZi)});
		
		String str = "";
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			
			str = String.format("[%02d %02d %02d]", cursor.getInt(0), cursor.getInt(1),cursor.getInt(2));
		}
		cursor.close();
		return str;
	}
	
	private ArrayList<ZiCiObject> generateCandidates4Symbol(Setting setting, TypingState ts, SQLiteDatabase hemaDatabase)
	{

		resultZiCiObjArr.clear();
		Cursor c = null;
		
		switch(ts.maShu)
		{
			case 2:
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, M2 as PromptMa, m1, m2, m3,m4,0 " +
						"from HanZi where M1=? and M3 = 11 order by m2" ,
						new String [] {String.valueOf(ts.ma1)});
				// need to clean the saved ArrayList
				danZiResult4ShuMa4.clear();
				ciZuResult4ShuMa6.clear();

				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}
				break;
			case 3:
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M2-?) as PromptMa,m1,m2,m3,m4,0 " +
						"from HanZi where M1=? and M2>? and M2<? and M3 = 11" ,
						new String [] {String.valueOf(ts.ma2*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2*10),String.valueOf((ts.ma2+1)*10)});

				danZiResult4ShuMa4.clear();

				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}

				break;
			case 4:
				// if danZiResult4ShuMa4.size() > 0, means it is typeback, just use saved danZiResult4ShuMa4
				if (danZiResult4ShuMa4.size() == 0) {
					c = hemaDatabase.rawQuery("select HanZi as ZiCi, M3 as PromptMa, m1,m2,m3,m4,0 " +
									"from HanZi where M1=? and M2= ? ",
							new String[]{String.valueOf(ts.ma1), String.valueOf(ts.ma2)});

					if (c != null) {
						c.moveToFirst();
						insertCursorRow2ResultZiCiObjArray(c);
						c.close();
					}

					danZiResult4ShuMa4 = (ArrayList<ZiCiObject>)resultZiCiObjArr.clone();
				}
				else
					// maShu decreasing to 4
					resultZiCiObjArr = (ArrayList<ZiCiObject>)danZiResult4ShuMa4.clone();

				break;
			//*
			case 5:
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M3-?) as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3>? and M3<? ",
						new String [] {String.valueOf(ts.ma3*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3*10),String.valueOf((ts.ma3+1)*10)});
						//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3/10 == ts.ma3){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma3%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}
				break;
			case 6:
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, 0 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=?" ,
						new String [] {String.valueOf(ts.ma1), String.valueOf(ts.ma2), String.valueOf(ts.ma3)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3 == ts.ma3){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = 0;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}
				break;

			default:
				c = null;
				break;
		}

		return resultZiCiObjArr;
	}

	private void insertCursorRow2ResultZiCiObjArray (Cursor c) {
		c.moveToFirst();
		while(!c.isAfterLast()) {
			resultZiCiObjArr.add(new ZiCiObject(c.getString(0), c.getInt(1),c.getInt(2),c.getInt(3),c.getInt(4),c.getInt(5),c.getInt(6)));
			c.moveToNext();
		}
	}

	private void insertCursorRow2CiZuResult4ShuMa6 (Cursor c) {
		c.moveToFirst();
		while(!c.isAfterLast()) {
			ciZuResult4ShuMa6.add(new ZiCiObject(c.getString(0), c.getInt(1),c.getInt(2),c.getInt(3),c.getInt(4),c.getInt(5),c.getInt(6)));
			c.moveToNext();
		}
	}
	
	public ArrayList<ZiCiObject> generateCandidates(Setting setting, TypingState ts, SQLiteDatabase hemaDatabase)
	{
		//Symbol
		if(ts.ma1>=6 && ts.ma1 <= 9)
		{
			return generateCandidates4Symbol(setting, ts, hemaDatabase);
		}

		String danZiOrder =  null;
		String ciZuJianFanStatement = null;
		String heMaOrderStatement = null;

		String danZiGBKStatement = null;
		
		if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode)
		{
			danZiOrder = " GBOrder ";
			ciZuJianFanStatement = " and JianFan < 2 ";
			heMaOrderStatement = ", GBOrder as HeMaOrder ";
		}
		else //if(setting.currentKeyMode == Setting.InputMode.HeMa_Traditional_Mode)
		{
			danZiOrder = " B5Order ";
			ciZuJianFanStatement = " and JianFan <> 1 ";
			heMaOrderStatement = ", B5Order as HeMaOrder ";
		}
		
		if(setting.bNormalZiKu)
		{
			danZiGBKStatement = " and "+ danZiOrder + " <> " + HEMAORDER_GBK + " ";
		}
		else
		{
			danZiGBKStatement = "";
		}

		resultZiCiObjArr.clear();
		Cursor c = null;

		switch(ts.maShu)
		{
			case 1:
				switch (ts.ma1)
				{
					case 1: //for 'Bu' char
					{
						resultZiCiObjArr.add(new ZiCiObject("不", 1, 11, 42, 51, 0,0));
					}
						break;
					case 2: //
					{
						resultZiCiObjArr.add(new ZiCiObject("是", 4, 24, 11, 43, 21,0));
					}
						break;
					case 3: //for 'Shi' char
					{
						resultZiCiObjArr.add(new ZiCiObject("去", 2, 32, 43, 0, 0,0));
					}
						break;
					case 4: //for 'He' char
					{
						resultZiCiObjArr.add(new ZiCiObject("和", 1, 41, 34, 23, 0,0));
					}
						break;
					case 5: //for 'Qing' char
					{
						if(setting.currentKeyMode == Setting.InputMode.HeMa_Simplified_Mode)
						{
							resultZiCiObjArr.add(new ZiCiObject("请", 2, 52, 32, 25, 11,0));
						}
						else
						{
							resultZiCiObjArr.add(new ZiCiObject("請", 2, 52, 32, 25, 11,0));
						}
					}
						break;
					default:
						break;
				}

				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M1-?) as PromptMa, m1,m2,m3,m4, 0 " +
						"from HanZi where M1>? and M1<? and M2 = 0 "+ danZiGBKStatement+ " and " +danZiOrder +">?" +" and "+ danZiOrder + "<?" + " order by M1 DESC",
						new String [] {String.valueOf(ts.ma1*10),String.valueOf(ts.ma1*10),String.valueOf((ts.ma1+1)*10), String.valueOf(HEMAORDER_1MAZISPECIAL), String.valueOf(HEMAORDER_RONGCUOMA)});

				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}

				break;
			case 2:
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, M2 as PromptMa, m1,m2,m3,m4,0 " +
						"from HanZi where M1=? and M2= 0 "+danZiGBKStatement+" order by "+danZiOrder,
						new String [] {String.valueOf(ts.ma1)});

				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}

				danZiResult4ShuMa4.clear();

				break;
			case 3:
				//Order = 3SHUMaZi
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M2-?) as PromptMa,m1,m2,m3,m4,0 " +
						"from HanZi where M1=? and M2>? and M2<? and " + danZiOrder +"=? ",
						new String [] {String.valueOf(ts.ma2*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2*10),String.valueOf((ts.ma2+1)*10), String.valueOf(HEMAORDER_3SHUMAZI)});
				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}

				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M1-?) as PromptMa,m1,m2,m3,m4,0 " +
						"from HanZi where M1>? and M1<? and M2 = 0 "+ danZiGBKStatement+ " and " +danZiOrder +">?" +" and "+ danZiOrder + "<?" + " order by M1 DESC",
						new String [] {String.valueOf(ts.ma2*10),String.valueOf(ts.ma2*10),String.valueOf((ts.ma2+1)*10), String.valueOf(HEMAORDER_1MAZISPECIAL), String.valueOf(HEMAORDER_RONGCUOMA)});

				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}

				danZiResult4ShuMa4.clear();

				break;
			case 4:

				if (danZiResult4ShuMa4.size()==0) {
					//Order >= 2MaZi
					c = hemaDatabase.rawQuery("select HanZi as ZiCi, M3 as PromptMa,m1,m2,m3,m4 " + heMaOrderStatement +
									"from HanZi where M1=? and M2= ? " + danZiGBKStatement + " and " + danZiOrder + ">=? order by " + danZiOrder,
							new String[]{String.valueOf(ts.ma1), String.valueOf(ts.ma2), String.valueOf(HEMAORDER_2MAZI)});

					if (c != null) {
						c.moveToFirst();
						insertCursorRow2ResultZiCiObjArray(c);
						c.close();
					}

					//Order < 2MaZi
					c = hemaDatabase.rawQuery("select HanZi as ZiCi, M3 as PromptMa,m1,m2,m3,m4 " + heMaOrderStatement +
									"from HanZi where M1=? and M2= ? and " + danZiOrder + "<? order by " + danZiOrder,
							new String[]{String.valueOf(ts.ma1), String.valueOf(ts.ma2), String.valueOf(HEMAORDER_2MAZI)});
					if (c != null) {
						c.moveToFirst();
						insertCursorRow2ResultZiCiObjArray(c);
						c.close();
					}
					//CiZu m3=0
					c = hemaDatabase.rawQuery("select CiZu as ZiCi, M3 as PromptMa,m1,m2,m3,m4,0 " +
									"from CiZu where M1=? and M2= ? and M3=0 " + ciZuJianFanStatement + "order by HeMaOrder",
							new String[]{String.valueOf(ts.ma1), String.valueOf(ts.ma2)});

					if (c != null) {
						c.moveToFirst();
						insertCursorRow2ResultZiCiObjArray(c);
						c.close();
					}

					danZiResult4ShuMa4 = (ArrayList<ZiCiObject>) resultZiCiObjArr.clone();
				} else  // maShu decreasing to 4
					resultZiCiObjArr = (ArrayList<ZiCiObject>)danZiResult4ShuMa4.clone();

				ciZuResult4ShuMa6.clear();

				break;

			case 5:
				//DanZi hemaorder ==HEMAORDER_4MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M3-?) as PromptMa " +
								"from HanZi where M1=? and M2= ? and M3>? and M3<? "+danZiGBKStatement +" and " +danZiOrder +"=? ",
						new String [] {String.valueOf(ts.ma3*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3*10),String.valueOf((ts.ma3+1)*10), String.valueOf(HEMAORDER_4MAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3/10 == ts.ma3 && ziCiObj.heMaOrder == HEMAORDER_4MAZI){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma3%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//CiZu HEMAORDER <=2 21597 or <=3 : 36611; <=5
				c = hemaDatabase.rawQuery("select CiZu as ZiCi, M3 as PromptMa, m1,m2,m3,m4,HeMaOrder " +
								"from CiZu where M1=? and M2= ? and M3>? and M3<? and HeMaOrder<=6 "+ciZuJianFanStatement+ " order by HeMaOrder limit 10",
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3*10),String.valueOf((ts.ma3+1)*10)});

				if (c != null) {
					c.moveToFirst();
					insertCursorRow2ResultZiCiObjArray(c);
					c.close();
				}

				//DanZi hemaorder ==HEMAORDER_3MAZI or >HEMAORDER_4MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M3-?) as PromptMa " +
								"from HanZi where M1=? and M2= ? and M3>? and M3<? "+danZiGBKStatement +"and ("+danZiOrder +"=? or "+ danZiOrder +">?) order by " + danZiOrder,
						new String [] {String.valueOf(ts.ma3*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3*10),String.valueOf((ts.ma3+1)*10),String.valueOf(HEMAORDER_3MAZI), String.valueOf(HEMAORDER_4MAZI)});
				//*/

				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3/10 == ts.ma3 && (ziCiObj.heMaOrder == HEMAORDER_3MAZI || ziCiObj.heMaOrder > HEMAORDER_4MAZI)){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma3%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaOrder< HEMAORDER_3MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M3-?) as PromptMa " +
								"from HanZi where M1=? and M2= ? and M3>? and M3<? and " +danZiOrder + "<? order by "+danZiOrder,
						new String [] {String.valueOf(ts.ma3*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3*10),String.valueOf((ts.ma3+1)*10),String.valueOf(HEMAORDER_3MAZI)});
				//*/

				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3/10 == ts.ma3 && ziCiObj.heMaOrder < HEMAORDER_3MAZI){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma3%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				if (ciZuResult4ShuMa6.size()>0)
					ciZuResult4ShuMa6.clear();

			break;
			case 6:
				//DanZi hemaorder >=HEMAORDER_3SHUMAZI and <=HEMAORDER_4MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, M4 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and " + danZiOrder + ">=? and "+danZiOrder+"<=? order by "+danZiOrder,
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(HEMAORDER_3SHUMAZI), String.valueOf(HEMAORDER_4MAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3 == ts.ma3 && ziCiObj.heMaOrder >= HEMAORDER_3SHUMAZI && ziCiObj.heMaOrder <= HEMAORDER_4MAZI ){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				if (ciZuResult4ShuMa6.size() == 0) {
					//All CiZu HEMAORDER_CIZU
					c = hemaDatabase.rawQuery("select CiZu as ZiCi, M4 as PromptMa,m1,m2,m3,m4, HeMaOrder " +
									"from CiZu where M1=? and M2= ? and M3=? " + ciZuJianFanStatement + " order by HeMaOrder",
							new String[]{String.valueOf(ts.ma1), String.valueOf(ts.ma2), String.valueOf(ts.ma3)});

					insertCursorRow2CiZuResult4ShuMa6(c);
				}
				resultZiCiObjArr.addAll(ciZuResult4ShuMa6);

				//DanZi hemaorder > HEMAORDER_CIZU
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, M4 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=?"+ danZiGBKStatement + "and " + danZiOrder +">? order by "+danZiOrder,
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(HEMAORDER_CIZU)});
				//*/

				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3 == ts.ma3 && ziCiObj.heMaOrder > HEMAORDER_CIZU ){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaOrder< HEMAORDER_3SHUMAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, M4 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and "+danZiOrder+"<? order by " + danZiOrder,
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(HEMAORDER_3SHUMAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma3 == ts.ma3 && ziCiObj.heMaOrder < HEMAORDER_3SHUMAZI ){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				break;
			case 7:
				//All CiZu HEMAORDER_CIZU	
				/*
				mCursors[0] = hemaDatabase.rawQuery("select CiZu as ZiCi, (M4-?) as PromptMa " +
						"from CiZu where M1=? and M2= ? and M3=? and M4>? and M4<?"+ ciZuJianFanStatement + " order by HeMaOrder",
						new String [] {String.valueOf(ts.ma4*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4*10),String.valueOf((ts.ma4+1)*10)});
				//*/
				for (ZiCiObject ziCiObj : ciZuResult4ShuMa6) {
					if(ziCiObj.ma4/10 == ts.ma4){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaorder == HEMAORDER_4MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M4-?) as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and M4>? and M4<? and "+danZiOrder + "=? ",
						new String [] {String.valueOf(ts.ma4*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3), String.valueOf(ts.ma4*10),String.valueOf((ts.ma4+1)*10), String.valueOf(HEMAORDER_4MAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma4/10 == ts.ma4 && ziCiObj.heMaOrder == HEMAORDER_4MAZI){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaorder > HEMAORDER_CIZU
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M4-?) as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and M4>? and M4<?"+danZiGBKStatement + "and " + danZiOrder +">? order by " + danZiOrder,
						new String [] {String.valueOf(ts.ma4*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4*10),String.valueOf((ts.ma4+1)*10),String.valueOf(HEMAORDER_CIZU)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma4/10 == ts.ma4 && ziCiObj.heMaOrder > HEMAORDER_CIZU){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaorder < HEMAORDER_4MaZi
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, (M4-?) as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and M4>? and M4<? and "+danZiOrder +"<? order by " + danZiOrder,
						new String [] {String.valueOf(ts.ma4*10),String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4*10),String.valueOf((ts.ma4+1)*10),String.valueOf(HEMAORDER_4MAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma4/10 == ts.ma4 && ziCiObj.heMaOrder < HEMAORDER_4MAZI){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = ziCiObjT.ma4%10;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				break;
			case 8:
				//resultCursor = hemaDatabase.rawQuery("select HanZi as ZiCi, M4 as PromptMa,GBOrder as HeMaOrder from HanZi where M1=? and M2= ? and M3=? and M4=? order by HeMaOrder",new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4)});
				//DanZi hemaorder == HEMAORDER_4MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, 0 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and M4=? and "+danZiOrder + "=? ",
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3), String.valueOf(ts.ma4), String.valueOf(HEMAORDER_4MAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma4 == ts.ma4 && ziCiObj.heMaOrder == HEMAORDER_4MAZI){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = 0;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//All CiZu HEMAORDER_CIZU
				/*
				mCursors[1] = hemaDatabase.rawQuery("select CiZu as ZiCi, 0 as PromptMa " +
						"from CiZu where M1=? and M2= ? and M3=? and M4=? "+ciZuJianFanStatement+" order by HeMaOrder",
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4)});
				//resultCursor = new MergeCursor(mCursors);
				//*/
				for (ZiCiObject ziCiObj : ciZuResult4ShuMa6) {
					if(ziCiObj.ma4 == ts.ma4){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = 0;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaorder > HEMAORDER_CIZU
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, 0 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and M4=?"+danZiGBKStatement + "and " + danZiOrder+">? order by "+ danZiOrder,
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4),String.valueOf(HEMAORDER_CIZU)});
				//*/

				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma4 == ts.ma4 && ziCiObj.heMaOrder > HEMAORDER_CIZU){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = 0;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}

				//DanZi hemaOrder< HEMAORDER_4MAZI
				/*
				c = hemaDatabase.rawQuery("select HanZi as ZiCi, 0 as PromptMa " +
						"from HanZi where M1=? and M2= ? and M3=? and M4=? and "+danZiOrder+"<? order by "+danZiOrder,
						new String [] {String.valueOf(ts.ma1),String.valueOf(ts.ma2),String.valueOf(ts.ma3),String.valueOf(ts.ma4),String.valueOf(HEMAORDER_4MAZI)});
				//*/
				for (ZiCiObject ziCiObj : danZiResult4ShuMa4) {
					if(ziCiObj.ma4 == ts.ma4 && ziCiObj.heMaOrder < HEMAORDER_4MAZI){
						ZiCiObject ziCiObjT = ZiCiObject.copy(ziCiObj);
						ziCiObjT.promptShuMa = 0;
						resultZiCiObjArr.add(ziCiObjT);
					}
				}
				break;
			default:
				break;
		}

		return resultZiCiObjArr;
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}
}
