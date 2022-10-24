package wrteam.multivendor.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class WebViewFragment extends Fragment {
    public ProgressBar prgLoading;
    public WebView mWebView;
    public String type;
    View root;
    Activity activity;
    Button btnPrint;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_web_view, container, false);
        setHasOptionsMenu(true);
        activity = getActivity();
        type = requireArguments().getString("type");

        prgLoading = root.findViewById(R.id.prgLoading);
        btnPrint = root.findViewById(R.id.btnPrint);
        mWebView = root.findViewById(R.id.webView1);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });

        try {
                switch (type) {
                    case "Privacy Policy":
                        GetContent(Constant.GET_PRIVACY, "privacy");
                        break;
                    case "Terms & Conditions":
                        GetContent(Constant.GET_TERMS, "terms");
                        break;
                    case "Contact Us":
                        GetContent(Constant.GET_CONTACT, "contact");
                        break;
                    case "About Us":
                        GetContent(Constant.GET_ABOUT_US, "about");
                        break;
                    default:
                        GetInvoice(type);
                        break;
                }
                activity.invalidateOptionsMenu();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    public void GetInvoice(final String type) {
        try {
            mWebView.loadUrl(Constant.INVOICE_URL + "?id=" + type.split("#")[1] + "&token=" + ApiConfig.createJWT("eKart", "eKart Authentication"));
            btnPrint.setVisibility(View.VISIBLE);
            btnPrint.setOnClickListener(v -> createWebPagePrint(mWebView));
        } catch (Exception e) {
            e.printStackTrace();
            prgLoading.setVisibility(View.GONE);
        }
    }

    public void createWebPagePrint(WebView webView) {
        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
        String jobName = getString(R.string.order) + "_" + type.split("#")[1];
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        PrintJob printJob = printManager.print(jobName, printAdapter, builder.build());

        if (printJob.isCompleted()) {
            Toast.makeText(activity, R.string.print_complete, Toast.LENGTH_SHORT).show();
        } else if (printJob.isFailed()) {
            Toast.makeText(activity, R.string.print_failed, Toast.LENGTH_SHORT).show();
        }
        // Save the job object for later status checking
    }


    public void GetContent(final String type, final String key) {
        prgLoading.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(type, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean(Constant.ERROR)) {
                        String privacyStr = obj.getString(key);
                        mWebView.setVerticalScrollBarEnabled(true);
                        mWebView.loadDataWithBaseURL("", privacyStr, "text/html", "UTF-8", "");

                        prgLoading.setVisibility(View.GONE);
                    } else {
                        prgLoading.setVisibility(View.GONE);
                        Toast.makeText(activity, obj.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                    }
                    prgLoading.setVisibility(View.GONE);
                } catch (JSONException e) {
                        e.printStackTrace();

                    prgLoading.setVisibility(View.GONE);

                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = requireArguments().getString("type");
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}