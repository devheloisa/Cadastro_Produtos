package br.com.seuapp.model;

public class Categoria {
    private int id;
    private String nome;
    private String descricao;
    private String setor;

    public Categoria() {}

    public Categoria(int id, String nome, String descricao, String setor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.setor = setor;
    }

    // getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getSetor() { return setor; }

    // setters
    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setSetor(String setor) { this.setor = setor; }

    @Override
    public String toString() {
        return nome + " (" + setor + ")";
    }
}
