// Utility class to manage the current user session (logged-in user ID)
public class Session {
    // Stores the ID of the currently logged-in user (static for global access)
    private static String loggedInUserId;

    // Sets the ID of the currently logged-in user
    public static void setLoggedInUserId(String id) {
        loggedInUserId = id;
    }

    // Returns the ID of the currently logged-in user
    public static String getLoggedInUserId() {
        return loggedInUserId;
    }
} 