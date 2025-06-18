package com.switchtester.app.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.User;
import com.switchtester.app.util.PasswordHasher; // Import PasswordHasher

/**
 * Manages loading, saving, and providing access to User configurations.
 * Users are stored in a JSON file.
 */
public class UserManager {

    private static final String USERS_FILE_PATH = "users.json"; // Stored in app's root directory
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
    }

    /**
     * Loads user configurations from the JSON file. If the file doesn't exist or is empty,
     * it creates and saves default users.
     * @return A list of User objects.
     */
    public static List<User> loadUsers() {
        File usersFile = new File(USERS_FILE_PATH);
        if (!usersFile.exists() || usersFile.length() == 0) {
            // If file doesn't exist or is empty, create default users
            List<User> defaultUsers = createDefaultUsers();
            saveUsers(defaultUsers); // Save defaults for the first time
            return defaultUsers;
        }
        try {
            return new ArrayList<>(Arrays.asList(objectMapper.readValue(usersFile, User[].class)));
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading user configurations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Saves a list of User objects to the JSON file.
     * @param users The list of User objects to save.
     */
    public static void saveUsers(List<User> users) {
        File usersFile = new File(USERS_FILE_PATH);
        try {
            objectMapper.writeValue(usersFile, users);
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error saving user configurations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a user by their username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    public static Optional<User> getUserByUsername(String username) {
        return loadUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Adds a new user to the system.
     * @param newUser The User object to add.
     * @return true if the user was added, false if a user with the same username already exists.
     */
    public static boolean addUser(User newUser) {
        List<User> users = loadUsers();
        if (users.stream().noneMatch(user -> user.getUsername().equalsIgnoreCase(newUser.getUsername()))) {
            users.add(newUser);
            saveUsers(users);
            return true;
        }
        return false; // User with this username already exists
    }

    /**
     * Updates an existing user in the system.
     * @param updatedUser The User object with updated information.
     * @return true if the user was updated, false if the user was not found.
     */
    public static boolean updateUser(User updatedUser) {
        List<User> users = loadUsers();
        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                updated = true;
                break;
            }
        }
        if (updated) {
            saveUsers(users);
        }
        return updated;
    }

    /**
     * Deletes a user from the system.
     * @param username The username of the user to delete.
     * @return true if the user was deleted, false if the user was not found.
     */
    public static boolean deleteUser(String username) {
        List<User> users = loadUsers();
        boolean removed = users.removeIf(user -> user.getUsername().equalsIgnoreCase(username));
        if (removed) {
            saveUsers(users);
        }
        return removed;
    }

    /**
     * Creates and returns a list of hardcoded default users for the application.
     * These users include predefined permissions for demonstration.
     */
    private static List<User> createDefaultUsers() {
        List<User> defaultUsers = new ArrayList<>();

        // Admin User: Has all permissions
        Set<String> adminPermissions = new HashSet<>(Arrays.asList(
            "dashboardNav", "projectsNav", "executionNav", "reportsNav", "debugNav", "settingsNav", "usersNav"
        ));
        defaultUsers.add(new User(
            "admin",
            PasswordHasher.hashPassword("password"), // Hashed password
            "Admin",
            adminPermissions,
            false // Not hidden
        ));

        // Production User: Limited permissions, settings require password
        Set<String> productionPermissions = new HashSet<>(Arrays.asList(
            "dashboardNav", "projectsNav", "executionNav", "reportsNav", "settingsNav" // No debug, no users
        ));
        defaultUsers.add(new User(
            "production",
            PasswordHasher.hashPassword("password"), // Hashed password
            "Production",
            productionPermissions,
            false // Not hidden
        ));

        // Quality User: Similar to Admin for navigation, but settings require password
        Set<String> qualityPermissions = new HashSet<>(Arrays.asList(
            "dashboardNav", "projectsNav", "executionNav", "reportsNav", "debugNav", "settingsNav", "usersNav"
        ));
        defaultUsers.add(new User(
            "quality",
            PasswordHasher.hashPassword("password"), // Hashed password
            "Quality",
            qualityPermissions,
            false // Not hidden
        ));

        // Maintenance User: Debug available, no users option, settings require password
        Set<String> maintenancePermissions = new HashSet<>(Arrays.asList(
            "dashboardNav", "projectsNav", "executionNav", "reportsNav", "debugNav", "settingsNav" // No users
        ));
        defaultUsers.add(new User(
            "maintenance",
            PasswordHasher.hashPassword("password"), // Hashed password
            "Maintenance",
            maintenancePermissions,
            false // Not hidden
        ));

        return defaultUsers;
    }
}
