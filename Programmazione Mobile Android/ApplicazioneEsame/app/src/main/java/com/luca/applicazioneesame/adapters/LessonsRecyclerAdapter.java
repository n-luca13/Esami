package com.luca.applicazioneesame.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.luca.applicazioneesame.R;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;
import com.luca.applicazioneesame.sqlhelper.Lezioni;
import com.luca.applicazioneesame.sqlhelper.Materie;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LessonsRecyclerAdapter extends RecyclerView.Adapter<LessonsRecyclerAdapter.LessonsViewHolder> {

    ArrayList<Lezioni> lezioni;
    Context context;
    ClickInterface clickInterface;

    public LessonsRecyclerAdapter(ArrayList<Lezioni> lezioni, Context context, ClickInterface clickInterface) {
        this.lezioni = lezioni;
        this.context = context;
        this.clickInterface = clickInterface;
    }

    @NonNull
    @Override
    public LessonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_lessons_recycler, parent, false);
        return new LessonsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LessonsViewHolder holder, int position) {
        DatabaseHelper db = new DatabaseHelper(context);
        Materie materia = db.getMateriaFromDB(db.getDocenteFromDB(lezioni.get(position).getId_docente()).getId_materia());
        holder.textViewInsegnamento.setText(materia.getNome_materia());                                     // INSEGNAMENTO

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        holder.textViewData.setText(lezioni.get(position).getParsedData().toLocalDate().format(formatter)); // GIORNO

        holder.textViewStato.setText(lezioni.get(position).getStato_lezione());                             // STATO LEZIONE
        if(lezioni.get(position).getStato_lezione().equals("disdetta")){
            holder.textViewStato.setTextColor(ContextCompat.getColor(context, R.color.yellow));
        }else if(lezioni.get(position).getStato_lezione().equals("frequentata")){
            holder.textViewStato.setTextColor(ContextCompat.getColor(context, R.color.green));
        }else{
            holder.textViewStato.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

    }

    @Override
    public int getItemCount() {
        return lezioni.size();
    }

    public class LessonsViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView textViewInsegnamento;
        AppCompatTextView textViewData;
        AppCompatTextView textViewStato;

        public LessonsViewHolder(View view) {
            super(view);
            textViewInsegnamento = view.findViewById(R.id.textViewInsegnamento);
            textViewData = view.findViewById(R.id.textViewData);
            textViewStato = view.findViewById(R.id.textViewStato);

            view.setOnClickListener(view1 -> clickInterface.onItemClick(getAdapterPosition()));
        }
    }

    public interface ClickInterface{
        void onItemClick(int positionOfLesson);
    }
}