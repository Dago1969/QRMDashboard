param(
    [string]$SourcePath = 'C:\Users\FM.Tripodi\Desktop\gi_db_comuni.sql',
    [string]$TargetPath = 'D:\workspace\QTM\QTMDashboard\src\main\resources\data.sql'
)

function Split-SqlValues([string]$text) {
    $values = New-Object System.Collections.Generic.List[string]
    $current = New-Object System.Text.StringBuilder
    $inQuote = $false
    $escape = $false

    foreach ($char in $text.ToCharArray()) {
        if ($escape) {
            [void]$current.Append($char)
            $escape = $false
            continue
        }

        if ($inQuote -and $char -eq '\') {
            [void]$current.Append($char)
            $escape = $true
            continue
        }

        if ($char -eq "'") {
            $inQuote = -not $inQuote
            [void]$current.Append($char)
            continue
        }

        if (-not $inQuote -and $char -eq ',') {
            $values.Add($current.ToString().Trim())
            $null = $current.Clear()
            continue
        }

        [void]$current.Append($char)
    }

    $values.Add($current.ToString().Trim())
    return $values
}

function Get-Key([string]$token) {
    if ($null -eq $token -or $token -eq 'NULL') {
        return $null
    }

    if ($token.StartsWith("'") -and $token.EndsWith("'")) {
        return $token.Substring(1, $token.Length - 2)
    }

    return $token
}

function Get-InsertValues([string]$normalizedLine) {
    $start = $normalizedLine.IndexOf('VALUES (')
    if ($start -lt 0) {
        return $null
    }

    return $normalizedLine.Substring($start + 8, $normalizedLine.Length - $start - 10)
}

if (-not (Test-Path $SourcePath)) {
    throw "File sorgente non trovato: $SourcePath"
}

$countryRows = New-Object System.Collections.Generic.List[object]
$regionRows = New-Object System.Collections.Generic.List[object]
$provinceRows = New-Object System.Collections.Generic.List[object]
$cityRows = New-Object System.Collections.Generic.List[object]
$countryIdByCode = @{}
$regionIdByCode = @{}
$provinceIdByCode = @{}

Get-Content -Path $SourcePath | ForEach-Object {
    $normalizedLine = $_.Trim().Replace([string][char]96, '')

    if ($normalizedLine.StartsWith('INSERT INTO country VALUES (')) {
        $parts = Split-SqlValues (Get-InsertValues $normalizedLine)
        $id = $countryRows.Count + 1
        $countryRows.Add([PSCustomObject]@{
            Id = $id
            CountryCode = $parts[0]
            BelfioreCode = $parts[1]
            Name = $parts[2]
            NationalityName = $parts[3]
        })
        $countryIdByCode[(Get-Key $parts[0])] = $id
        return
    }

    if ($normalizedLine.StartsWith('INSERT INTO region VALUES (')) {
        $parts = Split-SqlValues (Get-InsertValues $normalizedLine)
        $id = $regionRows.Count + 1
        $regionRows.Add([PSCustomObject]@{
            Id = $id
            GeographicArea = $parts[0]
            RegionCode = $parts[1]
            Name = $parts[2]
            RegionType = $parts[3]
            ProvinceCount = $parts[4]
            CityCount = $parts[5]
            AreaSquareKm = $parts[6]
        })
        $regionIdByCode[(Get-Key $parts[1])] = $id
        return
    }

    if ($normalizedLine.StartsWith('INSERT INTO province VALUES (')) {
        $parts = Split-SqlValues (Get-InsertValues $normalizedLine)
        $id = $provinceRows.Count + 1
        $provinceRows.Add([PSCustomObject]@{
            Id = $id
            RegionCode = $parts[0]
            ProvinceCode = $parts[1]
            Name = $parts[2]
            ProvinceType = $parts[3]
            CityCount = $parts[4]
            AreaSquareKm = $parts[5]
            SupraMunicipalCode = $parts[6]
        })
        $provinceIdByCode[(Get-Key $parts[1])] = $id
        return
    }

    if ($normalizedLine.StartsWith('INSERT INTO city VALUES (')) {
        $parts = Split-SqlValues (Get-InsertValues $normalizedLine)
        $id = $cityRows.Count + 1
        $cityRows.Add([PSCustomObject]@{
            Id = $id
            ProvinceCode = $parts[0]
            IstatCode = $parts[1]
            ItalianAlternativeName = $parts[2]
            Name = $parts[3]
            AlternativeName = $parts[4]
            CapitalFlag = $parts[5]
            BelfioreCode = $parts[6]
            Latitude = $parts[7]
            Longitude = $parts[8]
            AreaSquareKm = $parts[9]
            SupraMunicipalCode = $parts[10]
        })
    }
}

