package com.luca.applicazioneesame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;
import com.luca.applicazioneesame.sqlhelper.Docenti;
import com.luca.applicazioneesame.sqlhelper.Lezioni;
import com.luca.applicazioneesame.sqlhelper.Materie;

public class LessonInfo extends AppCompatActivity {
    private TextView txtGiorno, txtOra, txtMateria, txtDocente;
    private DatabaseHelper db;
    private ConstraintLayout lessonContainer;
    private Lezioni lezione;
    private Docenti docente;
    private Materie materia;
    private ConstraintLayout interactiveButtons;
    private Button btnCancel, btnConfirm;
    private Boolean isLessonUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_info);

        initViews();
        initObjects();

        txtGiorno.setText(lezione.getData().split(" ")[0]);
        txtOra.setText(String.format(getString(R.string.from_to_time), lezione.getFromTime(), lezione.getToTime()));
        txtMateria.setText(materia.getNome_materia());
        txtDocente.setText(docente.getNome_doc());

        switchView();
    }

    private void initViews() {
        lessonContainer = findViewById(R.id.lessonContainer);
        txtGiorno = findViewById(R.id.txtGiorno);
        txtOra = findViewById(R.id.txtOra);
        txtMateria = findViewById(R.id.txtMateria);
        txtDocente = findViewById(R.id.txtDocente);
        interactiveButtons = findViewById(R.id.interactiveButtons);
    }

    private void initObjects() {
        db = new DatabaseHelper(getApplicationContext());
        lezione = db.getLezioneFromDB(getIntent().getIntExtra("id_lezione", 0));
        docente = db.getDocenteFromDB(lezione.getId_docente());
        materia = db.getMateriaFromDB(docente.getId_materia());
    }

    private void switchView(){
        GradientDrawable drawable = (GradientDrawable) lessonContainer.getBackground();
        if(lezione.getStato_lezione().equals("disdetta")){
            TextView txtStatus = findViewById(R.id.txtStatus);
            drawable.setStroke(4, ContextCompat.getColor(this,R.color.primary));
            txtStatus.setVisibility(View.VISIBLE);
            interactiveButtons.setVisibility(View.GONE);
        }else if(lezione.getStato_lezione().equals("frequentata")){
            LinearLayout lessonFrequented = findViewById(R.id.lessonFrequented);
            drawable.setStroke(4, ContextCompat.getColor(this,R.color.green));
            lessonFrequented.setVisibility(View.VISIBLE);
            interactiveButtons.setVisibility(View.GONE);
        }else{
            btnCancel = findViewById(R.id.btnCancel);
            btnConfirm = findViewById(R.id.btnConfirm);
            interactiveButtons.setVisibility(View.VISIBLE);
            drawable.setStroke(4, ContextCompat.getColor(this,R.color.gray));
            setOnClickListeners();
        }
    }


    private void setOnClickListeners(){
        btnCancel.setOnClickListener(view ->
                new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_cancel)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.si, (dialog, id) -> {
                    lezione.setStato_lezione("disdetta");
                    if(db.updateStatoLezione(lezione)){
                        isLessonUpdated = true;
                        switchView();
                    }else{
                        Toast.makeText(LessonInfo.this, "SERVIZIO MOMENTANEAMENTE NON DISPONIBILE", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.no,null)
                .show());

        btnConfirm.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_confirm)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.si, (dialog, id) -> {
                    lezione.setStato_lezione("frequentata");
                    if(db.updateStatoLezione(lezione)){
                        isLessonUpdated = true;
                        switchView();
                    }else{
                        Toast.makeText(LessonInfo.this, "SERVIZIO MOMENTANEAMENTE NON DISPONIBILE", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.no,null)
                .show());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if(isLessonUpdated){
            Log.d("FLAG", "TRUE");
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}