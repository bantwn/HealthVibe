package com.example.vicky.healthvibe;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vicky on 30/8/2017.
 */

public class ConfirmPasswordDialog extends DialogFragment {



    private static final String TAG = "ConfirmPasswordDialog";

    public interface OnConfirmPasswordListener{
        public void onConfirmPassword(String Password);
    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;

    // vars
    TextView mPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container,false);
        mPassword = (TextView)view.findViewById(R.id.confirm_password);

        Log.d(TAG, "OnCreateView: started ");

        TextView confirmDialog = (TextView)view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: captured password and confirming");

                String password = mPassword.getText().toString();
                if(!password.equals("")){
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }else {
                    Toast.makeText(getActivity(),"you must enter a password",Toast.LENGTH_SHORT).show();
                }

            }
        });

        TextView cancelDialog = (TextView)view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: closing the dialog");
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
         mOnConfirmPasswordListener =(OnConfirmPasswordListener)getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG,"onAttach: ClassCastException: " +e.getMessage());

        }
    }
}
