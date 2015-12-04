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

public class TypingState {
	
	public TypingState() {
		super();
		this.ma1 = 0;
		this.ma2 = 0;
		this.ma3 = 0;
		this.ma4 = 0;
		this.maShu = 0;
		//this.typedShuMa = 0;
		this.savedShuMa = 0;

		typeSessionState = TypeSessionState.UnStart;
		
		engCharArray = new char[15];
		engCharArray[0] = '\0';

		engCharArrayLen=0;
		shengDiao = 0;
		engCharShuMa = 0;
		savedEngCharShuMa = 0;
		bEngCharArrayLenChanged = false;
						
	}
	
	//byte: The byte data type is an 8-bit signed two's complement integer. 
	//It has a minimum value of -128 and a maximum value of 127 (inclusive).
	//public char currentChar;		//shared by HeMa, PinYin, HeEnglish
	//public int typedShuMa;		//shared by HeMa, PinYin, HeEnglish
	
	public int ma1;
	public int ma2;
	public int ma3;
	public int ma4;
	
	public int maShu;  //value from 0 to 8, 
	
	//public char savedChar;
	//public int typedShuMa; or currentShuMa is bad concept here
	
	public int savedShuMa;	//typedMa
	public TypeSessionState typeSessionState;	
	
	char savedChar;
	
	public char[] engCharArray;
	public int engCharArrayLen;
	//public int engCharArrayLen;
	int shengDiao;
	public int engCharShuMa;
	int savedEngCharShuMa;		//engCharShuMaBefore
	//int formedengCharShuMa;	//engCharShuMaFormed
	public boolean bEngCharArrayLenChanged;	//Special for num input, only pinYinLenChanged then need to recaculate candidateList
	
	public boolean isValidEngCharShuMa(int shuMa)
	{
		boolean fRet = false;

		if (shuMa <= 0) {
			fRet = false;
		}
		else if(shuMa>0 && shuMa<10)
		{
			if(engCharShuMa == 0)
			{
				if(shuMa >=1 && shuMa<=5)
					fRet = true;
				else 
					fRet = false;
			}
			else
			{
				if(engCharShuMa == 3 && shuMa >=1 && shuMa<=6)  //for 36 : m
					fRet = true;
				else if(shuMa >=1 && shuMa<6) //for another chars
					fRet = true;
				else 
					fRet = false;
			}
		}
		else //shuMa > 10
		{
			fRet = (engCharShuMaToEngCharacter(shuMa) != '\0');
		}
		return fRet;
	}

	private boolean typeBackEngCharShuMa()
	{	
		boolean fRet = false;
		if(engCharShuMa>0 && engCharShuMa<11)
		{
			engCharShuMa = 0;
			bEngCharArrayLenChanged = false;
			savedEngCharShuMa=0;
			fRet = true;		//do not need to call FormCandList4PinYin()
		}
		else if(engCharArrayLen>0)  //engCharShuMa >10 or = 0
		{
			engCharArrayLen--;
			engCharArray[engCharArrayLen]='\0';
			bEngCharArrayLenChanged = true;
			fRet = true;
			
			if(savedEngCharShuMa>0 && savedEngCharShuMa<10) //back a digital number 1,2,3,4,5,6
			{
				engCharShuMa = savedEngCharShuMa;
				savedEngCharShuMa=0;
			}
		}
		return fRet;
	}
	
