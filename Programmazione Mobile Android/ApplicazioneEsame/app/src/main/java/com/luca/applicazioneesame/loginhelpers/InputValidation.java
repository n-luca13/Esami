package com.luca.applicazioneesame.loginhelpers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;


public class InputValidation {
    private final Context context;

    public InputValidation(Context context) {
        this.context = context;
    }

    // Controllare se l'utente ha digitato email e password
    public boolean isInputEditTextFilled(TextInputEditText textInputEditText, TextInputLayout textInputLayout, String message) {
        String value = Objects.requireNonNull(textInputEditText.getText()).toString().trim();
        if (value.isEmpty()) {
            textInputLayout.setError(message);
            hideKeyboardFrom(textInputEditText);
            return true;
        } else {
            textInputLayout.setErrorEnabled(false);
        }

        return false;
    }

    // CONTROLLARE VALIDITA' EMAIL: presenta le caratteristiche tipiche di un indirizzo email?
    public boolean isInputEditTextEmail(TextInputEditText textInputEditText, TextInputLayout textInputLayout, String message) {
        String value = Objects.requireNonNull(textInputEditText.getText()).toString().trim();
        if (value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            textInputLayout.setError(message);
            hideKeyboardFrom(textInputEditText);
            return true;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return false;
    }

    // Metodo per nascondere la tastiera
    private void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}