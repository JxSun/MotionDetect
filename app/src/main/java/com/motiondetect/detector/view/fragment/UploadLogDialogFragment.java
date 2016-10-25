package com.motiondetect.detector.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.motiondetect.detector.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadLogDialogFragment extends DialogFragment {
    public interface OnSetFileNameListener {
        void onSetFileNames(String analyzedName, String rawName);
    }

    private OnSetFileNameListener mListener;
    private Context mContext;

    public static UploadLogDialogFragment newInstance(Context context) {
        String[] names = generateTemplateFileNames();

        UploadLogDialogFragment fragment = new UploadLogDialogFragment();
        fragment.bindContext(context);
        Bundle bundle = new Bundle();
        bundle.putSerializable("analyzed_template_name", names[0]);
        bundle.putSerializable("raw_template_name", names[1]);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void bindContext(Context context) {
        mContext = context;
    }

    private static String[] generateTemplateFileNames() {
        String[] names = new String[2];
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTime = format.format(Calendar.getInstance().getTime());
        names[0] = "analyzed_" + dateTime + ".txt";
        names[1] = "raw_" + dateTime + ".txt";
        return names;
    }

    public void setOnSetFileNameListener(OnSetFileNameListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String analyzedTempName = (String) getArguments().getSerializable("analyzed_template_name");
        final String rawTempName = (String) getArguments().getSerializable("raw_template_name");

        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_file_names, null);
        final EditText analyzedEditor = (EditText) dialogView.findViewById(R.id.analyzed_file_name_editor);
        analyzedEditor.setText(analyzedTempName);
        final EditText rawEditor = (EditText) dialogView.findViewById(R.id.raw_file_name_editor);
        rawEditor.setText(rawTempName);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set the upload files' names")
                .setView(dialogView)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onSetFileNames(analyzedEditor.getText().toString(),
                                    rawEditor.getText().toString());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
