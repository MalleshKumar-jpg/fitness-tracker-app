package com.fitnesstracker;

import com.fitnesstracker.model.User;
import com.fitnesstracker.model.Workout;
import com.fitnesstracker.model.Measurement;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Handles the creation and management of the Hibernate SessionFactory.
 * This should be a singleton for the entire application lifecycle.
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    /**
     * Builds the SessionFactory by reading the hibernate.cfg.xml and registering entities.
     */
    public static void buildSessionFactory() {
        try {
            if (sessionFactory == null) {
                //reads configuration from hibernate.cfg.xml
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Workout.class);
                configuration.addAnnotatedClass(Measurement.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

                // 3. Build the SessionFactory
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            }
        } catch (Exception ex) {
            System.err.println("Initial SessionFactory creation failed. Check Oracle connection settings in hibernate.cfg.xml.");
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            buildSessionFactory();
        }
        return sessionFactory;
    }

    /**
     * Closes the connection pool and cleans up resources.
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("Hibernate SessionFactory shutdown complete.");
        }
    }
}