	public boolean typeEngCharShuMa(int shuMa)
	{
		boolean fRet = false;
		//Log.d("Debug","Location typeEngCharShuMa shuMa: "+shuMa);

		if(shuMa==100)
		{
			return typeBackEngCharShuMa();
		}

		// 0 is not valid shuMa here
		if(!isValidEngCharShuMa(shuMa))
		{
			return false;
		}	
		
		//Log.d("Debug","Location typeEngCharShuMa valided.....");
		
		int engCharShuMaBefore = engCharShuMa;
		int engCharArrayLenBefore = engCharArrayLen;
		
		if(shuMa <= 6)
		{
			if(engCharShuMa == 0 || engCharShuMa >10)
			{
				engCharShuMa = shuMa;
			}
			else		//engCharShuMa = 1,2,3,4,5
			{
				engCharShuMa = engCharShuMa*10 + shuMa;
			}			
			fRet = true;
		}
		else
		{
			engCharShuMa = shuMa;
			savedEngCharShuMa = 0;
			fRet = true;
		}
		
		if(engCharShuMa >= 11)
		{
			char newFormedChar = engCharShuMaToEngCharacter(engCharShuMa);			
			if(typeEngChar(newFormedChar))
			{
				engCharShuMa = 0;
			}
			else
			{
				engCharShuMa = engCharShuMaBefore;
				return false;
			}
		}
				
		if(engCharShuMaBefore != engCharShuMa || engCharArrayLenBefore != engCharArrayLen )// when m1 = 0 and maShu = 2 and typed 6,7,8,9, 
		{
			if(engCharArrayLenBefore == 0 && engCharShuMaBefore == 0) //typed first ShuMa
			{
				typeSessionState=TypeSessionState.FirstType;
			}
			else if(engCharArrayLenBefore > 0 && engCharArrayLen == 0 && engCharShuMa == 0) //tipically when type space or enter key
			{
				typeSessionState=TypeSessionState.Finished;
			}
			else if(engCharArrayLenBefore>0 && engCharArrayLen >0 ) //continue typing
			{
				typeSessionState=TypeSessionState.ContinueType;
			}
			else
				typeSessionState=TypeSessionState.UnStart;
				
			savedEngCharShuMa = shuMa;//<=6? shuMa : 0;
			return true;
		}
		else
		{
			engCharShuMa = engCharShuMaBefore;
			engCharArrayLen = engCharArrayLenBefore;
			return false;
		}
	}


	private char engCharShuMaToEngCharacter(int shuMa)
	{
		char		engChar = '\0';
		
		switch(shuMa)
		{
			case 11:
				engChar = 'f';
				break;
			case 12:
				engChar = 'e';
				break;
			case 13:
				engChar = 't';
				break;
			case 14:
				engChar = 'j';
				break;
			case 15:
				engChar = 'z';
				break;
			case 21:
				engChar = 'b';
				break;
			case 22:
				engChar = 'a';
				break;
			case 23:
				engChar = 'd';
				break;
			case 24:
				engChar = 'p';
				break;
			case 25:
				engChar = 'r';
				break;
			case 31:
				engChar = 'l';
				break;
			case 32:
				engChar = 'i';
				break;
			case 33:
				engChar = 'n';
				break;
			case 34:
				engChar = 'h';
				break;
			case 35:
				engChar = 'k';
				break;
			case 36:
				engChar = 'm';
				break;
			case 41:
				engChar = 'c';
				break;
			case 42:
				engChar = 'o';
				break;
			case 43:
				engChar = 's';
				break;
			case 44:
				engChar = 'g';
				break;
			case 45:
				engChar = 'q';
				break;
			case 46:
				engChar = 0x27;
				break;
			case 51:
				engChar = 'v';
				break;
			case 52:
				engChar = 'u';
				break;
			case 53:
				engChar = 'w';
				break;
			case 54:
				engChar = 'y';
				break;
			case 55:
				engChar = 'x';
				break;
			default:
				break;
		}
		return engChar;
	}

	private boolean isValid26Char(char engChar)
	{
		boolean bRet = false;
		
		if(engChar>=97 && engChar <= 122 ||	//abcd...xyz
				engChar>=65 && engChar<=90)		//ABCD..XYZ	   
		{
			bRet = true;
		}
		return bRet;
	}
	
	//only used by typingengCharShuMa()
	private boolean typeEngChar(char engChar)
	{
		if(!isValid26Char(engChar))
			return false;
		
		//engCharShuMa = 0;
		//englishShuMaFormed = 0;
		
		if(engCharArrayLen<15)
		{
			engCharArray[engCharArrayLen]=engChar;
			engCharArrayLen++;
			engCharArray[engCharArrayLen]='\0';
			bEngCharArrayLenChanged = true;
			return true;
		}
		else
			return false;
	}
	
