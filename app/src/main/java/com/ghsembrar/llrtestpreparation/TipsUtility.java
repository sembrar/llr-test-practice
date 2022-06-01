package com.ghsembrar.llrtestpreparation;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class TipsUtility {

    static void show_tip(Context context, int title_res_id, int tip_res_id) {
        new AlertDialog.Builder(context)
                .setTitle(title_res_id)
                .setMessage(tip_res_id)
                .setPositiveButton(R.string.tip_dialog_button_dismiss, null)
                .create().show();
    }
}
