# SAD Evolui — Design System

## Direção

Uma interface analítica direta e confiável, construída sobre azul cobalto para comunicar tecnologia, clareza e decisões fundamentadas. As páginas usam um fundo azul acinzentado sólido; cards e painéis têm branco puro e contorno definido para separar claramente as superfícies. Verde, âmbar e vermelho aparecem em status com significado próprio. Os gráficos são vetoriais e os ícones são lineares. A marca usa um símbolo geométrico de atividade, criado para o SAD Evolui.

O dashboard inicial consulta os indicadores persistidos no backend. Estados vazios são apresentados quando ainda não há registros; nenhuma métrica de exemplo é tratada como dado real.

## Cores

| Token | Valor | Uso |
| --- | --- | --- |
| `--color-primary` | `#2456D9` | Ações principais, seleção e gráficos |
| `--color-primary-strong` | `#1947B8` | Hover, marca e chamadas de atenção |
| `--color-primary-soft` | `#DCE7FF` | Cabeçalhos de tabela e realces informativos |
| `--color-ink` | `#152D4E` | Títulos e texto principal |
| `--color-muted` | `#52677F` | Texto secundário com contraste legível |
| `--color-subtle` | `#62768A` | Rótulos e metadados |
| `--color-background` | `#DDE7F6` | Fundo geral, distinguível das superfícies |
| `--color-surface` | `#FFFFFF` | Cards e cabeçalhos em branco sólido |
| `--color-border` | `#AFC0D7` | Contornos visíveis entre cards e fundo |
| `--color-success` | `#167A50` | Variações e status positivos |
| `--color-warning` | `#A85B00` | Atenção e prioridade média |
| `--color-danger` | `#B83B36` | Sinais negativos e prioridade alta |

Use o azul como cor primária. Cards permanecem em branco puro sobre o fundo azul acinzentado, com bordas azuis acinzentadas visíveis e sombra sólida. Ações selecionadas e destaques informativos podem usar fundos azuis sólidos com texto branco. Status usam preenchimentos sólidos de verde, âmbar ou vermelho com texto branco. Não use transparência, opacidade ou tons quase iguais ao fundo em cards e painéis.

## Tipografia

- **Manrope** para títulos e valores de métricas; pesos 500, 600, 700 e 800.
- **DM Sans** para navegação, corpo, controles e dados tabulares; pesos 400, 500, 600 e 700.
- Fallbacks locais de sistema garantem legibilidade se as fontes web não carregarem.
- Títulos usam espaçamento de letras levemente negativo. Rótulos curtos podem usar caixa alta e tracking ampliado.

## Espaçamento

Escala base de 4 px: `--space-1` (4), `--space-2` (8), `--space-3` (12), `--space-4` (16), `--space-5` (20), `--space-6` (24), `--space-8` (32) e `--space-10` (40). Cards usam 14–22 px de padding conforme densidade e viewport; grades usam 10–22 px de intervalo.

## Bordas e sombras

- `--radius-sm` (8 px): botões, badges e controles compactos.
- `--radius-md` (12 px): cards e painéis.
- `--radius-lg` (16 px) e `--radius-xl` (20 px): superfícies maiores e futuras seções de destaque.
- `--shadow-card`: sombra sólida curta que reforça a separação entre cards e fundo.
- `--shadow-float`: elevação sólida reservada para navegação móvel e camadas flutuantes.
- Contornos usam neutros claros; evite bordas escuras e sombras profundas.

## Componentes

- **Navegação lateral:** fundo branco, ícones lineares, seleção azul sólida com texto branco e espaço próprio para identidade do workspace.
- **Header:** breadcrumb da página e controle de navegação móvel em uma faixa compacta e fixa.
- **Botão primário:** azul sólido com texto branco e hover azul escuro. **Botão secundário:** branco sólido com contorno azul; no hover recebe azul sólido e texto branco. O foco por teclado usa contorno azul visível.
- **Cards de métrica:** valor em destaque, contexto curto e navegação para os registros relacionados.
- **Painéis analíticos:** reservar para indicadores calculados a partir dos dados armazenados, com títulos, descrições e legendas explícitas.
- **Badges:** preenchimento sólido de status com texto branco e rótulo explícito; nunca depender apenas da cor para comunicar prioridade.
- **Tabela:** cabeçalho de baixo contraste, divisórias horizontais, rótulos secundários e rolagem horizontal em telas pequenas.
- **Ícones:** Lucide React em traço fino (geralmente 1,8 px), usados para identificar ações e categorias.

## Layout e responsividade

- Desktop: sidebar fixa de 252 px; área principal fluida, com cabeçalho fixo e largura máxima de conteúdo.
- Até 1120 px: sidebar compacta e cards reorganizados em duas colunas.
- Até 850 px: navegação lateral passa a um drawer com camada de fundo e controle de fechar.
- Até 650 px: conteúdo em uma coluna, métricas em grade 2×2 e tabela com rolagem horizontal.
- Alvos interativos devem manter área confortável, foco visível e estado hover sem depender de cor isolada.
- Respeitar `prefers-reduced-motion` em transições e animações.
