import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

record Task(int id, String description, String status, Timestamp lastUpdated) {}

class TaskManager {
    private final Connection conn;

    public TaskManager() {
        Connection tempConn = null;
        try {
            tempConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/TaskDB", "postgres", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        conn = tempConn;
    }

    public synchronized void addTask(String description) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO tasks (description, status) VALUES (?, ?)")) {
            stmt.setString(1, description);
            stmt.setString(2, "Pending");
            stmt.executeUpdate();
            System.out.println("Added a new task");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateTask(int taskId, String newDescription, String newStatus) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE tasks SET description=?, status=?, last_updated=current_timestamp WHERE id=?")) {
            stmt.setString(1, newDescription);
            stmt.setString(2, newStatus);
            stmt.setInt(3, taskId);
            stmt.executeUpdate();
            System.out.println("Updated a task");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void deleteTask(int taskId) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE id=?")) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
            System.out.println("Deleted a task");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<Task> listTasks() {
        List<Task> tasks = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM tasks ORDER BY last_updated DESC")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    String status = rs.getString("status");
                    Timestamp lastUpdated = rs.getTimestamp("last_updated");
                    tasks.add(new Task(id, description, status, lastUpdated));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Task Manager!");

        while (true) {
            System.out.println("\nSelect an action:");
            System.out.println("1. Add Task");
            System.out.println("2. Update Task");
            System.out.println("3. Delete Task");
            System.out.println("4. List Tasks");
            System.out.println("5. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter task description: ");
                    String descriptionToAdd = scanner.nextLine();
                    taskManager.addTask(descriptionToAdd);
                    break;
                case 2:
                    System.out.print("Enter task ID to update: ");
                    int taskIdToUpdate = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Enter new description: ");
                    String newDescription = scanner.nextLine();
                    System.out.print("Enter new status: ");
                    String newStatus = scanner.nextLine();
                    taskManager.updateTask(taskIdToUpdate, newDescription, newStatus);
                    break;
                case 3:
                    System.out.print("Enter task ID to delete: ");
                    int taskIdToDelete = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    taskManager.deleteTask(taskIdToDelete);
                    break;
                case 4:
                    List<Task> tasks = taskManager.listTasks();
                    System.out.println("List of tasks:");
                    for (Task task : tasks) {
                        System.out.println("ID: " + task.id() + ", Description: " + task.description() + ", Status: " + task.status() + ", Last Updated: " + task.lastUpdated());
                    }
                    break;
                case 5:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }
}