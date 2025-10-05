Cadastro de Produtos (JavaFX + Maven + CSV)
Aplicação JavaFX para cadastro/consulta/listagem de produtos, persistindo em CSV e com relatórios usando Stream API.

*Requisitos

- JDK 17
- Não é necessário instalar Maven: o projeto inclui Maven Wrapper (mvnw / mvnw.cmd)

*Executar

Opção A) Terminal (Maven Wrapper)
- mvnw.cmd javafx:run

Opção B) IntelliJ IDEA (sem SDK do JavaFX local)
1 - File ▸ Open… e selecione o pom.xml (importar como Maven).
2 - Build, Execution, Deployment ▸ Build Tools ▸ Maven ▸ Runner ▸ marque Delegate IDE build/run actions to Mave
3 - Rode pelo RUN do IntelliJ ou crie uma Run Configuration Maven: 
  Run ▸ Edit Configurations… ▸ + ▸ Maven
  Working directory: pasta do projeto
  Command line: javafx:run
  JRE: JDK 17

 *Estrutura
  
src/
  main/
    java/br/com/seuapp/...
    resources/
      view/MainView.fxml
      data/produtos.csv
pom.xml

*CSV

- Carregado ao iniciar; salvo após cada operação.
- Separador: ponto-e-vírgula ;

*Funcionalidades

- Cadastrar / Consultar / Excluir / Listar
- Listar por Setor (digite o setor e liste apenas os produtos daquele setor)
- Relatórios (Stream API)
- Produtos próximos do vencimento (≤ 60 dias)
- Produtos com estoque baixo (< 10)
- Margem média por categoria: (precoVenda − precoCompra) / precoCompra




