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

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.res.Resources;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HeInput_Activation_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HeInput_Activation_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeInput_Activation_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String hostApplicationName;

    private OnFragmentInteractionListener mListener;

    private TextView setting_instruction = null;
    private Button activeHeInputBtn = null;
    private Button selectDefaultMethodBtn = null;
    private Button selectChineseDialectBtn = null;

    private EditText editText = null;
    private String inputId = "";

    public HeInput_Activation_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HeInput_Activation_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HeInput_Activation_Fragment newInstance(String param1, String param2) {
        HeInput_Activation_Fragment fragment = new HeInput_Activation_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_he_input_activation, container, false);

        if (mListener != null) {
            hostApplicationName = mListener.getHostApplicationName();
        }

        setting_instruction = (TextView)rootView.findViewById(R.id.setting_instruction);
        activeHeInputBtn = (Button)rootView.findViewById(R.id.activeHeInput);
        activeHeInputBtn.setOnClickListener(clickListener);

        selectDefaultMethodBtn = (Button)rootView.findViewById(R.id.selectDefaultInputMethod);
        selectDefaultMethodBtn.setOnClickListener(clickListener);

        selectChineseDialectBtn = (Button)rootView.findViewById(R.id.selectChineseDialect);
        selectChineseDialectBtn.setOnClickListener(clickListener);

        editText = (EditText)rootView.findViewById(R.id.editText_view);
        editText.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI()
    {
        boolean bActivated = false;
        boolean bDefaultKeyboard = false;
        boolean bDialectSelected = false;

        activeHeInputBtn.setEnabled(true);
        selectDefaultMethodBtn.setEnabled(false);
        selectDefaultMethodBtn.setTextColor(Color.GRAY);
        selectChineseDialectBtn.setEnabled(false);
        selectChineseDialectBtn.setTextColor(Color.GRAY);

        //Check Activited
        //http://stackoverflow.com/questions/4210086/how-can-i-get-a-list-of-all-the-input-methods-and-their-names-that-are-install
        InputMethodManager imeManager = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> InputMethods = imeManager.getEnabledInputMethodList();

        for (InputMethodInfo item : InputMethods)
        {
            if (item.getPackageName().toLowerCase().equals(hostApplicationName.toLowerCase()))
            {
                bActivated = true;

                inputId = item.getId();
                Log.d("Input Id: ","InputId: " +inputId);
                setting_instruction.setText(getString(R.string.instruction_2_steps));

                activeHeInputBtn.setEnabled(false);
                activeHeInputBtn.setTextColor(Color.GRAY);
                activeHeInputBtn.setText(getString(R.string.heInput_activated));

                selectDefaultMethodBtn.setEnabled(true);
                selectDefaultMethodBtn.setTextColor(Color.RED);

                selectChineseDialectBtn.setEnabled(false);
                selectChineseDialectBtn.setTextColor(Color.GRAY);

                break;
            }
        }

        // Check default keyboard
        if (bActivated)
        {
            String defaultName = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            if (defaultName.toLowerCase().contains(hostApplicationName.toLowerCase()))
            {
                bDefaultKeyboard = true;
            }
        }

        if (bActivated && bDefaultKeyboard)
        {
            setting_instruction.setText(getString(R.string.instruction_setting_done));

            selectDefaultMethodBtn.setEnabled(false);
            selectDefaultMethodBtn.setTextColor(Color.GRAY);
            selectDefaultMethodBtn.setText(getString(R.string.heInput_is_default));

            selectChineseDialectBtn.setEnabled(true);
            selectChineseDialectBtn.setTextColor(Color.RED);

            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            int viewId = v.getId();
            if (viewId == R.id.activeHeInput) {
                activeHeInput();
            }
            else if (viewId == R.id.selectDefaultInputMethod) {
                showInputMethodPicker();
            }
            else if (viewId == R.id.selectChineseDialect) {
                showChineseDialectSelection();
            }
        }
    };

    private void activeHeInput() {
        Intent i = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivity(i);
    }

    private void showInputMethodPicker()
    {
        InputMethodManager imeManager = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imeManager != null) {
            imeManager.showInputMethodPicker();
        }
    }

    //https://blog.swiftkey.com/tech-blog-android-input-method-subtypes/
    private void showChineseDialectSelection() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);

        intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, inputId);
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.select_chinese_dialect_title));  //"Select Enabled Subtypes"
        startActivity(intent);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        String getHostApplicationName();
    }
}
