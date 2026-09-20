# Roteiro de testes da API - Controle Financeiro
# Rode com o backend no ar:
#   powershell -ExecutionPolicy Bypass -File .\testar-api.ps1
# Compativel com Windows PowerShell 5.1 e PowerShell 7+.

$base   = "http://localhost:8080/api"
$falhas = 0
$total  = 0

function Invocar($metodo, $url, $corpo) {
    try {
        $p = @{ Uri = $url; Method = $metodo; UseBasicParsing = $true }
        if ($null -ne $corpo) {
            $p.Body        = ($corpo | ConvertTo-Json -Compress)
            $p.ContentType = "application/json; charset=utf-8"
        }
        $r = Invoke-WebRequest @p
        return @{ status = [int]$r.StatusCode; texto = $r.Content; headers = $r.Headers }
    }
    catch {
        $texto = $null
        try { $texto = $_.ErrorDetails.Message } catch { }
        $resp = $null
        try { $resp = $_.Exception.Response } catch { }
        if ($null -eq $resp) {
            return @{ status = -1; texto = $_.Exception.Message; headers = $null }
        }
        $status = 0
        try { $status = [int]$resp.StatusCode } catch { }
        if (-not $texto) {
            try {
                $sr    = New-Object System.IO.StreamReader($resp.GetResponseStream())
                $texto = $sr.ReadToEnd()
                $sr.Close()
            } catch { $texto = "" }
        }
        return @{ status = $status; texto = $texto; headers = $null }
    }
}

function Teste($nome, $metodo, $url, $corpo, $esperado) {
    $script:total++
    $r = Invocar $metodo $url $corpo
    if ($r.status -eq $esperado) {
        Write-Host ("  OK    [{0}] {1}" -f $r.status, $nome) -ForegroundColor Green
    }
    elseif ($r.status -eq -1) {
        $script:falhas++
        Write-Host ("  ERRO  [sem resposta] {0}" -f $nome) -ForegroundColor Magenta
        Write-Host ("        {0}" -f $r.texto) -ForegroundColor DarkGray
    }
    else {
        $script:falhas++
        Write-Host ("  FALHA [esperado {0}, veio {1}] {2}" -f $esperado, $r.status, $nome) -ForegroundColor Red
        if ($r.texto -and $r.texto.Length -lt 300) {
            Write-Host ("        {0}" -f $r.texto) -ForegroundColor DarkGray
        }
    }
}

Write-Host "`nVerificando o backend em $base ..." -ForegroundColor Cyan
$ping = Invocar GET "$base/transacoes" $null
if ($ping.status -eq -1) {
    Write-Host "`nNao consegui falar com o backend em localhost:8080." -ForegroundColor Red
    Write-Host "Suba antes:  cd backend ; .\mvnw spring-boot:run`n" -ForegroundColor Yellow
    exit 1
}
Write-Host "Backend respondeu. Iniciando testes.`n" -ForegroundColor Green

$mesAtual    = (Get-Date).ToString("yyyy-MM")
$mesAnterior = (Get-Date).AddMonths(-1).ToString("yyyy-MM")
$hoje        = (Get-Date).ToString("yyyy-MM-dd")

Write-Host "=== CAMINHO FELIZ ===" -ForegroundColor Cyan
Teste "listar mes atual"     GET "$base/transacoes?mes=$mesAtual"    $null 200
Teste "listar mes anterior"  GET "$base/transacoes?mes=$mesAnterior" $null 200
Teste "listar sem parametro" GET "$base/transacoes"                  $null 200
Teste "resumo do mes"        GET "$base/resumo?mes=$mesAtual"        $null 200
Teste "filtro por tipo"      GET "$base/transacoes?mes=$mesAtual&tipo=ENTRADA"          $null 200
Teste "filtro por categoria" GET "$base/transacoes?mes=$mesAtual&categoria=ALIMENTACAO" $null 200

Write-Host "`n=== FRONTEIRA: MES ===" -ForegroundColor Cyan
Teste "mes sem dados"       GET "$base/transacoes?mes=2019-01" $null 200
Teste "resumo de mes vazio" GET "$base/resumo?mes=2019-01"     $null 200
Teste "fevereiro bissexto"  GET "$base/transacoes?mes=2024-02" $null 200
Teste "dezembro"            GET "$base/transacoes?mes=2026-12" $null 200

