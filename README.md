# Controle Financeiro

API REST + interface web para registrar entradas e saídas e acompanhar o saldo do mês.

Desafio técnico — Estágio em Desenvolvimento (HSP Software).

---

## Stack

**Backend** — Java 21, Spring Boot 4.1.1, Spring Data JPA, Bean Validation, H2 (arquivo), Maven
**Frontend** — React 19, Vite, CSS puro (sem bibliotecas de UI ou de requisição)

## Estrutura

```
controle-financeiro/
├── backend/          API REST (Spring Boot)
├── frontend/         Interface (React + Vite)
└── testar-api.ps1    Bateria de testes da API
```

Um repositório, dois projetos. Backend e frontend têm convenções e raízes próprias
(`pom.xml` e `package.json`, ambos com uma pasta `src/`), por isso pastas separadas;
mas são uma entrega só, com um histórico de commits contínuo.

---

## Como rodar

**Pré-requisitos:** Java 21+ e Node 18+

### 1. Backend

```bash
cd backend
./mvnw spring-boot:run        # Linux/macOS
.\mvnw spring-boot:run        # Windows
```

Sobe em `http://localhost:8080`.

Na primeira execução o banco é criado e populado com ~15 transações de exemplo,
distribuídas entre o mês atual e o anterior — a aplicação já abre com dados reais.

### 2. Frontend

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`.

### Inspecionar o banco

Console do H2 em `http://localhost:8080/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/financeiro` |
| Usuário | `sa` |
| Senha | *(vazia)* |

Para começar do zero: pare a aplicação, apague `backend/data/` e suba de novo.

---

