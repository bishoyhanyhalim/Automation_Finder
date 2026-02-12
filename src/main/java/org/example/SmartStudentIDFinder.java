package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class SmartStudentIDFinder {

    private static final Map<Character, Integer> ARABIC_ALPHABET = new HashMap<>();

    static {
        char[] arabicLetters = {
                'ا', 'ب', 'ت', 'ث', 'ج', 'ح', 'خ', 'د', 'ذ', 'ر',
                'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف',
                'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'و', 'ي'
        };

        for (int i = 0; i < arabicLetters.length; i++) {
            ARABIC_ALPHABET.put(arabicLetters[i], i + 1);
        }

        ARABIC_ALPHABET.put('أ', 1);
        ARABIC_ALPHABET.put('إ', 1);
        ARABIC_ALPHABET.put('آ', 1);
        ARABIC_ALPHABET.put('ء', 1);
        ARABIC_ALPHABET.put('ة', 5);
        ARABIC_ALPHABET.put('ى', 28);
    }

    private static String normalizeArabicText(String text) {
        if (text == null) return "";
        text = text.replaceAll("[\\u064B-\\u065F\\u0670]", "");
        text = text.replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا');
        text = text.replace('ة', 'ه').replace('ى', 'ي');
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Extract first two words (first name and last name) from a full name
     */
    private static String extractFirstTwoNames(String fullName) {
        String normalized = normalizeArabicText(fullName);
        String[] parts = normalized.split("\\s+");

        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        } else if (parts.length == 1) {
            return parts[0];
        }
        return normalized;
    }

    private static boolean namesMatch(String targetName, String resultName) {
        String targetFirstTwo = extractFirstTwoNames(targetName).toLowerCase();
        String resultFirstTwo = extractFirstTwoNames(resultName).toLowerCase();

        // Debug output
        System.out.print("(Comparing: '" + targetFirstTwo + "' vs '" + resultFirstTwo + "') ");

        return targetFirstTwo.equals(resultFirstTwo);
    }

    private static int compareArabicNames(String name1, String name2) {
        String n1 = normalizeArabicText(name1).replace(" ", "");
        String n2 = normalizeArabicText(name2).replace(" ", "");

        int minLen = Math.min(n1.length(), n2.length());

        for (int i = 0; i < minLen; i++) {
            Integer v1 = ARABIC_ALPHABET.getOrDefault(n1.charAt(i), 0);
            Integer v2 = ARABIC_ALPHABET.getOrDefault(n2.charAt(i), 0);

            if (!v1.equals(v2)) return v1.compareTo(v2);
        }

        return Integer.compare(n1.length(), n2.length());
    }

    private static String getStudentName(WebDriver driver, WebDriverWait wait, int studentID, String websiteURL)
            throws Exception {
        driver.get(websiteURL);

        WebElement textbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id=\"contentdata\"]/tbody/tr[5]/td/form/div/input[1]")));
        textbox.clear();
        textbox.sendKeys(String.valueOf(studentID));

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"submit1\"]")));
        submitButton.click();

        Thread.sleep(800);

        try {
            WebElement nameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id=\"rsval\"]/table/tbody/tr[2]/th")));
            return nameElement.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Take screenshot and save it
     */
    private static String takeScreenshot(WebDriver driver, String studentName, String studentID) {
        try {
            // Create screenshots directory if it doesn't exist
            File screenshotDir = new File("screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Generate filename with timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String sanitizedName = studentName.replaceAll("[^a-zA-Z0-9\\u0600-\\u06FF\\s]", "").trim().replaceAll("\\s+", "_");
            String filename = "Student_" + studentID + "_" + sanitizedName + "_" + timestamp + ".png";

            // Take screenshot
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destinationFile = new File(screenshotDir, filename);

            // Copy file
            org.apache.commons.io.FileUtils.copyFile(screenshot, destinationFile);

            System.out.println("\n✓ Screenshot saved: " + destinationFile.getAbsolutePath());
            return destinationFile.getAbsolutePath();

        } catch (Exception e) {
            System.out.println("\n✗ Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save student information to text file
     */
    private static void saveStudentInfo(String studentName, String studentID, int attempts, long timeTaken, String screenshotPath) {
        try {
            // Create results directory if it doesn't exist
            File resultsDir = new File("results");
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }

            // Generate filename
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String filename = "Student_Info_" + studentID + "_" + timestamp + ".txt";
            File resultFile = new File(resultsDir, filename);

            // Write information
            try (PrintWriter writer = new PrintWriter(new FileWriter(resultFile))) {
                writer.println("╔════════════════════════════════════════════════╗");
                writer.println("║         STUDENT INFORMATION REPORT             ║");
                writer.println("╚════════════════════════════════════════════════╝");
                writer.println();
                writer.println("Search Date/Time: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();
                writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                writer.println("STUDENT DETAILS:");
                writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                writer.println();
                writer.println("Student Name: " + studentName);
                writer.println("Student ID: " + studentID);
                writer.println();
                writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                writer.println("SEARCH STATISTICS:");
                writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                writer.println();
                writer.println("Search Attempts: " + attempts);
                writer.println("Time Taken: " + timeTaken + " seconds");
                writer.println("Efficiency: " + String.format("%.1f", (900.0 / attempts)) + "x faster than linear search");
                writer.println();

                if (screenshotPath != null) {
                    writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    writer.println("SCREENSHOT:");
                    writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    writer.println();
                    writer.println("Screenshot Path: " + screenshotPath);
                    writer.println();
                }

                writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                writer.println("Generated by Ultra Smart Student ID Finder");
                writer.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            System.out.println("✓ Student information saved: " + resultFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("✗ Failed to save student information: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   Ultra Smart Student ID Finder (Adaptive)    ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        System.out.print("Enter student first name (الاسم الأول): ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Enter student last name (الاسم الثاني): ");
        String lastName = scanner.nextLine().trim();

        String targetName = firstName + " " + lastName;

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Searching for: " + targetName);
        System.out.println("Note: Will match first two names only");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // REPLACE WITH YOUR URL
        String websiteURL = "YOUR_WEBSITE_URL";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        int attempts = 0;
        long startTime = System.currentTimeMillis();
        boolean found = false;
        String foundStudentName = "";
        String foundStudentID = "";

        try {
            // True binary search with boundaries
            int low = 45001;
            int high = 46900;
            int mid;

            System.out.println("Using binary search algorithm...\n");

            while (low <= high) {
                mid = low + (high - low) / 2;
                attempts++;

                String resultName = getStudentName(driver, wait, mid, websiteURL);

                if (resultName == null || resultName.isEmpty()) {
                    System.out.printf("Attempt %d: ID %d - No student, adjusting range\n", attempts, mid);
                    low = mid + 1;
                    continue;
                }

                System.out.printf("Attempt %d: ID %d | Found: %s | ", attempts, mid, resultName);

                // Check if first two names match
                if (namesMatch(targetName, resultName)) {
                    // STUDENT FOUND!
                    WebElement idElement = driver.findElement(
                            By.xpath("//*[@id=\"rsval\"]/table/tbody/tr[3]/td[2]"));
                    String displayedID = idElement.getText().trim();

                    long totalTime = (System.currentTimeMillis() - startTime) / 1000;

                    foundStudentName = resultName;
                    foundStudentID = displayedID;
                    found = true;

                    System.out.println("✓ MATCH!\n");
                    System.out.println("╔════════════════════════════════════════════════╗");
                    System.out.println("║           ✓ STUDENT FOUND!                     ║");
                    System.out.println("╚════════════════════════════════════════════════╝");
                    System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.out.println("STUDENT INFORMATION:");
                    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.out.println("\nFull Name: " + resultName);
                    System.out.println("Student ID: " + displayedID);
                    System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.out.println("SEARCH STATISTICS:");
                    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.out.println("\nAttempts: " + attempts);
                    System.out.println("Time taken: " + totalTime + " seconds");
                    System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

                    // Ask if user wants to take screenshot
                    System.out.print("Do you want to take a screenshot? (y/n): ");
                    String screenshotChoice = scanner.nextLine().trim().toLowerCase();

                    String screenshotPath = null;
                    if (screenshotChoice.equals("y") || screenshotChoice.equals("yes")) {
                        screenshotPath = takeScreenshot(driver, resultName, displayedID);
                    }

                    // Save student information to file
                    saveStudentInfo(resultName, displayedID, attempts, totalTime, screenshotPath);

                    System.out.println("\n✓ All results saved successfully!");

                    break;

                } else {
                    // Use alphabetical comparison for binary search
                    int comparison = compareArabicNames(resultName, targetName);

                    if (comparison < 0) {
                        System.out.println("Range: " + (mid + 1) + "-" + high);
                        low = mid + 1;
                    } else {
                        System.out.println("Range: " + low + "-" + (mid - 1));
                        high = mid - 1;
                    }
                }

                Thread.sleep(500);
            }

            if (!found) {
                long totalTime = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("\n╔════════════════════════════════════════════════╗");
                System.out.println("║           ✗ STUDENT NOT FOUND                  ║");
                System.out.println("╚════════════════════════════════════════════════╝");
                System.out.println("\nAttempts: " + attempts);
                System.out.println("Time: " + totalTime + " seconds");
                System.out.println("\nPossible reasons:");
                System.out.println("- First/Last name spelling is different on website");
                System.out.println("- Student ID is outside 45001-45900 range");
                System.out.println("- Student doesn't exist in database");
            }

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\nPress Enter to close browser...");
            scanner.nextLine();
            driver.quit();
            scanner.close();

            // Display final summary
            if (found) {
                System.out.println("\n╔════════════════════════════════════════════════╗");
                System.out.println("║              FINAL RESULT                      ║");
                System.out.println("╚════════════════════════════════════════════════╝");
                System.out.println("\nFull Student Name: " + foundStudentName);
                System.out.println("Student ID: " + foundStudentID);
                System.out.println("\nResults saved in 'results' folder");
                System.out.println("Screenshots saved in 'screenshots' folder (if taken)");
                System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }
}