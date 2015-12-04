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

package net.HeZi.Android.HeLibrary.HeBase;

public class Basic_Helper {
	public int ma1,ma2,ma3,maCurrent;
	
	public static String combine(String a, String b) {
        return a + b;
    }

	/*
    private final char[] ZiGen25SetArray =
			new char[]{'一','乛','彐','又','王','一','十','土','卄','木','丨','冂','口','囗','日','丿','亻','人','女','犭','丶','冫','氵','心','米'};
	private final char[] siblingSet1Array = "一フ匚力耳一寸七大戈乚山口母目丿匕八月钅丶丷辶忄六".toCharArray();
	//*/

	public static int shuMaToIndex(int sm)
	{
		int col = sm/10;
		int row = sm%10;

		int ind = -1;
		if(col>=1 && col<=5 && row >=1 && row <=5)
		{
			//ind = (sm/10-1)*5 + (sm%10-1);
			ind = (col-1)*5 + (row - 1);
		}
		else if(sm == 36)  //English Char M
		{
			ind = 25; //index: 0,1,2...24,25
		}

		return ind;
	}

	public static int indexToShuMa(int index)
	{
		return (index/5)*10 + index%5+ 11;
	}

	//convert "134133君1" to "134133-1" 
	public String processFileName(String fileName)
	{
		fileName.replaceFirst("[^0-9]", "-");
		 
		return fileName;
	}
	public int indexToShuMaF(int ind)
	{
		return (ind/5)*10 + ind%5+ 11; 	
	}

	public int indexToRowF(int ind)
	{
		int ma;
		switch (ind) {
			case 0: ma=1; 	break;
			case 1: ma=2; 	break;
			case 2: ma=3; 	break;
			case 3: ma=4; 	break;
			case 4: ma=5; 	break;
			case 5: ma=1; 	break;
			case 6: ma=2; 	break;
			case 7: ma=3; 	break;
			case 8: ma=4; 	break;
			case 9: ma=5; 	break;
			case 10: ma=1; 	break;
			case 11: ma=2; 	break;
			case 12: ma=3; 	break;
			case 13: ma=4; 	break;
			case 14: ma=5; 	break;
			case 15: ma=1; 	break;
			case 16: ma=2; 	break;
			case 17: ma=3; 	break;
			case 18: ma=4; 	break;
			case 19: ma=5; 	break;
			case 20: ma=1; 	break;
			case 21: ma=2; 	break;
			case 22: ma=3; 	break;
			case 23: ma=4; 	break;
			case 24: ma=5; 	break;
			case 25: ma=1; 	break;
			default :
				ma=1;
				break;
		}
		
		return ma-1;
	}

	public int indexToColumnF(int ind)
	{
		int ma;
		switch (ind) {
			case 0: ma=1; 	break;
			case 1: ma=1; 	break;
			case 2: ma=1; 	break;
			case 3: ma=1; 	break;
			case 4: ma=1; 	break;
			case 5: ma=2; 	break;
			case 6: ma=2; 	break;
			case 7: ma=2; 	break;
			case 8: ma=2; 	break;
			case 9: ma=2; 	break;
			case 10: ma=3; 	break;
			case 11: ma=3; 	break;
			case 12: ma=3; 	break;
			case 13: ma=3; 	break;
			case 14: ma=3; 	break;
			case 15: ma=4; 	break;
			case 16: ma=4; 	break;
			case 17: ma=4; 	break;
			case 18: ma=4; 	break;
			case 19: ma=4; 	break;
			case 20: ma=5; 	break;
			case 21: ma=5; 	break;
			case 22: ma=5; 	break;
			case 23: ma=5; 	break;
			case 24: ma=5; 	break;
			case 25: ma=1; 	break;
			default :
				ma=1;
				break;
		}
		
		return ma-1;
	}

	public static char shuMaToUnichar(int sm)
	{
		char unic;
		switch (sm) {
			case 11: unic='T'; 	break;
			case 12: unic='R'; 	break;
			case 13: unic='E'; 	break;
			case 14: unic='W'; 	break;
			case 15: unic='Q'; 	break;
			case 31: unic='G'; 	break;
			case 32: unic='F'; 	break;
			case 33: unic='D'; 	break;
			case 34: unic='S'; 	break;
			case 35: unic='A'; 	break;
			case 51: unic='B'; 	break;
			case 52: unic='V'; 	break;
			case 53: unic='C'; 	break;
			case 54: unic='X'; 	break;
			case 55: unic='Z'; 	break;
			case 21: unic='Y'; 	break;
			case 22: unic='U'; 	break;
			case 23: unic='I'; 	break;
			case 24: unic='O'; 	break;
			case 25: unic='P'; 	break;
			case 41: unic='H'; 	break;
			case 42: unic='J'; 	break;
			case 43: unic='K'; 	break;
			case 44: unic='L'; 	break;
			case 45: unic='N'; 	break;
			case 0: unic='M'; 	break;
			default :
				unic = 'm';
				break;
		}
		
		return unic;
	}

