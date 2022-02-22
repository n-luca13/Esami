package com.luca.applicazioneesame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import com.luca.applicazioneesame.loginhelpers.InputValidation;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;
import com.luca.applicazioneesame.sqlhelper.Studenti;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private TextView txtLogin, txtSignup, txtPasswordForgot;
    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword, textInputLayoutUsername, textInputLayoutName;
    private TextInputEditText textInputEditTextEmail, textInputEditTextPassword, textInputEditTextUsername, textInputEditTextName;
    private Button btnSubmit;
    private boolean towardsLogin = true;
    private DatabaseHelper db;
    private InputValidation inputValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ApplicazioneEsame);
        setContentView(R.layout.activity_login);

        initViews();
        initObjects();
        switchLoginSignup();


        txtLogin.setOnClickListener(view -> {
            if(towardsLogin){
                switchLoginSignup();
            }
        });

        txtSignup.setOnClickListener(view -> {
            if(!towardsLogin){
                switchLoginSignup();
            }
        });

        btnSubmit.setOnClickListener(view -> {
            if(towardsLogin){
                registerUserSQLLite();
            }else{
                verifyFromSQLite();
            }
        });
    }

    private void initViews() {
        txtLogin = findViewById(R.id.txtLogin);
        txtSignup = findViewById(R.id.txtSignUp);
        txtPasswordForgot = findViewById(R.id.txtForgot);
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutName = findViewById(R.id.textInputLayoutName);
        btnSubmit = findViewById(R.id.btnSubmit);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        textInputEditTextUsername = findViewById(R.id.textInputEditTextUsername);
        textInputEditTextName = findViewById(R.id.textInputEditTextName);
    }

    private void initObjects() {
        db = new DatabaseHelper(getApplicationContext());
        inputValidation = new InputValidation(getApplicationContext());
    }

    private void verifyFromSQLite(){
        // Il campo email è pieno/digitato?
        if (inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email_missing))) {
            return;
        }
        // Controllo validità email: presenta le caratteristiche tipiche di un indirizzo mail?
        if (inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        // Il campo password è pieno/digitato?
        if (inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return;
        }

        // Controllo email e password nel DB
        Studenti studente = db.checkUser(
                Objects.requireNonNull(textInputEditTextEmail.getText()).toString().trim(),
                Objects.requireNonNull(textInputEditTextPassword.getText()).toString().trim()
        );
        if (studente != null) {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            Gson gson = new Gson();
            String jsonStudent = gson.toJson(studente);
            mainIntent.putExtra("studente", jsonStudent);
            textInputEditTextEmail.setText(null);
            textInputEditTextPassword.setText(null);
            startActivity(mainIntent);
        } else {
            // Messaggio di errore generico: Controllare email e/o password
            textInputLayoutPassword.setError(getString(R.string.error_valid_email_password));
            textInputLayoutEmail.setError(getString(R.string.error_valid_email_password));
        }
    }

    private void switchLoginSignup(){
        // MOSTRA SCHERMATA DI LOGIN
        if (towardsLogin){
            towardsLogin = false;
            txtLogin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.text_selected, null));
            txtLogin.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light));
            txtLogin.setTypeface(Typeface.DEFAULT_BOLD);
            txtLogin.setElevation(4);

            txtSignup.setElevation(0);
            txtSignup.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.text_unselected, null));
            txtSignup.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            txtSignup.setTypeface(Typeface.DEFAULT);

            btnSubmit.setText(R.string.log_in);
            txtPasswordForgot.setVisibility(View.VISIBLE);
            textInputLayoutUsername.setVisibility(View.GONE);
            textInputLayoutName.setVisibility(View.GONE);
        // MOSTRA SCHERMATA DI REGISTRAZIONE
        }else{
            towardsLogin = true;
            textInputLayoutPassword.setErrorEnabled(false);
            textInputLayoutEmail.setErrorEnabled(false);
            txtLogin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.text_unselected, null));
            txtLogin.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            txtLogin.setTypeface(Typeface.DEFAULT);
            txtLogin.setElevation(0);

            txtSignup.setElevation(4);
            txtSignup.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.text_selected, null));
            txtSignup.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light));
            txtSignup.setTypeface(Typeface.DEFAULT_BOLD);

            btnSubmit.setText(R.string.sign_up);
            txtPasswordForgot.setVisibility(View.GONE);
            textInputLayoutUsername.setVisibility(View.VISIBLE);
            textInputLayoutName.setVisibility(View.VISIBLE);
        }
    }

    private void registerUserSQLLite() {
        // Il campo email è pieno/digitato?
        if (inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email_missing))) {
            return;
        }
        // Controllo validità email: presenta le caratteristiche tipiche di un indirizzo mail?
        if (inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        // Il campo username è pieno/digitato?
        if (inputValidation.isInputEditTextFilled(textInputEditTextUsername, textInputLayoutUsername, getString(R.string.error_message_username))) {
            return;
        }
        // Il campo nome/cognome è pieno/digitato?
        if (inputValidation.isInputEditTextFilled(textInputEditTextName, textInputLayoutName, getString(R.string.error_message_name))) {
            return;
        }
        // Il campo password è pieno/digitato?
        if (inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return;
        }

        // Controllo esistenza indirizzo email nel database
        if (!db.checkUser(Objects.requireNonNull(textInputEditTextEmail.getText()).toString().trim())) {
            Studenti s = new Studenti();
            s.setUsername(Objects.requireNonNull(textInputEditTextUsername.getText()).toString().trim());
            s.setNome_st(Objects.requireNonNull(textInputEditTextName.getText()).toString().trim());
            s.setEmail(textInputEditTextEmail.getText().toString().trim());
            s.setPass(Objects.requireNonNull(textInputEditTextPassword.getText()).toString().trim());

            if(db.insertStudente(s)){ // Se l'inserimento va a buon fine...
                Toast.makeText(LoginActivity.this, "REGISTRAZIONE EFFETTUATA", Toast.LENGTH_LONG).show();
                textInputEditTextName.setText(null);
                textInputEditTextUsername.setText(null);
                switchLoginSignup();
            }else{
                Toast.makeText(LoginActivity.this, "SERVIZIO MOMENTANEAMENTE NON DISPONIBILE", Toast.LENGTH_LONG).show();
            }
        } else { // Email già presente nel DB
            textInputLayoutEmail.setError(getString(R.string.error_message_email_duplicate));
        }
    }

}