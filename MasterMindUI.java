import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class MasterMindUI {
    private static final String[] COLOURS = {"R", "G", "B", "Y", "O", "P"}; // Stores the colours; Red, Green, Blue, Yellow, Orange, and Purple
    private static final int DefCodeLength = 4; //Default Game mode code length of 4
    private static final int DefMaxAttempts = 6;//Default Game mode maximum number of attempts = 6
    private static int userScore = 0;
    private static int computerScore = 0;
    private static int roundNum = 1;
    private static SwiftBotAPI swiftbot;
    private static final String fileLog = "game_log.txt"; //logs fileLog into a txt file called game_log.txt
    private static boolean playAgain = false;
    private static final Object lock = new Object();

    public static void main(String[] args) throws Exception {
        clearLogFile(); //Clears the text file 

        swiftbot = new SwiftBotAPI();
        swiftbot.setButtonLight(Button.A, true); //Switches on the button light for Button A on the Swiftbot
        swiftbot.setButtonLight(Button.B, true); //Switches on the button light for Button B on the Swiftbot
        System.out.println("Press Button A for the Default Game Mode or Button B for the Custom Game Mode.");

        swiftbot.enableButton(Button.A, () -> {
            swiftbot.setButtonLight(Button.A, false);
            swiftbot.setButtonLight(Button.B, false);
            swiftbot.disableButton(Button.A);
            startGame(false); //Game starts in default game mode 
        });
        
        
        
        

        swiftbot.enableButton(Button.B, () -> {
            swiftbot.setButtonLight(Button.A, false);
            swiftbot.setButtonLight(Button.B, false);
            swiftbot.disableButton(Button.B);
            startGame(true); //Game starts in custom game mode
        });
    }

    private static void clearLogFile() {
        try (FileWriter writer = new FileWriter(fileLog, false)) {  //Tries to overwrite the file with an empty file
        } catch (IOException e) {
            System.out.println("Error clearing log file: " + e.getMessage()); //Prints out an error message if the file cannot be overwritten 
        }
    }

    private static void startGame(boolean customMode) { //Custom Game Mode method
        Scanner scanner = new Scanner(System.in); //Reads input from the user 
        int codeLength = DefCodeLength;
        int maxAttempts = DefMaxAttempts;

        if (customMode) { //Checks if the custom game mode option is selected 
            System.out.print("Enter the number of colours you would like in the randomized colour code. Choose between 3-6 colours: ");
            while (true) {
                if (scanner.hasNextInt()) { //Checks if user has entered an integer value 
                    codeLength = scanner.nextInt(); //If valid integer value has been entered, program updates the code length with the valid integer value
                    if (codeLength >= 3 && codeLength <= 6) { // checks if the entered integer value is within the range 
                        break;
                    }
                }
                System.out.print("Invalid input. Enter a number between 3 and 6: ");
                scanner.nextLine();
            }

            System.out.print("Enter the maximum number of guesses: ");
            while (true) { 
                if (scanner.hasNextInt()) { 
                    maxAttempts = scanner.nextInt();
                    if (maxAttempts > 0) { //cannot have 0 guesses. Checks to see if the number of guesses is greater than 0
                        break;
                    }
                }
                System.out.print("Invalid input. Enter a valid positive integer number: ");
                scanner.nextLine();
            }
            scanner.nextLine();
        }

        do { //Ensures game is played atleast once
            String[] code = generateCode(codeLength); //generates random code based on the code length entered
            int attemptsLeft = maxAttempts; //sets attempts left to the maximum number of guesses entered earlier 
            System.out.println("Round " + roundNum + " begins!!! Code length: " + codeLength); //Displays the current round 
            
            String[] userGuess = null;

            while (attemptsLeft > 0) { //Game continues to run as long as attempts left is greater than 0
                System.out.println("Enter your guess by scanning colour cards.");
                
                try {
                    userGuess = scanColours(swiftbot, scanner, codeLength); //calls the scanColours method
                } catch (Exception e) {
                    System.out.println("An error occurred while scanning colours: " + e.getMessage());
                    e.printStackTrace(); //Helps to trace the error 
                    return;
                }

                String userFeedback = getUserFeedback(code, userGuess); //calls the getUserFeedback to compare the user's guess with the program generated code
                System.out.println("Feedback: " + userFeedback); //prints out the '+' or '-' symbols based on the user's guess
                System.out.println("Your guess: " + String.join("", userGuess)); //Prints the users guess   

                if (userFeedback.equals("+".repeat(codeLength))) { //checks if the feedback contains all the '+' symbols to see if the user has won the game
                    System.out.println("Congratulations! You guessed the code."); //Prints out a congratulatory message
                    userScore++; //Increases the users score by 1
                    break;
                }

                attemptsLeft--; //if the guess was incorrect, the number of attempts left decreases by 1 
                System.out.println("Attempts left: " + attemptsLeft);
            }

            if (attemptsLeft == 0) { //checks to see if no attempts are left
                System.out.println("Game over! The correct code was: " + String.join("", code));
                computerScore++; //computers score is increased by 1 
            }

            logGameDetails(roundNum, code, userScore, computerScore, maxAttempts, attemptsLeft, String.join("", userGuess)); //logs the details of the game to the text file called 'game_log'
            roundNum++; //Round Number is increased by 1 

            System.out.println("Score - Player: " + userScore + " | Computer: " + computerScore);
            System.out.println("Press Button Y to play again or Button X to exit.");

            
            waitForButtonPress(); //waits for user to press button 'X' to quit the game or button 'Y' to play again
        } while (playAgain); // the game repeats if Button 'Y' is pressed 
    }

    private static String[] generateCode(int length) {
        List<String> colours = new ArrayList<>(); //Generates an empty string to store the colours
        Collections.addAll(colours, COLOURS); //Adds all the colours from the COLOURS arrayList
        Collections.shuffle(colours); //Randomly rearranges the colours in the colours arrayList
        return colours.subList(0, length).toArray(new String[0]); //Takes a sub list from coloursArray starting at index 0 and ending at the given length from the user 
    }

    private static String[] scanColours(SwiftBotAPI bot, Scanner scanner, int length) {
        String[] userGuess = new String[length]; //Creates an array and stores the colours that the user scans 
        for (int i = 0; i < length; i++) {
            System.out.print("Scan colour card " + (i + 1) + ": "); //Prompts the user to scan colour card 1 then 2 and so on and so forth
            scanner.nextLine(); // Waits for user to hit Enter 

            
            BufferedImage img = bot.takeStill(ImageSize.SQUARE_720x720);  // Captures the image after the user hits Enter. Image size is set to 720x720
            try {
                String imagePath = "/data/home/pi/ColourCard_" + (i + 1) + ".jpg"; //Creates a file path where the image will be saved 
                ImageIO.write(img, "jpg", new File(imagePath)); //Saves the captured image as a JPEG file

                
                userGuess[i] = detectColour(imagePath); //Calls the detectColour method to detect the colour of the scanned colour card
            } catch (IOException e) {
                System.out.println("Error saving image: " + e.getMessage()); //Prints an error message if there was an issue saving the image
                userGuess[i] = "Unknown"; // Default colour is set to 'Unknown if all else fails'
            }
        }
        return userGuess; //Returns userGuess Array which contain the scanned colour cards
    }

    private static String detectColour(String imagePath) { 
        try {
            BufferedImage img = ImageIO.read(new File(imagePath)); //Tries to read an image from the given file path
            int width = img.getWidth(); //Gets the images width
            int height = img.getHeight(); //Gets the images height

            
            int redTotal = 0, greenTotal = 0, blueTotal = 0; //Initializes variables for RGB values
            int pixelCount = 0; //Tracks number of pixels processed 

            
            for (int y = 0; y < height; y++) { //Loops through each pixel in the image
                for (int x = 0; x < width; x++) {
                    Color pixelColour = new Color(img.getRGB(x, y)); //Gets the colour of the pixel and converts it into a colour object
                    redTotal += pixelColour.getRed(); //Adds RGB values to either Red, Green or Blue
                    greenTotal += pixelColour.getGreen(); //Adds RGB values to either Red, Green or Blue
                    blueTotal += pixelColour.getBlue(); //Adds RGB values to either Red, Green or Blue
                    pixelCount++; //Pixelcount increases 
                }
            }

           
            int avgRed = redTotal / pixelCount; //Calculates the average RGB value by dividing the total colour values by the number of pixels 
            int avgGreen = greenTotal / pixelCount; //Calculates the average RGB value by dividing the total colour values by the number of pixels
            int avgBlue = blueTotal / pixelCount; //Calculates the average RGB value by dividing the total colour values by the number of pixels

            
            String closestColour = "Unknown"; 
            int minDiff = Integer.MAX_VALUE; //Finds the smallest difference between the detected colour and the pre defined set of colours

           
            for (String colour : COLOURS) { //Loops through the colours stored in the COLOURS method
                int colourDifference = 0;
                switch (colour) {
                    case "R":
                        colourDifference = Math.abs(avgRed - 255) + Math.abs(avgGreen) + Math.abs(avgBlue); //
                        break;
                    case "G":
                        colourDifference = Math.abs(avgRed) + Math.abs(avgGreen - 255) + Math.abs(avgBlue);
                        break;
                    case "B":
                        colourDifference = Math.abs(avgRed) + Math.abs(avgGreen) + Math.abs(avgBlue - 255);
                        break;
                    case "Y":
                        colourDifference = Math.abs(avgRed - 255) + Math.abs(avgGreen - 255) + Math.abs(avgBlue);
                        break;
                    case "O":
                        colourDifference = Math.abs(avgRed - 255) + Math.abs(avgGreen - 165) + Math.abs(avgBlue);
                        break;
                    case "P":
                        colourDifference = Math.abs(avgRed - 128) + Math.abs(avgGreen) + Math.abs(avgBlue - 128);
                        break;
                }

                
                if (colourDifference < minDiff) { //Updates colourDifference and minDiff
                    minDiff = colourDifference;
                    closestColour = colour;
                }
            }

            return closestColour; //Returns colour with the smallest RGB difference

        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private static String getUserFeedback(String[] code, String[] userGuess) {
        int length = code.length;
        boolean[] codeMatched = new boolean[length]; // Tracks matched positions in the code
        boolean[] guessMatched = new boolean[length]; // Tracks matched positions in the guess
        StringBuilder userFeedback = new StringBuilder();

         
        for (int i = 0; i < length; i++) { // Checks if the colour is an exact match and assigns a '+' symbol
            if (code[i].equals(userGuess[i])) {
                userFeedback.append("+");
                codeMatched[i] = true; // Mark this position as matched
                guessMatched[i] = true;
            }
        }

         
        for (int i = 0; i < length; i++) { // Checks if the colour is within the code but not in the position it is supposed to be in and assigns a '-' symbol to it
            if (!guessMatched[i]) { 
                for (int j = 0; j < length; j++) {
                    if (!codeMatched[j] && userGuess[i].equals(code[j])) {
                        userFeedback.append("-");
                        codeMatched[j] = true; 
                        break;
                    }
                }
            }
        }

        return userFeedback.toString(); // Ensures '+' symbol prints before the '-' symbol
    }


    private static void logGameDetails(int round, String[] code, int userScore, int computerScore, int maxAttempts, int attemptsLeft, String userGuess) {
        try (FileWriter writer = new FileWriter(fileLog, true)) {
            writer.write(String.format("Round %d: Code: %s, User Score: %d, Computer Score: %d, Max Attempts: %d, Attempts Left: %d, User Guess: %s\n", //Logs the game details into the txt file called game_log in this format
                    round, String.join("", code), userScore, computerScore, maxAttempts, attemptsLeft, userGuess));
        } catch (IOException e) {
            System.out.println("Error logging game details: " + e.getMessage()); //If file can not be updated; error message will be displayed
        }
    }

    private static void waitForButtonPress() { //Waits for the user to press a Button 
        swiftbot.disableButton(Button.Y);
        swiftbot.disableButton(Button.X);

        synchronized (lock) {
            swiftbot.enableButton(Button.Y, () -> { //Enables Button 'Y'
                synchronized (lock) {
                    playAgain = true; //User wants to play the game again
                    lock.notify();  
                }
                System.out.println("Starting a new game..."); //Prints a message to indicate the new game that is starting
            });

            swiftbot.enableButton(Button.X, () -> { //Enables button 'X'
                synchronized (lock) {
                    playAgain = false; //User would like to quit the game
                    lock.notify();  
                }
                System.out.println("Exiting the game..."); 
                try {
                    System.out.println("Game data logged at: " + new File(fileLog).getAbsolutePath()); //Prints out where the file is saved at (The Location of the log file)
                    System.exit(0); //Exits the programme
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            try {
                lock.wait();  //Nothing will happen until a button is pressed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
