# Relatório POC: Resultados Reais dos Cenários FIPE

Este documento captura a execução simulando alta volumetria e valida os cenários esperados.

## 1. Tabela de Performance (K6 - 200 VUs por 20s)

| Cenário | Volumetria (Reqs) | RPS | Cache MISS | Erros (Timeouts/Fails) | Latência p90 | Latência p95 |
|---------|-------------------|-----|------------|-------------------------|--------------|--------------|
| Direct DB (Gargalo) | 24854 | 1239 | - | 738 | 279.52ms | 374.13ms |
| Cache com TTL (Stampede) | 26711 | 1331 | 800 | 667 | 261.04ms | 355.85ms |
| Cache com Warming (Ideal) | 35546 | 1774 | 5 | 0 | 208.9ms | 256.16ms |

**Análise Final**:
- **Cenário 1 (DB Direto):** O gargalo do Pool de Conexões do banco limitou o throughput bruto.
- **Cenário 2 (Cache TTL):** Houve grande melhoria de latência geral, porém picos de latência e concorrência sobrecarregando o sistema periodicamente ("Cache Stampede").
- **Cenário 3 (Cache com Warmup):** Desempenho máximo, mantendo p95 muito baixa sem onerar o banco, entregando zero erros.

## 2. Evidência do Fenômeno "Cache Stampede" (Cenário 2)

O Cache Stampede acontece porque, ao expirar uma chave em alta concorrência `N` VUs, dezenas de instâncias percebem o *Cache MISS* e fazem a consulta simultânea ao banco de dados no exato momento, re-saturando o pool do HikariCP desnecessariamente e gerando interrupções no tempo de reposta da API que seriam invisíveis na média bruta.

**Comportamento capturado nos logs do Spring Boot durante o teste de carga:**
- Aos segundos `xx:xx:04:16:56`, exatas **41 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=1, ano=2023` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:56`, exatas **39 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=2, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:56`, exatas **34 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=3, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:56`, exatas **38 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=4, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:56`, exatas **48 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=5, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:57`, exatas **18 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=1, ano=2023` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:57`, exatas **25 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=2, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:57`, exatas **30 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=3, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:57`, exatas **21 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=4, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:57`, exatas **22 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=5, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:58`, exatas **6 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=1, ano=2023` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:58`, exatas **37 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=2, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:58`, exatas **4 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=3, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:58`, exatas **3 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=4, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:58`, exatas **10 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=5, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:16:59`, exatas **13 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=2, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:04`, exatas **29 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=1, ano=2023` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:04`, exatas **13 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=3, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:04`, exatas **55 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=4, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:04`, exatas **44 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=5, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:05`, exatas **16 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=1, ano=2023` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:05`, exatas **64 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=2, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:10`, exatas **43 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=4, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:10`, exatas **54 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=5, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:11`, exatas **20 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=1, ano=2023` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:11`, exatas **39 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=3, ano=2021` e sobrecarregaram o banco.
- Aos segundos `xx:xx:04:17:12`, exatas **33 threads** tiveram `Cache MISS` simultaneamente para o mesmo registro `modelo=2, ano=2021` e sobrecarregaram o banco.
