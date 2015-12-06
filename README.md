# OpenHeInput-Android

Open source HeInput Android project

This application is based on Google SoftKeyboard sample:
https://android.googlesource.com/platform/development/+/master/samples/SoftKeyboard/

Android IME Frameword guide:
http://developer.android.com/intl/ja/guide/topics/text/creating-input-method.html

Now it is full functional Chinese Input application, can be deployed to Android devices.

This application is also available in the Android Play Store:
https://play.google.com/store/apps/details?id=net.HeZi.Android.HeInput

Input features include:

1. 21,000+ Chinese words;
2. 184,000+ Chinese phrases;
3. Chinese Simplified and Traditional option;
4. Search code with PinYin;
5. Convenient switch between HeInput/PinYin/English/Number input.

# Application Structure

Created with Android Studio 1.5 and Android sdk 6.0.

Project Name: HeChinese, include 3 modules:

1. Android Library Module: HeLibrary; 
2. Android Library Module: HeInputLibrary; 
3. Android Application Module: OpenHeInput. 

The main parts of code are:

1. Hekeyboard which include CandidateListView;
2. HeInput_DataServer which provide input data and functions for Android InputMethodService;
   1. HeInput_DataServer includes EngineCollection;
   2. EngineCollection includes HeMaEngine, PinYinEngine, HeEnglishEngine, etc;
   3. Each Engine access SQLite database;

# Database Structure

Include a SQLite database: hema_db.sqlite, it includes tables:

create table HanZi
(
--_id INTEGER PRIMARY KEY,
HanZi text,	
M1 numeric,
M2 numeric,
M3 numeric,
M4 numeric,
GBOrder numeric,
B5Order numeric
);

create table CiZu
(
--_id INTEGER PRIMARY KEY,
CiZu text,	
M1 numeric,
M2 numeric,
M3 numeric,
M4 numeric,
HeMaOrder numeric,
JianFan numeric
);

create table English_Word
(
--_id INTEGER PRIMARY KEY,
word text,	
HeMaOrder numeric
);

create table PinYin_Number
(
--_id INTEGER PRIMARY KEY,
PinYin text,
number numeric
);

create table PinYin_HanZi
(
--_id INTEGER PRIMARY KEY,
PinYin text,	
HanZiString text
);

create table HanZi_PinYin
(
--_id INTEGER PRIMARY KEY,
HanZi text,	
PinYin text,
ShengDiao numeric
);

# ToDo list in short

1. Convienent function for user adding words and phrases;
2. Function for adding users favorite phrases collection;
3. Keyboard skin;
4. Convienent way to input emoji;
5. Test and make UI dimens changes for more Android devices;
6. Modify and publish this app to different Android platform.

# HeInput related information:

http://www.hezi.net/He/UserGuide_Concise/en-us/Set/HeChinese_Guide_Concise.htm
