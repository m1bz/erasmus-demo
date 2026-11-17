# Recomandări Erasmus demonstrative

Aplicație universitară cu date fictive. Recomandările arată numai echivalări declarate în baza de date și **nu reprezintă aprobări Erasmus oficiale**.

## Pornire pas cu pas

Ai nevoie de **Java 21** și de internet la prima rulare, pentru descărcarea Maven Wrapper și a dependențelor. Nu trebuie să instalezi Maven separat.

1. Deschide un terminal în directorul proiectului, acolo unde se află `pom.xml`.
2. Verifică versiunea Java:

```bash
java -version
```

3. Pornește aplicația:

```bash
./mvnw spring-boot:run
```

Pe Windows, folosește `mvnw.cmd spring-boot:run`. Când apare mesajul `Started ErasmusDemoApplication`, deschide **http://localhost:8080/**. Oprește aplicația cu `Ctrl+C` în terminal.

Baza H2 funcționează în memorie și se reconstruiește la fiecare repornire. În profilul implicit `demo`, consola H2 este la **http://localhost:8080/h2-console**, cu URL JDBC `jdbc:h2:mem:erasmus`, utilizator `sa` și parolă goală.

Pentru a rula testele și a construi arhiva aplicației:

```bash
./mvnw clean verify
```

Arhiva rezultată se află în `target/erasmus-demo-0.0.1-SNAPSHOT.jar`. O poți porni și direct cu:

```bash
java -jar target/erasmus-demo-0.0.1-SNAPSHOT.jar
```

## API JSON și HTTPS local

Aplicația oferă și un API JSON pentru aceleași date demonstrative:

* `GET /api/v1/health` verifică dacă serviciul răspunde.
* `GET /api/v1/options` returnează universitățile, facultățile, programele și perioadele disponibile.
* `GET /api/v1/recommendations` calculează recomandările. Parametrii necesari sunt `universityId`, `facultyId`, `programId`, `studyYear`, `academicYear` și `semester`.
* `GET /api/v1/recommendations/{agreementId}/comparison` returnează comparația cursurilor pentru destinația aleasă și aceiași parametri de profil.
* `POST /api/v1/agreements` creează un parteneriat dintr-un corp JSON cu programele, anul academic, semestrul și starea activă.
* `POST /api/v1/equivalences` creează o echivalare JSON pentru un parteneriat și două cursuri.

Exemplu pentru profilul demonstrativ A:

```text
http://localhost:8080/api/v1/recommendations?universityId=1&facultyId=1&programId=1&studyYear=2&academicYear=2026-2027&semester=FIRST
```

Răspunsurile folosesc DTO-uri JSON independente de entitățile JPA. Datele invalide primesc HTTP 400, profilurile fără curriculum sau destinații primesc HTTP 404, iar datele incoerente primesc HTTP 500 cu un mesaj JSON în limba română.

Operațiile POST validează referințele și regulile de domeniu, apoi salvează în H2. La creare răspund cu HTTP 201 și un antet `Location`; referințele inexistente primesc HTTP 404, iar duplicatele sau conflictele de perioadă/ECTS primesc HTTP 409.

Pentru pornire locală prin HTTPS:

```bash
./scripts/run-local-https.sh
```

Scriptul creează certificatul local `dev-keystore.p12` și pornește aplicația la **https://localhost:8443**. Certificatul este autosemnat și va produce un avertisment în browser; este destinat numai dezvoltării locale. Poți furniza propriul fișier folosind `EMP_KEYSTORE_PATH` și parola sa prin `EMP_KEYSTORE_PASSWORD`.


## Selecții demonstrative

Toate selecțiile folosesc anul de studiu **2** și anul academic **2026-2027**.

| Profil | Selecție | Rezultate, în ordine |
|---|---|---|
| A | Universitatea Dunărea Nouă → Informatică → Informatică → Semestrul 1 | Universitatea Alpină 30/30 (100,0%, COMPLETĂ); Universitatea Baltică 22/30 (73,3%, PARȚIALĂ); Universitatea Centrală 0/30 (0,0%, FĂRĂ ECHIVALĂRI). Universitatea Iberică lipsește deoarece parteneriatul este inactiv. |
| B | Universitatea Carpaților → Economie → Economie → Semestrul 2 | Universitatea Alpină 30/30 (100,0%, COMPLETĂ); Universitatea Baltică 26/30 (86,7%, PARȚIALĂ); Universitatea Centrală 26/30 (86,7%, PARȚIALĂ); Universitatea Iberică 17/30 (56,7%, PARȚIALĂ). |

## Reguli și limite

Curriculumul cuprinde cursurile programului, anului de studiu, anului academic și semestrului selectat. Fiecare curriculum demo are 30 ECTS. Se iau în calcul numai parteneriatele active pentru aceeași perioadă și numai echivalările 1:1 declarate, cu ECTS egale. O echivalare din alt an de studiu nu contribuie la acoperire; o echivalare cu program, perioadă sau credite incoerente produce eroare de integritate. Procentul este rotunjit la o zecimală, iar statutul se calculează din creditele întregi. La egalitate, destinațiile sunt ordonate alfabetic.

Formularul afișează doar opțiuni prezente în date și resetează selecțiile dependente când se schimbă universitatea, facultatea sau programul. Adresele rezultatelor și comparației conțin întreg profilul și pot fi redeschise fără sesiune.

Simplificare aleasă: universitățile de origine din formular sunt cele cu cel puțin un parteneriat activ; profilurile demo acoperă numai licența, anul 2, un semestru din 2026–2027.

Aplicația nu verifică admiterea, locurile disponibile, orarul sau aprobările instituționale.
