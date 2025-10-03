import java.io.*;                    // Imports classes for file reading and writing
import java.util.ArrayList;         // Allows use of ArrayList for dynamic lists
import java.util.List;              // More flexible interface for list-based operations

/**
 * File_Utils - A utility class for handling file operations
 * Provides methods for reading, writing, and updating data in text files
 * Used throughout the application for data persistence
 */
public class File_Utils {

    /**
     * Reads all non-empty lines from a specified file
     * @param filename The path to the file to read
     * @return ArrayList<String> containing all non-empty lines from the file
     */
    public static ArrayList<String> readLines(String filename) {
        ArrayList<String> lines = new ArrayList<>();          // List to store all non-empty lines
        File file = new File(filename);                       // Create a File object pointing to the given filename

        // Debugging line to show the absolute path being read
        System.out.println("📂 File Path: " + file.getAbsolutePath());

        // If the file doesn't exist, log a warning and return an empty list
        if (!file.exists()) {
            System.out.println("⚠️ File not found: " + filename);
            return lines;
        }

        // Try-with-resources to safely read the file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {                 // Ignore blank lines
                    lines.add(line);                          // Add valid line to the list
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Error reading file: " + filename);  // Catch and log any read errors
        }
        return lines;  // Return the list of lines read
    }

    /**
     * Appends a single line to the end of a file
     * Opens file in append mode, so existing content is preserved
     * @param filename The path to the file to append to
     * @param line The line of text to append
     */
    public static void appendLine(String filename, String line) {
        // Try-with-resources to write to file in append mode
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(line);       // Write the line
            writer.newLine();         // Move to the next line
        } catch (IOException e) {
            System.out.println("⚠️ Error appending to file: " + filename);  // Log error
        }
    }

    /**
     * Overwrites a file with a new set of lines
     * Completely replaces existing content with the provided lines
     * @param filename The path to the file to write to
     * @param lines ArrayList of strings to write to the file
     */
    public static void writeLines(String filename, ArrayList<String> lines) {
        // FileWriter opened in overwrite mode (false = not appending)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false))) {
            for (String line : lines) {
                writer.write(line);   // Write each line from the list
                writer.newLine();     // Separate with a new line
            }
        } catch (IOException e) {
            System.out.println("⚠️ Error writing to file: " + filename);  // Log error
        }
    }

    /**
     * Updates a specific line in a file based on an identifier
     * Typically used to update status or other fields in a record
     * @param filename The path to the file to update
     * @param identifier The unique identifier (usually ID) to find the line to update
     * @param newStatus The new value to set in the 11th column (index 10)
     * 
     * Note: This method assumes CSV format with comma-separated values
     * and updates the 11th column (index 10) of the matching line
     */
    public static void updateLine(String filename, String identifier, String newStatus) {
        List<String> lines = readLines(filename);              // Read all current lines
        ArrayList<String> updatedLines = new ArrayList<>();    // New list to store modified content

        for (String line : lines) {
            String[] parts = line.split(",", -1);              // Split line by commas, keeping empty values
            if (parts.length > 0 && parts[0].equals(identifier)) {
                // Update the 11th column (index 10) with the new status
                parts[10] = newStatus;
                updatedLines.add(String.join(",", parts));     // Rebuild and store the updated line
            } else {
                updatedLines.add(line);                        // Keep original line if not matched
            }
        }

        writeLines(filename, updatedLines);  // Overwrite file with the new content
    }

    /**
     * Updates the stock quantity of an item in the data/items_data.txt file
     * Adds the specified quantity to the existing stock level
     * @param itemCode The unique code of the item to update
     * @param quantityToAdd The quantity to add to the current stock
     * 
     * Note: This method specifically works with data/items_data.txt
     * and updates the stock quantity in the 4th column (index 3)
     */
    public static void updateItemStock(String itemCode, int quantityToAdd) {
        List<String> lines = readLines("data/items_data.txt");      // Read all current item records
        ArrayList<String> updatedLines = new ArrayList<>();    // New list to store updated lines

        for (String line : lines) {
            String[] parts = line.split(",", -1);              // Split by commas, preserving all values
            if (parts.length > 0 && parts[0].equals(itemCode)) {
                // Update stock at 4th column (index 3)
                int currentStock = Integer.parseInt(parts[3]); // Convert existing stock to int
                parts[3] = String.valueOf(currentStock + quantityToAdd);  // Add quantity and update
                updatedLines.add(String.join(",", parts));     // Save updated record
            } else {
                updatedLines.add(line);                        // Keep unchanged record
            }
        }

        writeLines("data/items_data.txt", updatedLines);  // Save back to file
    }
}