## API

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/transacoes?mes=2026-09&tipo=&categoria=` | Lista do mês; filtros opcionais |
| `GET` | `/api/transacoes/{id}` | Busca uma transação |
| `POST` | `/api/transacoes` | Cria (201 + header `Location`) |
| `PUT` | `/api/transacoes/{id}` | Atualiza |
| `DELETE` | `/api/transacoes/{id}` | Remove (204) |
| `GET` | `/api/resumo?mes=2026-09` | Totais e saldo do mês |

`mes` é opcional em todas as rotas — omitido, assume o mês corrente.

**Transação**

```json
{
  "id": 1,
  "descricao": "Supermercado",
  "valor": 715.80,
  "tipo": "SAIDA",
  "categoria": "ALIMENTACAO",
  "categoriaLabel": "Alimentação",
  "data": "2026-09-09"
}
```

**Resumo**

```json
{
  "mes": "2026-09",
  "totalEntradas": 4442.30,
  "totalSaidas": 2548.55,
  "saldo": 1893.75,
  "porCategoria": [
    { "categoria": "ALIMENTACAO", "categoriaLabel": "Alimentação", "total": 715.80 }
  ]
}
```

**Erros** — todos seguem o mesmo formato:

```json
{ "mensagem": "Dados invalidos", "campos": { "valor": "deve ser maior que 0" } }
```

`campos` vem preenchido apenas em erros de validação; nos demais é `null`.

---

## Decisões técnicas

### Valor sempre positivo, direção no campo `tipo`

Guardar saídas como valores negativos permitiria somar tudo direto, mas espalha a
regra: o usuário teria que digitar `-50`, e "listar só as saídas" viraria `valor < 0`
— uma regra escondida dentro do dado. Com `tipo` explícito, a regra fica onde pode
ser validada e testada.

### `BigDecimal`, nunca `double`

`0.1 + 0.2` em ponto flutuante dá `0.30000000000000004`. Inaceitável para dinheiro.
A coluna é `NUMERIC(12,2)` e o DTO valida `@Digits(integer = 10, fraction = 2)`.

### Categoria carrega o tipo a que pertence

Cada constante do enum `Categoria` sabe se é de entrada ou de saída, e o service
valida a combinação. Isso impede lançar "Salário" como despesa — o que sujaria o
relatório por categoria. É a única regra de negócio real do sistema, e ela mora no
domínio, não no controller.

### Resumo calculado em memória

Os totais do mês são obtidos percorrendo as transações em Java, não por agregação
no banco. Com o volume esperado (dezenas de registros por mês) a diferença é
irrelevante, e o código fica legível. **Com volume maior, essa soma deveria ir para
uma query agregada**, para não trafegar registros que só serão somados.

### H2 em arquivo, não em memória

Em memória, os dados sumiriam a cada restart — quem avalia cadastraria algo,
reiniciaria e veria tela vazia. Em arquivo, persistem, sem exigir Docker ou
instalação de banco. Trocar por PostgreSQL é mudar o `application.properties`.

### Carga inicial via `CommandLineRunner`, não `data.sql`

Um `data.sql` roda a cada inicialização. Como o H2 é em arquivo, isso duplicaria os
dados de exemplo a cada restart. O `CommandLineRunner` verifica `count() > 0` antes
de inserir, e as transações passam pelo próprio modelo da aplicação.

### Proxy do Vite em vez de `@CrossOrigin`

CORS em desenvolvimento existe porque Vite (5173) e Spring (8080) são origens
diferentes — algo que não ocorre em produção. Em vez de afrouxar o backend com
`@CrossOrigin`, o Vite encaminha `/api` para o backend: o front usa caminhos
relativos, CORS deixa de existir, e o código escrito é o mesmo que funcionaria em
produção. Resolve a causa, não o sintoma.

### Camadas separadas

`Controller` → `Service` → `Repository`. O service lança exceções de domínio
(`TransacaoNaoEncontradaException`, `RegraNegocioException`) e não importa nada de
`org.springframework.http`. Quem traduz exceção em status HTTP é o
`@RestControllerAdvice`. O mesmo service atenderia uma CLI sem mudar uma linha.

### DTOs como `record`, nunca a entidade no JSON

Expor a entidade JPA faria do esquema do banco o contrato público da API: renomear
uma coluna quebraria o front. Os `record` do Java 21 dão imutabilidade sem
boilerplate.

### Front sem bibliotecas

Sem axios, react-query, Tailwind ou lib de UI. `fetch` nativo e CSS à mão. Menos
dependências para instalar e o código visível é o meu.

### Datas formatadas como texto

`new Date("2026-09-05")` é lido como UTC e, no fuso do Brasil, volta como dia 04.
A formatação é feita por manipulação de string, sem `Date`.

---

## Testes

`testar-api.ps1` é uma bateria de 36 verificações contra a API rodando:

```powershell
powershell -ExecutionPolicy Bypass -File .\testar-api.ps1
```

Cobre caminho feliz, fronteira de mês (mês sem dados, fevereiro bissexto, dezembro),
fronteira de valor (zero, negativo, R$ 0,01, casas decimais excedentes, estouro de
precisão), fronteira de texto (2, 3 e 121 caracteres, só espaços), a regra
categoria×tipo nos dois sentidos, 404 em todas as rotas por id, entrada malformada
e um ciclo CRUD completo.

**A bateria encontrou três defeitos reais**, todos corrigidos:

1. Valor com 3 casas decimais era aceito e a API respondia `0.001`, mas o banco
   gravava `0.00` — a resposta divergia do estado real. Corrigido com `@Digits`.
2. Valor acima da precisão da coluna devolvia 500. Mesmo `@Digits` resolve.
3. **Regressão introduzida por uma correção:** ao adicionar
   `@ExceptionHandler(Exception.class)` como rede de segurança, ele passou a
   capturar exceções que o Spring já tratava com 400, transformando-as em 500.
   Corrigido declarando os handlers específicos, que o Spring prioriza por serem
   mais específicos.

O terceiro item é a justificativa da bateria existir: a correção de um problema
criou outro, e o teste pegou antes da entrega.

---

## O que ficou de fora, e por quê

O prazo era de um dia. Priorizei o requisito central — entradas, saídas e saldo do
mês — entregue funcionando de ponta a ponta, em vez de várias funcionalidades pela
metade.

- **Testes automatizados (JUnit).** O maior débito. Optei pela bateria de testes de
  integração acima, que exercita a API de verdade e cabia no tempo. Num projeto real,
  os testes do service e dos controllers viriam primeiro.
- **Autenticação e multiusuário.** O escopo não pede, e exigiria modelar usuário,
  sessão e isolamento de dados.
- **Edição pela interface.** O `PUT` existe e está testado na API, mas a tela só
  cadastra, lista e exclui. Preferi deixar esses três polidos.
- **Paginação e busca textual.** O recorte mensal já limita o volume naturalmente.
- **Gráfico por categoria.** `/api/resumo` já devolve `porCategoria`; a tela ainda
  não consome esse dado.
- **Empacotamento em JAR único.** Seria possível embutir o build do front em
  `resources/static` e entregar um `java -jar`, mas não é avaliado e adicionava
  risco perto do prazo.