$italyId = $countryIdByCode['IT']
if (-not $italyId) {
    throw 'Country code IT non trovato nel file sorgente.'
}

$output = New-Object System.Collections.Generic.List[string]
$output.Add('-- Seed geografico generato da gi_db_comuni.sql')
$output.Add('-- Contiene tutte le nazioni, le regioni italiane, le province e i comuni del dataset.')
$output.Add('SET FOREIGN_KEY_CHECKS = 0;')
$output.Add('DELETE FROM city;')
$output.Add('DELETE FROM province;')
$output.Add('DELETE FROM region;')
$output.Add('DELETE FROM country;')
$output.Add('SET FOREIGN_KEY_CHECKS = 1;')
$output.Add('')

foreach ($row in $countryRows) {
    $output.Add("INSERT INTO country (id, sigla_nazione, codice_belfiore, name, denominazione_cittadinanza) VALUES ($($row.Id), $($row.CountryCode), $($row.BelfioreCode), $($row.Name), $($row.NationalityName));")
}

$output.Add('')

foreach ($row in $regionRows) {
    $output.Add("INSERT INTO region (id, country_id, ripartizione_geografica, codice_regione, name, tipologia_regione, numero_province, numero_comuni, superficie_kmq) VALUES ($($row.Id), $italyId, $($row.GeographicArea), $($row.RegionCode), $($row.Name), $($row.RegionType), $($row.ProvinceCount), $($row.CityCount), $($row.AreaSquareKm));")
}

$output.Add('')

foreach ($row in $provinceRows) {
    $regionId = $regionIdByCode[(Get-Key $row.RegionCode)]
    if (-not $regionId) {
        throw "Regione non trovata per la provincia $($row.ProvinceCode)"
    }

    $output.Add("INSERT INTO province (id, region_id, sigla_provincia, name, tipologia_provincia, numero_comuni, superficie_kmq, codice_sovracomunale) VALUES ($($row.Id), $regionId, $($row.ProvinceCode), $($row.Name), $($row.ProvinceType), $($row.CityCount), $($row.AreaSquareKm), $($row.SupraMunicipalCode));")
}

$output.Add('')

foreach ($row in $cityRows) {
    $provinceId = $provinceIdByCode[(Get-Key $row.ProvinceCode)]
    if (-not $provinceId) {
        throw "Provincia non trovata per il comune $($row.IstatCode)"
    }

    $output.Add("INSERT INTO city (id, province_id, codice_istat, denominazione_ita_altra, name, cap, denominazione_altra, flag_capoluogo, codice_belfiore, lat, lon, superficie_kmq, codice_sovracomunale) VALUES ($($row.Id), $provinceId, $($row.IstatCode), $($row.ItalianAlternativeName), $($row.Name), '', $($row.AlternativeName), $($row.CapitalFlag), $($row.BelfioreCode), $($row.Latitude), $($row.Longitude), $($row.AreaSquareKm), $($row.SupraMunicipalCode));")
}

Set-Content -Path $TargetPath -Value $output -Encoding UTF8
Write-Output ("Generated data.sql with {0} countries, {1} regions, {2} provinces, {3} cities" -f $countryRows.Count, $regionRows.Count, $provinceRows.Count, $cityRows.Count)