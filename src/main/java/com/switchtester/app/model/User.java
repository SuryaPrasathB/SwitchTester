package com.switchtester.app.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user in the Switch Tester application.
 * Includes user credentials, primary profile type, and specific dashboard panel permissions.
 */
public class User {

    private String username;
    private String passwordHash; // BCrypt hashed password
    private String profileType; // e.g., "Admin", "Production", "Quality", "Maintenance"
    private Set<String> permissions; // Set of dashboard panel IDs/names (e.g., "dashboardNav", "projectsNav")
    private boolean hidden; // To indicate if the user should be hidden from lists (soft delete/hide)

    /**
     * Constructor for creating a new User object.
     * Used by Jackson for deserialization from JSON.
     *
     * @param username The user's unique username.
     * @param passwordHash The BCrypt hashed password for the user.
     * @param profileType The primary profile type/role of the user.
     * @param permissions A set of permission strings (e.g., dashboard panel IDs).
     * @param hidden A boolean indicating if the user is hidden.
     */
    @JsonCreator
    public User(@JsonProperty("username") String username,
                @JsonProperty("passwordHash") String passwordHash,
                @JsonProperty("profileType") String profileType,
                @JsonProperty("permissions") Set<String> permissions,
                @JsonProperty("hidden") boolean hidden) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.profileType = Objects.requireNonNull(profileType, "Profile type cannot be null");
        this.permissions = (permissions != null) ? new HashSet<>(permissions) : new HashSet<>();
        this.hidden = hidden;
    }

    // --- Getters ---
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getProfileType() {
        return profileType;
    }

    public Set<String> getPermissions() {
        // Return a copy to prevent external modification
        return new HashSet<>(permissions);
    }

    public boolean isHidden() {
        return hidden;
    }

    // --- Setters (for updates) ---
    public void setUsername(String username) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
    }

    public void setProfileType(String profileType) {
        this.profileType = Objects.requireNonNull(profileType, "Profile type cannot be null");
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = (permissions != null) ? new HashSet<>(permissions) : new HashSet<>();
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Checks if the user has a specific permission.
     * @param permission The permission string to check.
     * @return true if the user has the permission, false otherwise.
     */
    @JsonIgnore // Do not serialize this method as a property
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Adds a permission to the user's set of permissions.
     * @param permission The permission string to add.
     * @return true if the permission was added, false if it was already present.
     */
    public boolean addPermission(String permission) {
        return permissions.add(permission);
    }

    /**
     * Removes a permission from the user's set of permissions.
     * @param permission The permission string to remove.
     * @return true if the permission was removed, false if it was not present.
     */
    public boolean removePermission(String permission) {
        return permissions.remove(permission);
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", profileType='" + profileType + '\'' +
               ", permissions=" + permissions +
               ", hidden=" + hidden +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username); // Username is the unique identifier
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
