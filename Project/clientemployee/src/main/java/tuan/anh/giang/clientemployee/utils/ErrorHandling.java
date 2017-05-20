package tuan.anh.giang.clientemployee.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import tuan.anh.giang.clientemployee.R;



public class ErrorHandling {
    public static void BackendlessErrorCode(Context context, String ErrorCode){
        switch (Integer.parseInt(ErrorCode)){
            case 3033:
                showErrorDialog(context,context.getResources().getString(R.string.er_3033));
                break;
            case 3040:
                showErrorDialog(context,context.getResources().getString(R.string.er_3040));
                break;
            case 3087:
                showErrorDialog(context,context.getResources().getString(R.string.er_3087));
                break;
            case 1155:
                showErrorDialog(context,context.getResources().getString(R.string.er_3087));
                break;
            case 3003:
                showErrorDialog(context,context.getResources().getString(R.string.er_3003));
                break;
            default:
                showErrorDialog(context,context.getResources().getString(R.string.undefined_er));
                break;
        }
    }
    private static void showErrorDialog(Context context, String errorMessage){
        new AlertDialog.Builder(context)
                .setTitle(R.string.dlg_error)
                .setMessage(errorMessage)
                .create()
                .show();
    }
}
