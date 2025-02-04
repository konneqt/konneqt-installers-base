import java.io.*;

public class DockerComposeRunner {

    public static void runDockerCompose() {
        String command = "docker compose up -d";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sh", "-c", command); // Use "cmd", "/c" for Windows
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read and print output from the process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor(); // Wait for process to finish
            if (exitCode == 0) {
                System.out.println("✅ Docker Compose started successfully!");
            } else {
                System.err.println("❌ Error starting Docker Compose! Exit Code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Failed to execute Docker Compose: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        runDockerCompose();
    }
}
