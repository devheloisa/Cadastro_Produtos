package br.com.seuapp.controller;

import br.com.seuapp.model.Categoria;
import br.com.seuapp.model.Produto;
import br.com.seuapp.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

public class MainController {

    @FXML private TextField txtCodigo, txtNome, txtDescricao, txtPrecoCompra, txtPrecoVenda, txtQuantidade;
    @FXML private TextField txtCatId, txtCatNome, txtCatDesc, txtCatSetor;
    @FXML private DatePicker dpFabricacao, dpValidade;

    @FXML private TableView<Produto> tblProdutos;

    // Colunas da tabela (versão completa)
    @FXML private TableColumn<Produto, String> colCodigo, colNome, colDescricao;
    @FXML private TableColumn<Produto, String> colFab, colVal;          // datas formatadas como String
    @FXML private TableColumn<Produto, String> colCompra, colVenda;     // preços formatados como String
    @FXML private TableColumn<Produto, Integer> colQtde;                // quantidade
    @FXML private TableColumn<Produto, Number> colCatId;                // usa SimpleIntegerProperty
    @FXML private TableColumn<Produto, String> colCatNome, colCatDesc, colCatSetor;



    private ProdutoService service;

    @FXML
    public void initialize() {
        // Caminho do CSV (p/ desenvolvimento). Em produção, prefira pasta do usuário.
        Path csv = Path.of("src/main/resources/data/produtos.csv");
        service = new ProdutoService(csv);

        // ====== Formatações ======
        var fmtData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var money   = java.text.NumberFormat.getNumberInstance(new java.util.Locale("pt","BR"));
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        // ====== Colunas - propriedades diretas ======
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colQtde.setCellValueFactory(new PropertyValueFactory<>("quantidadeEstoque"));

        // ====== Colunas - datas ======
        colFab.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getDataFabricacao() != null
                                ? p.getValue().getDataFabricacao().format(fmtData)
                                : ""
                )
        );
        colVal.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getDataValidade() != null
                                ? p.getValue().getDataValidade().format(fmtData)
                                : ""
                )
        );

        // ====== Colunas - preços (String formatada) ======
        colCompra.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getPrecoCompra() != null
                                ? money.format(p.getValue().getPrecoCompra())
                                : ""
                )
        );
        colVenda.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getPrecoVenda() != null
                                ? money.format(p.getValue().getPrecoVenda())
                                : ""
                )
        );

        // ====== Colunas - categoria (aninhadas) ======
        colCatId.setCellValueFactory(p ->
                new javafx.beans.property.SimpleIntegerProperty(
                        p.getValue().getCategoria() != null ? p.getValue().getCategoria().getId() : 0
                )
        );
        colCatNome.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getCategoria() != null ? p.getValue().getCategoria().getNome() : ""
                )
        );
        colCatDesc.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getCategoria() != null ? p.getValue().getCategoria().getDescricao() : ""
                )
        );
        colCatSetor.setCellValueFactory(p ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> p.getValue().getCategoria() != null ? p.getValue().getCategoria().getSetor() : ""
                )
        );

        // (opcional) Limitadores de entrada
        txtQuantidade.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        txtCatId.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        txtPrecoCompra.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[\\d., R$]*") ? c : null));
        txtPrecoVenda.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[\\d., R$]*") ? c : null));

        // Tabela começa vazia — só mostra quando clicar em "Listar" ou relatórios
        limparTabela();
    }


    private void limparTabela() {
        tblProdutos.setItems(FXCollections.observableArrayList());
        tblProdutos.refresh();
    }


    private BigDecimal parseMoney(String raw, String msgErro) {
        if (raw == null) throw new IllegalArgumentException(msgErro);
        // remove "R$", espaços e caracteres invisíveis
        String norm = raw.replace("R$", "").replaceAll("\\s+", "");
        // remove separador de milhar . e troca vírgula por ponto
        norm = norm.replace(".", "").replace(",", ".");
        if (norm.isBlank()) throw new IllegalArgumentException(msgErro);
        try {
            return new BigDecimal(norm);
        } catch (Exception e) {
            throw new IllegalArgumentException(msgErro + " Valor recebido: " + raw);
        }
    }

    private int parseInt(String raw, String msgErro) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(msgErro + " Valor recebido: " + raw);
        }
    }


    private void atualizarTabela() {
        tblProdutos.setItems(FXCollections.observableArrayList(service.listar()));
    }

    private Produto lerFormulario() {
        var p = new Produto();

        String codigo = txtCodigo.getText().trim();
        if (codigo.length() != 8 || !codigo.matches("[A-Za-z0-9]{8}"))
            throw new IllegalArgumentException("Código deve ter 8 caracteres alfanuméricos.");

        p.setCodigo(codigo);

        String nome = txtNome.getText().trim();
        if (nome.length() < 2) throw new IllegalArgumentException("Nome mínimo 2 caracteres.");
        p.setNome(nome);

        p.setDescricao(txtDescricao.getText().trim());
        p.setDataFabricacao(dpFabricacao.getValue());
        p.setDataValidade(dpValidade.getValue());

        p.setPrecoCompra(parseMoney(txtPrecoCompra.getText(), "Preço de compra inválido."));
        p.setPrecoVenda (parseMoney(txtPrecoVenda.getText(),  "Preço de venda inválido."));

        p.setQuantidadeEstoque(parseInt(txtQuantidade.getText(), "Quantidade inválida."));

        int cId = txtCatId.getText().isBlank() ? 0 : parseInt(txtCatId.getText(), "Categoria ID inválido.");
        var c = new br.com.seuapp.model.Categoria(
                cId,
                txtCatNome.getText().trim(),
                txtCatDesc.getText().trim(),
                txtCatSetor.getText().trim()
        );
        p.setCategoria(c);
        return p;
    }


    // Botões
    @FXML
    private void onCadastrar() {
        try {
            var p = lerFormulario();
            service.cadastrar(p);
            limparTabela(); // não lista automaticamente
            alertInfo("Sucesso", "Produto cadastrado/atualizado.");
        } catch (Exception e) {
            alertErro("Erro ao cadastrar", e.getMessage());
        }
    }


    @FXML
    private void onConsultar() {
        var codigo = txtCodigo.getText().trim();
        var opt = service.consultar(codigo);
        if (opt.isPresent()) {
            var p = opt.get();
            preencherFormulario(p);
            alertInfo("Consulta", "Produto encontrado.");
        } else {
            alertInfo("Consulta", "Produto não encontrado.");
        }
    }

    @FXML
    private void onExcluir() {
        var codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) { alertInfo("Exclusão", "Informe o código para excluir."); return; }

        boolean ok = service.excluir(codigo);
        limparTabela(); // não lista automaticamente

        if (ok) {
            alertInfo("Exclusão", "Produto " + codigo + " removido com sucesso.");
            // (opcional) limpar formulário...
        } else {
            alertInfo("Exclusão", "Nenhum produto com código " + codigo + " foi encontrado.");
        }
    }



    @FXML
    private void onListar() {
        atualizarTabela();             // agora SÓ aqui lista tudo
        alertInfo("Listagem", "Lista atualizada.");
    }



    @FXML
    private void onRelatorioVencimento() {
        var lista = service.proximosAVencer(60);
        tblProdutos.setItems(FXCollections.observableArrayList(lista));
        alertInfo("Relatório", "Produtos que vencem nos próximos 60 dias: " + lista.size());
    }

    @FXML
    private void onRelatorioEstoque() {
        var lista = service.estoqueBaixo(10);
        tblProdutos.setItems(FXCollections.observableArrayList(lista));
        alertInfo("Relatório", "Produtos com estoque baixo (<10): " + lista.size());
    }

    @FXML
    private void onRelatorioMargem() {
        var mapa = service.margemMediaPorCategoria();
        alertInfo("Margem média por categoria", mapa.toString());
    }

    @FXML
    private void onListarPorSetor() {
        String setor = txtCatSetor.getText();
        if (setor == null || setor.isBlank()) {
            alertInfo("Listar por setor", "Preencha o campo \"Categoria: Setor\" para filtrar.");
            return;
        }
        var lista = service.listarPorSetor(setor);
        tblProdutos.setItems(FXCollections.observableArrayList(lista));
        alertInfo("Listar por setor", "Itens no setor \"" + setor.trim() + "\": " + lista.size());
    }


    private void preencherFormulario(Produto p) {
        txtCodigo.setText(p.getCodigo());
        txtNome.setText(p.getNome());
        txtDescricao.setText(p.getDescricao());
        dpFabricacao.setValue(p.getDataFabricacao());
        dpValidade.setValue(p.getDataValidade());
        txtPrecoCompra.setText(p.getPrecoCompra()!=null? p.getPrecoCompra().toString() : "");
        txtPrecoVenda.setText(p.getPrecoVenda()!=null? p.getPrecoVenda().toString() : "");
        txtQuantidade.setText(String.valueOf(p.getQuantidadeEstoque()));
        if (p.getCategoria()!=null) {
            txtCatId.setText(String.valueOf(p.getCategoria().getId()));
            txtCatNome.setText(p.getCategoria().getNome());
            txtCatDesc.setText(p.getCategoria().getDescricao());
            txtCatSetor.setText(p.getCategoria().getSetor());
        }
    }

    private void alertInfo(String titulo, String msg) {
        var a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void alertErro(String titulo, String msg) {
        var a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }


}
