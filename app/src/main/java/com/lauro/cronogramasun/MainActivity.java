package com.lauro.cronogramasun; // Verifique o seu pacote

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView chipBiologia, chipAnatomia;
    private LinearLayout containerAtrasadas;
    private LottieAnimationView lottiePuppySleeping; // NOVA ANIMAÇÃO MAPEADA
    private TextView txtChipBiologia, txtChipAnatomia;
    private ViewPager2 viewPagerDesempenho;
    private DesempenhoAdapter adapter;
    private List<DesempenhoData> listaCards;

    private TextView txtRevisaoMateria, txtRevisaoAssunto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chipBiologia = findViewById(R.id.chipBiologia);
        chipAnatomia = findViewById(R.id.chipAnatomia);
        txtChipBiologia = findViewById(R.id.txtChipBiologia);
        txtChipAnatomia = findViewById(R.id.txtChipAnatomia);

        containerAtrasadas = findViewById(R.id.containerAtrasadas);
        lottiePuppySleeping = findViewById(R.id.lottiePuppySleeping); // MAPEANDO A ANIMAÇÃO

        viewPagerDesempenho = findViewById(R.id.viewPagerDesempenho);
        txtRevisaoMateria = findViewById(R.id.txtRevisaoMateria);
        txtRevisaoAssunto = findViewById(R.id.txtRevisaoAssunto);

        listaCards = new ArrayList<>();
        adapter = new DesempenhoAdapter(listaCards);
        viewPagerDesempenho.setAdapter(adapter);
        viewPagerDesempenho.setOffscreenPageLimit(1);

        atualizarParaBiologia();

        chipBiologia.setOnClickListener(v -> atualizarParaBiologia());
        chipAnatomia.setOnClickListener(v -> atualizarParaAnatomia());

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
            bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet);
            bottomSheetDialog.show();
        });
    }

    private void atualizarParaBiologia() {
        chipBiologia.setCardBackgroundColor(Color.parseColor("#584039"));
        txtChipBiologia.setTextColor(Color.WHITE);
        chipBiologia.setStrokeWidth(0);

        chipAnatomia.setCardBackgroundColor(Color.WHITE);
        txtChipAnatomia.setTextColor(Color.parseColor("#8D7B73"));
        chipAnatomia.setStrokeWidth(2);

        txtRevisaoMateria.setText("Mitocôndria");
        txtRevisaoAssunto.setText("Revisão 2");

        // Esconde o container (Título + Animação de Preguiça + Card)
        containerAtrasadas.setVisibility(View.GONE);
        // Sênior: Para a animação para economizar recursos
        if (lottiePuppySleeping != null) {
            lottiePuppySleeping.cancelAnimation();
        }

        viewPagerDesempenho.setCurrentItem(0, false);

        listaCards.clear();
        listaCards.add(new DesempenhoData("Média da Matéria", "Biologia", 8.2f, true, "1 Resumo pendente", false, "Próxima aula: Sexta", false));
        listaCards.add(new DesempenhoData("Desempenho: Assunto", "Mitocôndria", 6.9f, false, "", false, "", false));
        adapter.notifyDataSetChanged();
    }

    private void atualizarParaAnatomia() {
        chipBiologia.setCardBackgroundColor(Color.WHITE);
        txtChipBiologia.setTextColor(Color.parseColor("#8D7B73"));
        chipBiologia.setStrokeWidth(2);

        chipAnatomia.setCardBackgroundColor(Color.parseColor("#584039"));
        txtChipAnatomia.setTextColor(Color.WHITE);
        chipAnatomia.setStrokeWidth(0);

        txtRevisaoMateria.setText("Ossos do Crânio");
        txtRevisaoAssunto.setText("Revisão 1");

        // Mostra o container (Título + Animação de Preguiça + Card)
        containerAtrasadas.setVisibility(View.VISIBLE);
        // Sênior: Garante que a animação comece a rodar
        if (lottiePuppySleeping != null) {
            lottiePuppySleeping.playAnimation();
        }

        viewPagerDesempenho.setCurrentItem(0, false);

        listaCards.clear();
        listaCards.add(new DesempenhoData("Média da Matéria", "Anatomia", 4.5f, true, "Resumo da aula 04 sobre o sistema esquelético pendente (Atrasado!)", true, "Prova amanhã!", true));
        listaCards.add(new DesempenhoData("Desempenho: Assunto", "Ossos do Crânio", 9.8f, false, "", false, "", false));
        adapter.notifyDataSetChanged();
    }

    // ARQUITETURA SÊNIOR: CLASSES INTERNAS MANTIDAS
    static class DesempenhoData {
        String titulo, subtitulo;
        float nota;
        boolean isMateria;
        String anotacao, evento;
        boolean isAnotacaoUrgente, isEventoUrgente;

        public DesempenhoData(String titulo, String subtitulo, float nota, boolean isMateria, String anotacao, boolean isAnotacaoUrgente, String evento, boolean isEventoUrgente) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.nota = nota;
            this.isMateria = isMateria;
            this.anotacao = anotacao;
            this.isAnotacaoUrgente = isAnotacaoUrgente;
            this.evento = evento;
            this.isEventoUrgente = isEventoUrgente;
        }
    }

    class DesempenhoAdapter extends RecyclerView.Adapter<DesempenhoAdapter.CardViewHolder> {
        private List<DesempenhoData> dados;

        public DesempenhoAdapter(List<DesempenhoData> dados) {
            this.dados = dados;
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_desempenho, parent, false);
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
            DesempenhoData data = dados.get(position);

            holder.txtTitulo.setText(data.titulo);
            holder.txtSubtitulo.setText(data.subtitulo);

            if (data.isMateria) {
                holder.cardContainer.setCardBackgroundColor(Color.WHITE);
                holder.cardContainer.setStrokeWidth(0);
                holder.linhaDivisoria.setVisibility(View.VISIBLE);
                holder.layoutAnotacoes.setVisibility(View.VISIBLE);
                holder.layoutEventos.setVisibility(View.VISIBLE);

                holder.txtAnotacoes.setText(data.anotacao);
                holder.txtEventos.setText(data.evento);

                configurarFundoUrgencia(data.isAnotacaoUrgente, holder.layoutAnotacoes, holder.iconAnotacoes, holder.txtAnotacoes);
                configurarFundoUrgencia(data.isEventoUrgente, holder.layoutEventos, holder.iconEventos, holder.txtEventos);

            } else {
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FDFBF9"));
                holder.cardContainer.setStrokeColor(Color.parseColor("#EFEFEF"));
                holder.cardContainer.setStrokeWidth(2);

                holder.linhaDivisoria.setVisibility(View.GONE);
                holder.layoutAnotacoes.setVisibility(View.GONE);
                holder.layoutEventos.setVisibility(View.GONE);
            }

            animarGrafico(holder.progressCircular, holder.txtNota, data.nota);
        }

        private void configurarFundoUrgencia(boolean isUrgente, LinearLayout layout, ImageView icone, TextView texto) {
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(50f);

            if (isUrgente) {
                shape.setColor(Color.parseColor("#FFF3F3"));
                layout.setBackground(shape);
                icone.setColorFilter(Color.parseColor("#D9534F"));
                texto.setTextColor(Color.parseColor("#D9534F"));
                texto.setTypeface(null, Typeface.BOLD);
            } else {
                shape.setColor(Color.TRANSPARENT);
                layout.setBackground(shape);
                icone.setColorFilter(Color.parseColor("#8D7B73"));
                texto.setTextColor(Color.parseColor("#8D7B73"));
                texto.setTypeface(null, Typeface.NORMAL);
            }
        }

        private void animarGrafico(CircularProgressIndicator progressBar, TextView textView, float nota) {
            int notaInteira = (int) (nota * 10);
            if (nota >= 7.0f) {
                progressBar.setIndicatorColor(Color.parseColor("#A5D6A7"));
            } else if (nota >= 5.0f) {
                progressBar.setIndicatorColor(Color.parseColor("#FFE082"));
            } else {
                progressBar.setIndicatorColor(Color.parseColor("#EF9A9A"));
            }

            textView.setText(String.valueOf(nota));
            progressBar.setProgress(0);
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, notaInteira);
            animation.setDuration(1500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }

        @Override
        public int getItemCount() {
            return dados.size();
        }

        class CardViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardContainer;
            TextView txtTitulo, txtSubtitulo, txtNota, txtAnotacoes, txtEventos;
            CircularProgressIndicator progressCircular;
            View linhaDivisoria;
            LinearLayout layoutAnotacoes, layoutEventos;
            ImageView iconAnotacoes, iconEventos;

            public CardViewHolder(@NonNull View itemView) {
                super(itemView);
                cardContainer = itemView.findViewById(R.id.cardContainer);
                txtTitulo = itemView.findViewById(R.id.txtTituloCard);
                txtSubtitulo = itemView.findViewById(R.id.txtSubtituloCard);
                progressCircular = itemView.findViewById(R.id.progressCircular);
                txtNota = itemView.findViewById(R.id.txtNota);
                linhaDivisoria = itemView.findViewById(R.id.linhaDivisoria);
                layoutAnotacoes = itemView.findViewById(R.id.layoutAnotacoes);
                layoutEventos = itemView.findViewById(R.id.layoutEventos);
                iconAnotacoes = itemView.findViewById(R.id.iconAnotacoes);
                iconEventos = itemView.findViewById(R.id.iconEventos);
                txtAnotacoes = itemView.findViewById(R.id.txtAnotacoesDin);
                txtEventos = itemView.findViewById(R.id.txtEventosDin);
            }
        }
    }
}