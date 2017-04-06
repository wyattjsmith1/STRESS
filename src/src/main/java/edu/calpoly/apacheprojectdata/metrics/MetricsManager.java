package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.Settings;
import edu.calpoly.apacheprojectdata.data.Project;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.*;

import static edu.calpoly.apacheprojectdata.metrics.MetricDescription.*;

/**
 * Class allowing the user to search the data.
 */
public class MetricsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsManager.class);

    @Setter
    private
    Collection<Project> projects;

    private Session session;
    private EnumMap<MetricDescription.Group, List<MetricField>> metricFields;

    @Getter
    private static MetricsManager instance;

    private MetricsManager(Collection<Project> projects) throws InterruptedException, IOException, GitAPIException {
        this.projects = projects;
        Properties properties = new Properties();
        properties.load(new FileReader(Settings.getApacheConfig()));
        SessionFactory sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(ProjectMetrics.class)
                .addAnnotatedClass(LanguageMetrics.class)
                .addAnnotatedClass(VcsMetrics.class)
                .addAnnotatedClass(BugDatabaseMetrics.class)
                .addAnnotatedClass(NumberMetrics.class)
                .addAnnotatedClass(Snapshot.class)
                .buildSessionFactory();
        session = sessionFactory.openSession();
    }

    public static void createProjectManager(Collection<Project> projects) throws InterruptedException, GitAPIException, IOException {
        instance = new MetricsManager(projects);
    }

    public void updateAllProjects() throws InterruptedException, GitAPIException, IOException {
        // For now, clear everything out and restart.
        final int[] i = {0};

        Snapshot snapshot = new Snapshot();
        Transaction tx = session.beginTransaction();
        session.save(snapshot);
        tx.commit();

        projects.parallelStream().forEach(p -> {
                try {
                    int a = ++i[0];
                    System.out.println("Starting: " + p.getName() + " " + a + "/" + projects.size());
                    saveMetrics(new ProjectMetrics(p, snapshot), session);
                    p.freeResources();
                    System.out.println("Finishing: " + p.getName() + " " + a + "/" + projects.size());
                } catch (IOException | GitAPIException e) {
                    LOGGER.error("Unable to create project metrics for " + p, e);
                }
            });

        snapshot.finish();
        tx = session.beginTransaction();
        session.update(snapshot);
        tx.commit();
    }

    private synchronized void saveMetrics(ProjectMetrics metrics, Session session) {
        Transaction tx = session.beginTransaction();
        session.save(metrics);
        tx.commit();
    }

    public List getSnapshots() {
        Query query = session.createQuery(String.format("from %s ORDER BY started DESC", Snapshot.class.getName()));
        return query.getResultList();
    }

    public Map<MetricDescription.Group, List<MetricField>> getFields() {
        if (metricFields == null) {
            metricFields = recursiveFieldSearch(ProjectMetrics.class, "p.", "");
        }
        return metricFields;
    }

    private EnumMap<MetricDescription.Group, List<MetricField>> recursiveFieldSearch(Class cls, String prefix, String descPrefix) {
        EnumMap<Group, List<MetricField>> fields = new EnumMap<>(Group.class);
        for (Field f : cls.getDeclaredFields()) {
            if (f.isAnnotationPresent(MetricDescription.class)) {
                if ("languages".equals(f.getName())) {
                    for (String lang : LanguageMetrics.FILE_TYPE.keySet()) {
                        MetricField percent = new MetricField();
                        MetricField count = new MetricField();
                        percent.setType(MetricField.DataType.NUMBER);
                        count.setType(MetricField.DataType.NUMBER);
                        percent.setDisplayName(lang + " line percent");
                        count.setDisplayName(lang + " line count");
                        percent.setFieldName(prefix + "languages['" + lang + "'].linesPercent");
                        count.setFieldName(prefix + "languages['" + lang + "'].linesCount");
                        addFieldToGroup(fields, percent, Group.LANGUAGE);
                        addFieldToGroup(fields, count, Group.LANGUAGE);
                    }
                } else if (f.getType() == NumberMetrics.class) {
                    merge(fields, recursiveFieldSearch(NumberMetrics.class, prefix + f.getName() + ".", f.getAnnotation(MetricDescription.class).displayName()));
                } else {
                    MetricField next = new MetricField();
                    if (f.getType() == Integer.class ||
                            f.getType() == Double.class ||
                            f.getType() == Long.class) {
                        next.setType(MetricField.DataType.NUMBER);
                    } else if (f.getType() == ZonedDateTime.class) {
                        next.setType(MetricField.DataType.DATE);
                    } else if (f.getType() == String.class) {
                        next.setType(MetricField.DataType.STRING);
                    } else if(f.getType() == Boolean.class) {
                        next.setType(MetricField.DataType.BOOLEAN);
                    } else {
                        merge(fields, recursiveFieldSearch(f.getType(), prefix + f.getName() + ".", ""));
                        continue;
                    }
                    next.setFieldName(prefix + f.getName());
                    next.setDisplayName(descPrefix + " " + f.getAnnotation(MetricDescription.class).displayName());
                    if (!"".equals(next.getDisplayName())) {
                        addFieldToGroup(fields, next, f.getAnnotation(MetricDescription.class).group());
                    }
                }
            }
        }
        return fields;
    }

    private void merge(Map<Group, List<MetricField>> a, Map<Group, List<MetricField>> b) {
        for (Map.Entry<Group, List<MetricField>> entry : b.entrySet()) {
            if (!a.containsKey(entry.getKey())) {
                a.put(entry.getKey(), new LinkedList<>());
            }
            a.get(entry.getKey()).addAll(entry.getValue());
        }
    }

    private void addFieldToGroup(Map<Group, List<MetricField>> fields, MetricField metric, Group group) {
        if (!fields.containsKey(group)) {
            fields.put(group, new LinkedList<>());
        }
        fields.get(group).add(metric);
    }

    public List search(List<Filter> filters, Integer snapshot) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("select p from %s p where p.snapshot.id=%d and p.bugDatabase.averageDevelopersPerCompleteTicket != -1", ProjectMetrics.class.getName(), snapshot));
        //Set<String> fields = new HashSet<>();
        for (Filter filter : filters) {
            query.append(String.format(" and %s %s %s", filter.getField(), filter.getComparator(), filter.getValue()));
            //fields.add(filter.getField());
        }
        return session.createQuery(query.toString()).getResultList();
    }
}
