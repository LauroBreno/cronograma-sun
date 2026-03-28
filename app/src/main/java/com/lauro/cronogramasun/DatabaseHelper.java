package com.lauro.cronogramasun;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Informações Básicas do Banco
    private static final String DATABASE_NAME = "CronogramaSun.db";
    private static final int DATABASE_VERSION = 3;

    // ==========================================
    // NOMES DAS TABELAS
    // ==========================================
    public static final String TABELA_SEMESTRES = "semestres";
    public static final String TABELA_MATERIAS = "materias";
    public static final String TABELA_ASSUNTOS = "assuntos";
    public static final String TABELA_REVISOES = "revisoes";
    public static final String TABELA_DESEMPENHO = "desempenho";
    public static final String TABELA_ANOTACOES = "anotacoes";
    public static final String TABELA_EVENTOS = "eventos";

    // ==========================================
    // CRIAÇÃO DA TABELA: SEMESTRES
    // ==========================================
    private static final String CREATE_TABLE_SEMESTRES = "CREATE TABLE " + TABELA_SEMESTRES + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "nome TEXT NOT NULL, "
            + "status TEXT DEFAULT 'ATIVO');"; // ATIVO ou FINALIZADO

    // ==========================================
    // CRIAÇÃO DA TABELA: MATÉRIAS
    // ==========================================
    private static final String CREATE_TABLE_MATERIAS = "CREATE TABLE " + TABELA_MATERIAS + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "semestre_id INTEGER, "
            + "nome TEXT NOT NULL, "
            + "meta_nota REAL, "
            + "data_criacao TEXT, "
            + "FOREIGN KEY(semestre_id) REFERENCES " + TABELA_SEMESTRES + "(id) ON DELETE CASCADE);";

    // ==========================================
    // CRIAÇÃO DA TABELA: ASSUNTOS
    // ==========================================
    private static final String CREATE_TABLE_ASSUNTOS = "CREATE TABLE " + TABELA_ASSUNTOS + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "materia_id INTEGER, "
            + "nome TEXT NOT NULL, "
            + "qtd_revisoes INTEGER, "
            + "data_criacao TEXT, "
            + "FOREIGN KEY(materia_id) REFERENCES " + TABELA_MATERIAS + "(id) ON DELETE CASCADE);";

    // ==========================================
    // CRIAÇÃO DA TABELA: REVISÕES (Cronograma)
    // ==========================================
    private static final String CREATE_TABLE_REVISOES = "CREATE TABLE " + TABELA_REVISOES + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "assunto_id INTEGER, "
            + "numero_revisao INTEGER, "
            + "data_programada TEXT, "
            + "status TEXT DEFAULT 'PENDENTE', " // PENDENTE, CONCLUIDA, ATRASADA
            + "FOREIGN KEY(assunto_id) REFERENCES " + TABELA_ASSUNTOS + "(id) ON DELETE CASCADE);";

    // ==========================================
    // CRIAÇÃO DA TABELA: DESEMPENHO (Notas)
    // ==========================================
    private static final String CREATE_TABLE_DESEMPENHO = "CREATE TABLE " + TABELA_DESEMPENHO + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "assunto_id INTEGER, "
            + "nota_direta REAL, "
            + "acertos INTEGER, "
            + "total_questoes INTEGER, "
            + "data_registro TEXT, "
            + "FOREIGN KEY(assunto_id) REFERENCES " + TABELA_ASSUNTOS + "(id) ON DELETE CASCADE);";

    // ==========================================
    // CRIAÇÃO DA TABELA: ANOTAÇÕES (Insights)
    // ==========================================
    private static final String CREATE_TABLE_ANOTACOES = "CREATE TABLE " + TABELA_ANOTACOES + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "materia_id INTEGER, "
            + "texto TEXT NOT NULL, "
            + "is_prioridade INTEGER DEFAULT 0, " // 0 = Falso, 1 = Verdadeiro
            + "data_criacao TEXT, "
            + "FOREIGN KEY(materia_id) REFERENCES " + TABELA_MATERIAS + "(id) ON DELETE CASCADE);";

    // ==========================================
    // CRIAÇÃO DA TABELA: EVENTOS (Provas/Testes)
    // ==========================================
    private static final String CREATE_TABLE_EVENTOS = "CREATE TABLE " + TABELA_EVENTOS + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "materia_id INTEGER, "
            + "titulo TEXT NOT NULL, "
            + "data_evento TEXT, "
            + "is_prioridade INTEGER DEFAULT 0, "
            + "FOREIGN KEY(materia_id) REFERENCES " + TABELA_MATERIAS + "(id) ON DELETE CASCADE);";

    // Construtor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Configuração Sênior: Ativa as restrições de Chave Estrangeira (Foreign Keys)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // O Android executa isso UMA ÚNICA VEZ quando o app é instalado
        db.execSQL(CREATE_TABLE_SEMESTRES);
        db.execSQL(CREATE_TABLE_MATERIAS);
        db.execSQL(CREATE_TABLE_ASSUNTOS);
        db.execSQL(CREATE_TABLE_REVISOES);
        db.execSQL(CREATE_TABLE_DESEMPENHO);
        db.execSQL(CREATE_TABLE_ANOTACOES);
        db.execSQL(CREATE_TABLE_EVENTOS);

        // Sênior: Vamos já inserir o "Semestre 1" padrão para o app não nascer vazio
        db.execSQL("INSERT INTO " + TABELA_SEMESTRES + " (nome, status) VALUES ('Semestre Atual', 'ATIVO');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se mudarmos a versão do banco no futuro, ele limpa e recria
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_EVENTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_ANOTACOES);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_DESEMPENHO);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_REVISOES);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_ASSUNTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MATERIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_SEMESTRES);
        onCreate(db);
    }

    // ==========================================
    // MÉTODOS DE AÇÃO (CRUD)
    // ==========================================

    // 1. Salvar Nova Matéria no Banco
    // 1. Inserir nova Matéria (AGORA COM TRAVA ANTI-DUPLICIDADE)
    public long inserirMateria(String nome, double meta) {
        SQLiteDatabase db = this.getWritableDatabase();

        // SEGURANÇA: Verifica se já existe uma matéria com esse nome (ignora maiúscula/minúscula)
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABELA_MATERIAS + " WHERE LOWER(nome) = LOWER(?)", new String[]{nome});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return -2; // -2 é o nosso código secreto para "Essa matéria já existe!"
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("meta_nota", meta);

        // Pega a data atual
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));
        values.put("data_criacao", sdf.format(new java.util.Date()));

        long id = db.insert(TABELA_MATERIAS, null, values);
        db.close();
        return id;
    }

    // 2. Buscar nomes das matérias para o Dropdown de Assuntos
    public List<String> obterNomesMaterias() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Puxa tudo em ordem alfabética
        Cursor cursor = db.rawQuery("SELECT nome FROM " + TABELA_MATERIAS + " ORDER BY nome ASC", null);

        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // 3. Salvar Assunto E as Revisões com as Datas Personalizadas
    public long inserirAssuntoComRevisoes(String nomeMateria, String nomeAssunto, int qtdRevisoes, List<String> datasPersonalizadas) {
        SQLiteDatabase db = this.getWritableDatabase();
        long assuntoId = -1;

        Cursor cursorMateria = db.rawQuery("SELECT id FROM " + TABELA_MATERIAS + " WHERE nome = ?", new String[]{nomeMateria});
        int materiaId = -1;
        if (cursorMateria.moveToFirst()) {
            materiaId = cursorMateria.getInt(0);
        }
        cursorMateria.close();

        if (materiaId != -1) {
            // Verifica se JÁ EXISTE esse assunto DENTRO dessa matéria
            Cursor checkCursor = db.rawQuery("SELECT id FROM " + TABELA_ASSUNTOS +
                            " WHERE materia_id = ? AND LOWER(nome) = LOWER(?)",
                    new String[]{String.valueOf(materiaId), nomeAssunto});

            if (checkCursor.getCount() > 0) {
                checkCursor.close();
                db.close();
                return -2; // Código secreto: "-2" significa "Assunto Duplicado"
            }
            ContentValues valoresAssunto = new ContentValues();
            valoresAssunto.put("materia_id", materiaId);
            valoresAssunto.put("nome", nomeAssunto);
            valoresAssunto.put("qtd_revisoes", qtdRevisoes);

            // Usamos a primeira data como a 'data_criacao' de base do assunto
            if (!datasPersonalizadas.isEmpty()) {
                valoresAssunto.put("data_criacao", datasPersonalizadas.get(0));
            }

            assuntoId = db.insert(TABELA_ASSUNTOS, null, valoresAssunto);

            if (assuntoId != -1) {
                // Loop exato baseado nas datas que o usuário escolheu à mão
                for (int i = 0; i < qtdRevisoes; i++) {
                    // Garante que não vai dar erro se a lista vier menor por algum bug
                    String dataEscolhida = (i < datasPersonalizadas.size()) ? datasPersonalizadas.get(i) : "";

                    ContentValues valoresRevisao = new ContentValues();
                    valoresRevisao.put("assunto_id", assuntoId);
                    valoresRevisao.put("numero_revisao", (i + 1));
                    valoresRevisao.put("data_programada", dataEscolhida);
                    valoresRevisao.put("status", "PENDENTE");

                    db.insert(TABELA_REVISOES, null, valoresRevisao);
                }
            }
        }
        db.close();
        return assuntoId;
    }

    // 4. Buscar todas as Revisões Pendentes cruzando 3 tabelas!
    public List<RevisaoDTO> obterRevisoesPendentes() {
        List<RevisaoDTO> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // A Mágica do SQL: Junta as tabelas Revisoes (r), Assuntos (a) e Materias (m)
        String query = "SELECT r.id, m.nome, a.nome, r.numero_revisao, r.data_programada " +
                "FROM " + TABELA_REVISOES + " r " +
                "INNER JOIN " + TABELA_ASSUNTOS + " a ON r.assunto_id = a.id " +
                "INNER JOIN " + TABELA_MATERIAS + " m ON a.materia_id = m.id " +
                "WHERE r.status = 'PENDENTE'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                RevisaoDTO rev = new RevisaoDTO();
                rev.idRevisao = cursor.getInt(0);
                rev.nomeMateria = cursor.getString(1);
                rev.nomeAssunto = cursor.getString(2);
                rev.numeroRevisao = cursor.getInt(3);
                rev.dataProgramada = cursor.getString(4);
                lista.add(rev);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // Classe de transporte de dados (DTO)
    public static class RevisaoDTO {
        public int idRevisao;
        public String nomeMateria;
        public String nomeAssunto;
        public int numeroRevisao;
        public String dataProgramada;
        public long diasDiferenca;
    }

    // 4.1 Buscar Revisões Pendentes FILTRADAS por Matéria
    public List<RevisaoDTO> obterRevisoesPendentesPorMateria(String nomeMateria) {
        List<RevisaoDTO> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.id, m.nome, a.nome, r.numero_revisao, r.data_programada " +
                "FROM " + TABELA_REVISOES + " r " +
                "INNER JOIN " + TABELA_ASSUNTOS + " a ON r.assunto_id = a.id " +
                "INNER JOIN " + TABELA_MATERIAS + " m ON a.materia_id = m.id " +
                "WHERE r.status = 'PENDENTE' AND m.nome = ?";

        Cursor cursor = db.rawQuery(query, new String[]{nomeMateria});

        if (cursor.moveToFirst()) {
            do {
                RevisaoDTO rev = new RevisaoDTO();
                rev.idRevisao = cursor.getInt(0);
                rev.nomeMateria = cursor.getString(1);
                rev.nomeAssunto = cursor.getString(2);
                rev.numeroRevisao = cursor.getInt(3);
                rev.dataProgramada = cursor.getString(4);
                lista.add(rev);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // ==========================================
    // MÉTODOS DE DESEMPENHO (NOTAS)
    // ==========================================

    // 5. Buscar Assuntos filtrados por Matéria (Para o Dropdown Encadeado)
    public List<String> obterAssuntosPorMateria(String nomeMateria) {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.nome FROM " + TABELA_ASSUNTOS + " a " +
                "INNER JOIN " + TABELA_MATERIAS + " m ON a.materia_id = m.id " +
                "WHERE m.nome = ? ORDER BY a.nome ASC";

        Cursor cursor = db.rawQuery(query, new String[]{nomeMateria});
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // 6. Salvar o Desempenho (Nota Direta ou Fração)
    public long inserirDesempenho(String nomeAssunto, double notaDireta, int acertos, int totalQuestoes) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        // 1º Pega o ID do Assunto selecionado
        Cursor c = db.rawQuery("SELECT id FROM " + TABELA_ASSUNTOS + " WHERE nome = ?", new String[]{nomeAssunto});
        int assuntoId = -1;
        if (c.moveToFirst()) {
            assuntoId = c.getInt(0);
        }
        c.close();

        // 2º Salva a Nota vinculada a esse Assunto
        if (assuntoId != -1) {
            ContentValues values = new ContentValues();
            values.put("assunto_id", assuntoId);

            // Pega a data de hoje para sabermos quando ela tirou essa nota
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));
            values.put("data_registro", sdf.format(new java.util.Date()));

            // Verifica qual modo de nota o usuário escolheu (Nota direta usa >= 0. Fração usa -1 na nota direta)
            if (notaDireta >= 0) {
                values.put("nota_direta", notaDireta);
                values.put("acertos", 0);
                values.put("total_questoes", 0);
            } else {
                values.put("nota_direta", -1.0);
                values.put("acertos", acertos);
                values.put("total_questoes", totalQuestoes);
            }

            id = db.insert(TABELA_DESEMPENHO, null, values);

            if (id != -1) {
                // Encontra a revisão pendente com o menor número (a mais antiga) e marca como CONCLUIDA
                String updateQuery = "UPDATE " + TABELA_REVISOES +
                        " SET status = 'CONCLUIDA' " +
                        " WHERE id = (SELECT id FROM " + TABELA_REVISOES +
                        " WHERE assunto_id = ? AND status = 'PENDENTE' ORDER BY numero_revisao ASC LIMIT 1)";

                db.execSQL(updateQuery, new Object[]{assuntoId});
            }
        }
        db.close();
        return id;
    }

    // 7. Calcular Média Real da Matéria
    public float calcularMediaMateria(String nomeMateria) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT d.nota_direta, d.acertos, d.total_questoes " +
                "FROM " + TABELA_DESEMPENHO + " d " +
                "INNER JOIN " + TABELA_ASSUNTOS + " a ON d.assunto_id = a.id " +
                "INNER JOIN " + TABELA_MATERIAS + " m ON a.materia_id = m.id " +
                "WHERE m.nome = ?";

        Cursor cursor = db.rawQuery(query, new String[]{nomeMateria});

        float somaNotas = 0;
        int quantidadeNotas = 0;

        if (cursor.moveToFirst()) {
            do {
                double notaDireta = cursor.getDouble(0);
                if (notaDireta >= 0) {
                    // É nota direta (Ex: 8.5)
                    somaNotas += notaDireta;
                } else {
                    // É fração (Ex: 15 acertos de 20 questões)
                    int acertos = cursor.getInt(1);
                    int total = cursor.getInt(2);
                    if (total > 0) {
                        somaNotas += ((float) acertos / total) * 10.0f; // Converte para base 10
                    }
                }
                quantidadeNotas++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // Se não tem nenhuma nota registrada ainda, retorna 0.0
        if (quantidadeNotas == 0) return 0.0f;

        // Calcula a média e formata para 1 casa decimal (ex: 8.5333 vira 8.5)
        float media = somaNotas / quantidadeNotas;
        return (float) (Math.round(media * 10.0) / 10.0);
    }

    // 8. Calcular Média Real de um Assunto Específico
    public float calcularMediaAssunto(String nomeAssunto) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT d.nota_direta, d.acertos, d.total_questoes " +
                "FROM " + TABELA_DESEMPENHO + " d " +
                "INNER JOIN " + TABELA_ASSUNTOS + " a ON d.assunto_id = a.id " +
                "WHERE a.nome = ?";

        Cursor cursor = db.rawQuery(query, new String[]{nomeAssunto});

        float somaNotas = 0;
        int quantidadeNotas = 0;

        if (cursor.moveToFirst()) {
            do {
                double notaDireta = cursor.getDouble(0);
                if (notaDireta >= 0) {
                    somaNotas += notaDireta;
                } else {
                    int acertos = cursor.getInt(1);
                    int total = cursor.getInt(2);
                    if (total > 0) {
                        somaNotas += ((float) acertos / total) * 10.0f;
                    }
                }
                quantidadeNotas++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (quantidadeNotas == 0) return 0.0f;

        float media = somaNotas / quantidadeNotas;
        return (float) (Math.round(media * 10.0) / 10.0);
    }

    // 9. Descobrir qual é a próxima revisão que será paga
    public String obterProximaRevisaoPendente(String nomeAssunto) {
        SQLiteDatabase db = this.getReadableDatabase();
        String result = null;

        String query = "SELECT r.numero_revisao FROM " + TABELA_REVISOES + " r " +
                "INNER JOIN " + TABELA_ASSUNTOS + " a ON r.assunto_id = a.id " +
                "WHERE a.nome = ? AND r.status = 'PENDENTE' " +
                "ORDER BY r.numero_revisao ASC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{nomeAssunto});
        if (cursor.moveToFirst()) {
            result = "Referente à Revisão " + cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return result;
    }

    // 10. Obter APENAS assuntos que ainda têm revisões pendentes
    public List<String> obterAssuntosComRevisoesPendentes(String nomeMateria) {
        List<String> assuntos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // O "DISTINCT" garante que não apareçam nomes repetidos (arrumando o bug das antigas duplicações)
        String query = "SELECT DISTINCT a.nome FROM " + TABELA_ASSUNTOS + " a " +
                "INNER JOIN " + TABELA_MATERIAS + " m ON a.materia_id = m.id " +
                "INNER JOIN " + TABELA_REVISOES + " r ON r.assunto_id = a.id " +
                "WHERE m.nome = ? AND r.status = 'PENDENTE'";

        Cursor cursor = db.rawQuery(query, new String[]{nomeMateria});
        if (cursor.moveToFirst()) {
            do {
                assuntos.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return assuntos;
    }

    // 11. Verifica se a Matéria já tem algum Assunto planeado
    public boolean verificarSeMateriaTemAssuntos(String nomeMateria) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABELA_ASSUNTOS + " a " +
                "INNER JOIN " + TABELA_MATERIAS + " m ON a.materia_id = m.id " +
                "WHERE m.nome = ?";
        Cursor cursor = db.rawQuery(query, new String[]{nomeMateria});
        boolean tem = false;
        if (cursor.moveToFirst()) {
            tem = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return tem;
    }
}

