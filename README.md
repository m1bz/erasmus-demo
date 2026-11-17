# Recomandări Erasmus demonstrative

Aplicație Java pentru explorarea echivalărilor dintre cursuri și calcularea acoperirii ECTS la o universitate parteneră. Datele sunt fictive; rezultatele nu sunt aprobări Erasmus oficiale și nu reprezintă o Learning Agreement individuală.

## Pornire pas cu pas

Ai nevoie de **Java 21** și de internet la prima rulare, pentru descărcarea dependențelor. Maven nu trebuie instalat separat.

1. Deschide un terminal în directorul proiectului, lângă `pom.xml`.
2. Verifică Java:

   ```bash
   java -version
   ```

3. Pornește aplicația:

   ```bash
   ./mvnw spring-boot:run
   ```

   Pe Windows: `mvnw.cmd spring-boot:run`.
4. Când apare mesajul `Started ErasmusDemoApplication`, deschide [http://localhost:8080/](http://localhost:8080/). Oprește aplicația cu `Ctrl+C`.

Profilul implicit folosește H2 în memorie. Datele și schimbările făcute prin API se resetează când oprești aplicația. Consola H2 este la [http://localhost:8080/h2-console](http://localhost:8080/h2-console), cu URL `jdbc:h2:mem:erasmus`, utilizatorul `sa` și parola goală.

### Păstrează schimbările local

Pentru a salva datele într-un fișier local H2, pornește aplicația cu profilul `persistent`:

```bash
SPRING_PROFILES_ACTIVE=demo,persistent ./mvnw spring-boot:run
```

Fișierul bazei de date apare în `data/erasmus`. La prima pornire se încarcă seed-ul demonstrativ; pornirile următoare păstrează schimbările. Profilul persistent folosește `ddl-auto=update`, pe când profilul implicit recreează schema și datele demo la fiecare pornire. Directoarele `data/` sunt excluse din Git.

Pentru a rula testele și a construi aplicația:

```bash
./mvnw clean verify
```

Arhiva este `target/erasmus-demo-0.0.1-SNAPSHOT.jar` și poate fi pornită cu `java -jar target/erasmus-demo-0.0.1-SNAPSHOT.jar`.

## API JSON

Endpoint-urile JSON sunt sub `/api/v1`; exemplele complete și ordonate sunt în colecția Postman [`docs/erasmus-demo.postman_collection.json`](docs/erasmus-demo.postman_collection.json).

| Metodă și adresă | Scop |
|---|---|
| `GET /health` | Verifică dacă aplicația răspunde. |
| `GET /universities` | Listează universitățile și ID-urile lor. |
| `GET /programs?universityId=...` | Listează programele unei universități. |
| `GET /courses?programId=...` | Listează cursurile unui program; filtrele `academicYear`, `semester` și `studyYear` sunt opționale. |
| `GET /agreements?homeProgramId=...&academicYear=...&semester=...` | Caută parteneriatele de ieșire. |
| `GET /agreements/{id}` | Citește un parteneriat. |
| `POST /agreements` | Creează un parteneriat. |
| `PATCH /agreements/{id}/status` | Activează sau dezactivează un parteneriat. |
| `GET /equivalences?agreementId=...` | Listează echivalările parteneriatului. |
| `GET /equivalences/{id}` | Citește o echivalare. |
| `POST /equivalences` | Adaugă o echivalare. |
| `PUT /equivalences/{id}` | Actualizează o echivalare existentă. |
| `GET /options?...` | Returnează opțiunile disponibile pentru formular. |
| `GET /recommendations?...` | Calculează acoperirea ECTS pentru profilul selectat. |
| `GET /recommendations/{agreementId}/comparison?...` | Compară disciplina de origine cu cea gazdă. |

ID-urile se obțin din răspunsurile de catalog; nu trebuie memorate. Toate operațiile de scriere validează referințele și regulile de domeniu. Crearea răspunde cu `201 Created`; datele invalide răspund cu `400`, referințele lipsă cu `404`, iar conflictele de domeniu cu `409`. Erorile API includ un mesaj în limba română.

### Exemplu de flux: adaugă echivalări și recalculează

Profilul **A** este Universitatea Dunărea Nouă → Informatică → anul 2 → 2026–2027 → semestrul 1. Pentru Universitatea Baltică, seed-ul acoperă inițial **22 din 30 ECTS**. API-ul poate adăuga echivalarea de 5 ECTS pentru `INF1-4`, recalculând acoperirea la **27/30**; apoi echivalarea de 3 ECTS pentru `INF1-6` o duce la **30/30**.

Colecția Postman descoperă universitățile, programele, cursurile și parteneriatul după nume/cod, apoi rulează acest flux. În Postman, apasă **Import**, alege [`docs/erasmus-demo.postman_collection.json`](docs/erasmus-demo.postman_collection.json), apoi rulează toate cele 12 cereri în ordine. Rulează colecția o singură dată pe o bază proaspătă: după creare, API-ul corect respinge duplicatele cu `409 Conflict`. Profilul demo implicit se resetează la repornire; profilul persistent păstrează echivalările adăugate. Pentru scrieri manuale, folosește răspunsurile `GET` pentru a completa ID-urile.

Pentru `POST /agreements`, trimite `homeProgramId`, `hostProgramId`, `academicYear`, `semester` (`FIRST` sau `SECOND`) și `active`. Pentru `POST /equivalences`, trimite `agreementId`, `homeCourseId`, `hostCourseId` și `justification`. Folosește ID-urile obținute din catalog sau din colecția Postman. Cursurile trebuie să aparțină programelor și perioadei parteneriatului și să aibă același număr de ECTS; fiecare curs poate apărea o singură dată în echivalările parteneriatului.

## HTTPS local

Pornește varianta HTTPS cu:

```bash
./scripts/run-local-https.sh
```

Aceasta ascultă la [https://localhost:8443](https://localhost:8443) cu un certificat autosemnat generat local; browserul va afișa un avertisment. Datele rămân în memorie și se resetează implicit. Pentru a păstra datele și în varianta HTTPS:

```bash
EMP_DATA_PROFILE=persistent ./scripts/run-local-https.sh
```

Poți indica propriul keystore prin `EMP_KEYSTORE_PATH` și parola prin `EMP_KEYSTORE_PASSWORD`. HTTPS-ul oferit este pentru dezvoltare locală, nu pentru expunere publică.

Pentru a folosi API-ul HTTPS din Postman cu certificatul autosemnat, setează variabila `baseUrl` a colecției la `https://localhost:8443` și dezactivează **Settings → General → SSL certificate verification** în Postman. Repornește verificarea certificatelor când revii la servicii HTTPS publice.

## Date demonstrative

Fiecare selecție de curriculum însumează 30 ECTS. Profilurile originale de anul 2 sunt păstrate pentru anul academic 2026–2027; profilul de Informatică are în plus anul 3, semestrul 1, pentru 2027–2028.

| Profil | Selecție | Destinații și acoperire |
|---|---|---|
| A | Dunărea Nouă → Informatică, anul 2, semestrul 1 | Alpină 30/30; Baltică 22/30; Centrală 0/30. Iberică este inactivă și nu apare. |
| B | Carpaților → Economie, anul 2, semestrul 2 | Alpină 30/30; Baltică 26/30; Centrală 26/30; Iberică 17/30. |
| C | Dunărea Nouă → Informatică, anul 3, semestrul 1 | Alpină 30/30; Baltică 22/30; Centrală 0/30. Iberică este inactivă și nu apare. |

Rezultatele sunt ordonate după acoperire, apoi alfabetic. Sunt numărate doar parteneriatele active pentru aceeași perioadă și echivalările 1:1 cu ECTS egale. Profilurile sunt demonstrative și nu verifică admiterea, locurile disponibile, orarul, acreditarea instituțională sau aprobările Erasmus.

## Cerințe și scope

Proiectul păstrează un model simplu pentru recomandări de cursuri: universitate, facultate, program, curs, parteneriat și echivalare. Matricea [`docs/cerinte-curs.md`](docs/cerinte-curs.md) arată ce acoperă sprinturile de domeniu/persistență și ce rămâne o alegere de interfață ori de extindere. Aplicația nu implementează fluxul complet al unei Learning Agreement student, nominalizarea sau aprobarea instituțională.
