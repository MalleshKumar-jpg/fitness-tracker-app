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
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Checks if a user exists with the given email and password.
     * WITH DEBUGGING
     */
    public User authenticateUser(String name, String password) {
        System.out.println("\n=== AUTHENTICATION DEBUG ===");
        System.out.println("Searching for user with name: '" + name + "'");
        System.out.println("Checking password: '" + password + "'");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // First, let's try to find the user by name only
            Query<User> userQuery = session.createQuery(
                    "FROM User WHERE name = :name", User.class);
            userQuery.setParameter("name", name);
            User foundUser = userQuery.uniqueResult();

            if (foundUser == null) {
                System.out.println("✗ No user found with name: '" + name + "'");
                return null;
            }

            System.out.println("✓ User found: " + foundUser.getName());
            System.out.println("Stored password: '" + foundUser.getPassword() + "'");
            System.out.println("Entered password: '" + password + "'");
            System.out.println("Password match: " + foundUser.getPassword().equals(password));
            System.out.println("Stored password length: " + foundUser.getPassword().length());
            System.out.println("Entered password length: " + password.length());

            // Now check if password matches
            if (foundUser.getPassword().equals(password)) {
                System.out.println("✓ Authentication successful!");
                return foundUser;
            } else {
                System.out.println("✗ Password mismatch!");
                // Check character by character
                String stored = foundUser.getPassword();
                for (int i = 0; i < Math.max(stored.length(), password.length()); i++) {
                    char storedChar = i < stored.length() ? stored.charAt(i) : '?';
                    char enteredChar = i < password.length() ? password.charAt(i) : '?';
                    if (storedChar != enteredChar) {
                        System.out.println("Difference at position " + i + ": stored='" + storedChar + "' (" + (int)storedChar + "), entered='" + enteredChar + "' (" + (int)enteredChar + ")");
                    }
                }
                return null;
            }
        } catch (Exception e) {
            System.out.println("✗ Exception during authentication:");
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
                    "SELECT count(userId) FROM User WHERE name = :name", Long.class);
            query.setParameter("name", name);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Find a user by name only (for debugging)
     */
    public User findUserByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE name = :name", User.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all users from the database (for debugging)
     */
    public List<User> findAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User";
            Query<User> query = session.createQuery(hql, User.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Retrieves all workouts for a specific user, ordered by date.
     */
    public List<Workout> findAllWorkoutsByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Workout WHERE user.userId = :userId ORDER BY workoutdate ASC";
            Query<Workout> query = session.createQuery(hql, Workout.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Retrieves all measurements for a specific user, ordered by date.
     */
    public List<Measurement> findAllMeasurementsByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Measurement WHERE user.userId = :userId ORDER BY recorddate ASC";
            Query<Measurement> query = session.createQuery(hql, Measurement.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
