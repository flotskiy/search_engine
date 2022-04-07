package main.com.github.flotskiy.search.engine.crawler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

public class MainTest {
    private static final BufferedReader BUFFERED_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SOURCE = getPath();
    private static final Set<String> WEBPAGES_PATH = new TreeSet<>();

    public static void main(String[] args) throws IOException {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(registry)
                .getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        URL url = new URL(SOURCE);
        String homePage = url.getProtocol() + "://" + url.getHost() + "/";

        new ForkJoinPool().invoke(new PageCrawler(WEBPAGES_PATH, homePage, session));

        transaction.commit();
        sessionFactory.close();
    }

    private static String getPath() {
        String input = "";
        System.out.println("Please input site url:");
        try {
            input = BUFFERED_READER.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (input.equals("exit")) {
            System.out.println("Goodbye!");
            System.exit(0);
        }
        return input;
    }
}
