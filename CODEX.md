# SAD Evolui — Documento de Desenvolvimento

## 1. Visão geral

O SAD Evolui é um Sistema de Apoio à Decisão para análise de feedbacks e priorização de melhorias em aplicativos.

O sistema permite que gestores cadastrem projetos, aplicativos e feedbacks de usuários. A partir dessas informações, são realizadas análises para identificar sentimentos, problemas recorrentes, padrões e oportunidades de melhoria.

Os resultados das análises são utilizados como apoio à tomada de decisão, permitindo priorizar melhorias por meio do método multicritério SAW (Simple Additive Weighting).

O objetivo é transformar grandes volumes de feedbacks em informações organizadas e úteis para decisões sobre a evolução de aplicativos.

## 2. Arquitetura e tecnologias

### Backend
- Java 17
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Spring Security
- JWT
- Flyway
- Jakarta Validation
- Lombok
- Springdoc OpenAPI
- JUnit 5
- Mockito

Arquitetura: monolítica modular.

Pacote base: `com.sadevolui`.

### Frontend
- React
- TypeScript
- Vite
- Axios
- React Router
- TanStack Query
- ESLint

### Banco de dados
- PostgreSQL
- Docker Compose
- Flyway para versionamento das migrations.

## 3. Organização do sistema

A organização será multi, permitindo o gerenciamento de diferentes projetos e aplicativos.

O modelo de análise será híbrido, combinando processamento automático dos feedbacks com avaliações e intervenções do gestor.

O controle dos usuários será simples, sem introduzir níveis complexos de permissões nesta primeira versão.

## 4. Módulos funcionais

### Autenticação e usuários
- Cadastro de usuários.
- Login.
- Autenticação JWT.
- Proteção das rotas.
- Identificação do usuário responsável pelas operações.

### Projetos
- Cadastro de projetos.
- Listagem.
- Edição.
- Exclusão.
- Consulta dos aplicativos vinculados.

### Aplicativos
- Cadastro de aplicativos.
- Associação a projetos.
- Nome, descrição e informações básicas.
- Listagem e gerenciamento.

### Feedbacks
- Cadastro manual de feedbacks.
- Associação ao aplicativo correspondente.
- Registro de nota, comentário e data.
- Listagem e filtros.
- Edição e exclusão.
- Preparação para futura importação de fontes externas.

### Categorias
- Cadastro e gerenciamento de categorias.
- Associação dos feedbacks às categorias.
- Categorias iniciais: desempenho, autenticação, interface, navegação, estabilidade, funcionalidades e outros.

### Análises

Implementar três funções principais:

#### Classificação
Identificar o sentimento dos feedbacks:
- Positivo.
- Negativo.
- Neutro.

Permitir também a classificação temática dos feedbacks.

A classificação deverá utilizar critérios documentados e exemplos previamente classificados.

Não considerar resultados de modelos não treinados como classificações confiáveis.

#### Agrupamento
Identificar feedbacks semelhantes e reunir comentários relacionados ao mesmo problema.

Exemplos:
- Problemas de login.
- Lentidão.
- Falhas de funcionamento.
- Dificuldades de navegação.

Utilizar uma técnica de agrupamento adequada aos dados disponíveis.

Avaliar a coerência dos grupos encontrados e permitir sua identificação e interpretação pelo gestor.

#### Sumarização
Implementar estatística descritiva, distribuição de frequências, agregação e indicadores.

Indicadores:
- Quantidade total de feedbacks.
- Média das avaliações.
- Mediana.
- Moda.
- Nota mínima e máxima.
- Desvio padrão.
- Quantidade de feedbacks positivos.
- Quantidade de feedbacks negativos.
- Quantidade de feedbacks neutros.
- Percentual de cada sentimento.
- Frequência de feedbacks por categoria.
- Problemas mais recorrentes.
- Evolução dos feedbacks por período.

Permitir agregações por:
- Projeto.
- Aplicativo.
- Categoria.
- Sentimento.
- Período.
- Grupo de problemas.

Apresentar os resultados por meio de gráficos, tabelas e indicadores.

Definir corretamente o tratamento de dados ausentes, notas inválidas e amostras sem observações.

### Melhorias
- Cadastro de oportunidades de melhoria.
- Associação com problemas e feedbacks identificados.
- Descrição da melhoria proposta.
- Registro de impacto esperado.
- Registro de urgência.
- Registro de frequência.
- Estimativa de esforço.
- Acompanhamento do status.