	public boolean typeShuMa(int typedShuMa)
	{
		//Log.d("Debug","typedShuMa: "+typedShuMa + ", maShu="+maShu);

		if(typedShuMa == 100) //Backspace
			return typeBackShuMa();

		//ma1 == 99 indidate LianXiang
		if(typedShuMa == 99)
		{
			ma1 = typedShuMa; //99
			maShu =2;
			
			return true;
		}

		//Mode Selection
		if(typedShuMa == -2)
		{
			if(ma1==-2)  //turn off mode selection
			{
				ma1=0;
				maShu = 0;
			}
			else
			{
				ma1 = typedShuMa; //turn on mode selection
				maShu =2;
			}
			return true;
		}

		if(!isValidHeInputShuMaPlus0(typedShuMa))
			return false;

		//bypass some errors
		if(typedShuMa<0 || typedShuMa>55)
			return false;

		int maShuBefore=maShu;
		int ma1Before = ma1;
		//int ma2Before = ma2;

		switch (maShu)  //this maShu is before process type
		{
			case 0:
			case 8://Continue type
			{
				ma1=typedShuMa;		//include 99
				ma2=0;
				ma3=0;
				ma4=0;
				
				if (typedShuMa>0 && typedShuMa<6)
					maShu=1;
				else //typedShuMa >10, == 0
					maShu=2;				
			}
				break;
			case 1:	//MaShu==1
			{
				ma2=0;
				ma3=0;
				ma4=0;
				if (typedShuMa>10) //typedShuMa= 11,12..55
				{
					ma1=typedShuMa;
					maShu=2;
				}
				else if (typedShuMa>0 && typedShuMa<6)//typedShuMa=1,2,3,4,5
				{
					ma1 = ma1*10 + typedShuMa;
					maShu = 2;
				}
				else //typedShuMa ==0, 6,7,8,9
				{
				//  //Don't change it 
				}
			}
				break;
			case 2:	//MaShu==2
			{
				ma2=0;
				ma3=0;
				ma4=0;
				if(ma1==0) //MaShu==2
				{
					switch (typedShuMa)
					{
						case 0:
						{
							//for repeat input
							ma2=0;
							maShu=4;
						}
							break;
						case 1:
						case 2:
						case 3:
						case 4:
						case 5:
						{
							ma2=typedShuMa;
							maShu=3;
						}
							break;
						case 11:
							ma1=6; //mashu = 2;
							break;
						case 12:
							ma1=7;
							break;
						case 13:
							ma1=8;
							break;
						case 14:
							ma1=9;
							break;
						case 15:
							ma1=62;
							break;
						case 21:
						case 22:
						case 23:
						case 24:
						case 25:		//Switch User Cizu edit
						case 31:
						case 32:
						case 33:
						case 34:
						case 35:
						case 41:
						case 42:
						case 43:
						case 44:
						case 45:
						case 51:
						case 52:
						case 53:
						case 54:
						case 55:
						{
							ma1=0;
							ma2=typedShuMa;
							maShu=4;
						}
							break;							
						default:
							break; //ignore
					}					
				}
				else if (typedShuMa>10)  //MaShu==2 && ma1 != 0 //typedShuMa= 11 12 ... 55 
				{
			        ma2=typedShuMa;
					maShu=4;
				}
				else if(typedShuMa==0)   //MaShu==2 && ma1 != 0
				{
					maShu=4;
					ma2=typedShuMa;
				}
				else if (typedShuMa<6)		//MaShu==2 && ma1 != 0  //typedShuMa=1,2,3,4,5
				{
					ma2= typedShuMa;
					maShu = 3;
				}
				else if (typedShuMa<10) //MaShu==2 && ma1 != 0 //typedShuMa = 6,7,8,9
				{
					//ignore
				}
			}
				break;
			case 3: //maShu=3
			{
				if(ma1==0)	//maShu==3 and ma1==0
				{
					if (typedShuMa>10)	//maShu==3 and ma1==0 and typedShuMa>10
					{
						switch (typedShuMa)
						{
							case 11:
								ma1=6;
								ma2 = 0;
								maShu=2;
								break;
							case 12:
								ma1=7;
								ma2 = 0;
								maShu=2;
								break;
							case 13:
								ma1=8;
								ma2 = 0;
								maShu=2;
								break;
							case 14:
								ma1=9;
								ma2 = 0;
								maShu=2;
								break;
							case 15:
								ma1=62;
								ma2 = 0;
								maShu=2;
								break;
							case 21:
							case 22:
							case 23:
							case 24:
							case 25:		//switch �ַ��� User Cizu edit
							case 31:
							case 32:
							case 33:
							case 34:  //add
							case 35:
							case 41:
							case 42:
							case 43:
							case 44:
							case 45:
							case 51:
							case 52:
							case 53:
							case 54:
							case 55:
							{
								ma1=0;
								ma2=typedShuMa;
								maShu=4;
							}
								break;
							default:
								break; //ignore
								
						}
					}
					else if (typedShuMa<6)  //maShu==3 and ma1==0 and typedShuMa<6
					{
						int mT= ma2*10+typedShuMa;
						switch (mT)
						{
							case 11:
								ma1=6;
								ma2 = 0;
								maShu=2;
								break;
							case 12:
								ma1=7;
								ma2 = 0;
								maShu=2;
								break;
							case 13:
								ma1=8;
								ma2 = 0;
								maShu=2;
								break;
							case 14:
								ma1=9;
								ma2 = 0;
								maShu=2;
								break;
							case 15:
								ma1=62;
								ma2 = 0;
								maShu=2;
								break;
							case 21:
							case 22:
							case 23:
							case 24:
							case 25:		//User Cizu edit
							case 31:
							case 32:
							case 33:
							case 34:  //new
							case 35:  //new
							case 41:
							case 42:
							case 43:
							case 44:
							case 45:
							case 51:
							case 52:
							case 53:
							case 54:
							case 55:
							{
								ma1=0;
								ma2=mT;
								maShu=4;
							}
								break;
								
							default:
								break; //ignore
						}
					}
				}
				else if (typedShuMa>10) //maShu==3 and ma1 != 0 and typedShuMa>10 //typedShuMa= 11 12 ... 55 except 11 22 33
				{
					ma2=typedShuMa;
					maShu=4;
				}
		        else if(typedShuMa==0)  ////maShu==3 and ma1 != 0 and typedShuMa==0
				{
					maShu=4;
					ma2=typedShuMa;
				}
				else if (typedShuMa<6)	//maShu==3 and ma1 != 0 and typedShuMa != 0 and typedShuMa<6 //typedShuMa=1,2,3,4,5
				{
					//Log.d("Debug","typedShuMa M1: "+ma2 + "MaShu="+maShu);
					ma2= ma2*10+typedShuMa;
					maShu = 4;
					//Log.d("Debug","typedShuMa M1: "+ma2 + "MaShu="+maShu);
				}
				else if (typedShuMa<10) //maShu==3 and ma1 != 0 and typedShuMa>5 and typedShuMa<10 //typedShuMa = 6,7,8,9
				{
					//ignore all
				}
			}
				break;
			case 4: //maShu=4
			{		
				if (typedShuMa>10) //maShu==4 and typedShuMa>10 //typedShuMa= 11,12..55
				{
					ma3=typedShuMa;
					maShu=6;
				}
		        else if(typedShuMa==0)// && ma2>0)		//maShu==4  and typedShuMa==0  don't include m2=0 for query m2
				{
		        	//it is valid for 14 0 0 0; 44 0 0 0
					maShu=6;
					ma3=typedShuMa;
				}
				else if (typedShuMa<6)		//maShu==4  and typedShuMa>0 and typedShuMa<6 //typedShuMa=1,2,3,4,5
				{
					ma3= typedShuMa;
					maShu = 5;
				}
				else if (typedShuMa<10) //maShu==4  and typedShuMa>5 and typedShuMa<10 //typedShuMa = 6,7,8,9
				{
				}
			}
				break;
			case 5://maShu=5
			{
				if (typedShuMa>10)	//maShu==5 and typedShuMa>10 //typedShuMa= 11,12..55
				{
					ma3=typedShuMa;
					maShu=6;
				}
		        else if(typedShuMa==0 && ma2>0)	//maShu==5 and typedShuMa = 0, don't include ma2=0 for query ma2
				{
		        	//ignore  //do not procese query ma of ma3 = 0
					maShu=6;
					ma3=typedShuMa;
				}
				else if (typedShuMa<6)		//maShu==5 and typedShuMa<6 //typedShuMa=1,2,3,4,5
				{
					ma3= ma3*10+typedShuMa;
					maShu = 6;
				}
				else if (typedShuMa<10)	//maShu==5 and typedShuMa<10 //typedShuMa = 6,7,8,9
				{
				}
			}
				break;
			case 6: //maShu=6
			{
				if(ma1==99)  //indidate lianXiang
				{
					//ignore, since lianXiang only proce 3 typedShuMa, and ma1=99
				}
				else if((ma1==0)||(ma1==6)||(ma1==7)||(ma1==8)||(ma1==9)||(ma1==61)||(ma1==62)) //maShu==6 //ma1=6,7,8 only have 3 typedShuMa, ma1=0 has 2 typedShuMa
				{
					//ignore
				}
		        else if(typedShuMa==0)// && ma2>0)		//maShu==6 and typedShuMa=0 
				{
		        	//it is valid for 14 0 0 0; 44 0 0 0
					maShu=8;
					ma4=typedShuMa;
				}
				else if (typedShuMa > 0 && typedShuMa<6)	//maShu==6 and typedShuMa<6 //typedShuMa=1,2,3,4,5
				{
					ma4= typedShuMa;
					maShu = 7;
				}
				else if (typedShuMa>10) //maShu==6 and typedShuMa>10 //typedShuMa= 11,12..55
				{
					if (ma2==0) // && ma3>0)  //after ma2 queried
					{
						ma2=typedShuMa;
						ma4=0;
						maShu=6;
					}
					else if (ma2>0)
					{
						ma4=typedShuMa;
						maShu=8;
					}
				}
				else //if (typedShuMa<10) //maShu==6 and typedShuMa<10 //typedShuMa = 6,7,8,9
				{
					//ignore
				}
			}
				break;
			case 7://maShu=7
			{
		        if(typedShuMa==0)	//maShu==7 and typedShuMa==0 
				{
		        	//ignore
				}
				else if (typedShuMa<6)	//maShu==7 and typedShuMa<6 //typedShuMa=1,2,3,4,5
				{
					if (ma2>0)
					{
						ma4= ma4*10+typedShuMa;
						maShu = 8;
					}
					else if (ma2==0 && ma3>0)
					{
						ma2=ma4*10+typedShuMa;
						ma4=0;
						maShu=6;
					}
					else //if (ma2==0 && ma3==0) 
					{
						//ignore
					}
				}
				else if (typedShuMa>10) //maShu==7 //typedShuMa= 11,12..55
				{
					if (ma2>0)
					{
						ma4=typedShuMa;
						maShu=8;
					}
					else if (ma2==0 && ma3>0)  //after ma2 queried
					{
						ma2=typedShuMa;
						maShu=6;
					}
					else //if (ma2==0 && ma3==0) 
					{
						//ignore
					}
				}
				else //if (typedShuMa<10) //maShu==7 and typedShuMa<10 //typedShuMa = 6,7,8,9
				{
					//ignore
				}
			}
				break;
			default:
				clearState();
				break;
		}

		if(maShuBefore != maShu || ma1Before != ma1 )// when m1 = 0 and maShu = 2 and typed 6,7,8,9, 
		{
			if(maShuBefore == 0 && maShu>=1) //typed first ShuMa
			{
				typeSessionState=TypeSessionState.FirstType;
			}
			else if(maShuBefore>0 && maShu == 0) //tipically when type space or enter key
			{
				typeSessionState=TypeSessionState.Finished;
			}
			else if(maShuBefore>0 && maShu >0 ) //continue typing
			{
				typeSessionState=TypeSessionState.ContinueType;
			}
			else if(maShuBefore==8 && (maShu==1 || maShu==2) ) //continue typing
			{
				typeSessionState=TypeSessionState.SecondSession;
			}
			else
				typeSessionState=TypeSessionState.UnStart;
				
			savedShuMa=typedShuMa;
			return true;
		}
		else
		{
			//mean typingState did not change for this typing
			//do not need call typeback()
			return false;
		}
	}

