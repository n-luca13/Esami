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
import com.luca.applicazioneesame.sqlhelper.Docenti;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class DocentiRecyclerAdapter extends RecyclerView.Adapter<DocentiRecyclerAdapter.DocentiViewHolder> {
    private final ArrayList<Docenti> docenti;
    private int posDocenteSelezionato = -1;
    private int posUltimaSelezione = -1;
    Context context;
    ClickInterface clickInterface;

    public DocentiRecyclerAdapter(ArrayList<Docenti> docenti, Context context, ClickInterface clickInterface) {
        this.docenti = docenti;
        this.context = context;
        this.clickInterface = clickInterface;
    }

    @NonNull
    @Override
    public DocentiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.docente_row, parent, false);
        return new DocentiViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DocentiViewHolder holder, final int position) {
        String nomeDocente = docenti.get(position).getNome_doc();
        holder.getTextView().setText(nomeDocente);

        String uriImgDocente = "drawable/"+nomeDocente.toLowerCase().replace(" ", "_");
        int imageResource = context.getResources().getIdentifier(uriImgDocente, null, context.getPackageName());
        if(imageResource != 0) {
            holder.getCircleImageView().setImageResource(imageResource);
        }

        if(position == posDocenteSelezionato){
            holder.selectedBg();
        }else{
            holder.defaultBg();
        }
    }

    @Override
    public int getItemCount() {
        return docenti.size();
    }

    public class DocentiViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout backgroundDocente;
        public TextView txtNomeDocente;
        public CircleImageView doc_image;

        public DocentiViewHolder(View view) {
            super(view);
            txtNomeDocente = view.findViewById(R.id.txtNomeDocente);
            doc_image = view.findViewById(R.id.doc_image);
            backgroundDocente = view.findViewById(R.id.linearLayoutDocente);

            view.setOnClickListener(view1 -> {
                clickInterface.onItemClick(getAdapterPosition());
                posDocenteSelezionato = getAdapterPosition();
                if(posUltimaSelezione != -1){
                    notifyItemChanged(posUltimaSelezione);
                }
                posUltimaSelezione = posDocenteSelezionato;
                notifyItemChanged(posDocenteSelezionato);
            });
        }

        public void defaultBg(){
            txtNomeDocente.setTextColor(ContextCompat.getColor(context, R.color.color_text));
            backgroundDocente.setBackgroundResource(R.color.color_background);
            backgroundDocente.setElevation(0);
        }

        public void selectedBg(){
            txtNomeDocente.setTextColor(Color.BLACK);
            backgroundDocente.setBackgroundResource(R.color.danger);
            backgroundDocente.setElevation(5);
        }

        public TextView getTextView(){
            return txtNomeDocente;
        }

        public CircleImageView getCircleImageView(){
            return doc_image;
        }
    }

    public interface ClickInterface{
        void onItemClick(int positionOfDocente);
    }

    public void setPosDocenteSelezionato(int previousSelection){
        posDocenteSelezionato = previousSelection;
        if(posUltimaSelezione != -1){
            notifyItemChanged(posUltimaSelezione);
        }
        posUltimaSelezione = posDocenteSelezionato;
        notifyItemChanged(posDocenteSelezionato);
    }
}