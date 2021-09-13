package com.navitend.ble1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class multipleChoiceDialogFragment extends DialogFragment {
    public interface onMultiChoiceListener{
        void onPositiveButtonClicked(String[] list, ArrayList<String> selectedItemList);
        void onNegativeButtonClicked();}
        onMultiChoiceListener mListener = null;
        @Override
        public void onAttach(Context context){
            super.onAttach(context);
            try {
                mListener= (onMultiChoiceListener) context;
            } catch (Exception e) {
                throw new ClassCastException(getActivity().toString()+"onMultiChoiceListener must be implemented");
            }
        }

public Dialog onCreateDialog(Bundle savedInstanceState)
{
    final ArrayList<String> selectedItemList = new ArrayList<String>();
    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

    final String[] list=getActivity().getResources().getStringArray(R.array.configuration_options);

    builder.setTitle("Select Your Choice").setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {

        @Override
        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
            if(b){
                selectedItemList.add(list[i]);

            }
            else {
                selectedItemList.remove(list[i]);
            }
        }
    })
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onPositiveButtonClicked(list, selectedItemList);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onNegativeButtonClicked();
                }
            });
    return builder.create();
}}
