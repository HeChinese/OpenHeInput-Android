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

package net.HeZi.Android.HeInputLibrary;

import java.util.HashMap;
import java.util.List;

import net.HeZi.Android.HeInputLibrary.HeInputService;
import net.HeZi.Android.HeInputLibrary.R;
import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class CandidateListView extends ListView
implements OnItemClickListener
{
    private HeInputService mService;

    private Context cxt;

    protected CandidateItemInteractionListener itemListener;

    public interface CandidateItemInteractionListener {

        public void onItemInteraction(int itemIndexOnThePage);
    }

    public CandidateListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
		this.setClickable(false);
		setOnItemClickListener(this);
	}

	/*
	public HeListView(Context context) {
		super(context);
		cxt = context;
		itemIndex = 0;
		totalNumber = 0;
		setOnItemClickListener(this);
	}
	//*/

    public void printListViewPage(List<HashMap<String, String>> onePageList, int itemIndex)
	{
		String[] columns = new String[] {"ZiCi","PromptMa"};
	    // the XML defined views which the data will be bound to
	    int[] to = new int[] { R.id.ziCiText, R.id.shuMaPrompt };
	 
	    //onePageRows = getOnePageList();

	    SimpleAdapter adapter = new SimpleAdapter(cxt, onePageList, R.layout.item, columns, to);
	    
	 	this.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		this.setAdapter(adapter);
		this.setItemChecked(itemIndex, true);
	}
	
	protected void onDraw(Canvas canvas) {
		   super.onDraw(canvas);
		   
           //canvas.translate(200, -50);            
           
	}
	
    /**
     * A connection back to the service to communicate with the text field
     * @param listener
     */
    public void setService(HeInputService listener) {

        mService = listener;
        itemListener = listener;
    }

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		super.setSelection(position);
	}
	//*

	@Override
	public void onItemClick(AdapterView<?> listView, View view, 
	     int position, long id) {

	    //Toast.makeText(cxt, "Magic", Toast.LENGTH_LONG).show();
	    itemListener.onItemInteraction(position);
	   }
}