	public int charToRow(char unic)
	{
		int ma;
		switch (unic) {
			case 't': ma=1; 	break;
			case 'r': ma=2; 	break;
			case 'e': ma=3; 	break;
			case 'w': ma=4; 	break;
			case 'q': ma=5; 	break;
			case 'g': ma=1; 	break;
			case 'f': ma=2; 	break;
			case 'd': ma=3; 	break;
			case 's': ma=4; 	break;
			case 'a': ma=5; 	break;
			case 'b': ma=1; 	break;
			case 'v': ma=2; 	break;
			case 'c': ma=3; 	break;
			case 'x': ma=4; 	break;
			case 'z': ma=5; 	break;
			case 'y': ma=1; 	break;
			case 'u': ma=2; 	break;
			case 'i': ma=3; 	break;
			case 'o': ma=4; 	break;
			case 'p': ma=5; 	break;
			case 'h': ma=1; 	break;
			case 'j': ma=2; 	break;
			case 'k': ma=3; 	break;
			case 'l': ma=4; 	break;
			case 'n': ma=5; 	break;
			case 'm': ma=1; 	break;
			default :
				ma=1;
				break;
		}
		
		return ma-1;
	}

	public int charToColumn(char unic)
	{
		int ma;
		switch (unic) {
			case 't': ma=1; 	break;
			case 'r': ma=1; 	break;
			case 'e': ma=1; 	break;
			case 'w': ma=1; 	break;
			case 'q': ma=1; 	break;
			case 'g': ma=3; 	break;
			case 'f': ma=3; 	break;
			case 'd': ma=3; 	break;
			case 's': ma=3; 	break;
			case 'a': ma=3; 	break;
			case 'b': ma=5; 	break;
			case 'v': ma=5; 	break;
			case 'c': ma=5; 	break;
			case 'x': ma=5; 	break;
			case 'z': ma=5; 	break;
			case 'y': ma=2; 	break;
			case 'u': ma=2; 	break;
			case 'i': ma=2; 	break;
			case 'o': ma=2; 	break;
			case 'p': ma=2; 	break;
			case 'h': ma=4; 	break;
			case 'j': ma=4; 	break;
			case 'k': ma=4; 	break;
			case 'l': ma=4; 	break;
			case 'n': ma=4; 	break;
			case 'm': ma=1; 	break;
			default :
				ma=1;
				break;
		}
		
		return ma-1;
	}

	public static int numberFromNumPadToShuMa(char unic)
	{
		//on Mac, numPad:0~9, keyCode: 48~57
		int ma = unic - 48;
		return ma;
	}

	public static int charToShuMa(char c)
	{
		int ma;
		switch (c) {
			case 't': case 'T': ma=11; 	break;
			case 'r': case 'R': ma=12; 	break;
			case 'e': case 'E': ma=13; 	break;
			case 'w': case 'W': ma=14; 	break;
			case 'q': case 'Q': ma=15; 	break;
			case 'y': case 'Y': ma=21; 	break;
			case 'u': case 'U': ma=22; 	break;
			case 'i': case 'I': ma=23; 	break;
			case 'o': case 'O': ma=24; 	break;
			case 'p': case 'P': ma=25; 	break;
			case 'g': case 'G': ma=31; 	break;
			case 'f': case 'F': ma=32; 	break;
			case 'd': case 'D': ma=33; 	break;
			case 's': case 'S': ma=34; 	break;
			case 'a': case 'A': ma=35; 	break;			
			case 'h': case 'H': ma=41; 	break;
			case 'j': case 'J': ma=42; 	break;
			case 'k': case 'K': ma=43; 	break;
			case 'l': case 'L': ma=44; 	break;
			case 'n': case 'N': ma=45; 	break;
			case 'b': case 'B': ma=51; 	break;
			case 'v': case 'V': ma=52; 	break;
			case 'c': case 'C': ma=53; 	break;
			case 'x': case 'X': ma=54; 	break;
			case 'z': case 'Z': ma=55; 	break;
			case 'm': case 'M': ma=0; 	break;
			default :
				ma=0;
				break;
		}
		
		return ma;
	}
}