### Decisões
Implementar o método SAW para calcular a prioridade das melhorias.

Critérios:
- Impacto.
- Urgência.
- Frequência.
- Esforço de implementação.

Os três primeiros são critérios de benefício e o esforço é um critério de custo.

Utilizar normalização adequada:
- Benefício: valor da alternativa dividido pelo maior valor observado.
- Custo: menor valor observado dividido pelo valor da alternativa.

Calcular a pontuação final utilizando os pesos definidos pelo gestor.

Validar que a soma dos pesos seja igual a 1 ou 100%, conforme a representação adotada.

Apresentar:
- Critérios utilizados.
- Pesos.
- Valores atribuídos.
- Valores normalizados.
- Pontuação final.
- Ranking das melhorias.

Armazenar o histórico das avaliações para permitir consultar decisões anteriores.

### Dashboard
Apresentar:
- Total de projetos.
- Total de aplicativos.
- Total de feedbacks.
- Média das avaliações.
- Distribuição de sentimentos.
- Feedbacks por categoria.
- Problemas mais recorrentes.
- Evolução dos feedbacks.
- Melhorias cadastradas.
- Ranking de prioridades.

Permitir filtrar os indicadores por projeto, aplicativo e período.

## 5. Fundamentação das variáveis

Nenhuma variável deverá ser utilizada sem definição e justificativa.

### Impacto
Representa a gravidade do problema e o prejuízo causado à experiência dos usuários.

### Urgência
Representa a necessidade de resolver o problema em determinado período.

### Frequência
Representa a recorrência do problema nos feedbacks.

### Esforço
Representa a complexidade e os recursos necessários para implementar a melhoria.

Utilizar inicialmente uma escala de 1 a 5, com descrições objetivas para cada nível.

Não atribuir valores arbitrários. Registrar a origem da avaliação e permitir intervenção do gestor.

Os pesos do SAW também deverão possuir justificativa e ser configuráveis.

## 6. Regras importantes

- Não inventar resultados de mineração de dados.
- Não apresentar modelos demonstrativos como modelos validados.
- Separar dados de treinamento e avaliação.
- Documentar a origem das variáveis.
- Não confundir frequência de comentários com quantidade de usuários afetados.
- Evitar duplicidade de feedbacks.
- Preservar o histórico das análises e decisões.
- Permitir intervenção do gestor nos resultados automáticos.
- Identificar claramente resultados automáticos e avaliações manuais.
- Utilizar dados fictícios apenas quando identificados como demonstração.
- Garantir que os indicadores sejam calculados a partir dos dados reais armazenados.
- Não criar microsserviços.
- Não introduzir tecnologias desnecessárias.

## 7. Qualidade e testes

Implementar testes para:
- Serviços.
- Repositórios quando necessário.
- Controllers.
- Autenticação.
- Validações.
- Cálculos estatísticos.
- Classificação.
- Agrupamento.
- Normalização SAW.
- Cálculo das pontuações.
- Ordenação do ranking.

Validar os fluxos principais de integração entre frontend, backend e PostgreSQL.

## 8. Diretrizes de desenvolvimento

Antes de modificar qualquer arquivo:
1. Inspecionar o projeto existente.
2. Identificar o que já foi implementado.
3. Identificar funcionalidades incompletas.
4. Preservar implementações válidas.
5. Evitar duplicação de código.

Manter código simples, organizado e modular.

Evitar comentários desnecessários.

Não criar abstrações sem necessidade.

Não implementar funcionalidades fora do escopo sem justificativa.

## 9. Resultado esperado

Entregar uma aplicação funcional na qual seja possível:

1. Criar uma conta e acessar o sistema.
2. Cadastrar projetos e aplicativos.
3. Registrar feedbacks.
4. Executar análises de classificação e agrupamento.
5. Consultar sumarizações estatísticas.
6. Identificar oportunidades de melhoria.
7. Configurar critérios e pesos do SAW.
8. Calcular e visualizar o ranking de prioridades.
9. Consultar os resultados no dashboard.

O sistema deverá estar integrado, documentado e testado.

Toda funcionalidade implementada deverá possuir um fluxo real de funcionamento, sem botões decorativos, dados estáticos apresentados como resultados reais ou operações simuladas sem identificação.