	public void clearState()
	{
		ma1=ma2=ma3=ma4=maShu=0;
		
		engCharArrayLen=0;
		shengDiao = 0;
		engCharShuMa = 0;
		savedEngCharShuMa = 0;
		bEngCharArrayLenChanged = false;
		engCharArray[0] = '\0';		
	}
	
	private boolean isValidHeInputShuMaPlus0(int newMa)
	{
		//here we do not conside PinYin ShuMa m:36
		boolean fRet = false;

		switch(maShu)
		{
			case 0:
			case 8:
			{
				//0,1,2,3,...9
				if(newMa >=0 && newMa<=9)
				{
					fRet = true;
				}
				// >=11 and <=55
				else if((newMa/10 >= 1 && newMa/10 <=5) && (newMa%10 >= 1 && newMa%10 <=5)) 
				{
					fRet = true;
				}
				//Lian Xiang
				else if(newMa == 99)
				{
					fRet = true;
				}
				//Mode Selection
				else if(newMa == -2)
				{
					fRet = true;
				}
				else 
					fRet = false;
			}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			{
				//1,2,3,4,5
				if(newMa >=0 && newMa<=5)  //0 is valid input for all time such as 14 0 0 0, 44 0 0 0
				{
					fRet = true;
				}
				// >=11 and <=55
				else if((newMa/10 >= 1 && newMa/10 <=5) && (newMa%10 >= 1 && newMa%10 <=5)) 
				{
					fRet = true;
				}
				else
					fRet = false;
			}
				break;
			default:
				fRet = false;
				break;
		}

		return fRet;
	}
		
