package com.lauro.cronogramasun; // MANTENHA O SEU PACOTE AQUI

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout containerChipsMaterias;
    private LinearLayout containerAtrasadas;
    private LottieAnimationView lottiePuppySleeping;
    private LinearLayout containerListaAtrasadas;
    private LinearLayout containerListaProximas;
    private ViewPager2 viewPagerDesempenho;
    private DesempenhoAdapter adapter;
    private List<DesempenhoData> listaCards;
    private DatabaseHelper dbHelper;
    private LinearLayout containerProximas;
    private LinearLayout containerModoZen;
    private TextView txtZenTitulo;
    private TextView txtZenSubtitulo;
    private LottieAnimationView lottieMeditatingDog;
    private String materiaSelecionadaAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);

        containerChipsMaterias = findViewById(R.id.containerChipsMaterias);
        containerAtrasadas = findViewById(R.id.containerAtrasadas);
        containerProximas = findViewById(R.id.containerProximas);
        containerModoZen = findViewById(R.id.containerModoZen);
        txtZenTitulo = findViewById(R.id.txtZenTitulo);
        txtZenSubtitulo = findViewById(R.id.txtZenSubtitulo);
        lottieMeditatingDog = findViewById(R.id.lottieMeditatingDog);
        lottiePuppySleeping = findViewById(R.id.lottiePuppySleeping);
        viewPagerDesempenho = findViewById(R.id.viewPagerDesempenho);
        containerListaAtrasadas = findViewById(R.id.containerListaAtrasadas);
        containerListaProximas = findViewById(R.id.containerListaProximas);

        listaCards = new ArrayList<>();
        adapter = new DesempenhoAdapter(listaCards);
        viewPagerDesempenho.setAdapter(adapter);
        viewPagerDesempenho.setOffscreenPageLimit(1);

        // O filtro foi desativado conforme você pediu! O Carrossel agora só serve para ver as médias.
        viewPagerDesempenho.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Vazio propositalmente. O deslize não apaga as listas mais!
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> abrirMenuOpcoes());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Chama o nosso novo Maestro centralizador!
        atualizarTelaCompleta();
    }

    // ==========================================
    // O MAESTRO DA TELA (Evita o bug da tela branca)
    // ==========================================
    private void atualizarTelaCompleta() {
        List<String> materiasSalvas = dbHelper.obterNomesMaterias();

        if (materiasSalvas.isEmpty()) {
            materiaSelecionadaAtual = "";
            carregarMenuMaterias();
            carregarCardsDesempenho();
            carregarCronograma();
            return;
        }

        // Se estiver vazio ou a matéria foi apagada, puxa a primeira
        if (materiaSelecionadaAtual == null || materiaSelecionadaAtual.isEmpty() || !materiasSalvas.contains(materiaSelecionadaAtual)) {
            materiaSelecionadaAtual = materiasSalvas.get(0);
        }

        // Executa exatamente na ordem, sem um chamar o outro!
        carregarMenuMaterias();
        carregarCardsDesempenho();
        carregarCronograma();
    }

    // ==========================================
    // PENEIRA DE DADOS (Sanitização)
    // ==========================================
    private String padronizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) return "";
        // Remove espaços extras e deixa tudo minúsculo
        String limpo = texto.trim().replaceAll("\\s+", " ").toLowerCase();

        // Capitaliza a primeira letra de cada palavra
        StringBuilder resultado = new StringBuilder();
        boolean capitalizarProxima = true;
        for (char c : limpo.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                capitalizarProxima = true;
            } else if (capitalizarProxima) {
                c = Character.toUpperCase(c);
                capitalizarProxima = false;
            }
            resultado.append(c);
        }
        return resultado.toString();
    }

    private void aplicarMascaraNota(com.google.android.material.textfield.TextInputEditText editText) {
        if (editText == null) return;
        editText.addTextChangedListener(new android.text.TextWatcher() {
            boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                String str = s.toString().replaceAll("[^\\d]", "");
                try {
                    if (str.isEmpty()) {
                        editText.setText("");
                    } else {
                        double parsed = Double.parseDouble(str) / 10.0;
                        if (parsed > 10.0) parsed = 10.0;
                        String formatted = String.format(java.util.Locale.US, "%.1f", parsed);
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isUpdating = false;
            }
        });
    }

    private void mostrarNotificacao(String mensagem, boolean isErro) {
        View viewRaiz = findViewById(android.R.id.content);
        if (viewRaiz != null) {
            mostrarNotificacao(viewRaiz, mensagem, isErro);
        }
    }

    private void mostrarNotificacao(View viewAlvo, String mensagem, boolean isErro) {
        if (viewAlvo == null) return;

        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(viewAlvo, mensagem, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        ViewGroup.LayoutParams genericParams = snackbarView.getLayoutParams();
        if (genericParams instanceof android.widget.FrameLayout.LayoutParams) {
            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) genericParams;
            params.gravity = android.view.Gravity.TOP;
            params.setMargins(40, 120, 40, 0);
            snackbarView.setLayoutParams(params);
        } else if (genericParams instanceof androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {
            androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams params = (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) genericParams;
            params.gravity = android.view.Gravity.TOP;
            params.setMargins(40, 120, 40, 0);
            snackbarView.setLayoutParams(params);
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(40f);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextSize(15f);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        if (isErro) {
            shape.setColor(Color.parseColor("#EADCDA"));
            shape.setStroke(3, Color.parseColor("#C62828"));
            textView.setTextColor(Color.parseColor("#C62828"));
        } else {
            shape.setColor(Color.parseColor("#E6DCD5"));
            shape.setStroke(3, Color.parseColor("#584039"));
            textView.setTextColor(Color.parseColor("#584039"));
        }

        snackbarView.setBackground(shape);
        snackbarView.setElevation(10f);
        snackbar.show();
    }

    private void abrirMenuOpcoes() {
        BottomSheetDialog menuDialog = new BottomSheetDialog(MainActivity.this);
        menuDialog.setContentView(R.layout.layout_bottom_sheet_opcoes);

        View btnNovaNota = menuDialog.findViewById(R.id.btnNovaNota);
        View btnNovaMateria = menuDialog.findViewById(R.id.btnNovaMateria);
        View btnEditar = menuDialog.findViewById(R.id.btnEditar);
        View btnExcluir = menuDialog.findViewById(R.id.btnExcluir);

        boolean temMateria = !dbHelper.obterNomesMaterias().isEmpty();

        if (btnNovaNota != null) {
            if (temMateria) {
                btnNovaNota.setAlpha(1f);
                btnNovaNota.setOnClickListener(v -> {
                    menuDialog.dismiss();
                    abrirFormularioNota();
                });
            } else {
                btnNovaNota.setAlpha(0.4f);
                btnNovaNota.setOnClickListener(v -> {
                    mostrarNotificacao("Crie uma matéria primeiro para adicionar notas!", true);
                });
            }
        }

        if (btnNovaMateria != null) {
            btnNovaMateria.setOnClickListener(v -> {
                menuDialog.dismiss();
                abrirFormularioMateriaAssunto();
            });
        }

        // IMPLEMENTAÇÃO DO BOTÃO EDITAR
        if (btnEditar != null) {
            if (temMateria) {
                btnEditar.setAlpha(1f);
                btnEditar.setOnClickListener(v -> {
                    menuDialog.dismiss();
                    // abrirModalEdicao();
                    mostrarNotificacao("Modal de Edição em breve!", false);
                });
            } else {
                btnEditar.setAlpha(0.4f);
                btnEditar.setOnClickListener(v -> mostrarNotificacao("Não há dados para editar!", true));
            }
        }

        // IMPLEMENTAÇÃO DO BOTÃO EXCLUIR
        if (btnExcluir != null) {
            if (temMateria) {
                btnExcluir.setAlpha(1f);
                btnExcluir.setOnClickListener(v -> {
                    menuDialog.dismiss();
                    // abrirModalExclusao();
                    mostrarNotificacao("Modal de Exclusão em breve!", false);
                });
            } else {
                btnExcluir.setAlpha(0.4f);
                btnExcluir.setOnClickListener(v -> mostrarNotificacao("Não há dados para excluir!", true));
            }
        }

        menuDialog.show();
    }

    private void abrirFormularioMateriaAssunto() {
        BottomSheetDialog formDialog = new BottomSheetDialog(MainActivity.this);
        formDialog.setContentView(R.layout.layout_bottom_sheet_materia);

        MaterialButtonToggleGroup toggleGroup = formDialog.findViewById(R.id.toggleGroupMateriaAssunto);
        LinearLayout layoutFormMateria = formDialog.findViewById(R.id.layoutFormMateria);
        LinearLayout layoutFormAssunto = formDialog.findViewById(R.id.layoutFormAssunto);
        ImageView btnFechar = formDialog.findViewById(R.id.btnFecharMateria);

        TextView lblTituloForm = formDialog.findViewById(R.id.lblTituloForm);
        com.google.android.material.button.MaterialButton btnConfirmar = formDialog.findViewById(R.id.btnConfirmarCadastro);
        com.google.android.material.button.MaterialButton btnToggleMateria = formDialog.findViewById(R.id.btnToggleMateria);
        com.google.android.material.button.MaterialButton btnToggleAssunto = formDialog.findViewById(R.id.btnToggleAssunto);

        com.google.android.material.textfield.TextInputEditText inputNomeMateria = formDialog.findViewById(R.id.inputNomeMateria);
        com.google.android.material.textfield.TextInputEditText inputMetaMateria = formDialog.findViewById(R.id.inputMetaMateria);
        com.google.android.material.textfield.TextInputEditText inputNomeAssunto = formDialog.findViewById(R.id.inputNomeAssunto);
        android.widget.AutoCompleteTextView autoCompleteVinculoMateria = formDialog.findViewById(R.id.autoCompleteVinculoMateria);
        android.widget.AutoCompleteTextView autoQtdRevisoes = formDialog.findViewById(R.id.autoCompleteQtdRevisoes);

        LinearLayout containerDatasRevisoes = formDialog.findViewById(R.id.containerDatasRevisoes);
        List<com.google.android.material.textfield.TextInputEditText> listaInputsData = new ArrayList<>();

        aplicarMascaraNota(inputMetaMateria);

        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> {
                formDialog.dismiss();
                abrirMenuOpcoes();
            });
        }

        List<String> materiasSalvas = dbHelper.obterNomesMaterias();
        android.widget.ArrayAdapter<String> adapterMateria = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, materiasSalvas);
        if (autoCompleteVinculoMateria != null) autoCompleteVinculoMateria.setAdapter(adapterMateria);

        String[] qtds = new String[]{"1", "2", "3", "4", "5"};
        android.widget.ArrayAdapter<String> adapterQtd = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, qtds);

        if (autoQtdRevisoes != null) {
            autoQtdRevisoes.setAdapter(adapterQtd);
            autoQtdRevisoes.setOnItemClickListener((parent, view, position, id) -> {
                int quantidade = Integer.parseInt(qtds[position]);
                containerDatasRevisoes.removeAllViews();
                listaInputsData.clear();

                for (int i = 1; i <= quantidade; i++) {
                    View viewItem = getLayoutInflater().inflate(R.layout.item_input_data, containerDatasRevisoes, false);
                    TextInputLayout layoutInput = (TextInputLayout) viewItem;
                    com.google.android.material.textfield.TextInputEditText inputData = viewItem.findViewById(R.id.inputDataDinamica);

                    layoutInput.setHint("Data da Revisão " + i);
                    final int indexAnterior = i - 2;

                    inputData.setOnClickListener(v -> {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(MainActivity.this,
                                (view1, year, month, dayOfMonth) -> {
                                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                                    inputData.setText(date);
                                },
                                cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)
                        );

                        if (indexAnterior >= 0) {
                            String dataAnteriorStr = listaInputsData.get(indexAnterior).getText().toString();
                            if (dataAnteriorStr.isEmpty()) {
                                mostrarNotificacao(inputData, "Preencha a data anterior primeiro!", true);
                                return;
                            }
                            try {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));
                                java.util.Date dataAnterior = sdf.parse(dataAnteriorStr);
                                datePickerDialog.getDatePicker().setMinDate(dataAnterior.getTime());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                        }
                        datePickerDialog.show();
                    });

                    containerDatasRevisoes.addView(layoutInput);
                    listaInputsData.add(inputData);
                }
            });
        }

        if (lblTituloForm != null) lblTituloForm.setText("Nova Matéria");
        if (btnConfirmar != null) btnConfirmar.setText("Salvar Matéria");
        if (btnToggleMateria != null && btnToggleAssunto != null) {
            btnToggleMateria.setBackgroundColor(Color.parseColor("#584039"));
            btnToggleMateria.setTextColor(Color.WHITE);
            btnToggleAssunto.setBackgroundColor(Color.TRANSPARENT);
            btnToggleAssunto.setTextColor(Color.parseColor("#8D7B73"));
        }

        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (inputNomeMateria != null) inputNomeMateria.setText("");
                    if (inputMetaMateria != null) inputMetaMateria.setText("");
                    if (inputNomeAssunto != null) inputNomeAssunto.setText("");
                    if (autoCompleteVinculoMateria != null) autoCompleteVinculoMateria.setText("", false);
                    if (autoQtdRevisoes != null) autoQtdRevisoes.setText("", false);
                    containerDatasRevisoes.removeAllViews();
                    listaInputsData.clear();

                    if (checkedId == R.id.btnToggleMateria) {
                        if (lblTituloForm != null) lblTituloForm.setText("Nova Matéria");
                        if (btnConfirmar != null) btnConfirmar.setText("Salvar Matéria");
                        layoutFormMateria.setVisibility(View.VISIBLE);
                        layoutFormAssunto.setVisibility(View.GONE);
                        btnToggleMateria.setBackgroundColor(Color.parseColor("#584039"));
                        btnToggleMateria.setTextColor(Color.WHITE);
                        btnToggleAssunto.setBackgroundColor(Color.TRANSPARENT);
                        btnToggleAssunto.setTextColor(Color.parseColor("#8D7B73"));

                    } else if (checkedId == R.id.btnToggleAssunto) {
                        if (lblTituloForm != null) lblTituloForm.setText("Novo Assunto");
                        if (btnConfirmar != null) btnConfirmar.setText("Salvar Assunto");
                        layoutFormMateria.setVisibility(View.GONE);
                        layoutFormAssunto.setVisibility(View.VISIBLE);
                        btnToggleAssunto.setBackgroundColor(Color.parseColor("#584039"));
                        btnToggleAssunto.setTextColor(Color.WHITE);
                        btnToggleMateria.setBackgroundColor(Color.TRANSPARENT);
                        btnToggleMateria.setTextColor(Color.parseColor("#8D7B73"));
                    }
                }
            });
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                Runnable acaoConfirmar = null;

                if (layoutFormMateria.getVisibility() == View.VISIBLE) {
                    // PENEIRA APLICADA NA MATÉRIA!
                    String nomeMateria = padronizarTexto(inputNomeMateria.getText().toString());
                    String metaString = inputMetaMateria.getText().toString().trim();

                    if (nomeMateria.isEmpty()) {
                        mostrarNotificacao(btnConfirmar, "Preencha o nome da matéria!", true);
                        return;
                    }

                    acaoConfirmar = () -> {
                        double meta = metaString.isEmpty() ? 0.0 : Double.parseDouble(metaString);
                        long id = dbHelper.inserirMateria(nomeMateria, meta);

                        View viewSegura = findViewById(R.id.fabAdd);
                        if (viewSegura == null) viewSegura = findViewById(android.R.id.content);

                        if (id == -2) {
                            mostrarNotificacao(viewSegura, "Erro: A matéria '" + nomeMateria + "' já existe!", true);
                        } else if (id != -1) {
                            materiaSelecionadaAtual = nomeMateria;
                            mostrarNotificacao(viewSegura, "Matéria salva com sucesso!", false);
                        }
                    };

                    mostrarDialogConfirmacaoMateria(formDialog, nomeMateria, metaString, acaoConfirmar);

                } else {
                    String materiaVinculada = autoCompleteVinculoMateria.getText().toString().trim();
                    // PENEIRA APLICADA NO ASSUNTO!
                    String nomeAssunto = padronizarTexto(inputNomeAssunto.getText().toString());
                    String qtdRevisoesStr = autoQtdRevisoes.getText().toString().trim();

                    if (materiaVinculada.isEmpty() || nomeAssunto.isEmpty() || qtdRevisoesStr.isEmpty()) {
                        mostrarNotificacao(btnConfirmar, "Preencha todos os campos do assunto!", true);
                        return;
                    }

                    List<String> datasPreenchidas = new ArrayList<>();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));
                    java.util.Date ultimaData = null;

                    for (int i = 0; i < listaInputsData.size(); i++) {
                        com.google.android.material.textfield.TextInputEditText input = listaInputsData.get(i);
                        String dataText = input.getText().toString().trim();

                        if (dataText.isEmpty() || !dataText.contains("/")) {
                            mostrarNotificacao(btnConfirmar, "Preencha TODAS as datas de revisão corretamente!", true);
                            return;
                        }

                        try {
                            java.util.Date dataAtual = sdf.parse(dataText);
                            if (ultimaData != null && !dataAtual.after(ultimaData)) {
                                mostrarNotificacao(btnConfirmar, "A Revisão " + (i + 1) + " deve ser DEPOIS da revisão anterior!", true);
                                return;
                            }
                            ultimaData = dataAtual;
                        } catch (Exception e) {
                            mostrarNotificacao(btnConfirmar, "Formato de data inválido!", true);
                            return;
                        }
                        datasPreenchidas.add(dataText);
                    }

                    acaoConfirmar = () -> {
                        int qtd = Integer.parseInt(qtdRevisoesStr);
                        long id = dbHelper.inserirAssuntoComRevisoes(materiaVinculada, nomeAssunto, qtd, datasPreenchidas);

                        View viewSegura = findViewById(R.id.fabAdd);
                        if (viewSegura == null) viewSegura = findViewById(android.R.id.content);

                        if (id == -2) {
                            mostrarNotificacao(viewSegura, "Este assunto já existe nessa matéria!", true);
                        } else if (id != -1) {
                            mostrarNotificacao(viewSegura, "Assunto e Revisões salvos!", false);
                        }
                    };

                    String dataInicial = datasPreenchidas.get(0);
                    String dataFinal = datasPreenchidas.get(datasPreenchidas.size() - 1);
                    int qtd = Integer.parseInt(qtdRevisoesStr);

                    mostrarDialogConfirmacaoAssunto(formDialog, materiaVinculada, nomeAssunto, qtd, dataInicial, dataFinal, acaoConfirmar);
                }
            });
        }
        formDialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        formDialog.show();
    }

    private void mostrarDialogConfirmacaoMateria(BottomSheetDialog formPai, String nomeMateria, String metaTxt, Runnable acaoSalvar) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

        TextView txtType = dialogView.findViewById(R.id.txtResumoLinha1Type);
        TextView txtMeta = dialogView.findViewById(R.id.txtResumoLinha1Meta);
        TextView txtName = dialogView.findViewById(R.id.txtResumoLinha2Name);
        TextView txtEx1 = dialogView.findViewById(R.id.txtResumoLinha3Extra1);
        TextView txtEx2 = dialogView.findViewById(R.id.txtResumoLinha4Extra2);

        txtType.setText("Nova Matéria");
        txtMeta.setVisibility(View.VISIBLE);
        txtMeta.setText("Meta: " + (metaTxt.isEmpty() ? "0.0" : metaTxt));
        txtName.setText(nomeMateria);

        if(txtEx1 != null) txtEx1.setVisibility(View.GONE);
        if(txtEx2 != null) txtEx2.setVisibility(View.GONE);

        dialogView.findViewById(R.id.btnCancelarDialog).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirmarDialog).setOnClickListener(v -> {
            if (acaoSalvar != null) {
                acaoSalvar.run();
                atualizarTelaCompleta(); // Chama o Maestro
            }
            dialog.dismiss();
            formPai.dismiss();
        });
        dialog.show();
    }

    private void mostrarDialogConfirmacaoAssunto(BottomSheetDialog formPai, String nomeMateria, String nomeAssunto, int qtdRevisoes, String dataInicial, String dataFinal, Runnable acaoSalvar) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

        TextView txtType = dialogView.findViewById(R.id.txtResumoLinha1Type);
        TextView txtMeta = dialogView.findViewById(R.id.txtResumoLinha1Meta);
        TextView txtName = dialogView.findViewById(R.id.txtResumoLinha2Name);
        TextView txtEx1 = dialogView.findViewById(R.id.txtResumoLinha3Extra1);
        TextView txtEx2 = dialogView.findViewById(R.id.txtResumoLinha4Extra2);

        txtType.setText("Matéria: " + nomeMateria);
        if (txtMeta != null) txtMeta.setVisibility(View.GONE);
        txtName.setText("Assunto: " + nomeAssunto);

        if (txtEx1 != null) {
            txtEx1.setVisibility(View.VISIBLE);
            if (qtdRevisoes > 1) {
                txtEx1.setText(qtdRevisoes + " Revisões (" + dataInicial + " - " + dataFinal + ")");
            } else {
                txtEx1.setText("1 Revisão (" + dataInicial + ")");
            }
        }

        if(txtEx2 != null) txtEx2.setVisibility(View.GONE);

        dialogView.findViewById(R.id.btnCancelarDialog).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirmarDialog).setOnClickListener(v -> {
            if (acaoSalvar != null) {
                acaoSalvar.run();
                atualizarTelaCompleta(); // Chama o Maestro
            }
            dialog.dismiss();
            formPai.dismiss();
        });
        dialog.show();
    }

    private void mostrarDialogConfirmacaoNota(BottomSheetDialog formPai, String nomeMateria, String nomeAssunto, String tipoNota, String valorNota, String refRevisao, Runnable acaoSalvar) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

        TextView txtType = dialogView.findViewById(R.id.txtResumoLinha1Type);
        TextView txtMeta = dialogView.findViewById(R.id.txtResumoLinha1Meta);
        TextView txtName = dialogView.findViewById(R.id.txtResumoLinha2Name);
        TextView txtEx1 = dialogView.findViewById(R.id.txtResumoLinha3Extra1);
        TextView txtEx2 = dialogView.findViewById(R.id.txtResumoLinha4Extra2);

        txtType.setText("Nota de " + nomeMateria);
        if (txtMeta != null) txtMeta.setVisibility(View.GONE);

        txtName.setText("Assunto: " + nomeAssunto);

        if (txtEx1 != null) {
            txtEx1.setVisibility(View.VISIBLE);
            txtEx1.setText(tipoNota + ": " + valorNota + " (" + refRevisao + ")");
        }
        if (txtEx2 != null) txtEx2.setVisibility(View.GONE);

        dialogView.findViewById(R.id.btnCancelarDialog).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirmarDialog).setOnClickListener(v -> {
            if (acaoSalvar != null) {
                acaoSalvar.run();
                atualizarTelaCompleta(); // Chama o Maestro
            }
            dialog.dismiss();
            formPai.dismiss();
        });
        dialog.show();
    }

    private void abrirFormularioNota() {
        BottomSheetDialog formDialog = new BottomSheetDialog(MainActivity.this);
        formDialog.setContentView(R.layout.layout_bottom_sheet_nota);

        MaterialButtonToggleGroup toggleGroup = formDialog.findViewById(R.id.toggleGroupModoNota);
        TextInputLayout layoutNotaDireta = formDialog.findViewById(R.id.layoutNotaDireta);
        LinearLayout layoutNotaFracao = formDialog.findViewById(R.id.layoutNotaFracao);
        ImageView btnFechar = formDialog.findViewById(R.id.btnFechar);

        com.google.android.material.button.MaterialButton btnModoDireta = formDialog.findViewById(R.id.btnModoDireta);
        com.google.android.material.button.MaterialButton btnModoFracao = formDialog.findViewById(R.id.btnModoFracao);
        com.google.android.material.button.MaterialButton btnConfirmar = formDialog.findViewById(R.id.btnSalvarNota);

        android.widget.AutoCompleteTextView autoCompleteMateria = formDialog.findViewById(R.id.autoCompleteMateria);
        android.widget.AutoCompleteTextView autoCompleteAssunto = formDialog.findViewById(R.id.autoCompleteAssunto);
        com.google.android.material.textfield.TextInputLayout layoutAssuntoContainer = formDialog.findViewById(R.id.layoutAssunto);

        com.google.android.material.textfield.TextInputEditText inputNotaDiretaObj = formDialog.findViewById(R.id.inputNotaDireta);
        com.google.android.material.textfield.TextInputEditText inputAcertos = formDialog.findViewById(R.id.inputAcertos);
        com.google.android.material.textfield.TextInputEditText inputTotalQuestoes = formDialog.findViewById(R.id.inputTotal);

        aplicarMascaraNota(inputNotaDiretaObj);

        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> {
                formDialog.dismiss();
                abrirMenuOpcoes();
            });
        }

        List<String> materiasSalvas = dbHelper.obterNomesMaterias();
        android.widget.ArrayAdapter<String> adapterMateria = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, materiasSalvas);
        if (autoCompleteMateria != null) autoCompleteMateria.setAdapter(adapterMateria);

        if (autoCompleteMateria != null && autoCompleteAssunto != null) {
            autoCompleteMateria.setOnItemClickListener((parent, view, position, id) -> {
                String materiaEscolhida = adapterMateria.getItem(position);
                autoCompleteAssunto.setText("", false);

                List<String> assuntosDaMateria = dbHelper.obterAssuntosComRevisoesPendentes(materiaEscolhida);

                if (assuntosDaMateria.isEmpty()) {
                    View viewSegura = findViewById(android.R.id.content);
                    mostrarNotificacao(btnConfirmar, "Você já concluiu todas as revisões desta matéria!", false);
                    if (layoutAssuntoContainer != null) layoutAssuntoContainer.setEnabled(false);
                    autoCompleteAssunto.setEnabled(false);
                } else {
                    android.widget.ArrayAdapter<String> adapterAssunto = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, assuntosDaMateria);
                    autoCompleteAssunto.setAdapter(adapterAssunto);
                    if (layoutAssuntoContainer != null) layoutAssuntoContainer.setEnabled(true);
                    autoCompleteAssunto.setEnabled(true);
                }
            });
        }

        if (btnModoDireta != null && btnModoFracao != null) {
            btnModoDireta.setBackgroundColor(Color.parseColor("#584039"));
            btnModoDireta.setTextColor(Color.WHITE);
            btnModoFracao.setBackgroundColor(Color.TRANSPARENT);
            btnModoFracao.setTextColor(Color.parseColor("#8D7B73"));
        }

        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (inputNotaDiretaObj != null) inputNotaDiretaObj.setText("");
                    if (inputAcertos != null) inputAcertos.setText("");
                    if (inputTotalQuestoes != null) inputTotalQuestoes.setText("");

                    if (checkedId == R.id.btnModoDireta) {
                        layoutNotaDireta.setVisibility(View.VISIBLE);
                        layoutNotaFracao.setVisibility(View.GONE);
                        btnModoDireta.setBackgroundColor(Color.parseColor("#584039"));
                        btnModoDireta.setTextColor(Color.WHITE);
                        btnModoFracao.setBackgroundColor(Color.TRANSPARENT);
                        btnModoFracao.setTextColor(Color.parseColor("#8D7B73"));

                    } else if (checkedId == R.id.btnModoFracao) {
                        layoutNotaDireta.setVisibility(View.GONE);
                        layoutNotaFracao.setVisibility(View.VISIBLE);
                        btnModoFracao.setBackgroundColor(Color.parseColor("#584039"));
                        btnModoFracao.setTextColor(Color.WHITE);
                        btnModoDireta.setBackgroundColor(Color.TRANSPARENT);
                        btnModoDireta.setTextColor(Color.parseColor("#8D7B73"));
                    }
                }
            });
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                String assuntoEscolhido = autoCompleteAssunto.getText().toString().trim();
                String materiaEscolhida = autoCompleteMateria.getText().toString().trim();

                if (assuntoEscolhido.isEmpty()) {
                    mostrarNotificacao(btnConfirmar, "Escolha um Assunto primeiro!", true);
                    return;
                }

                String refRevisao = dbHelper.obterProximaRevisaoPendente(assuntoEscolhido);
                if (refRevisao == null) {
                    mostrarNotificacao(btnConfirmar, "Todas as revisões deste assunto já foram concluídas!", true);
                    return;
                }
                Runnable acaoConfirmar;
                String tipoNotaStr;
                String valorNotaStr;

                if (layoutNotaDireta.getVisibility() == View.VISIBLE) {
                    String notaStr = inputNotaDiretaObj.getText().toString().trim();
                    if (notaStr.isEmpty()) {
                        mostrarNotificacao(btnConfirmar, "Insira a nota!", true);
                        return;
                    }

                    double nota = Double.parseDouble(notaStr);
                    tipoNotaStr = "Nota Direta";
                    valorNotaStr = notaStr;

                    acaoConfirmar = () -> {
                        long id = dbHelper.inserirDesempenho(assuntoEscolhido, nota, 0, 0);
                        if (id != -1) {
                            View viewSegura = findViewById(android.R.id.content);
                            mostrarNotificacao(viewSegura, "Nota Salva com sucesso!", false);
                        }
                    };

                } else {
                    String acertosStr = inputAcertos.getText().toString().trim();
                    String totalStr = inputTotalQuestoes.getText().toString().trim();

                    if (acertosStr.isEmpty() || totalStr.isEmpty()) {
                        mostrarNotificacao(btnConfirmar, "Insira os acertos e o total!", true);
                        return;
                    }

                    int acertos = Integer.parseInt(acertosStr);
                    int total = Integer.parseInt(totalStr);

                    if (acertos > total) {
                        mostrarNotificacao(btnConfirmar, "Erro: Mais acertos do que questões!", true);
                        return;
                    }

                    tipoNotaStr = "Fração";
                    valorNotaStr = acertosStr + " de " + totalStr;

                    acaoConfirmar = () -> {
                        long id = dbHelper.inserirDesempenho(assuntoEscolhido, -1, acertos, total);
                        if (id != -1) {
                            View viewSegura = findViewById(android.R.id.content);
                            mostrarNotificacao(viewSegura, "Desempenho Salvo com sucesso!", false);
                        }
                    };
                }

                mostrarDialogConfirmacaoNota(formDialog, materiaEscolhida, assuntoEscolhido, tipoNotaStr, valorNotaStr, refRevisao, acaoConfirmar);
            });
        }

        formDialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        formDialog.show();
    }

    // ==========================================
    // APENAS A LÓGICA DE INTERFACE DOS CHIPS
    // ==========================================
    private void carregarMenuMaterias() {
        if (containerChipsMaterias == null) return;
        containerChipsMaterias.removeAllViews();

        List<String> materiasSalvas = dbHelper.obterNomesMaterias();
        if (materiasSalvas.isEmpty()) return;

        for (String nomeMateria : materiasSalvas) {
            View viewChip = getLayoutInflater().inflate(R.layout.item_chip_materia, containerChipsMaterias, false);
            MaterialCardView cardChip = viewChip.findViewById(R.id.cardChipMateria);
            TextView txtNome = viewChip.findViewById(R.id.txtNomeMateriaChip);

            txtNome.setText(nomeMateria);

            if (nomeMateria.equals(materiaSelecionadaAtual)) {
                cardChip.setCardBackgroundColor(Color.parseColor("#584039"));
                cardChip.setStrokeWidth(0);
                txtNome.setTextColor(Color.WHITE);
            } else {
                cardChip.setCardBackgroundColor(Color.WHITE);
                cardChip.setStrokeWidth(1);
                txtNome.setTextColor(Color.parseColor("#8D7B73"));
            }

            cardChip.setOnClickListener(v -> {
                if (!nomeMateria.equals(materiaSelecionadaAtual)) {
                    materiaSelecionadaAtual = nomeMateria;
                    atualizarTelaCompleta(); // Usa o Maestro! Sem tela branca!
                }
            });

            containerChipsMaterias.addView(viewChip);
        }

        ViewParent parent = containerChipsMaterias.getParent();
        if (parent instanceof android.widget.HorizontalScrollView) {
            android.widget.HorizontalScrollView scrollView = (android.widget.HorizontalScrollView) parent;
            scrollView.post(() -> {
                for (int i = 0; i < containerChipsMaterias.getChildCount(); i++) {
                    View child = containerChipsMaterias.getChildAt(i);
                    TextView txtNome = child.findViewById(R.id.txtNomeMateriaChip);
                    if (txtNome != null && txtNome.getText().toString().equals(materiaSelecionadaAtual)) {
                        int scrollX = child.getLeft() - (scrollView.getWidth() / 2) + (child.getWidth() / 2);
                        scrollView.smoothScrollTo(scrollX, 0);
                        break;
                    }
                }
            });
        }
    }

    // ==========================================
    // APENAS A LÓGICA DAS LISTAS EMBAIXO
    // ==========================================
    private void carregarCronograma() {
        if (containerAtrasadas != null) containerAtrasadas.setVisibility(View.GONE);
        if (containerProximas != null) containerProximas.setVisibility(View.GONE);
        if (containerModoZen != null) containerModoZen.setVisibility(View.GONE);
        if (lottiePuppySleeping != null) lottiePuppySleeping.cancelAnimation();
        if (lottieMeditatingDog != null) lottieMeditatingDog.cancelAnimation();

        if (containerListaAtrasadas != null) containerListaAtrasadas.removeAllViews();
        if (containerListaProximas != null) containerListaProximas.removeAllViews();

        if (materiaSelecionadaAtual == null || materiaSelecionadaAtual.isEmpty()) {
            if (containerModoZen != null) {
                containerModoZen.setVisibility(View.VISIBLE);
                if (txtZenTitulo != null) txtZenTitulo.setText("Opa, parece que você ainda não começou!");
                if (txtZenSubtitulo != null) txtZenSubtitulo.setText("Que tal adicionar a primeira matéria clicando no + ali embaixo?");
                if (lottieMeditatingDog != null) {
                    lottieMeditatingDog.setAnimation(R.raw.the_wolf_turns_head);
                    lottieMeditatingDog.playAnimation();
                }
            }
            return;
        }

        // Puxa TODAS as revisões da matéria fixa e não filtra por assunto do carrossel
        List<DatabaseHelper.RevisaoDTO> revisoes = dbHelper.obterRevisoesPendentesPorMateria(materiaSelecionadaAtual);

        List<DatabaseHelper.RevisaoDTO> listaAtrasadas = new ArrayList<>();
        List<DatabaseHelper.RevisaoDTO> listaProximas = new ArrayList<>();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));
        java.util.Calendar calHoje = java.util.Calendar.getInstance();
        calHoje.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calHoje.set(java.util.Calendar.MINUTE, 0);
        calHoje.set(java.util.Calendar.SECOND, 0);
        calHoje.set(java.util.Calendar.MILLISECOND, 0);

        for (DatabaseHelper.RevisaoDTO rev : revisoes) {
            if (rev.dataProgramada == null || !rev.dataProgramada.contains("/")) continue;
            try {
                java.util.Date dataProg = sdf.parse(rev.dataProgramada);
                long diffMillis = dataProg.getTime() - calHoje.getTimeInMillis();
                long diffDias = diffMillis / (1000 * 60 * 60 * 24);

                rev.diasDiferenca = diffDias;

                if (diffDias < 0) {
                    listaAtrasadas.add(rev);
                } else {
                    listaProximas.add(rev);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        listaAtrasadas.sort((r1, r2) -> Long.compare(r1.diasDiferenca, r2.diasDiferenca));
        listaProximas.sort((r1, r2) -> Long.compare(r1.diasDiferenca, r2.diasDiferenca));

        if (!listaAtrasadas.isEmpty()) {
            if (containerAtrasadas != null) {
                containerAtrasadas.setVisibility(View.VISIBLE);
                if (lottiePuppySleeping != null) lottiePuppySleeping.playAnimation();
            }

            int count = 0;
            for (DatabaseHelper.RevisaoDTO rev : listaAtrasadas) {
                if (count >= 3) break;

                View itemCard = getLayoutInflater().inflate(R.layout.item_revisao_card, containerListaAtrasadas, false);
                TextView txtMateria = itemCard.findViewById(R.id.txtMateriaDinamica);
                TextView txtAssunto = itemCard.findViewById(R.id.txtAssuntoDinamica);
                TextView txtDias = itemCard.findViewById(R.id.txtDiasDinamica);
                View linhaLateral = itemCard.findViewById(R.id.linhaCorLateral);

                txtMateria.setText(rev.nomeMateria);
                txtAssunto.setText(rev.nomeAssunto + " - Revisão " + rev.numeroRevisao);

                long diasAtraso = Math.abs(rev.diasDiferenca);
                if (diasAtraso > 10) {
                    txtDias.setText("Atrasou em\n" + rev.dataProgramada);
                } else {
                    txtDias.setText(diasAtraso + " dias\natrasada");
                }

                int corAtrasada = Color.parseColor("#D9534F");
                txtMateria.setTextColor(corAtrasada);
                txtAssunto.setTextColor(corAtrasada);
                txtDias.setTextColor(corAtrasada);
                linhaLateral.setBackgroundColor(corAtrasada);

                if (containerListaAtrasadas != null) containerListaAtrasadas.addView(itemCard);
                count++;
            }
        }

        if (!listaProximas.isEmpty()) {
            if (containerProximas != null) containerProximas.setVisibility(View.VISIBLE);

            int count = 0;
            for (DatabaseHelper.RevisaoDTO rev : listaProximas) {
                if (count >= 3) break;

                View itemCard = getLayoutInflater().inflate(R.layout.item_revisao_card, containerListaProximas, false);
                TextView txtMateria = itemCard.findViewById(R.id.txtMateriaDinamica);
                TextView txtAssunto = itemCard.findViewById(R.id.txtAssuntoDinamica);
                TextView txtDias = itemCard.findViewById(R.id.txtDiasDinamica);
                View linhaLateral = itemCard.findViewById(R.id.linhaCorLateral);

                txtMateria.setText(rev.nomeMateria);
                txtAssunto.setText(rev.nomeAssunto + " - Revisão " + rev.numeroRevisao);

                if (rev.diasDiferenca <= 10) {
                    txtDias.setText(rev.diasDiferenca == 0 ? "É Hoje!" : "Faltam\n" + rev.diasDiferenca + " dias");
                } else {
                    txtDias.setText("Até\n" + rev.dataProgramada);
                }

                int corProxima = Color.parseColor("#584039");
                int corAssuntoProxima = Color.parseColor("#8D7B73");
                txtMateria.setTextColor(corProxima);
                txtAssunto.setTextColor(corAssuntoProxima);
                txtDias.setTextColor(corProxima);
                linhaLateral.setBackgroundColor(Color.parseColor("#D6C4B8"));

                if (containerListaProximas != null) containerListaProximas.addView(itemCard);
                count++;
            }
        }

        if (listaAtrasadas.isEmpty() && listaProximas.isEmpty()) {
            if (containerModoZen != null) {
                containerModoZen.setVisibility(View.VISIBLE);

                boolean temAssuntos = false;
                if (!materiaSelecionadaAtual.isEmpty()) {
                    temAssuntos = dbHelper.verificarSeMateriaTemAssuntos(materiaSelecionadaAtual);
                }

                if (!temAssuntos) {
                    if (txtZenTitulo != null) txtZenTitulo.setText("Poxa Sun, você ainda não se planejou?");
                    if (txtZenSubtitulo != null) txtZenSubtitulo.setText("Vamos lá, me deixa te ajudar! Clique no + e adicione um Assunto.");
                    if (lottieMeditatingDog != null) {
                        lottieMeditatingDog.setAnimation(R.raw.the_wolf_turns_head);
                        lottieMeditatingDog.playAnimation();
                    }
                } else {
                    if (txtZenTitulo != null) txtZenTitulo.setText("Nossa, você já fez todas as revisões?");
                    if (txtZenSubtitulo != null) txtZenSubtitulo.setText("Quando crescer quero ser igual a você!");
                    if (lottieMeditatingDog != null) {
                        lottieMeditatingDog.setAnimation(R.raw.meditating_dog);
                        lottieMeditatingDog.playAnimation();
                    }
                }
            }
        }
    }

    // ==========================================
    // APENAS A LÓGICA DO CARROSSEL
    // ==========================================
    private void carregarCardsDesempenho() {
        listaCards.clear();

        if (materiaSelecionadaAtual == null || materiaSelecionadaAtual.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        float mediaRealMateria = dbHelper.calcularMediaMateria(materiaSelecionadaAtual);
        listaCards.add(new DesempenhoData(
                "Média da Matéria",
                materiaSelecionadaAtual,
                mediaRealMateria,
                true,
                "Anotações: Em breve",
                false,
                "Eventos: Em breve",
                false
        ));

        List<String> assuntosDessaMateria = dbHelper.obterAssuntosPorMateria(materiaSelecionadaAtual);

        for (String assunto : assuntosDessaMateria) {
            float mediaDoAssunto = dbHelper.calcularMediaAssunto(assunto);
            listaCards.add(new DesempenhoData(
                    "Desempenho: Assunto",
                    assunto,
                    mediaDoAssunto,
                    false,
                    "",
                    false,
                    "",
                    false
            ));
        }

        adapter.notifyDataSetChanged();
        viewPagerDesempenho.setCurrentItem(0, false);
    }

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
            progressBar.clearAnimation();
            int notaInteira = (int) (nota * 10);

            if (nota >= 7.0f) {
                progressBar.setIndicatorColor(Color.parseColor("#A5D6A7"));
            } else if (nota >= 5.0f) {
                progressBar.setIndicatorColor(Color.parseColor("#FFE082"));
            } else {
                progressBar.setIndicatorColor(Color.parseColor("#EF9A9A"));
            }

            textView.setText(String.valueOf(nota));
            textView.setAlpha(1.0f);
            progressBar.setProgress(0);

            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, notaInteira);
            animator.setDuration(1500);
            animator.setInterpolator(new DecelerateInterpolator());

            animator.addUpdateListener(animation -> {
                int progress = (int) animation.getAnimatedValue();
                progressBar.setProgress(progress);
            });

            animator.start();
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