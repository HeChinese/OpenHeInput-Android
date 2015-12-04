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

public class Setting {	
	public boolean bHealthy;

	//Confusing bool bZiCiCodePrompt; //used for HeMa Training, to tell ZiCiPrompt on or off.
	public boolean bDanZiOnly;							//default to false, true for DanZi input mode, false for DanZi and CiZu together
																//false for numPad is input Chinese
	//public InputMode numPadMode;				//Current Keyboard input mode
	//public InputMode mainKeyboardMode;		//Current Keyboard input mode
	public InputMode currentKeyMode;			//Current Key input mode
	public InputMode systemKeyMode;
	
	//public boolean bSimplified_Chinese;			//is value is dependent on install setting, it will extract at this class created
														//true for Simplified_Chinese, false for traditional Chinese
	public boolean bNormalZiKu;						//default to ture, true for oftern used danZi set

	public boolean bLianXiangPurchased;
	public boolean bLianXiang;		//default is ture;
	public boolean bPinYinPrompt;
	public boolean bHeMaModeNumpad;
	public boolean bPinYinModeNumpad;
	public boolean bHeEnglishModeNumpad;
	
	public Setting() {
		super();
		
		bHealthy = true;
		bDanZiOnly = false;

		// systemKeyMode setting is relay on InputSubType.
		//systemKeyMode = InputMode.HeMa_Simplified_Mode;
		//numPadMode = InputMode.HeMaMode;
		//currentKeyMode = InputMode.HeMa_Traditional_Mode;

		//bSimplified_Chinese = true;
		bNormalZiKu = true;
		
		bLianXiangPurchased = false;
		bLianXiang = false;
		bPinYinPrompt = true;
		
		bHeMaModeNumpad = true;
		bPinYinModeNumpad = true;
		bHeEnglishModeNumpad = true;
	}

	public enum InputMode
	{
		//Main Keyboard HeMa Simplified (ZiXin)
		HeMa_Simplified_Mode,
		HeMa_Traditional_Mode,
		PinYinMode,

		HeEnglishMode,
		EnglishMode,

		NumberMode,
		//NumberModeTemp	//input number between HeMaMode input, * key can be used for change back to HeMaMode.
		//LianXiangMode				//Indicated by m1 == 99
	}
}


