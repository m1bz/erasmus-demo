package ro.universitate.erasmus.seed;

import jakarta.persistence.EntityManager;
import java.util.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ro.universitate.erasmus.entity.*;

@Component
@Profile("demo")
public class DemoDataLoader implements ApplicationRunner {
    public static final String YEAR = "2026-2027";
    private final EntityManager entityManager;
    private final TransactionTemplate transaction;
    private final DataIntegrityValidator validator;

    public DemoDataLoader(EntityManager entityManager, TransactionTemplate transaction, DataIntegrityValidator validator) {
        this.entityManager = entityManager; this.transaction = transaction; this.validator = validator;
    }

    @Override public void run(ApplicationArguments args) {
        transaction.executeWithoutResult(status -> {
            if (entityManager.createQuery("select count(u) from University u", Long.class).getSingleResult() != 0) return;
            seed();
            entityManager.flush();
            validator.validate();
        });
    }

    private void seed() {
        String[][] places = {
            {"Universitatea Dunărea Nouă", "Galați", "România"},
            {"Universitatea Carpaților", "Brașov", "România"},
            {"Universitatea Alpină", "Graz", "Austria"},
            {"Universitatea Baltică", "Riga", "Letonia"},
            {"Universitatea Centrală", "Praga", "Cehia"},
            {"Universitatea Iberică", "Porto", "Portugalia"}
        };
        StudyProgram[][] programs = new StudyProgram[6][2];
        Course[][][][] curricula = new Course[6][2][2][];
        for (int u = 0; u < places.length; u++) {
            University university = save(new University(places[u][0], places[u][1], places[u][2]));
            for (int p = 0; p < 2; p++) {
                String facultyName = p == 0 ? "Informatică" : "Economie";
                Faculty faculty = save(new Faculty(university, facultyName));
                StudyProgram program = save(new StudyProgram(faculty, p == 0 ? "Informatică" : "Economie"));
                programs[u][p] = program;
                for (int s = 0; s < 2; s++) {
                    Semester semester = s == 0 ? Semester.FIRST : Semester.SECOND;
                    String[] names = names(p, s, u >= 2);
                    int[] credits = credits(p);
                    Course[] list = new Course[names.length];
                    for (int i = 0; i < names.length; i++) {
                        String description = "Studiază " + names[i].toLowerCase(Locale.ROOT) + " prin seminare și proiecte aplicate.";
                        list[i] = save(new Course(program, (p == 0 ? "INF" : "ECO") + (s + 1) + "-" + (i + 1),
                                                  names[i], description, credits[i], 2, YEAR, semester));
                    }
                    curricula[u][p][s] = list;
                }
            }
        }
        for (int home = 0; home < 2; home++) for (int program = 0; program < 2; program++)
            for (int semester = 0; semester < 2; semester++) {
                Course[] origin = curricula[home][program][semester];
                Semester term = semester == 0 ? Semester.FIRST : Semester.SECOND;
                addAgreement(programs[home][program], programs[2][program], term, true,
                             origin, curricula[2][program][semester], all(origin.length));
                int[] partial = home == 1 && program == 1 && semester == 1
                    ? new int[]{0, 1, 2, 3, 4}
                    : program == 0 ? new int[]{0, 1, 2, 4} : new int[]{0, 1, 2, 3};
                addAgreement(programs[home][program], programs[3][program], term, true,
                             origin, curricula[3][program][semester], partial);
                if (home == 0 && program == 0 && semester == 0) {
                    addAgreement(programs[home][program], programs[4][program], term, true,
                                 origin, curricula[4][program][semester], new int[]{});
                    addAgreement(programs[home][program], programs[5][program], term, false,
                                 origin, curricula[5][program][semester], all(origin.length));
                }
                if (home == 1 && program == 1 && semester == 1) {
                    addAgreement(programs[home][program], programs[4][program], term, true,
                                 origin, curricula[4][program][semester], new int[]{0, 1, 2, 3, 4});
                    addAgreement(programs[home][program], programs[5][program], term, true,
                                 origin, curricula[5][program][semester], new int[]{0, 1, 3});
                }
                if (home == 0 && program == 0 && semester == 1)
                    addAgreement(programs[home][program], programs[5][program], term, true,
                                 origin, curricula[5][program][semester], new int[]{0, 1});
            }
    }

    private void addAgreement(StudyProgram home, StudyProgram host, Semester semester, boolean active,
                              Course[] homeCourses, Course[] hostCourses, int[] indices) {
        ExchangeAgreement agreement = save(new ExchangeAgreement(home, host, YEAR, semester, active));
        for (int index : indices) {
            Course origin = homeCourses[index], destination = hostCourses[index];
            String reason = "Conținutul despre " + origin.name.toLowerCase(Locale.ROOT) +
                " și proiectul aplicat de la „" + destination.name + "” acoperă aceleași competențe și " +
                origin.ects + " credite ECTS.";
            save(new CourseEquivalence(agreement, origin, destination, reason));
        }
    }

    private static int[] all(int n) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) values[i] = i;
        return values;
    }
    private static int[] credits(int program) {
        return program == 0 ? new int[]{7, 6, 6, 5, 3, 3} : new int[]{7, 6, 5, 4, 4, 4};
    }
    private static String[] names(int program, int semester, boolean host) {
        if (program == 0 && semester == 0) return host
            ? new String[]{"Algoritmi avansați", "Sisteme de date", "Rețele de calculatoare", "Inginerie software",
                           "Probabilități aplicate", "Interfețe web"}
            : new String[]{"Algoritmi și structuri de date", "Baze de date", "Rețele informatice", "Proiectare software",
                           "Statistică aplicată", "Dezvoltare web"};
        if (program == 0) return host
            ? new String[]{"Sisteme de operare", "Programare concurentă", "Securitate digitală", "Analiza datelor",
                           "Metode numerice", "Interacțiune om-calculator"}
            : new String[]{"Sisteme de operare", "Programare paralelă", "Securitatea informației", "Analiza datelor",
                           "Calcul numeric", "Interfețe utilizator"};
        if (semester == 0) return host
            ? new String[]{"Microeconomie", "Contabilitate managerială", "Econometrie", "Marketing strategic",
                           "Finanțe corporative", "Drept comercial"}
            : new String[]{"Teorie microeconomică", "Contabilitate de gestiune", "Metode econometrice", "Marketing",
                           "Finanțele întreprinderii", "Dreptul afacerilor"};
        return host
            ? new String[]{"Macroeconomie", "Analiză financiară", "Politici economice", "Management operațional",
                           "Comerț internațional", "Antreprenoriat"}
            : new String[]{"Teorie macroeconomică", "Analiză financiară", "Politici publice", "Managementul operațiunilor",
                           "Economie internațională", "Inițiativa antreprenorială"};
    }
    private <T> T save(T entity) { entityManager.persist(entity); return entity; }
}
