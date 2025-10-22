package com.fitnesstracker.dao;

import com.fitnesstracker.model.User;
import com.fitnesstracker.model.Workout;
import com.fitnesstracker.model.Measurement;
import com.fitnesstracker.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Centralized Data Access Object for all fitness activities and user management.
 * This DAO handles authentication, registration, and logging of workouts/measurements.
 */
public class ActivityDAO {

    public void save(Object entity) {
        Transaction transaction = null;
        Session session = null;

        try {
            // 1. Open Session and begin Transaction
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // 2. Perform the generic save operation
            session.save(entity);

            // 3. Commit the transaction
            transaction.commit();

        } catch (Exception e) {
            // 4. Rollback if the transaction is active (this must happen before session closure)
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            // Re-throw the exception to allow the controller to update the UI with an error message
            throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
        } finally {
            // 5. Close the session in the finally block to release resources
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Checks if a user exists with the given email and password.
     */
    public User authenticateUser(String name, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Note: In a real application, you would hash the password before comparing.
            Query<User> query = session.createQuery(
                    "FROM User WHERE name = :name AND password = :password", User.class);
            query.setParameter("name", name);
            query.setParameter("password", password);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an email is already in use by another user.
     */
    public boolean nameExists(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT count(id) FROM User WHERE name = :name", Long.class);
            query.setParameter("name", query);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Default to true on error to prevent data loss or duplicate entries
        }
    }

    /**
     * Retrieves all workouts for a specific user, ordered by date.
     * FIXED: Eagerly fetch the user relationship to prevent LazyInitializationException
     */
    public List<Workout> findAllWorkoutsByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use JOIN FETCH to eagerly load the user relationship
            String hql = "FROM Workout w JOIN FETCH w.user WHERE w.user.userId = :userId ORDER BY w.workoutdate ASC";
            Query<Workout> query = session.createQuery(hql, Workout.class);
            query.setParameter("userId", userId);
            List<Workout> workouts = query.list();

            System.out.println("DEBUG: Loaded " + workouts.size() + " workouts for user " + userId);
            return workouts;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load workouts for user " + userId);
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Retrieves all measurements for a specific user, ordered by date.
     * FIXED: Eagerly fetch the user relationship to prevent LazyInitializationException
     */
    public List<Measurement> findAllMeasurementsByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use JOIN FETCH to eagerly load the user relationship
            String hql = "FROM Measurement m JOIN FETCH m.user WHERE m.user.userId = :userId ORDER BY m.recorddate ASC";
            Query<Measurement> query = session.createQuery(hql, Measurement.class);
            query.setParameter("userId", userId);
            List<Measurement> measurements = query.list();

            System.out.println("DEBUG: Loaded " + measurements.size() + " measurements for user " + userId);
            return measurements;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load measurements for user " + userId);
            e.printStackTrace();
            return List.of();
        }
    }
}