	private boolean typeBackShuMa()
	{
		//For ma1 = 6,7,8,9, and 0,1,2,3,4,5 and 99
		if(maShu==2 && (ma1<10 || ma1 ==99))
		{
			clearState();
			return true;
		}
		
		if(savedShuMa>10 || savedShuMa == 0)
		{
			switch(maShu)
			{
				case 0:
				case 1:
				case 2:
					clearState();
					break;
				case 3:
				case 4:
				{
					maShu = 2;
					ma2 = 0;
				}
					break;
				case 5:
				case 6:
				{
					maShu = 4;
					ma3 = 0;
				}
					break;
				case 7:
				case 8:
				{
					maShu = 6;
					ma4 = 0;
				}
					break;
				default:
					break;
			}
		}
		else		//savedShuMa<=10
		{
			switch (maShu)
			{
				case 0:
				case 1:
					clearState();
					break;
				case 2:
				{
					maShu = 1;
					ma1 = ma1/10;
				}
					break;
				case 3:
				{
					maShu = 2;
					ma2 = 0;
				}
					break;
				case 4:
				{
					maShu = 3;
					ma2 = ma2/10;
				}
					break;
				case 5:
				{
					maShu = 4;
					ma3 = 0;
				}
					break;
				case 6:
				{
					maShu = 5;
					ma3 = ma3/10;
				}
					break;
				case 7:
				{
					maShu = 6;
					ma4 = 0;
				}
					break;
				case 8:
				{
					maShu = 7;
					ma4 = ma4/10;
				}
					break;
				default:
					break;
			}
		}
		//always return true;
		if(maShu == 0)
		{
			typeSessionState=TypeSessionState.Finished;
		}
		else
			typeSessionState=TypeSessionState.TypeBack;
		
		return true;		
	}		
	
