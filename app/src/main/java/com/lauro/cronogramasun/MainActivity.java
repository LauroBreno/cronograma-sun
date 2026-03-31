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
    private String abaPendenteModal = "";

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
        // Encontre o seu botão de + (substitua o ID pelo ID real do seu botão)
        android.view.View btnAdicionarMain = findViewById(R.id.fabAdd);

        // ==========================================
        // MÁGICA UX: ANIMAÇÃO DE PULSO (Heartbeat)
        // ==========================================
        Runnable animacaoPulso = new Runnable() {
            @Override
            public void run() {
                // O botão cresce 15% (1.15f) rapidamente
                btnAdicionarMain.animate()
                        .scaleX(1.20f).scaleY(1.20f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            // Quando termina de crescer, ele volta ao tamanho original (1f)
                            btnAdicionarMain.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(200)
                                    .start();
                        }).start();

                // Repete esse "batimento" a cada 4 segundos
                btnAdicionarMain.postDelayed(this, 4000);
            }
        };

        // Inicia a animação 2 segundos após o aplicativo abrir
        btnAdicionarMain.postDelayed(animacaoPulso, 2000);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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
                    abrirModalEdicao();
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
                    abrirModalExclusao();
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

        TextView btnAddMateria = formDialog.findViewById(R.id.btnAdicionarNovaMateria);
        TextView btnAddAssunto = formDialog.findViewById(R.id.btnAdicionarNovoAssunto);

// A aba padrão ao abrir é Matéria, então escondemos o botão de Assunto
        if (btnAddMateria != null) btnAddMateria.setVisibility(View.VISIBLE);
        if (btnAddAssunto != null) btnAddAssunto.setVisibility(View.GONE);

