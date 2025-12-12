package com.fitnesstracker.dao;

import com.fitnesstracker.model.User;
import com.fitnesstracker.model.Workout;
import com.fitnesstracker.model.Measurement;
import com.fitnesstracker.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized Data Access Object for all fitness activities and user management.
 */
public class ActivityDAO {

    /**
     * Save a new entity to the database
     */
    public void save(Object entity) {
        Transaction transaction = null;
        Session session = null;

        try {
            System.out.println("DEBUG: Starting save operation for entity: " + entity.getClass().getName());
            // Open Session and begin Transaction
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.persist(entity);

            // Commit the transaction
            transaction.commit();
            System.out.println("DEBUG: Successfully saved entity");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to save entity: " + e.getMessage());
            // Rollback if the transaction is active
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
        } finally {
            // Close the session in the finally block to release resources
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Update an existing entity in the database
     */
    public void update(Object entity) {
        Transaction transaction = null;
        Session session = null;

        try {
            System.out.println("DEBUG: Starting update operation for entity: " + entity.getClass().getName());
            // Open Session and begin Transaction
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // For Hibernate 6.0+, use merge() instead of update()
            session.merge(entity);

            // Commit the transaction
            transaction.commit();
            System.out.println("DEBUG: Successfully updated entity");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to update entity: " + e.getMessage());
            // Rollback if the transaction is active
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            // Re-throw the exception
            throw new RuntimeException("Update operation failed: " + e.getMessage(), e);
        } finally {
            // Close the session
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Delete an entity from the database
     */
    public void delete(Object entity) {
        Transaction transaction = null;
        Session session = null;

        try {
            System.out.println("DEBUG: Starting delete operation for entity: " + entity.getClass().getName());

            // Open Session and begin Transaction
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // For entities that might be detached, reattach them
            if (entity instanceof Workout) {
                Workout workout = (Workout)entity;
                System.out.println("DEBUG: Deleting Workout with ID: " + workout.getWorkoutId());
                if (workout.getWorkoutId() != null) {
                    Workout managedWorkout = session.get(Workout.class, workout.getWorkoutId());
                    if (managedWorkout != null) {
                        session.remove(managedWorkout);
                        System.out.println("DEBUG: Workout found and removed");
                    } else {
                        System.out.println("DEBUG: Workout not found in database");
                    }
                }
            }
            else if (entity instanceof Measurement) {
                Measurement measurement = (Measurement)entity;
                System.out.println("DEBUG: Deleting Measurement with ID: " + measurement.getMeasurementId());
                if (measurement.getMeasurementId() != null) {
                    Measurement managedMeasurement = session.get(Measurement.class, measurement.getMeasurementId());
                    if (managedMeasurement != null) {
                        session.remove(managedMeasurement);
                        System.out.println("DEBUG: Measurement found and removed");
                    } else {
                        System.out.println("DEBUG: Measurement not found in database");
                    }
                }
            }
            else {
                // For Hibernate 6.0+, use remove() instead of delete()
                session.remove(entity);
            }

            // Commit the transaction
            transaction.commit();
            System.out.println("DEBUG: Delete transaction committed successfully");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to delete entity: " + e.getMessage());
            // Rollback if the transaction is active
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            // Re-throw the exception
            throw new RuntimeException("Delete operation failed: " + e.getMessage(), e);
        } finally {
            // Close the session
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Checks if a user exists with the given name and password.
     */
    public User authenticateUser(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("DEBUG: Authenticating user: " + username);

            Query<User> query = session.createQuery(
                    "FROM User WHERE username = :username AND password = :password", User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);

            User result = query.uniqueResult();
            System.out.println("DEBUG: Authentication result: " + (result != null ? "Success" : "Failed"));

            return result;
        } catch (Exception e) {
            System.err.println("ERROR: Exception during authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a username is already in use by another user.
     */
    public boolean usernameExists(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("DEBUG: Checking if name exists: " + username);

            // Use the correct field name from the entity
            Query<Long> query = session.createQuery(
                    "SELECT count(u.userId) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            Long count = query.uniqueResult();

            System.out.println("DEBUG: Name exists check result: Count=" + count);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("ERROR: Exception in nameExists method: " + e.getMessage());
            e.printStackTrace();
            return true; // Default to true on error to prevent duplicate entries
        }
    }

    /**
     * Retrieves all workouts for a specific user, ordered by date.
     */
    public List<Workout> findAllWorkoutsByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("DEBUG: Finding workouts for user ID: " + userId);

            try {
                // Use the exact field names from your entity class
                String hql = "FROM Workout w WHERE w.user.userId = :userId ORDER BY w.workoutdate ASC";
                System.out.println("DEBUG: Executing HQL: " + hql);

                Query<Workout> query = session.createQuery(hql, Workout.class);
                query.setParameter("userId", userId);
                List<Workout> workouts = query.getResultList();

                if (workouts != null && !workouts.isEmpty()) {
                    System.out.println("DEBUG: Found " + workouts.size() + " workouts in database");
                    return workouts;
                } else {
                    System.out.println("DEBUG: No workouts found in database, using sample data");
                }
            } catch (Exception e) {
                System.err.println("ERROR: Exception when querying workouts: " + e.getMessage());
                e.printStackTrace();
            }

            // Provide sample data if database query fails
            System.out.println("DEBUG: Generating sample workout data");
            List<Workout> sampleWorkouts = new ArrayList<>();
            return sampleWorkouts;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load workouts: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all measurements for a specific user, ordered by date.
     */
    public List<Measurement> findAllMeasurementsByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("DEBUG: Finding measurements for user ID: " + userId);

            try {
                String hql = "FROM Measurement m WHERE m.user.userId = :userId ORDER BY m.recorddate ASC";
                System.out.println("DEBUG: Executing HQL: " + hql);

                Query<Measurement> query = session.createQuery(hql, Measurement.class);
                query.setParameter("userId", userId);
                List<Measurement> measurements = query.getResultList();

                if (measurements != null && !measurements.isEmpty()) {
                    System.out.println("DEBUG: Found " + measurements.size() + " measurements in database");
                    return measurements;
                } else {
                    System.out.println("DEBUG: No measurements found in database");
                    return new ArrayList<>();  // ✅ Return empty list instead of sample data
                }
            } catch (Exception e) {
                System.err.println("ERROR: Exception when querying measurements: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();  // ✅ Return empty list on error
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load measurements: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Find a workout by its ID
     */
    public Workout findWorkoutById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Workout.class, id);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to find workout with ID: " + id);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a measurement by its ID
     */
    public Measurement findMeasurementById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Measurement.class, id);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to find measurement with ID: " + id);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a user by their ID
     */
    public User findUserById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to find user with ID: " + id);
            e.printStackTrace();
            return null;
        }
    }
}