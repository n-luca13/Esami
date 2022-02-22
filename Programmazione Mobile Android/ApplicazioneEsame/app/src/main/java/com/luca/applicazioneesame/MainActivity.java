package com.luca.applicazioneesame;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.luca.applicazioneesame.adapters.LessonsRecyclerAdapter;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;
import com.luca.applicazioneesame.sqlhelper.Lezioni;
import com.luca.applicazioneesame.sqlhelper.Studenti;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements LessonsRecyclerAdapter.ClickInterface{
    private TextView textViewName;
    private CardView cardViewNoContent;
    private LessonsRecyclerAdapter lessonsRecyclerAdapter;
    private RecyclerView recyclerViewLessons;
    private ImageButton startBooking;
    private ArrayList<Lezioni> lezioni;
    private DatabaseHelper db;
    private Studenti studente;
    int positionClicked;
    int idClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initObjects();

        // NUOVA PRENOTAZIONE
        startBooking.setOnClickListener(view -> {
            Intent goToBookingActivity = new Intent(MainActivity.this, BookingActivity.class);
            goToBookingActivity.putExtra("matricola", studente.getMatricola());
            startActivity(goToBookingActivity);
        });

        // SWIPE PER ELIMINARE UNA PRENOTAZIONE
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewLessons);
    }

    Lezioni deletedLesson = null;
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerViewLessons, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            deletedLesson = lezioni.get(position);
            lezioni.remove(position);
            lessonsRecyclerAdapter.notifyItemRemoved(position);
            Snackbar.make(recyclerViewLessons, "lezione eliminata", Snackbar.LENGTH_LONG)
                    .setAction("Undo", view -> {
                        lezioni.add(position, deletedLesson);
                        lessonsRecyclerAdapter.notifyItemInserted(position);
                    }).addCallback(new Snackbar.Callback(){
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                try{
                                    db.deleteLezione(deletedLesson.getId_lezione());
                                }catch (Exception e){
                                    Toast.makeText(MainActivity.this, "SERVIZIO MOMENTANEAMENTE NON DISPONIBILE", Toast.LENGTH_SHORT).show();
                                    getLessonsFromSQLite();
                                }
                            }
                        }
                    }).show();
        }
    };

    private void initViews() {
        textViewName = findViewById(R.id.textViewName);
        recyclerViewLessons = findViewById(R.id.recyclerViewLessons);
        startBooking = findViewById(R.id.img_add);
        cardViewNoContent = findViewById(R.id.cardViewNoContent);
    }

    private void initObjects() {
        lezioni = new ArrayList<>();
        lessonsRecyclerAdapter = new LessonsRecyclerAdapter(lezioni, this, this);
        db = new DatabaseHelper(MainActivity.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewLessons.setLayoutManager(mLayoutManager);
        recyclerViewLessons.setHasFixedSize(true);
        recyclerViewLessons.setAdapter(lessonsRecyclerAdapter);

        Gson gson = new Gson();
        studente = gson.fromJson(getIntent().getStringExtra("studente"), Studenti.class);
        if(studente == null){
            studente = db.getStudenteFromDB(getIntent().getIntExtra("matricola",1));
        }
        textViewName.setText(studente.getNome_st());

        getLessonsFromSQLite();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getLessonsFromSQLite() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            lezioni.clear();
            lezioni.addAll(db.readLezioni(studente.getMatricola()));
            if(!lezioni.isEmpty()){
                cardViewNoContent.setVisibility(View.GONE);
                handler.post(() -> lessonsRecyclerAdapter.notifyDataSetChanged());
            }else{
                cardViewNoContent.setVisibility(View.VISIBLE);
            }

        });
    }

    public void onItemClick(int positionOfLesson){
        idClicked = lezioni.get(positionOfLesson).getId_lezione();
        positionClicked = positionOfLesson;
        Intent intent = new Intent(this, LessonInfo.class);
        intent.putExtra("id_lezione", lezioni.get(positionClicked).getId_lezione());
        LessonResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> LessonResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    lezioni.set(positionClicked, db.getLezioneFromDB(idClicked));
                    lessonsRecyclerAdapter.notifyItemChanged(positionClicked);
                }
            }
        });

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}