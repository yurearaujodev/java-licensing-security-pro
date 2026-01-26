# Gerenciador de Licença

Aplicação desktop desenvolvida em **Java 21** (puro, sem frameworks de gerenciamento de licença) para cadastro e gerenciamento de empresas, representantes, contatos e dados fiscais.  
O projeto segue o padrão **MVC** (Model, Controller, DAO, Service, Util, View) e utiliza **Swing** com telas MDI responsivas.

---

## 🚀 Funcionalidades já implementadas

- **Criptografia e segurança**
  - Criptografia RSA e AES em classes utilitárias.
  - Uso de **bcrypt** para dados de login.
  - MasterKey simplificada sem valores hardcoded.
  - Senha criptografada já no primeiro acesso.

- **Banco de dados**
  - Banco MySQL com **17 tabelas**.
  - Conexão robusta utilizando **HikariCP** com otimizações.
  - Configuração salva em `arquivo.properties`.

- **Tela de Empresa**
  - 7 abas: **Dados Principais, Endereço, Contato, Dados Fiscais, Representante Legal, Dados Bancários, Complementar**.
  - Tela inteligente:
    - Menu **Cliente** → exibe apenas 3 abas.
    - Menu **Fornecedora** → exibe todas as 7 abas.
  - Funcionalidades de **salvar, alterar e buscar** já implementadas.

- **Tela de Consulta**
  - Consulta de dados funcionando com filtros e buscas.

- **Interface gráfica**
  - **Swing** com telas MDI (`JDesktopPane` + `JInternalFrame`).
  - **FlatLaf** para look and feel moderno.
  - **MigLayout** para layout flexível e responsivo.
  - Classes **Factory** para criação de telas.

---

## 🛠️ Tecnologias utilizadas

- **Java 21**
  - Uso de **virtual threads** para operações de CRUD.
  - Novo `switch` do Java 21.
- **Swing** (MDI, InternalFrames, MigLayout, FlatLaf).
- **Banco de dados**: MySQL 8.4.0.
- **Gerenciador de conexões**: HikariCP.
- **Maven** para gerenciamento de dependências.
- **Bibliotecas externas**:
  - `caelum-stella` → validações de documentos.
  - `slf4j` → logging.
  - `bcrypt` → criptografia de senhas.
  - `mysql-connector-j 8.4.0` → conexão com MySQL.
  - `libphonenumber` → validação de telefone.
  - `json` → manipulação de dados JSON.

---

## 📂 Estrutura do projeto
src/ └── com.br.yat.gerenciador/ ├── model/ ├── controller/ ├── dao/ ├── service/ ├── util/ ├── validation/ └── view/
