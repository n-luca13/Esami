package com.luca.applicazioneesame.BookingFragments.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.luca.applicazioneesame.R;
import com.luca.applicazioneesame.sqlhelper.Materie;

import java.util.ArrayList;


public class MaterieRecyclerAdapter extends RecyclerView.Adapter<MaterieRecyclerAdapter.MaterieViewHolder> {
    private final ArrayList<Materie> materie;
    private int posMateriaSelezionata = -1;
    private int posUltimaSelezione = -1;
    Context context;
    ClickInterface clickInterface;

    public MaterieRecyclerAdapter(ArrayList<Materie> materie, Context context, ClickInterface clickInterface) {
        this.materie = materie;
        this.context = context;
        this.clickInterface = clickInterface;
    }

    @NonNull
    @Override
    public MaterieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.materia_row, parent, false);
        return new MaterieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MaterieViewHolder holder, final int position) {
        holder.getTextView().setText(materie.get(position).getNome_materia());
        if(position == posMateriaSelezionata){
            holder.selectedBg();
        } else {
            holder.defaultBg();
        }

    }

    @Override
    public int getItemCount() {
        return materie.size();
    }

    public class MaterieViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout backgroundInsegnamento;
        public TextView textViewInsegnamento;

        public MaterieViewHolder(View view) {
            super(view);
            textViewInsegnamento = view.findViewById(R.id.textViewInsegnamento);
            backgroundInsegnamento = view.findViewById(R.id.linearLayoutInsegnamento);

            view.setOnClickListener(view1 -> {
                clickInterface.onItemClick(getAdapterPosition());
                posMateriaSelezionata = getAdapterPosition();
                if(posUltimaSelezione != -1){
                    notifyItemChanged(posUltimaSelezione);
                }
                posUltimaSelezione = posMateriaSelezionata;
                notifyItemChanged(posMateriaSelezionata);
            });
        }

        public void defaultBg(){
            textViewInsegnamento.setTextColor(ContextCompat.getColor(context, R.color.color_text));
            backgroundInsegnamento.setBackgroundResource(R.color.color_background);
            backgroundInsegnamento.setElevation(0);
        }

        public void selectedBg(){
            textViewInsegnamento.setTextColor(Color.BLACK);
            backgroundInsegnamento.setBackgroundResource(R.color.danger);
            backgroundInsegnamento.setElevation(5);
        }

        public TextView getTextView(){
            return textViewInsegnamento;
        }

    }

    public interface ClickInterface{
        void onItemClick(int positionOfMateria);
    }

    public void setPosMateriaSelezionata(int previousSelection){
        posMateriaSelezionata = previousSelection;
        if(posUltimaSelezione != -1){
            notifyItemChanged(posUltimaSelezione);
        }
        posUltimaSelezione = posMateriaSelezionata;
        notifyItemChanged(posMateriaSelezionata);
    }
}