	public String getTypedEngCharArray()
	{
		String str = "";
		for(int i=0; i<engCharArrayLen; i++)
		{
			str += engCharArray[i];
		}
		
		switch(engCharShuMa)
		{
			case 1:
				str += "1- F1E2T3J4Z5";//"1- f1e2t3j4z5";
				break;
			case 2:
				str += "2- B1A2D3P4R5";//"2- b1a2d3p4r5";
				break;
			case 3:
				str += "3- L1I2N3H4K5M6";//"3- l1i2n3h4k5m6";
				break;
			case 4:
				str += "4- C1O2S3G4Q5";//"4- c1o2s3g4q5";
				break;
			case 5:
				str += "5- V1U2W3Y4X5";//"5- v1u2w3y4x5";
				break;
			default:
				break;
		}		
		return str;
	}
	
    public String getTypedShuMaStr()
    {
    	String typedMaStr = "";
    	switch(maShu)
    	{
    		case 1:
    		case 2:
    			typedMaStr = typedMaStr + ma1;
    			break;
       		case 3:
    		case 4:
    			typedMaStr = typedMaStr + ma1 + " " + typedMaStr + ma2;
    			break;
       		case 5:
    		case 6:
    			typedMaStr = typedMaStr + ma1 + " " + typedMaStr + ma2  + " " + typedMaStr + ma3;
    			break;
       		case 7:
    		case 8:
    			typedMaStr = typedMaStr + ma1 + " " + typedMaStr + ma2  + " " + typedMaStr + ma3  + " " + typedMaStr + ma4;
    			break;
    		default:
    			//typedMaStr = "";
    			break;
    	}
    	return typedMaStr;
    }

	public static enum TypeSessionState
    {
        UnStart,
        FirstType,
        ContinueType,
        Finished,
        TypeBack,
        SecondSession;
    }
}
