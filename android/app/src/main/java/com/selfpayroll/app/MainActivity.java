package com.selfpayroll.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private boolean biometricRequested = false;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("self_payroll_security", MODE_PRIVATE);

        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view, android.webkit.WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");

        // Native Android biometric lock: PIN remains the fallback handled by the web app.
        // Fingerprint/biometric is only offered when the user has enabled it in the app.
        if (prefs.getBoolean("biometric_enabled", false)) {
            webView.setVisibility(WebView.INVISIBLE);
            showBiometricPrompt();
        }
    }


    private void showBiometricPrompt() {
        if (biometricRequested) return;
        biometricRequested = true;
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Self Payroll")
                .setSubtitle("Unlock with fingerprint or device biometric")
                .setNegativeButtonText("Use PIN")
                .build();

        BiometricPrompt prompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        runOnUiThread(() -> {
                            webView.setVisibility(WebView.VISIBLE);
                            biometricRequested = false;
                        });
                    }
                    @Override public void onAuthenticationError(int errorCode, CharSequence errString) {
                        runOnUiThread(() -> {
                            if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                biometricRequested = false;
                            }
                            // If user selects "Use PIN", the web PIN lock is shown.
                            webView.setVisibility(WebView.VISIBLE);
                            webView.evaluateJavascript(
                                    "if(window.showPinLockFromNative){window.showPinLockFromNative();}", null);
                        });
                    }
                    @Override public void onAuthenticationFailed() {
                        // Keep the biometric prompt available for another attempt.
                    }
                });
        prompt.authenticate(info);
    }

    public void enableBiometricLock() {
        prefs.edit().putBoolean("biometric_enabled", true).apply();
    }

    public void disableBiometricLock() {
        prefs.edit().putBoolean("biometric_enabled", false).apply();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
