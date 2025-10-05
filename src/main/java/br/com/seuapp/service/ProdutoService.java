package br.com.seuapp.service;

import br.com.seuapp.model.Produto;
import br.com.seuapp.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProdutoService {
    private final List<Produto> produtos;
    private final ProdutoRepository repo;
    private static final Pattern CODIGO_8 = Pattern.compile("^[A-Za-z0-9]{8}$");

    public ProdutoService(Path arquivoCSV) {
        this.repo = new ProdutoRepository(arquivoCSV);
        this.produtos = repo.carregar();
    }

    // CRUD
    public void cadastrar(Produto p) {
        validar(p, true);
        produtos.removeIf(x -> x.getCodigo().equalsIgnoreCase(p.getCodigo()));
        produtos.add(p);
        repo.salvar(produtos);
    }

    public boolean excluir(String codigo) {
        if (codigo == null) return false;
        String alvo = codigo.trim();
        boolean removed = produtos.removeIf(p ->
                p.getCodigo() != null && p.getCodigo().trim().equalsIgnoreCase(alvo)
        );
        if (removed) repo.salvar(produtos);
        return removed;
    }


    public Optional<Produto> consultar(String codigo) {
        return produtos.stream().filter(p -> p.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }

    public List<Produto> listar() {
        return new ArrayList<>(produtos);
    }

    // Relatórios (Streams)
    public List<Produto> proximosAVencer(int dias) {
        var limite = LocalDate.now().plusDays(dias);
        return produtos.stream()
                .filter(p -> p.getDataValidade()!=null && !p.getDataValidade().isBefore(LocalDate.now()) && !p.getDataValidade().isAfter(limite))
                .sorted(Comparator.comparing(Produto::getDataValidade))
                .toList();
    }

    public List<Produto> estoqueBaixo(int limite) {
        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() < limite)
                .sorted(Comparator.comparingInt(Produto::getQuantidadeEstoque))
                .toList();
    }

    public Map<String, Double> margemMediaPorCategoria() {
        // margem = (precoVenda - precoCompra) / precoCompra
        return produtos.stream()
                .filter(p -> p.getPrecoCompra()!=null && p.getPrecoCompra().compareTo(BigDecimal.ZERO) > 0
                        && p.getPrecoVenda()!=null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria()!=null? p.getCategoria().getNome() : "Sem Categoria",
                        Collectors.averagingDouble(p ->
                                p.getPrecoVenda().subtract(p.getPrecoCompra())
                                        .divide(p.getPrecoCompra(), 4, java.math.RoundingMode.HALF_UP)
                                        .doubleValue())
                ));
    }

    public List<Produto> listarPorSetor(String setor) {
        if (setor == null || setor.isBlank()) return List.of();
        String alvo = setor.trim();

        return produtos.stream()
                .filter(p -> p.getCategoria() != null
                        && p.getCategoria().getSetor() != null
                        && p.getCategoria().getSetor().trim().equalsIgnoreCase(alvo))
                .sorted(java.util.Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }


    // Validações
    private void validar(Produto p, boolean verificarCodigoUnico) {
        if (p.getCodigo()==null || !CODIGO_8.matcher(p.getCodigo()).matches())
            throw new IllegalArgumentException("Código deve ter 8 caracteres alfanuméricos.");

        if (verificarCodigoUnico && consultar(p.getCodigo()).isPresent())
            throw new IllegalArgumentException("Código já existente.");

        if (p.getNome()==null || p.getNome().trim().length() < 2)
            throw new IllegalArgumentException("Nome obrigatório (mínimo 2 caracteres).");

        if (p.getDataFabricacao()!=null && p.getDataFabricacao().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de fabricação não pode ser futura.");

        if (p.getDataValidade()!=null && p.getDataFabricacao()!=null
                && p.getDataValidade().isBefore(p.getDataFabricacao()))
            throw new IllegalArgumentException("Validade não pode ser anterior à fabricação.");

        if (p.getPrecoCompra()==null || p.getPrecoCompra().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço de compra deve ser positivo.");

        if (p.getPrecoVenda()==null || p.getPrecoVenda().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço de venda deve ser positivo.");

        if (p.getPrecoVenda().compareTo(p.getPrecoCompra()) <= 0)
            throw new IllegalArgumentException("Preço de venda deve ser maior que o de compra.");

        if (p.getQuantidadeEstoque() < 0)
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
    }

    // DTO leve para exibição (Java 17+)
    public static record NomePorSetor(String setor, String nome) {}


}
