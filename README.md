# Recomandări Erasmus demonstrative

Aplicație universitară cu date fictive. Recomandările arată numai echivalări declarate în baza de date și **nu reprezintă aprobări Erasmus oficiale**.

## Cerințe și pornire

- Java 21
- Conexiune la Maven Central pentru prima rulare a Maven Wrapper

```bash
./mvnw spring-boot:run
```

Deschide `http://localhost:8080/`. Baza H2 funcționează în memorie și se reconstruiește la fiecare repornire. În profilul implicit `demo`, consola H2 este la `http://localhost:8080/h2-console`, cu URL JDBC `jdbc:h2:mem:erasmus` și utilizatorul `sa` (parolă goală).

Pentru teste și verificarea pachetului:

```bash
./mvnw clean verify
```

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
