package org.kineticsfoundation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.kineticsfoundation.R;

/**
 * Class contains static methods for create terms and policy dialogs
 * Created by tsheremeta on 5/7/14.
 */
public class TermsDialog {
    public static enum Type {TERMS, POLICY}

    private static Dialog dialog;

    public static Dialog createTerms(final Context context, Type type) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View alertDialogView = inflater.inflate(R.layout.alert_dialog_layout, null);

        WebView myWebView = (WebView) alertDialogView.findViewById(R.id.DialogWebView);
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.setOnKeyListener(new DialogWebViewLinener());
        myWebView.setWebViewClient(new DialogWebViewClient(context));
        myWebView.loadUrl(getFileUrl(type));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(getResTitle(type)));
        builder.setView(alertDialogView);
        builder.setPositiveButton(context.getString(R.string.button_close)
                , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return dialog = builder.create();
    }

    private static int getResTitle(Type type) {
        switch (type) {
            case TERMS:
                return R.string.dialog_title_terms;
            case POLICY:
                return R.string.dialog_title_policy;
        }

        return R.string.dialog_title_terms;
    }

    private static String getFileUrl(Type type) {
        String urlBase = "file:///android_asset/";

        switch (type) {
            case TERMS:
                return urlBase + "terms.html";
            case POLICY:
                return  urlBase + "policy.html";
        }

        return urlBase + "terms.html";
    }

    private static class DialogWebViewLinener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event){
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                WebView webView = (WebView) v;

                switch(keyCode){
                    case KeyEvent.KEYCODE_BACK:
                        if(webView.canGoBack()){
                            webView.goBack();
                            return true;
                        }
                        break;
                }
            }
            return false;
        }
    }

    private static class DialogWebViewClient extends WebViewClient{
        private Context context;

        private DialogWebViewClient(Context context) {
            this.context = context;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            if(url != null && dialog != null){
                if(url.contains("terms.html")){
                    dialog.setTitle(R.string.dialog_title_terms);
                } else if (url.contains("policy.html")){
                    dialog.setTitle(R.string.dialog_title_policy);
                } else {
                    dialog.setTitle(view.getTitle());
                }
            }
            super.onPageFinished(view, url);
        }

    public boolean shouldOverrideUrlLoading(WebView view, String url){
        if(url.startsWith("mailto:")){
            Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            context.startActivity(Intent.createChooser(email, "Choose an Email client"));
        } else if(url.startsWith("http")){
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browser);
        }

        view.loadUrl(url);
        return true;
    }


}
}
