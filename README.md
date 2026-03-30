🚗 Fipe Search API
API escalável para consulta de preços de veículos, desenvolvida para demonstrar a implementação de uma camada de Cache Distribuído utilizando Redis e persistência de dados com PostgreSQL.
🛠 Tecnologias Utilizadas
 * Java 21 (LTS)
 * Spring Boot 3.4.3
 * Spring Data Redis (Gestão de Cache)
 * Spring Data JPA (Persistência de dados)
 * PostgreSQL (Base de dados relacional)
 * Docker & Docker Compose (Orquestração de infraestrutura)
 * Maven (Gestão de dependências)
💡 Conceito do Projeto
O foco principal deste projeto é a arquitetura de performance. Em vez de depender de chamadas constantes a serviços externos, a aplicação utiliza uma estratégia de cache para otimizar o tempo de resposta:
 * Simulação de Dados: Nesta versão, os dados dos veículos são gerados internamente (Mock) para garantir a estabilidade do ambiente de testes.
 * Cache Hit/Miss: Na primeira consulta, o sistema procura o dado na base de dados/mock e armazena-o no Redis.
 * Performance: Consultas subsequentes são servidas diretamente pela memória (Redis), reduzindo a latência para quase zero.
 * Warm-up: O sistema possui um mecanismo de carga inicial (Warm-up) que popula o cache com "hot keys" assim que a aplicação inicia.
🚀 Como Executar
Pré-requisitos
 * Docker e Docker Compose instalados.
 * JDK 21 (caso queira compilar localmente).
Passos para Instalação
# 1. Clonar o repositório
git clone [https://github.com/jeanvitorvieira/fipe-search.git](https://github.com/jeanvitorvieira/fipe-search.git)

# 2. Aceder à pasta do projeto
cd fipe-search

# 3. Compilar o executável (JAR)
./mvnw clean package -DskipTests

# 4. Subir os containers (Aplicação, Redis, Postgres)
cd docker
docker compose up -d --build


🔌 Endpoints Disponíveis
A API utiliza dois parâmetros principais para a localização do veículo: modeloId e anoModelo.
1. Consultar Veículo
Retorna os detalhes do preço. Se o dado já estiver no cache, a resposta será instantânea.
 * Método: GET
 * URL: /fipe/{modeloId}/{anoModelo}
 * Exemplo: curl -i http://localhost:8081/fipe/1/2023
2. Invalidar Cache
Remove manualmente o registo do Redis, forçando uma nova consulta na base de dados na próxima chamada.
 * Método: DELETE
 * URL: /fipe/{modeloId}/{anoModelo}/cache
 * Exemplo: curl -i -X DELETE http://localhost:8081/fipe/1/2023/cache
Desenvolvido por Jean Vitor Vieira como estudo de arquitetura Spring Boot e Cache Distribuído.
