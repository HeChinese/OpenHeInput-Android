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

public class ZiCiObject {

    public String ziCi ="";
    public int ma1, ma2, ma3, ma4;

    public int promptShuMa;
    public int heMaOrder;

    public ZiCiObject()
    {}

    public ZiCiObject(String ziCiStr,int promtSM, int m1, int m2, int m3, int m4, int order){

        ziCi = ziCiStr;
        promptShuMa = promtSM;
        ma1 = m1;
        ma2 = m2;
        ma3 = m3;
        ma4 = m4;
        heMaOrder = order;
    }

    public static ZiCiObject copy(ZiCiObject orignal) {

        ZiCiObject nObj = new ZiCiObject();

        nObj.ziCi = orignal.ziCi;
        nObj.promptShuMa = orignal.promptShuMa;
        nObj.ma1 = orignal.ma1;
        nObj.ma2 = orignal.ma2;
        nObj.ma3 = orignal.ma3;
        nObj.ma4 = orignal.ma4;
        nObj.heMaOrder = orignal.heMaOrder;

        return nObj;
    }

    public String generateShuMaString()
    {
        String shuMaStr = "";
        if (ma2==0)
        {
            shuMaStr = String.format("%02d",ma1);
        }
        else if (ma3 == 0)
        {
            shuMaStr = String.format("%02d %02d",ma1,ma2);
        }
        else if (ma3 > 0)
        {
            shuMaStr = String.format("%02d %02d %02d",ma1,ma2,ma3);
        }
        return shuMaStr;
    }
}
