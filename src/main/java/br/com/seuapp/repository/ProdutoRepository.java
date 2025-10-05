package br.com.seuapp.repository;

import br.com.seuapp.model.Categoria;
import br.com.seuapp.model.Produto;
import br.com.seuapp.util.CSVUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProdutoRepository {
    private final Path arquivo;
    private final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProdutoRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    public List<Produto> carregar() {
        try {
            var linhas = CSVUtils.readAllLines(arquivo);
            if (linhas.isEmpty()) return new ArrayList<>();
            // ignora cabeçalho
            return linhas.stream().skip(1).filter(l -> !l.isBlank())
                    .map(this::fromCSV)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void salvar(List<Produto> produtos) {
        var header = "codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;quantidadeEstoque;categoriaId;categoriaNome;categoriaDescricao;categoriaSetor";
        var linhas = new ArrayList<String>();
        linhas.add(header);

        for (var p : produtos) {
            var c = p.getCategoria();
            linhas.add(String.join(";",
                    safe(p.getCodigo()),
                    safe(p.getNome()),
                    safe(p.getDescricao()),
                    p.getDataFabricacao() != null ? p.getDataFabricacao().format(BR) : "",
                    p.getDataValidade() != null ? p.getDataValidade().format(BR) : "",
                    p.getPrecoCompra() != null ? p.getPrecoCompra().toString() : "",
                    p.getPrecoVenda() != null ? p.getPrecoVenda().toString() : "",
                    String.valueOf(p.getQuantidadeEstoque()),
                    c != null ? String.valueOf(c.getId()) : "",
                    c != null ? safe(c.getNome()) : "",
                    c != null ? safe(c.getDescricao()) : "",
                    c != null ? safe(c.getSetor()) : ""
            ));
        }
        try {
            CSVUtils.writeAllLines(arquivo, linhas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Normaliza texto para não quebrar o CSV (usa ';' como separador).
     */
    private String safe(String s) {
        if (s == null) return "";
        String t = s.trim();
        t = t.replace("\r", " ").replace("\n", " ").replace("\t", " "); // remove quebras
        t = t.replace(";", ",");     // evita separar colunas sem querer
        t = t.replace("\"", "'");    // evita aspas duplas no meio
        t = t.replaceAll("\\s{2,}", " "); // comprime múltiplos espaços
        return t;
    }


    private Produto fromCSV(String line) {
        var parts = line.split(";", -1); // mantém campos vazios
        var p = new Produto();

        // Helpers de leitura (sempre trim)
        String codigo = trimOrEmpty(parts, 0);
        String nome = trimOrEmpty(parts, 1);
        String descricao = trimOrEmpty(parts, 2);
        String dtFabStr = trimOrEmpty(parts, 3);
        String dtValStr = trimOrEmpty(parts, 4);
        String precoCompraStr = trimOrEmpty(parts, 5);
        String precoVendaStr = trimOrEmpty(parts, 6);
        String qtdeStr = trimOrEmpty(parts, 7);
        String catIdStr = trimOrEmpty(parts, 8);
        String catNome = trimOrEmpty(parts, 9);
        String catDesc = trimOrEmpty(parts, 10);
        String catSetor = trimOrEmpty(parts, 11);

        // Produto
        p.setCodigo(codigo);
        p.setNome(nome);
        p.setDescricao(descricao);
        p.setDataFabricacao(dtFabStr.isBlank() ? null : LocalDate.parse(dtFabStr, BR));
        p.setDataValidade(dtValStr.isBlank() ? null : LocalDate.parse(dtValStr, BR));
        p.setPrecoCompra(precoCompraStr.isBlank() ? null : new BigDecimal(precoCompraStr));
        p.setPrecoVenda(precoVendaStr.isBlank() ? null : new BigDecimal(precoVendaStr));
        p.setQuantidadeEstoque(qtdeStr.isBlank() ? 0 : Integer.parseInt(qtdeStr));

        // Categoria (só cria se houver algo)
        if (!catIdStr.isBlank() || !catNome.isBlank() || !catDesc.isBlank() || !catSetor.isBlank()) {
            var c = new Categoria(
                    catIdStr.isBlank() ? 0 : Integer.parseInt(catIdStr),
                    catNome,
                    catDesc,
                    catSetor
            );
            p.setCategoria(c);
        }
        return p;
    }

    /**
     * Lê o índice do vetor aplicando trim; se faltar coluna, retorna ""
     */
    private String trimOrEmpty(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) return "";
        var s = arr[idx];
        return (s == null) ? "" : s.trim();
    }
}