Write-Host "`n=== FRONTEIRA: VALOR ===" -ForegroundColor Cyan
Teste "minimo 0.01"      POST "$base/transacoes" @{descricao="Centavo";valor=0.01;tipo="SAIDA";categoria="LAZER";data=$hoje} 201
Teste "zero"             POST "$base/transacoes" @{descricao="Zero";valor=0;tipo="SAIDA";categoria="LAZER";data=$hoje} 400
Teste "negativo"         POST "$base/transacoes" @{descricao="Negativo";valor=-10;tipo="SAIDA";categoria="LAZER";data=$hoje} 400
Teste "3 casas decimais" POST "$base/transacoes" @{descricao="Tres casas";valor=0.001;tipo="SAIDA";categoria="LAZER";data=$hoje} 400
Teste "valor gigante"    POST "$base/transacoes" @{descricao="Gigante";valor=99999999999999;tipo="SAIDA";categoria="LAZER";data=$hoje} 400

Write-Host "`n=== FRONTEIRA: TEXTO ===" -ForegroundColor Cyan
Teste "descricao 2 chars"   POST "$base/transacoes" @{descricao="ab";valor=10;tipo="SAIDA";categoria="LAZER";data=$hoje} 400
Teste "descricao 3 chars"   POST "$base/transacoes" @{descricao="abc";valor=10;tipo="SAIDA";categoria="LAZER";data=$hoje} 201
Teste "descricao 121 chars" POST "$base/transacoes" @{descricao=("x"*121);valor=10;tipo="SAIDA";categoria="LAZER";data=$hoje} 400
Teste "descricao em branco" POST "$base/transacoes" @{descricao="   ";valor=10;tipo="SAIDA";categoria="LAZER";data=$hoje} 400

Write-Host "`n=== REGRA DE NEGOCIO (categoria x tipo) ===" -ForegroundColor Cyan
Teste "SAIDA com categoria de ENTRADA" POST "$base/transacoes" @{descricao="Salario errado";valor=100;tipo="SAIDA";categoria="SALARIO";data=$hoje} 400
Teste "ENTRADA com categoria de SAIDA" POST "$base/transacoes" @{descricao="Comida errada";valor=100;tipo="ENTRADA";categoria="ALIMENTACAO";data=$hoje} 400

Write-Host "`n=== NAO ENCONTRADO ===" -ForegroundColor Cyan
Teste "GET id inexistente"    GET    "$base/transacoes/999999" $null 404
Teste "DELETE id inexistente" DELETE "$base/transacoes/999999" $null 404
Teste "PUT id inexistente"    PUT    "$base/transacoes/999999" @{descricao="Fantasma";valor=10;tipo="SAIDA";categoria="LAZER";data=$hoje} 404

Write-Host "`n=== ENTRADA MALFORMADA ===" -ForegroundColor Cyan
Teste "mes invalido"        GET  "$base/transacoes?mes=abc"       $null 400
Teste "mes 13"              GET  "$base/transacoes?mes=2026-13"   $null 400
Teste "tipo inexistente"    GET  "$base/transacoes?tipo=QUALQUER" $null 400
Teste "id nao numerico"     GET  "$base/transacoes/abc"           $null 400
Teste "enum invalido"       POST "$base/transacoes" @{descricao="Enum ruim";valor=10;tipo="ENTRADAX";categoria="LAZER";data=$hoje} 400
Teste "data formato errado" POST "$base/transacoes" @{descricao="Data ruim";valor=10;tipo="SAIDA";categoria="LAZER";data="31/02/2026"} 400
Teste "campos faltando"     POST "$base/transacoes" @{descricao="So isso"} 400

Write-Host "`n=== CICLO COMPLETO (CRUD) ===" -ForegroundColor Cyan
$total++
$novo = @{descricao="Teste ciclo completo";valor=123.45;tipo="SAIDA";categoria="TRANSPORTE";data=$hoje}
$r = Invocar POST "$base/transacoes" $novo
if ($r.status -eq 201) {
    $id = ($r.texto | ConvertFrom-Json).id
    Write-Host ("  OK    [201] criada com id {0}" -f $id) -ForegroundColor Green
    Teste "buscar a criada"     GET    "$base/transacoes/$id" $null 200
    Teste "atualizar"           PUT    "$base/transacoes/$id" @{descricao="Teste atualizado";valor=200;tipo="SAIDA";categoria="LAZER";data=$hoje} 200
    Teste "excluir"             DELETE "$base/transacoes/$id" $null 204
    Teste "buscar apos excluir" GET    "$base/transacoes/$id" $null 404
} else {
    $falhas++
    Write-Host ("  FALHA ao criar transacao do ciclo: {0}" -f $r.status) -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
if ($falhas -eq 0) {
    Write-Host ("TODOS OS {0} TESTES PASSARAM" -f $total) -ForegroundColor Green
} else {
    Write-Host ("{0} de {1} testes falharam" -f $falhas, $total) -ForegroundColor Red
    Write-Host "Falhas em FRONTEIRA: VALOR e ENTRADA MALFORMADA sao esperadas" -ForegroundColor Yellow
    Write-Host "antes das correcoes do relatorio." -ForegroundColor Yellow
}
Write-Host "========================================`n" -ForegroundColor Cyan