// Aqui você pode colocar a sua lógica funcional de clique neles:
        if (btnAddMateria != null) {
            btnAddMateria.setOnClickListener(v -> {
                // Sua lógica funcional de adicionar mais matérias
            });
        }
        if (btnAddAssunto != null) {
            btnAddAssunto.setOnClickListener(v -> {
                // Sua lógica funcional de adicionar mais assuntos
            });
        }

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
        if (autoCompleteVinculoMateria != null)
            autoCompleteVinculoMateria.setAdapter(adapterMateria);

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
                        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(MainActivity.this,R.style.TemaCalendarioSun,
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
                        datePickerDialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(android.content.DialogInterface dialogInterface) {
                                android.widget.Button btnPositivo = datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_POSITIVE);
                                android.widget.Button btnNegativo = datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_NEGATIVE);

                                if (btnPositivo != null) {
                                    btnPositivo.setTextColor(android.graphics.Color.parseColor("#584039"));
                                }
                                if (btnNegativo != null) {
                                    btnNegativo.setTextColor(android.graphics.Color.parseColor("#584039"));
                                }
                            }
                        });
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
                    if (autoCompleteVinculoMateria != null)
                        autoCompleteVinculoMateria.setText("", false);
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
                        if (btnAddMateria != null) btnAddMateria.setVisibility(View.VISIBLE);
                        if (btnAddAssunto != null) btnAddAssunto.setVisibility(View.GONE);

                    } else if (checkedId == R.id.btnToggleAssunto) {
                        if (lblTituloForm != null) lblTituloForm.setText("Novo Assunto");
                        if (btnConfirmar != null) btnConfirmar.setText("Salvar Assunto");
                        layoutFormMateria.setVisibility(View.GONE);
                        layoutFormAssunto.setVisibility(View.VISIBLE);
                        btnToggleAssunto.setBackgroundColor(Color.parseColor("#584039"));
                        btnToggleAssunto.setTextColor(Color.WHITE);
                        btnToggleMateria.setBackgroundColor(Color.TRANSPARENT);
                        btnToggleMateria.setTextColor(Color.parseColor("#8D7B73"));
                        if (btnAddMateria != null) btnAddMateria.setVisibility(View.GONE);
                        if (btnAddAssunto != null) btnAddAssunto.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        if (btnConfirmar != null) {
            // Lógica: Salvar Matéria e limpar campos (Continuar na tela)
            if (btnAddMateria != null) {
                btnAddMateria.setOnClickListener(v -> {
                    String nomeMateria = padronizarTexto(inputNomeMateria.getText().toString());
                    String metaString = inputMetaMateria.getText().toString().trim();

                    if (nomeMateria.isEmpty()) {
                        mostrarNotificacao(btnAddMateria, "Preencha o nome da matéria!", true);
                        return;
                    }

                    double meta = metaString.isEmpty() ? 0.0 : Double.parseDouble(metaString);
                    long id = dbHelper.inserirMateria(nomeMateria, meta);

                    if (id == -2) {
                        mostrarNotificacao(btnAddMateria, "A matéria '" + nomeMateria + "' já existe!", true);
                    } else if (id != -1) {
                        materiaSelecionadaAtual = nomeMateria;
                        mostrarNotificacao(btnAddMateria, "Salvo! Pode adicionar a próxima.", false);
                        atualizarTelaCompleta();

                        // LIMPA OS CAMPOS PARA A PRÓXIMA!
                        inputNomeMateria.setText("");
                        inputMetaMateria.setText("");
                        inputNomeMateria.requestFocus();
                    }
                });
            }

            // Lógica: Salvar Assunto e limpar campos (Continuar na tela)
            if (btnAddAssunto != null) {
                btnAddAssunto.setOnClickListener(v -> {
                    String materiaVinculada = autoCompleteVinculoMateria.getText().toString().trim();
                    String nomeAssunto = padronizarTexto(inputNomeAssunto.getText().toString());
                    String qtdRevisoesStr = autoQtdRevisoes.getText().toString().trim();

                    if (materiaVinculada.isEmpty() || nomeAssunto.isEmpty() || qtdRevisoesStr.isEmpty()) {
                        mostrarNotificacao(btnAddAssunto, "Preencha todos os campos do assunto!", true);
                        return;
                    }

                    List<String> datasPreenchidas = new ArrayList<>();
                    for (com.google.android.material.textfield.TextInputEditText input : listaInputsData) {
                        String dataText = input.getText().toString().trim();
                        if (dataText.isEmpty() || !dataText.contains("/")) {
                            mostrarNotificacao(btnAddAssunto, "Preencha TODAS as datas!", true);
                            return;
                        }
                        datasPreenchidas.add(dataText);
                    }

                    int qtd = Integer.parseInt(qtdRevisoesStr);
                    long id = dbHelper.inserirAssuntoComRevisoes(materiaVinculada, nomeAssunto, qtd, datasPreenchidas);

                    if (id == -2) {
                        mostrarNotificacao(btnAddAssunto, "Este assunto já existe nessa matéria!", true);
                    } else if (id != -1) {
                        mostrarNotificacao(btnAddAssunto, "Assunto salvo! Insira o próximo.", false);
                        atualizarTelaCompleta();

                        // LIMPA OS CAMPOS PARA O PRÓXIMO!
                        inputNomeAssunto.setText("");
                        inputNomeAssunto.requestFocus();
                        // Mantém a matéria e a qtd de revisões, pois geralmente a pessoa vai cadastrar o próximo assunto do mesmo jeito!
                    }
                });
            }
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

    private void abrirModalEdicao() {
        BottomSheetDialog editDialog = new BottomSheetDialog(MainActivity.this);
        editDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        editDialog.setContentView(R.layout.layout_bottom_sheet_editar);

        LinearLayout layoutEditMateria = editDialog.findViewById(R.id.layoutEditMateria);
        LinearLayout layoutEditAssunto = editDialog.findViewById(R.id.layoutEditAssunto);
        LinearLayout layoutEditRevisao = editDialog.findViewById(R.id.layoutEditRevisao);
        LinearLayout layoutEditNota = editDialog.findViewById(R.id.layoutEditNota);

        ImageView btnFechar = editDialog.findViewById(R.id.btnFecharEdit);
        TextView lblTituloForm = editDialog.findViewById(R.id.lblTituloEditForm);
        com.google.android.material.button.MaterialButton btnSalvar = editDialog.findViewById(R.id.btnSalvarEdicao);

        TextView btnNavMateria = editDialog.findViewById(R.id.btnEditToggleMateria);
        TextView btnNavAssunto = editDialog.findViewById(R.id.btnEditToggleAssunto);
        TextView btnNavRevisao = editDialog.findViewById(R.id.btnEditToggleRevisao);
        TextView btnNavNota = editDialog.findViewById(R.id.btnEditToggleNota);

        android.widget.AutoCompleteTextView autoEditMateriaSelect = editDialog.findViewById(R.id.autoCompleteEditMateriaSelect);
        com.google.android.material.textfield.TextInputEditText inputEditNomeMateria = editDialog.findViewById(R.id.inputEditNomeMateria);
        com.google.android.material.textfield.TextInputEditText inputEditMetaMateria = editDialog.findViewById(R.id.inputEditMetaMateria);

        android.widget.AutoCompleteTextView autoEditMateriaForAssunto = editDialog.findViewById(R.id.autoCompleteEditMateriaForAssunto);
        android.widget.AutoCompleteTextView autoEditAssuntoSelect = editDialog.findViewById(R.id.autoCompleteEditAssuntoSelect);
        com.google.android.material.textfield.TextInputEditText inputEditNomeAssunto = editDialog.findViewById(R.id.inputEditNomeAssunto);

        android.widget.AutoCompleteTextView autoRevMateria = editDialog.findViewById(R.id.autoCompleteRevMateria);
        android.widget.AutoCompleteTextView autoRevAssunto = editDialog.findViewById(R.id.autoCompleteRevAssunto);
        TextView txtStatusRevisoes = editDialog.findViewById(R.id.txtStatusRevisoes);
        LinearLayout containerRevisoes = editDialog.findViewById(R.id.containerListaEditRevisoes);
        TextView btnAddRevisao = editDialog.findViewById(R.id.btnAdicionarNovaRevisao);

        android.widget.AutoCompleteTextView autoNotaMateria = editDialog.findViewById(R.id.autoCompleteNotaMateria);
        android.widget.AutoCompleteTextView autoNotaAssunto = editDialog.findViewById(R.id.autoCompleteNotaAssunto);
        TextView txtStatusNotas = editDialog.findViewById(R.id.txtStatusNotas);
        LinearLayout containerNotas = editDialog.findViewById(R.id.containerListaEditNotas);

        aplicarMascaraNota(inputEditMetaMateria);

        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> {
                editDialog.dismiss();
                abrirMenuOpcoes();
            });
        }

        View.OnClickListener clickNav = view -> {
            btnNavMateria.setBackgroundColor(Color.TRANSPARENT);
            btnNavMateria.setTextColor(Color.parseColor("#8D7B73"));
            btnNavMateria.setTypeface(null, Typeface.NORMAL);

            btnNavAssunto.setBackgroundColor(Color.TRANSPARENT);
            btnNavAssunto.setTextColor(Color.parseColor("#8D7B73"));
            btnNavAssunto.setTypeface(null, Typeface.NORMAL);

            btnNavRevisao.setBackgroundColor(Color.TRANSPARENT);
            btnNavRevisao.setTextColor(Color.parseColor("#8D7B73"));
            btnNavRevisao.setTypeface(null, Typeface.NORMAL);

            btnNavNota.setBackgroundColor(Color.TRANSPARENT);
            btnNavNota.setTextColor(Color.parseColor("#8D7B73"));
            btnNavNota.setTypeface(null, Typeface.NORMAL);

            layoutEditMateria.setVisibility(View.GONE);
            layoutEditAssunto.setVisibility(View.GONE);
            layoutEditRevisao.setVisibility(View.GONE);
            layoutEditNota.setVisibility(View.GONE);

            // ZERAR TODOS OS CAMPOS AO TROCAR DE ABA
            if (inputEditNomeMateria != null) inputEditNomeMateria.setText("");
            if (inputEditMetaMateria != null) inputEditMetaMateria.setText("");
            if (inputEditNomeAssunto != null) inputEditNomeAssunto.setText("");
            if (autoEditMateriaSelect != null) autoEditMateriaSelect.setText("", false);
            if (autoEditMateriaForAssunto != null) autoEditMateriaForAssunto.setText("", false);
            if (autoEditAssuntoSelect != null) autoEditAssuntoSelect.setText("", false);
            if (autoRevMateria != null) autoRevMateria.setText("", false);
            if (autoRevAssunto != null) autoRevAssunto.setText("", false);
            if (autoNotaMateria != null) autoNotaMateria.setText("", false);
            if (autoNotaAssunto != null) autoNotaAssunto.setText("", false);
            if (containerRevisoes != null) containerRevisoes.removeAllViews();
            if (containerNotas != null) containerNotas.removeAllViews();
            if (txtStatusRevisoes != null) txtStatusRevisoes.setText("Selecione um assunto para ver as revisões.");
            if (txtStatusNotas != null) txtStatusNotas.setText("Selecione um assunto para ver as notas.");
            if (btnAddRevisao != null) btnAddRevisao.setVisibility(View.GONE);

            TextView clicado = (TextView) view;
            clicado.setBackgroundColor(Color.parseColor("#584039"));
            clicado.setTextColor(Color.WHITE);
            clicado.setTypeface(null, Typeface.BOLD);

            if (clicado.getId() == R.id.btnEditToggleMateria) {
                lblTituloForm.setText("Editar Matéria");
                layoutEditMateria.setVisibility(View.VISIBLE);
                btnSalvar.setVisibility(View.VISIBLE);
            } else if (clicado.getId() == R.id.btnEditToggleAssunto) {
                lblTituloForm.setText("Editar Assunto");
                layoutEditAssunto.setVisibility(View.VISIBLE);
                btnSalvar.setVisibility(View.VISIBLE);
            } else if (clicado.getId() == R.id.btnEditToggleRevisao) {
                lblTituloForm.setText("Gerenciar Revisões");
                layoutEditRevisao.setVisibility(View.VISIBLE);
                btnSalvar.setVisibility(View.GONE);
            } else if (clicado.getId() == R.id.btnEditToggleNota) {
                lblTituloForm.setText("Editar Nota");
                layoutEditNota.setVisibility(View.VISIBLE);
                btnSalvar.setVisibility(View.GONE);
            }
        };

        btnNavMateria.setOnClickListener(clickNav);
        btnNavAssunto.setOnClickListener(clickNav);
        btnNavRevisao.setOnClickListener(clickNav);
        btnNavNota.setOnClickListener(clickNav);

        List<String> materiasSalvas = dbHelper.obterNomesMaterias();
        android.widget.ArrayAdapter<String> adapterMateria = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, materiasSalvas);

        if (autoEditMateriaSelect != null) autoEditMateriaSelect.setAdapter(adapterMateria);
        if (autoEditMateriaForAssunto != null) autoEditMateriaForAssunto.setAdapter(adapterMateria);
        if (autoRevMateria != null) autoRevMateria.setAdapter(adapterMateria);
        if (autoNotaMateria != null) autoNotaMateria.setAdapter(adapterMateria);

        if (autoEditMateriaSelect != null) {
            autoEditMateriaSelect.setOnItemClickListener((parent, view, position, id) -> {
                String matSelecionada = adapterMateria.getItem(position);
                double metaAtual = dbHelper.obterMetaMateria(matSelecionada);
                inputEditNomeMateria.setText(matSelecionada);
                inputEditMetaMateria.setText(metaAtual > 0 ? String.valueOf(metaAtual) : "");
            });
        }

        if (autoEditMateriaForAssunto != null && autoEditAssuntoSelect != null) {
            autoEditMateriaForAssunto.setOnItemClickListener((parent, view, position, id) -> {
                String matEscolhida = adapterMateria.getItem(position);
                autoEditAssuntoSelect.setText("", false);
                inputEditNomeAssunto.setText("");

                List<String> assuntosDaMateria = dbHelper.obterAssuntosPorMateria(matEscolhida);
                android.widget.ArrayAdapter<String> adapterAssunto = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, assuntosDaMateria);
                autoEditAssuntoSelect.setAdapter(adapterAssunto);
            });

            autoEditAssuntoSelect.setOnItemClickListener((parent, view, position, id) -> {
                String assuntoEscolhido = autoEditAssuntoSelect.getText().toString();
                inputEditNomeAssunto.setText(assuntoEscolhido);
            });
        }

        if (autoRevMateria != null && autoRevAssunto != null) {
            autoRevMateria.setOnItemClickListener((parent, view, position, id) -> {
                String matEscolhida = adapterMateria.getItem(position);
                autoRevAssunto.setText("", false);
                containerRevisoes.removeAllViews();
                txtStatusRevisoes.setText("Selecione um assunto para ver as revisões.");
                btnAddRevisao.setVisibility(View.GONE);

                List<String> assuntosDaMateria = dbHelper.obterAssuntosPorMateria(matEscolhida);
                android.widget.ArrayAdapter<String> adapterAssuntoRev = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, assuntosDaMateria);
                autoRevAssunto.setAdapter(adapterAssuntoRev);
            });

            autoRevAssunto.setOnItemClickListener((parent, view, position, id) -> {
                String mat = autoRevMateria.getText().toString();
                String ass = autoRevAssunto.getText().toString();
                carregarRevisoesNoEditor(mat, ass, containerRevisoes, txtStatusRevisoes, btnAddRevisao);
            });
        }

        if (autoNotaMateria != null && autoNotaAssunto != null) {
            autoNotaMateria.setOnItemClickListener((parent, view, position, id) -> {
                String matEscolhida = adapterMateria.getItem(position);
                autoNotaAssunto.setText("", false);
                containerNotas.removeAllViews();
                txtStatusNotas.setText("Selecione um assunto para ver as notas.");

                List<String> assuntosDaMateria = dbHelper.obterAssuntosPorMateria(matEscolhida);
                android.widget.ArrayAdapter<String> adapterAssuntoNota = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, assuntosDaMateria);
                autoNotaAssunto.setAdapter(adapterAssuntoNota);
            });

            autoNotaAssunto.setOnItemClickListener((parent, view, position, id) -> {
                String mat = autoNotaMateria.getText().toString();
                String ass = autoNotaAssunto.getText().toString();
                carregarNotasNoEditor(mat, ass, containerNotas, txtStatusNotas);
            });
        }

        if (btnSalvar != null) {
            btnSalvar.setOnClickListener(v -> {
                View viewSegura = findViewById(android.R.id.content);

                if (layoutEditMateria.getVisibility() == View.VISIBLE) {
                    String materiaAntiga = autoEditMateriaSelect.getText().toString().trim();
                    String novoNome = padronizarTexto(inputEditNomeMateria.getText().toString());
                    String novaMetaStr = inputEditMetaMateria.getText().toString().trim();

                    if (materiaAntiga.isEmpty() || novoNome.isEmpty()) {
                        mostrarNotificacao(btnSalvar, "Selecione uma matéria e defina um nome válido!", true);
                        return;
                    }

                    double novaMeta = novaMetaStr.isEmpty() ? 0.0 : Double.parseDouble(novaMetaStr);
                    double metaAntiga = dbHelper.obterMetaMateria(materiaAntiga);

                    String detalhes = "";
                    if (!materiaAntiga.equals(novoNome)) detalhes += "Nome: " + materiaAntiga + " ➔ " + novoNome + "\n";
                    if (novaMeta != metaAntiga) detalhes += "Meta: " + metaAntiga + " ➔ " + novaMeta;
                    if (detalhes.isEmpty()) detalhes = "Nenhuma alteração detectada.";

                    mostrarDialogUniversal(
                            "Salvar Alterações?",
                            "Resumo da Edição",
                            detalhes,
                            "Salvar",
                            false,
                            () -> {
                                boolean sucesso = dbHelper.atualizarMateria(materiaAntiga, novoNome, novaMeta);
                                if (sucesso) {
                                    if (materiaSelecionadaAtual.equals(materiaAntiga)) materiaSelecionadaAtual = novoNome;
                                    mostrarNotificacao(viewSegura, "Matéria atualizada com sucesso!", false);
                                    atualizarTelaCompleta();
                                    editDialog.dismiss();
                                } else {
                                    mostrarNotificacao(viewSegura, "Erro: Já existe outra matéria com esse nome!", true);
                                }
                            }
                    );

                } else if (layoutEditAssunto.getVisibility() == View.VISIBLE) {
                    String materiaSelecionada = autoEditMateriaForAssunto.getText().toString().trim();
                    String assuntoAntigo = autoEditAssuntoSelect.getText().toString().trim();
                    String novoNomeAssunto = padronizarTexto(inputEditNomeAssunto.getText().toString());

                    if (materiaSelecionada.isEmpty() || assuntoAntigo.isEmpty() || novoNomeAssunto.isEmpty()) {
                        mostrarNotificacao(btnSalvar, "Preencha todos os campos do assunto!", true);
                        return;
                    }

                    String detalhes = "Nome: " + assuntoAntigo + " ➔ " + novoNomeAssunto;

                    mostrarDialogUniversal(
                            "Salvar Alterações?",
                            "Resumo da Edição",
                            detalhes,
                            "Salvar",
                            false,
                            () -> {
                                boolean sucesso = dbHelper.atualizarAssunto(materiaSelecionada, assuntoAntigo, novoNomeAssunto);
                                if (sucesso) {
                                    mostrarNotificacao(viewSegura, "Assunto atualizado!", false);
                                    atualizarTelaCompleta();
                                    editDialog.dismiss();
                                }
                            }
                    );
                }
            });
        }

        editDialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        if ("MATERIA".equals(abaPendenteModal)) {
            btnNavMateria.performClick();
            abaPendenteModal = "";
        } else if ("ASSUNTO".equals(abaPendenteModal)) {
            btnNavAssunto.performClick();
            abaPendenteModal = "";
        } else if ("REVISAO".equals(abaPendenteModal)) {
            btnNavRevisao.performClick();
            abaPendenteModal = "";
        } else {
            btnNavMateria.performClick(); // Força a aba matéria ao abrir do zero
        }
        editDialog.show();
    }

    private void mostrarDialogConfirmacaoMateria(BottomSheetDialog formPai, String nomeMateria, String metaTxt, Runnable acaoSalvar) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.TemaAlertaSun);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
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

        if (txtEx1 != null) txtEx1.setVisibility(View.GONE);
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

    private void mostrarDialogConfirmacaoAssunto(BottomSheetDialog formPai, String nomeMateria, String nomeAssunto, int qtdRevisoes, String dataInicial, String dataFinal, Runnable acaoSalvar) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.TemaAlertaSun);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
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

    private void mostrarDialogConfirmacaoNota(BottomSheetDialog formPai, String nomeMateria, String nomeAssunto, String tipoNota, String valorNota, String refRevisao, Runnable acaoSalvar) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.TemaAlertaSun);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
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
            aplicarMenuFlutuante(cardChip, "MATERIA");
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
                if (txtZenTitulo != null)
                    txtZenTitulo.setText("Opa, parece que você ainda não começou!");
                if (txtZenSubtitulo != null)
                    txtZenSubtitulo.setText("Que tal adicionar a primeira matéria clicando no + ali embaixo?");
                if (lottieMeditatingDog != null) {
                    lottieMeditatingDog.setAnimation(R.raw.meditating_dog);
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

                aplicarMenuFlutuante(itemCard, "REVISAO");
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

                aplicarMenuFlutuante(itemCard, "REVISAO");
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
                    if (txtZenTitulo != null)
                        txtZenTitulo.setText("Poxa Sun, você ainda não se planejou?");
                    if (txtZenSubtitulo != null)
                        txtZenSubtitulo.setText("Vamos lá, me deixa te ajudar! Clique no + e adicione um Assunto.");
                    if (lottieMeditatingDog != null) {
                        lottieMeditatingDog.setAnimation(R.raw.the_wolf_turns_head);
                        lottieMeditatingDog.playAnimation();
                    }
                } else {
                    if (txtZenTitulo != null)
                        txtZenTitulo.setText("Nossa, você já fez todas as revisões?");
                    if (txtZenSubtitulo != null)
                        txtZenSubtitulo.setText("Quando crescer quero ser igual a você!");
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
            aplicarMenuFlutuante(holder.cardContainer, data.isMateria ? "MATERIA" : "ASSUNTO");
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

    // ==========================================
    // MÁGICA DA ABA DE REVISÕES - FATIA 9
    // ==========================================

    // 1. Validador de Ordem Cronológica
    private boolean validarOrdemDatas(String dataAnteriorStr, String dataNovaStr, String dataPosteriorStr) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));
        try {
            java.util.Date dataNova = sdf.parse(dataNovaStr);

            // A data nova TEM que ser estritamente DEPOIS da data anterior
            if (dataAnteriorStr != null && !dataAnteriorStr.isEmpty()) {
                java.util.Date dataAnterior = sdf.parse(dataAnteriorStr);
                if (!dataNova.after(dataAnterior)) return false;
            }

            // A data nova TEM que ser estritamente ANTES da próxima data
            if (dataPosteriorStr != null && !dataPosteriorStr.isEmpty()) {
                java.util.Date dataPosterior = sdf.parse(dataPosteriorStr);
                if (!dataNova.before(dataPosterior)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 2. Blindagem de Revisão
    private void carregarRevisoesNoEditor(String materia, String assunto, LinearLayout container, TextView txtStatus, TextView btnAdd) {
        container.removeAllViews();
        List<java.util.HashMap<String, String>> revisoes = dbHelper.obterRevisoesDoAssunto(materia, assunto);

        if (revisoes.isEmpty()) {
            txtStatus.setText("Nenhuma revisão encontrada para este assunto.");
            container.setBackground(null);
            btnAdd.setVisibility(View.GONE);
            return;
        }

        txtStatus.setText("Revisões cadastradas (" + revisoes.size() + "/5):");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));

        android.graphics.drawable.GradientDrawable shapeCard = new android.graphics.drawable.GradientDrawable();
        shapeCard.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shapeCard.setCornerRadius(48f);
        shapeCard.setColor(android.graphics.Color.TRANSPARENT);
        shapeCard.setStroke(3, android.graphics.Color.parseColor("#D7CCC8"));
        container.setBackground(shapeCard);
        container.setPadding(8, 16, 8, 16);

        for (int i = 0; i < revisoes.size(); i++) {
            java.util.HashMap<String, String> rev = revisoes.get(i);
            int revId = Integer.parseInt(rev.get("id"));
            String dataAtual = rev.get("data");
            String status = rev.get("status");
            int numeroRevisao = i + 1;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 32, 32, 32);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            row.setBackgroundResource(outValue.resourceId);

            TextView lblRev = new TextView(this);
            lblRev.setText("Revisão " + numeroRevisao);
            lblRev.setTextColor(android.graphics.Color.parseColor("#584039"));
            lblRev.setTextSize(16f);
            lblRev.setTypeface(null, android.graphics.Typeface.BOLD);
            lblRev.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            // CONTAINER DA DIREITA COM ALINHAMENTO FIXO
            LinearLayout rightContainer = new LinearLayout(this);
            rightContainer.setOrientation(LinearLayout.HORIZONTAL);
            rightContainer.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.END);

            TextView txtData = new TextView(this);
            txtData.setText(dataAtual);
            txtData.setTextSize(16f);

            ImageView iconCheck = new ImageView(this);
            int iconSize = (int) (20 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            iconParams.setMargins((int) (12 * getResources().getDisplayMetrics().density), 0, 0, 0);
            iconCheck.setLayoutParams(iconParams);
            iconCheck.setImageResource(R.drawable.ic_check_concluido);

            if (status.equalsIgnoreCase("CONCLUIDA")) {
                txtData.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Verde
                iconCheck.setVisibility(View.VISIBLE);
            } else {
                txtData.setTextColor(android.graphics.Color.parseColor("#8D7B73")); // Marrom
                iconCheck.setVisibility(View.INVISIBLE); // MÁGICA: Ocupa espaço, mas não aparece!
            }

            rightContainer.addView(txtData);
            rightContainer.addView(iconCheck);

            // CLIQUE PARA EDITAR DATA
            final int indexAtual = i;
            row.setOnClickListener(v -> {
                if (status.equalsIgnoreCase("CONCLUIDA")) {
                    mostrarNotificacao(container, "Esta revisão já foi concluída e não pode ser alterada!", true);
                    return;
                }

                java.util.Calendar calendarioInicial = java.util.Calendar.getInstance();
                try { calendarioInicial.setTime(sdf.parse(dataAtual)); } catch (Exception e) {}

                android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(MainActivity.this,R.style.TemaCalendarioSun, (view, year, month, dayOfMonth) -> {
                    String dataEscolhida = String.format(new java.util.Locale("pt", "BR"), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    if (dbHelper.atualizarDataRevisao(revId, dataEscolhida)) {
                        mostrarNotificacao(container, "Data atualizada!", false);
                        carregarRevisoesNoEditor(materia, assunto, container, txtStatus, btnAdd);
                        atualizarTelaCompleta();
                    }
                }, calendarioInicial.get(java.util.Calendar.YEAR), calendarioInicial.get(java.util.Calendar.MONTH), calendarioInicial.get(java.util.Calendar.DAY_OF_MONTH));

                try {
                    android.widget.DatePicker datePicker = datePickerDialog.getDatePicker();
                    java.util.Calendar minCal = java.util.Calendar.getInstance();

                    if (indexAtual > 0) {
                        java.util.Date dataAnt = sdf.parse(revisoes.get(indexAtual - 1).get("data"));
                        java.util.Calendar calAnt = java.util.Calendar.getInstance();
                        calAnt.setTime(dataAnt);
                        calAnt.add(java.util.Calendar.DAY_OF_MONTH, 1);
                        if (calAnt.after(minCal)) minCal = calAnt;
                    }
                    datePicker.setMinDate(minCal.getTimeInMillis());

                    if (indexAtual < revisoes.size() - 1) {
                        java.util.Date dataPost = sdf.parse(revisoes.get(indexAtual + 1).get("data"));
                        java.util.Calendar maxCal = java.util.Calendar.getInstance();
                        maxCal.setTime(dataPost);
                        maxCal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                        if (maxCal.getTimeInMillis() >= minCal.getTimeInMillis()) {
                            datePicker.setMaxDate(maxCal.getTimeInMillis());
                        } else {
                            datePicker.setMaxDate(minCal.getTimeInMillis());
                        }
                    }
                } catch (Exception e) {}

                datePickerDialog.setOnShowListener(d -> {
                    android.widget.Button btnPos = datePickerDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
                    android.widget.Button btnNeg = datePickerDialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);
                    if (btnPos != null) btnPos.setTextColor(android.graphics.Color.parseColor("#584039"));
                    if (btnNeg != null) btnNeg.setTextColor(android.graphics.Color.parseColor("#584039"));
                });
                datePickerDialog.show();
            });

            row.addView(lblRev);
            row.addView(rightContainer);
            container.addView(row);

            if (i < revisoes.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                divParams.setMargins(32, 0, 32, 0);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(android.graphics.Color.parseColor("#EFEAE4"));
                container.addView(divider);
            }
        }

        if (revisoes.size() < 5) {
            btnAdd.setVisibility(View.VISIBLE);
            btnAdd.setText("+ Adicionar Nova Revisão");
            btnAdd.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, 16, 0, 0);
            btnAdd.setLayoutParams(btnParams);
            btnAdd.setPadding(0, 32, 0, 16);

            btnAdd.setOnClickListener(v -> {
                java.util.Calendar calendarioInicial = java.util.Calendar.getInstance();
                java.util.Date dataUltima = new java.util.Date();
                try {
                    dataUltima = sdf.parse(revisoes.get(revisoes.size() - 1).get("data"));
                    calendarioInicial.setTime(dataUltima);
                    calendarioInicial.add(java.util.Calendar.DAY_OF_MONTH, 1);
                } catch (Exception e) {}

                android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(MainActivity.this,R.style.TemaCalendarioSun, (view, year, month, dayOfMonth) -> {
                    String dataEscolhida = String.format(new java.util.Locale("pt", "BR"), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    if (dbHelper.adicionarRevisaoExtra(materia, assunto, dataEscolhida)) {
                        mostrarNotificacao(container, "Nova revisão adicionada!", false);
                        carregarRevisoesNoEditor(materia, assunto, container, txtStatus, btnAdd);
                        atualizarTelaCompleta();
                    }
                }, calendarioInicial.get(java.util.Calendar.YEAR), calendarioInicial.get(java.util.Calendar.MONTH), calendarioInicial.get(java.util.Calendar.DAY_OF_MONTH));

                try {
                    java.util.Calendar minCal = java.util.Calendar.getInstance();
                    java.util.Calendar calUltima = java.util.Calendar.getInstance();
                    calUltima.setTime(dataUltima);
                    calUltima.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    if (calUltima.after(minCal)) minCal = calUltima;
                    datePickerDialog.getDatePicker().setMinDate(minCal.getTimeInMillis());
                } catch (Exception e) {}
                datePickerDialog.setOnShowListener(d -> {
                    datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#584039"));
                    datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.parseColor("#584039"));
                });
                datePickerDialog.show();
            });
        } else {
            btnAdd.setVisibility(View.GONE);
        }
    }

    // ==========================================
    // ABA NOTAS (COM ALINHAMENTO PERFEITO)
    // ==========================================
    private void carregarNotasNoEditor(String materia, String assunto, LinearLayout container, TextView txtStatus) {
        container.removeAllViews();
        List<java.util.HashMap<String, String>> revisoes = dbHelper.obterRevisoesComNotas(materia, assunto);

        if (revisoes.isEmpty()) {
            txtStatus.setText("Nenhuma revisão encontrada para este assunto.");
            container.setBackground(null);
            return;
        }

        txtStatus.setText("Notas das revisões. Clique para editar:");

        android.graphics.drawable.GradientDrawable shapeCard = new android.graphics.drawable.GradientDrawable();
        shapeCard.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shapeCard.setCornerRadius(48f);
        shapeCard.setColor(android.graphics.Color.TRANSPARENT);
        shapeCard.setStroke(3, android.graphics.Color.parseColor("#D7CCC8"));
        container.setBackground(shapeCard);
        container.setPadding(8, 16, 8, 16);

        for (int i = 0; i < revisoes.size(); i++) {
            java.util.HashMap<String, String> rev = revisoes.get(i);
            String status = rev.get("status");
            String notaAtual = rev.get("nota");
            String desempenhoId = rev.get("desempenho_id");
            int numeroRevisao = i + 1;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 32, 32, 32);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            row.setBackgroundResource(outValue.resourceId);

            TextView lblRev = new TextView(this);
            lblRev.setText("Revisão " + numeroRevisao);
            lblRev.setTextColor(android.graphics.Color.parseColor("#584039"));
            lblRev.setTextSize(16f);
            lblRev.setTypeface(null, android.graphics.Typeface.BOLD);
            lblRev.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            LinearLayout rightContainer = new LinearLayout(this);
            rightContainer.setOrientation(LinearLayout.HORIZONTAL);
            rightContainer.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.END);

            TextView txtNota = new TextView(this);
            txtNota.setTextSize(16f);
            txtNota.setTypeface(null, android.graphics.Typeface.BOLD);

            ImageView iconLock = new ImageView(this);
            int iconSize = (int) (20 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            iconParams.setMargins((int) (12 * getResources().getDisplayMetrics().density), 0, 0, 0);
            iconLock.setLayoutParams(iconParams);

            if (status.equalsIgnoreCase("CONCLUIDA")) {
                txtNota.setText(notaAtual);
                txtNota.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Verde
                iconLock.setImageResource(R.drawable.ic_check_concluido);
                iconLock.setVisibility(View.VISIBLE);

                Runnable pulseAnimation = new Runnable() {
                    @Override
                    public void run() {
                        row.animate().scaleX(1.08f).scaleY(1.08f).setDuration(200).withEndAction(() -> {
                            row.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                        }).start();
                        row.postDelayed(this, 5000);
                    }
                };
                row.postDelayed(pulseAnimation, 1500 + (i * 200));

                row.setOnClickListener(v -> {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);

                    LinearLayout layoutDialog = new LinearLayout(MainActivity.this);
                    layoutDialog.setOrientation(LinearLayout.VERTICAL);
                    layoutDialog.setBackgroundResource(R.drawable.bg_popup_arredondado);
                    layoutDialog.setPadding(64, 64, 64, 48);

                    TextView txtTitulo = new TextView(MainActivity.this);
                    txtTitulo.setText("Editar Nota - Revisão " + numeroRevisao);
                    txtTitulo.setTextColor(Color.parseColor("#584039"));
                    txtTitulo.setTextSize(18f);
                    txtTitulo.setTypeface(null, Typeface.BOLD);
                    txtTitulo.setGravity(android.view.Gravity.CENTER);

                    // Como a máscara do seu projeto exige um TextInputEditText, nós criamos um na hora!
                    final com.google.android.material.textfield.TextInputEditText inputNota = new com.google.android.material.textfield.TextInputEditText(MainActivity.this);
                    inputNota.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    inputNota.setText(notaAtual);
                    inputNota.setTextColor(Color.parseColor("#584039"));
                    inputNota.setGravity(android.view.Gravity.CENTER);

                    // MÁGICA 1: Aplica a máscara que você já criou para travar até 10.0
                    aplicarMascaraNota(inputNota);
                    inputNota.selectAll();

                    LinearLayout.LayoutParams paramsInput = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    paramsInput.setMargins(0, 32, 0, 32);
                    inputNota.setLayoutParams(paramsInput);

                    LinearLayout layoutBotoes = new LinearLayout(MainActivity.this);
                    layoutBotoes.setOrientation(LinearLayout.HORIZONTAL);
                    layoutBotoes.setGravity(android.view.Gravity.END);

                    android.app.AlertDialog dialogDigitacao = builder.create();
                    dialogDigitacao.setView(layoutDialog);
                    dialogDigitacao.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

                    TextView btnCancelar = new TextView(MainActivity.this);
                    btnCancelar.setText("Cancelar");
                    btnCancelar.setTextColor(Color.parseColor("#8D7B73"));
                    btnCancelar.setPadding(32, 16, 32, 16);
                    btnCancelar.setOnClickListener(v2 -> dialogDigitacao.dismiss());

                    TextView btnSalvar = new TextView(MainActivity.this);
                    btnSalvar.setText("Avançar"); // Muda o nome para indicar que tem mais um passo
                    btnSalvar.setTextColor(Color.parseColor("#584039"));
                    btnSalvar.setTypeface(null, Typeface.BOLD);
                    btnSalvar.setPadding(32, 16, 32, 16);
                    btnSalvar.setOnClickListener(v2 -> {
                        String notaDigitada = inputNota.getText().toString();
                        if(notaDigitada.isEmpty()) {
                            mostrarNotificacao(container, "A nota não pode ficar vazia!", true);
                            return;
                        }

                        dialogDigitacao.dismiss(); // Fecha a tela de digitar

                        // MÁGICA 2: Abre a sua confirmação do gatinho!
                        mostrarDialogUniversal(
                                "Salvar Nova Nota?",
                                "Revisão " + numeroRevisao,
                                "Nota: " + notaAtual + " ➔ " + notaDigitada, // Mostra o "Antes -> Depois"
                                "Salvar",
                                false, // Marrom, pois é salvar
                                () -> {
                                    try {
                                        double novaNota = Double.parseDouble(notaDigitada.replace(",", "."));
                                        if (dbHelper.atualizarNotaDesempenho(Integer.parseInt(desempenhoId), novaNota)) {
                                            mostrarNotificacao(container, "Nota atualizada!", false);
                                            carregarNotasNoEditor(materia, assunto, container, txtStatus);
                                            atualizarTelaCompleta();
                                        }
                                    } catch (Exception e) {
                                        mostrarNotificacao(container, "Erro ao salvar a nota!", true);
                                    }
                                }
                        );
                    });

                    layoutBotoes.addView(btnCancelar);
                    layoutBotoes.addView(btnSalvar);
                    layoutDialog.addView(txtTitulo);
                    layoutDialog.addView(inputNota);
                    layoutDialog.addView(layoutBotoes);

                    dialogDigitacao.show();
                });

            } else {
                txtNota.setText("Pendente");
                txtNota.setTextColor(android.graphics.Color.parseColor("#8D7B73"));
                iconLock.setImageResource(android.R.drawable.ic_secure);
                iconLock.setColorFilter(android.graphics.Color.parseColor("#8D7B73"));
                iconLock.setVisibility(View.VISIBLE);

                row.setOnClickListener(v -> mostrarNotificacao(container, "Conclua esta revisão primeiro para dar uma nota!", true));
            }

            rightContainer.addView(txtNota);
            rightContainer.addView(iconLock);
            row.addView(lblRev);
            row.addView(rightContainer);
            container.addView(row);

            if (i < revisoes.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                divParams.setMargins(32, 0, 32, 0);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(android.graphics.Color.parseColor("#EFEAE4"));
                container.addView(divider);
            }
        }
    }

    // ==========================================
    // LÓGICA DE EXCLUSÃO ESPECÍFICA (REVISÃO / NOTA)
    // ==========================================
    private void carregarListaExclusao(String materia, String assunto, LinearLayout container, TextView txtStatus, String tipo) {
        container.removeAllViews();
        List<java.util.HashMap<String, String>> revisoes = dbHelper.obterRevisoesComNotas(materia, assunto);

        if (revisoes.isEmpty()) {
            txtStatus.setText("Nenhum dado encontrado para excluir.");
            container.setBackground(null);
            return;
        }

        txtStatus.setText(tipo.equals("REVISAO") ? "Toque em uma revisão para excluí-la:" : "Toque em uma nota para excluí-la:");

        android.graphics.drawable.GradientDrawable shapeCard = new android.graphics.drawable.GradientDrawable();
        shapeCard.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shapeCard.setCornerRadius(48f);
        shapeCard.setColor(android.graphics.Color.TRANSPARENT);
        shapeCard.setStroke(3, android.graphics.Color.parseColor("#D7CCC8"));
        container.setBackground(shapeCard);
        container.setPadding(8, 16, 8, 16);

        for (int i = 0; i < revisoes.size(); i++) {
            java.util.HashMap<String, String> rev = revisoes.get(i);
            int revId = Integer.parseInt(rev.get("rev_id"));
            int desempenhoId = Integer.parseInt(rev.get("desempenho_id"));
            String status = rev.get("status");
            String nota = rev.get("nota");
            int numeroRevisao = i + 1;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 32, 32, 32);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            row.setBackgroundResource(outValue.resourceId);

            TextView lblEsq = new TextView(this);
            lblEsq.setText("Revisão " + numeroRevisao);
            lblEsq.setTextColor(android.graphics.Color.parseColor("#584039"));
            lblEsq.setTextSize(16f);
            lblEsq.setTypeface(null, android.graphics.Typeface.BOLD);
            lblEsq.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            LinearLayout rightContainer = new LinearLayout(this);
            rightContainer.setOrientation(LinearLayout.HORIZONTAL);
            rightContainer.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.END);

            TextView txtDir = new TextView(this);
            txtDir.setTextSize(16f);

            ImageView iconTrash = new ImageView(this);
            iconTrash.setImageResource(android.R.drawable.ic_menu_delete);
            iconTrash.setColorFilter(android.graphics.Color.parseColor("#D9534F"));
            int iconSize = (int) (20 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            iconParams.setMargins((int) (12 * getResources().getDisplayMetrics().density), 0, 0, 0);
            iconTrash.setLayoutParams(iconParams);

            boolean podeExcluir = false;

            if (tipo.equals("REVISAO")) {
                txtDir.setText(rev.get("data"));
                if (status.equalsIgnoreCase("CONCLUIDA")) {
                    txtDir.setTextColor(android.graphics.Color.parseColor("#8D7B73"));
                    iconTrash.setVisibility(View.INVISIBLE);
                } else {
                    txtDir.setTextColor(android.graphics.Color.parseColor("#584039"));
                    iconTrash.setVisibility(View.VISIBLE);
                    podeExcluir = true;
                }
            } else {
                if (status.equalsIgnoreCase("CONCLUIDA")) {
                    txtDir.setText("Nota: " + nota);
                    txtDir.setTextColor(android.graphics.Color.parseColor("#584039"));
                    iconTrash.setVisibility(View.VISIBLE);
                    podeExcluir = true;
                } else {
                    txtDir.setText("Sem Nota");
                    txtDir.setTextColor(android.graphics.Color.parseColor("#8D7B73"));
                    iconTrash.setVisibility(View.INVISIBLE);
                }
            }

            rightContainer.addView(txtDir);
            rightContainer.addView(iconTrash);

            if (podeExcluir) {
                row.setOnClickListener(v -> {
                    mostrarDialogUniversal(
                            tipo.equals("REVISAO") ? "Excluir Revisão" : "Excluir Nota",
                            tipo.equals("REVISAO") ? "Revisão " + numeroRevisao : "Nota: " + nota,
                            "Tem certeza que deseja apagar permanentemente?",
                            "Sim, Excluir",
                            true, // Botão vermelho
                            null,
                            () -> {
                                boolean sucesso;
                                if (tipo.equals("NOTA")) {
                                    android.database.Cursor cAssId = dbHelper.getReadableDatabase().rawQuery("SELECT a.id FROM assuntos a INNER JOIN materias m ON a.materia_id = m.id WHERE m.nome = ? AND a.nome = ?", new String[]{materia, assunto});
                                    int assId = -1;
                                    if (cAssId.moveToFirst()) assId = cAssId.getInt(0);
                                    cAssId.close();
                                    sucesso = dbHelper.excluirNotaEspecifica(desempenhoId, assId);
                                } else {
                                    sucesso = dbHelper.excluirRevisaoEspecifica(revId);
                                }

                                if (sucesso) {
                                    mostrarNotificacao(container, "Excluído com sucesso!", false);
                                    carregarListaExclusao(materia, assunto, container, txtStatus, tipo);
                                    atualizarTelaCompleta();
                                }
                            }
                    );
                });
            } else {
                row.setOnClickListener(v -> {
                    if (tipo.equals("REVISAO")) mostrarNotificacao(container, "Não é possível apagar uma revisão concluída.", true);
                    else mostrarNotificacao(container, "Não há nota para apagar aqui.", true);
                });
            }

            row.addView(lblEsq);
            row.addView(rightContainer);
            container.addView(row);

            if (i < revisoes.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                divParams.setMargins(32, 0, 32, 0);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(android.graphics.Color.parseColor("#EFEAE4"));
                container.addView(divider);
            }
        }
    }

    // ==========================================
    // PAINEL INFERIOR DE EXCLUSÃO
    // ==========================================
    private void abrirModalExclusao() {
        com.google.android.material.bottomsheet.BottomSheetDialog delDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);
        delDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        delDialog.setContentView(R.layout.layout_bottom_sheet_excluir);

        LinearLayout layoutDelMateria = delDialog.findViewById(R.id.layoutDelMateria);
        LinearLayout layoutDelAssunto = delDialog.findViewById(R.id.layoutDelAssunto);
        LinearLayout layoutDelListas = delDialog.findViewById(R.id.layoutDelListas);

        ImageView btnFechar = delDialog.findViewById(R.id.btnFecharDel);
        TextView lblTituloForm = delDialog.findViewById(R.id.lblTituloDelForm);
        com.google.android.material.button.MaterialButton btnConfirmar = delDialog.findViewById(R.id.btnConfirmarExclusao);

        TextView btnNavMateria = delDialog.findViewById(R.id.btnDelToggleMateria);
        TextView btnNavAssunto = delDialog.findViewById(R.id.btnDelToggleAssunto);
        TextView btnNavRevisao = delDialog.findViewById(R.id.btnDelToggleRevisao);
        TextView btnNavNota = delDialog.findViewById(R.id.btnDelToggleNota);

        android.widget.AutoCompleteTextView autoDelMateria = delDialog.findViewById(R.id.autoCompleteDelMateria);
        android.widget.AutoCompleteTextView autoDelMatAssunto = delDialog.findViewById(R.id.autoCompleteDelMatAssunto);
        android.widget.AutoCompleteTextView autoDelAssunto = delDialog.findViewById(R.id.autoCompleteDelAssunto);
        android.widget.AutoCompleteTextView autoDelListMat = delDialog.findViewById(R.id.autoCompleteDelListMat);
        android.widget.AutoCompleteTextView autoDelListAss = delDialog.findViewById(R.id.autoCompleteDelListAss);

        TextView txtStatusDelete = delDialog.findViewById(R.id.txtStatusDelete);
        LinearLayout containerListaDelete = delDialog.findViewById(R.id.containerListaDelete);

        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> {
                delDialog.dismiss();
                abrirMenuOpcoes();
            });
        }

        View.OnClickListener clickNav = view -> {
            btnNavMateria.setBackgroundColor(Color.TRANSPARENT);
            btnNavMateria.setTextColor(Color.parseColor("#8D7B73"));
            btnNavMateria.setTypeface(null, Typeface.NORMAL);

            btnNavAssunto.setBackgroundColor(Color.TRANSPARENT);
            btnNavAssunto.setTextColor(Color.parseColor("#8D7B73"));
            btnNavAssunto.setTypeface(null, Typeface.NORMAL);

            btnNavRevisao.setBackgroundColor(Color.TRANSPARENT);
            btnNavRevisao.setTextColor(Color.parseColor("#8D7B73"));
            btnNavRevisao.setTypeface(null, Typeface.NORMAL);

            btnNavNota.setBackgroundColor(Color.TRANSPARENT);
            btnNavNota.setTextColor(Color.parseColor("#8D7B73"));
            btnNavNota.setTypeface(null, Typeface.NORMAL);

            layoutDelMateria.setVisibility(View.GONE);
            layoutDelAssunto.setVisibility(View.GONE);
            layoutDelListas.setVisibility(View.GONE);

            // ZERAR TODOS OS CAMPOS AO TROCAR DE ABA
            if (autoDelMateria != null) autoDelMateria.setText("", false);
            if (autoDelMatAssunto != null) autoDelMatAssunto.setText("", false);
            if (autoDelAssunto != null) autoDelAssunto.setText("", false);
            if (autoDelListMat != null) autoDelListMat.setText("", false);
            if (autoDelListAss != null) autoDelListAss.setText("", false);
            if (containerListaDelete != null) containerListaDelete.removeAllViews();
            if (txtStatusDelete != null) txtStatusDelete.setText("Selecione um assunto.");

            TextView clicado = (TextView) view;
            clicado.setBackgroundColor(Color.parseColor("#D9534F"));
            clicado.setTextColor(Color.WHITE);
            clicado.setTypeface(null, Typeface.BOLD);

            if (clicado.getId() == R.id.btnDelToggleMateria) {
                lblTituloForm.setText("Excluir Matéria");
                layoutDelMateria.setVisibility(View.VISIBLE);
                btnConfirmar.setVisibility(View.VISIBLE);
            } else if (clicado.getId() == R.id.btnDelToggleAssunto) {
                lblTituloForm.setText("Excluir Assunto");
                layoutDelAssunto.setVisibility(View.VISIBLE);
                btnConfirmar.setVisibility(View.VISIBLE);
            } else if (clicado.getId() == R.id.btnDelToggleRevisao) {
                lblTituloForm.setText("Excluir Revisão");
                layoutDelListas.setVisibility(View.VISIBLE);
                btnConfirmar.setVisibility(View.GONE);
            } else if (clicado.getId() == R.id.btnDelToggleNota) {
                lblTituloForm.setText("Excluir Nota");
                layoutDelListas.setVisibility(View.VISIBLE);
                btnConfirmar.setVisibility(View.GONE);
            }
        };

        btnNavMateria.setOnClickListener(clickNav);
        btnNavAssunto.setOnClickListener(clickNav);
        btnNavRevisao.setOnClickListener(clickNav);
        btnNavNota.setOnClickListener(clickNav);

        List<String> materiasSalvas = dbHelper.obterNomesMaterias();
        android.widget.ArrayAdapter<String> adapterMateria = new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, materiasSalvas);

        if (autoDelMateria != null) autoDelMateria.setAdapter(adapterMateria);
        if (autoDelMatAssunto != null) autoDelMatAssunto.setAdapter(adapterMateria);
        if (autoDelListMat != null) autoDelListMat.setAdapter(adapterMateria);

        if (autoDelMatAssunto != null && autoDelAssunto != null) {
            autoDelMatAssunto.setOnItemClickListener((parent, view, position, id) -> {
                String mat = adapterMateria.getItem(position);
                autoDelAssunto.setText("", false);
                List<String> assuntos = dbHelper.obterAssuntosPorMateria(mat);
                autoDelAssunto.setAdapter(new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, assuntos));
            });
        }

        if (autoDelListMat != null && autoDelListAss != null) {
            autoDelListMat.setOnItemClickListener((parent, view, position, id) -> {
                String mat = adapterMateria.getItem(position);
                autoDelListAss.setText("", false);
                containerListaDelete.removeAllViews();
                txtStatusDelete.setText("Selecione um assunto.");
                List<String> assuntos = dbHelper.obterAssuntosPorMateria(mat);
                autoDelListAss.setAdapter(new android.widget.ArrayAdapter<>(this, R.layout.item_dropdown, assuntos));
            });

            autoDelListAss.setOnItemClickListener((parent, view, position, id) -> {
                String mat = autoDelListMat.getText().toString();
                String ass = autoDelListAss.getText().toString();

                if (btnNavRevisao.getCurrentTextColor() == Color.WHITE) {
                    carregarListaExclusao(mat, ass, containerListaDelete, txtStatusDelete, "REVISAO");
                } else {
                    carregarListaExclusao(mat, ass, containerListaDelete, txtStatusDelete, "NOTA");
                }
            });
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                View viewSegura = findViewById(android.R.id.content);

                if (layoutDelMateria.getVisibility() == View.VISIBLE) {
                    String mat = autoDelMateria.getText().toString().trim();
                    if (mat.isEmpty()) {
                        mostrarNotificacao(btnConfirmar, "Selecione uma matéria!", true);
                        return;
                    }

                    mostrarDialogUniversal(
                            "Excluir Matéria",
                            mat,
                            "Atenção: Apagará todas as revisões, assuntos e notas!",
                            "Sim, Excluir",
                            true,
                            () -> {
                                if (dbHelper.excluirMateria(mat)) {
                                    mostrarNotificacao(viewSegura, "Matéria excluída com sucesso!", false);
                                    atualizarTelaCompleta();
                                    delDialog.dismiss();
                                }
                            }
                    );

                } else if (layoutDelAssunto.getVisibility() == View.VISIBLE) {
                    String mat = autoDelMatAssunto.getText().toString().trim();
                    String ass = autoDelAssunto.getText().toString().trim();
                    if (mat.isEmpty() || ass.isEmpty()) {
                        mostrarNotificacao(btnConfirmar, "Selecione matéria e assunto!", true);
                        return;
                    }

                    mostrarDialogUniversal(
                            "Excluir Assunto",
                            ass,
                            "Atenção: Isso apagará este assunto e suas revisões!",
                            "Sim, Excluir",
                            true,
                            () -> {
                                if (dbHelper.excluirAssunto(mat, ass)) {
                                    mostrarNotificacao(viewSegura, "Assunto excluído!", false);
                                    atualizarTelaCompleta();
                                    delDialog.dismiss();
                                }
                            }
                    );
                }
            });
        }

        delDialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        if ("MATERIA".equals(abaPendenteModal)) {
            btnNavMateria.performClick();
            abaPendenteModal = "";
        } else if ("ASSUNTO".equals(abaPendenteModal)) {
            btnNavAssunto.performClick();
            abaPendenteModal = "";
        } else if ("REVISAO".equals(abaPendenteModal)) {
            btnNavRevisao.performClick();
            abaPendenteModal = "";
        } else {
            btnNavMateria.performClick(); // Força a aba matéria ao abrir do zero
        }
        delDialog.show();
    }

    // ==========================================
    // MENU FLUTUANTE ESTILIZADO (GESTOS PARA EXPERTS)
    // ==========================================
    private void aplicarMenuFlutuante(android.view.View viewAlvo, String abaFoco) {
        viewAlvo.setOnLongClickListener(v -> {

            // Aqui envelopamos o menu com a cor bege e marrom!
            android.content.Context wrapper = new android.view.ContextThemeWrapper(MainActivity.this, R.style.TemaMenuFlutuante);
            android.widget.PopupMenu popup = new android.widget.PopupMenu(wrapper, v);

            popup.getMenu().add(0, 1, 0, "Editar").setIcon(R.drawable.ic_menu_edit_novo);
            popup.getMenu().add(0, 2, 0, "Excluir").setIcon(R.drawable.ic_menu_delete_novo);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                popup.setForceShowIcon(true);
            }

            popup.setOnMenuItemClickListener(item -> {
                abaPendenteModal = abaFoco; // Salva o destino (Matéria, Assunto ou Revisão)

                if (item.getItemId() == 1) abrirModalEdicao();
                else if (item.getItemId() == 2) abrirModalExclusao();

                return true;
            });

            popup.show();
            return true;
        });
    }

    // ==========================================
    // O VERDADEIRO MAESTRO (USANDO O SEU MODAL DO GATINHO)
    // ==========================================
    // ==========================================
    // O VERDADEIRO MAESTRO (VERSÃO 1 - NORMAL, SEM CAMPO DE TEXTO)
    // ==========================================
    private void mostrarDialogUniversal(String titulo, String subtitulo, String textoAviso, String textoBotao, boolean isPerigo, Runnable acaoConfirmar) {
        mostrarDialogUniversal(titulo, subtitulo, textoAviso, textoBotao, isPerigo, null, acaoConfirmar);
    }

    // ==========================================
    // O VERDADEIRO MAESTRO (VERSÃO 2 - COMPLETA, COM CAMPO DE TEXTO EXTRA)
    // ==========================================
    private void mostrarDialogUniversal(String titulo, String subtitulo, String textoAviso, String textoBotao, boolean isPerigo, android.view.View inputExtra, Runnable acaoConfirmar) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirmacao, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        android.widget.TextView txtType = dialogView.findViewById(R.id.txtResumoLinha1Type);
        android.widget.TextView txtMeta = dialogView.findViewById(R.id.txtResumoLinha1Meta);
        android.widget.TextView txtName = dialogView.findViewById(R.id.txtResumoLinha2Name);
        android.widget.TextView txtEx1 = dialogView.findViewById(R.id.txtResumoLinha3Extra1);
        android.widget.TextView txtEx2 = dialogView.findViewById(R.id.txtResumoLinha4Extra2);

        android.view.View btnCancelar = dialogView.findViewById(R.id.btnCancelarDialog);
        android.view.View btnConfirmar = dialogView.findViewById(R.id.btnConfirmarDialog);

        if (txtType != null) txtType.setText(titulo);
        if (txtMeta != null) txtMeta.setVisibility(android.view.View.GONE);
        if (txtName != null) txtName.setText(subtitulo);

        if (txtEx1 != null) {
            if (textoAviso != null && !textoAviso.isEmpty()) {
                txtEx1.setVisibility(android.view.View.VISIBLE);
                txtEx1.setText(textoAviso);
                txtEx1.setTextColor(android.graphics.Color.parseColor(isPerigo ? "#D9534F" : "#8D7B73"));
            } else {
                txtEx1.setVisibility(android.view.View.GONE);
            }
        }
        if (txtEx2 != null) txtEx2.setVisibility(android.view.View.GONE);

        if (btnConfirmar instanceof android.widget.TextView) {
            ((android.widget.TextView) btnConfirmar).setText(textoBotao);
            if (isPerigo) {
                btnConfirmar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D9534F")));
            } else {
                btnConfirmar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#584039")));
            }
        }

        // MÁGICA: Injeta o campo de nota (EditText) antes dos botões, se existir!
        if (inputExtra != null && dialogView instanceof android.view.ViewGroup) {
            android.view.ViewGroup root = (android.view.ViewGroup) dialogView;
            android.view.View botoesParent = (android.view.View) btnConfirmar.getParent();
            int index = root.indexOfChild(botoesParent);
            if (index > 0) {
                root.addView(inputExtra, index);
            } else {
                root.addView(inputExtra);
            }
        }

        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialog.dismiss());
        if (btnConfirmar != null) btnConfirmar.setOnClickListener(v -> {
            if (acaoConfirmar != null) acaoConfirmar.run();
            dialog.dismiss();
        });

        dialog.show();
    }
}