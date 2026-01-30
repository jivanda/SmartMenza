[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/jivanda/SmartMenza)
# SmartMenza

LINK NA APP DEPLOYMENT: https://smartmenza-h5csfahadafnajaq.swedencentral-01.azurewebsites.net/swagger/index.html

## Projektni tim

Ime i prezime | E-mail adresa (FOI) | JMBAG | GitHub korisničko ime
------------  | ------------------- | ----- | ---------------------
Jurica Ivanda | jivanda@student.foi.hr | 0016143019 | jivanda
Karlo Mišić | kmisic22@student.foi.hr | 0016158466 | kmisic22
Nikola Polonijo | npolonijo22@student.foi.hr | 0016160034 | npolonijo22
Jakov Lisjak | jlisjak@student.foi.hr | 0016142102 | jlisjak1
Marko Ivić | mivic21@student.foi.hr | 0016152249 | MarkoIvic42

## Opis domene
SmartMenza je aplikacija za planiranje i praćenje prehrane u studentskoj menzi. Studentima omogućuje pregled dnevnog menija, postavljanje vlastitih prehrambenih ciljeva, AI preporuke obroka, označavanje favorita te ocjenjivanje i komentiranje jela. Zaposlenicima menze olakšava unos i uređivanje menija, automatsku AI nutritivnu analizu i pregled osnovne statistike popularnosti jela.

## Specifikacija projekta
Oznaka | Naziv | Kratki opis | Odgovorni član tima
------ | ----- | ----------- | -------------------
F01 | Registracija i prijava (Google ili klasična) | Student/Zaposlenik može se registrirati i prijaviti (Google ili klasično) radi korištenja funkcionalnosti. | Jakov Lisjak
F02 | Pregled dnevnog menija | Student može pregledati jelovnik za određeni dan. | Karlo Mišić
F03 | Unos vlastitih prehrambenih ciljeva (CRUD) | Student može unijeti, mijenjati i brisati ciljeve (npr. dnevni unos proteina, kalorija). | Karlo Mišić
F04 | AI preporuka obroka (AI)| Na temelju unesenih ciljeva i dnevnog menija, AI predlaže najprikladnije jelo. | Nikola Polonijo
F05 | Označavanje favorita (CRUD) | Student može označiti i spremiti svoja najdraža jela. | Jurica Ivanda
F06 | Ocjenjivanje i komentiranje jela (CRUD) | Student može ostaviti ocjenu i recenziju pojedinog jela. | Jakov Lisjak
F07 | Unos i uređivanje menija (CRUD) | Zaposlenik unosi dnevne menije (naziv, sastojci, cijena). | Marko Ivić
F08 | Nutritivna analiza menija (AI) | Na temelju sastojaka AI automatski izračunava nutritivne vrijednosti (kalorije, proteini, ugljikohidrati, masti). | Nikola Polonijo 
F09 | Pregled osnovne statistike | Pregled najpopularnijih jela i ocjena koje studenti ostavljaju. | Jurica Ivanda
F10 | ImageGenerator | Zaposlenik može uploadati sliku jela ili generirati sliku jela na temelju naziva jela. | Marko Ivić

## Tehnologije i oprema
Projekt će biti izrađen korištenjem sljedećih tehnologija i alata:
- Frontend: Android Studio (Kotlin, Jetpack Compose)
- Backend: ASP.NET Web API (.NET), Swagger (testiranje API-ja)
- Baza podataka: Microsoft SQL Server - relacijska baza podataka, Entity Framework Core - ORM sloj za mapiranje entiteta i upravljanje podacima
- AI i Cloud: Azure AI Services, Azure Cloud Services
- DevOps: Azure Repos, Azure Pipelines (CI/CD)
- Dizajn: Figma
- Verzije i dokumentacija: Azure DevOps i spojeni Repos sa GitHubom

## Repozitoriji i povezani alati
- GitHub (javni repozitorij): https://github.com/jivanda/SmartMenza
- Azure DevOps (backend): https://dev.azure.com/IMPLI/SmartMenza_backend
- Azure DevOps (frontend): https://dev.azure.com/IMPLI/SmartMenza_frontend
