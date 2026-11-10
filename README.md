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

## Publicarea proiectului pe GitHub

Acest proiect are **deja un repository Git local și commit-uri**. Nu rula `git init` din nou. Fișierul `.gitignore` exclude rezultatele din `target/`, fișierele locale ale editorului, jurnalele și fișierele locale cu secrete. Fișierele `mvnw`, `mvnw.cmd` și `.mvn/wrapper/maven-wrapper.properties` rămân în Git.

1. Pe GitHub, alege **New repository**, dă-i un nume (de exemplu `erasmus-demo`) și alege vizibilitatea. Creează-l **gol**: nu bifa README, `.gitignore` sau licență. README și `.gitignore` există deja local; poți adăuga o licență mai târziu, dacă dorești.
2. Din terminalul deschis în directorul proiectului, rulează comenzile de mai jos. Înlocuiește `UTILIZATOR` și `erasmus-demo` cu valorile tale sau copiază adresa HTTPS afișată de GitHub:

```bash
git status
git branch -M main
git remote add origin https://github.com/UTILIZATOR/erasmus-demo.git
git remote -v
git push -u origin main
```

Ramura locală se numește inițial `master`; comanda `git branch -M main` o redenumește înainte de primul push. La autentificarea prin HTTPS, dacă Git cere o parolă, folosește un **personal access token**, nu parola contului; vezi [instrucțiunile GitHub pentru autentificare](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/about-authentication-to-github). Nu scrie tokenul în fișierele proiectului sau în adresa remote.

Pentru modificările ulterioare:

```bash
git add .
git commit -m "Describe the change"
git push
```

Pașii pentru un repository local existent sunt descriși și în [documentația GitHub](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github).

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
