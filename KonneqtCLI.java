//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.7.4
//DEPS org.jline:jline:3.21.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.Terminal.Signal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


import java.io.IOException;

@Command(name = "menu", mixinStandardHelpOptions = true, description = "Konneqt CLI with ASCII Art and Colors")
public class KonneqtCLI implements Runnable {

    // ANSI Colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_PURPLE = "\u001B[35m";

    private boolean running = true; // Controls the loop

    public void run() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .variable(LineReader.LIST_MAX, 50) // Enable ANSI support
                    .build();

            // Print Konneqt ASCII Art
            printAsciiArt();

            // Handle Ctrl+C to exit safely
            terminal.handle(Signal.INT, signal -> {
                System.out.println("\n" + ANSI_RED + "❌ Ctrl+C detected! Exiting..." + ANSI_RESET);
                running = false;
            });

            // Shutdown hook for exit message
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n" + ANSI_YELLOW + "👋 Konneqt CLI closed!" + ANSI_RESET);
            }));

            while (running) {
                System.out.println(ANSI_CYAN + "\n=== Select one of the options bellow ===" + ANSI_RESET);
                System.out.println(ANSI_GREEN + "1. Check Docker Installation" + ANSI_RESET);
                System.out.println(ANSI_YELLOW + "2. Download the Product" + ANSI_RESET);
                System.out.println(ANSI_RED + "3. Exit" + ANSI_RESET);

                try {
                    String choice = lineReader.readLine(ANSI_BLUE + "Choose an option: " + ANSI_RESET);

                    switch (choice) {
                        case "1":
                        System.out.println(ANSI_YELLOW + DockerCheck.execute() + ANSI_RESET);
                            break;
                        case "2":
                             System.out.println(ANSI_YELLOW + FileDownloader.execute("https://releases.ubuntu.com/24.04.1/ubuntu-24.04.1-desktop-amd64.iso", "sample.iso") + ANSI_RESET);
                            //System.out.println(ANSI_YELLOW + "📅 Date & Time: " + java.time.LocalDateTime.now() + ANSI_RESET);
                            break;
                        case "3":
                            System.out.println(ANSI_RED + "👋 Exiting..." + ANSI_RESET);
                            running = false;
                            break;
                        default:
                            System.out.println(ANSI_RED + "❌ Invalid option! Try again." + ANSI_RESET);
                    }
                } catch (UserInterruptException e) {
                    System.out.println("\n" + ANSI_RED + "❌ Interruption detected (Ctrl+C). Closing..." + ANSI_RESET);
                    running = false; // Exit loop
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printAsciiArt() {
        String asciiArt = ANSI_PURPLE + """
                                                     
,--.                                          ,--.   
|  |,-. ,---. ,--,--, ,--,--,  ,---.  ,---. ,-'  '-. 
|     /| .-. ||      \\|      \\| .-. :| .-. |'-.  .-' 
|  \\  \\' '-' '|  ||  ||  ||  |\\   --.' '-' |  |  |   
`--'`--'`---' `--''--'`--''--' `----' `-|  |  `--'   
                                        `--'         
        """ + ANSI_RESET;

        System.out.println(asciiArt);
        System.out.println(ANSI_CYAN + "Welcome to Konneqt CLI!" + ANSI_RESET);
    }

    public static void main(String[] args) {
        clearTerminal();
        int exitCode = new CommandLine(new KonneqtCLI()).execute(args);
        System.exit(exitCode);
    }

    private static void clearTerminal() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows: Run "cls" via cmd
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Linux/macOS: Use ANSI escape code or "clear" command
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("⚠ Could not clear the terminal.");
        }
    }
}



class DockerCheck {
   
    public static String execute() {
        try {
            Process process = new ProcessBuilder("docker", "-v").start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return " 🐬 Docker is running";
            } else {
                return "🔴 Docker is not running/installed!";
            }
        } catch (IOException | InterruptedException e) {
            return "🔴 Some error happened + " + e.getMessage() ;

        }
    }

}

class FileDownloader {

    public static String execute(String fileURL, String savePath) throws IOException {
        System.out.println("🔄 Downloading: " + fileURL);

        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int fileSize = connection.getContentLength();

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(savePath)) {

            byte[] buffer = new byte[1024]; // 1 KB buffer (make it larger for larger files) 
            int bytesRead;
            int chunkSize = 1024000; // Print a dot for every 10 KB / 10240 = 10 bytes = 
            int totalRead = 0;

            System.out.print("📥 Progress: ");

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (totalRead % chunkSize == 0) {
                    System.out.print("┃"); // Print dot for every 10 KB downloaded
                    System.out.flush(); // Flush output to ensure dots appear immediately
                }
            }
        }

        return "\n✅ Download completed: " + savePath;
    }


}
