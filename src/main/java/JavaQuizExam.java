import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JavaQuizExam {
    private static final String USERS_FILE_PATH = "./src/main/resources/users.json";
    private static final String QUIZ_FILE_PATH = "./src/main/resources/quiz.json";
    private static final int NUM_QUESTIONS = 10;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("System:> Enter your username");
        String username = scanner.nextLine();
        System.out.println("System:> Enter password");
        String password = scanner.nextLine();

        JSONObject user = authenticateUser(username, password);
        if (user == null) {
            System.out.println("System:> Invalid username or password. Exiting...");
            return;
        }

        String role = (String) user.get("role");
        if (role.equals("admin")) {
            System.out.println("System:> Welcome admin! Please create new questions in the question bank.");
            addQuestions(scanner);
        } else if (role.equals("student")) {
            System.out.println("System:> Welcome " + username + " to the quiz! We will throw you " + NUM_QUESTIONS + " questions. Each MCQ mark is 1 and no negative marking. Are you ready? Press 's' to start.");
            String input = scanner.nextLine();
            if (input.equals("s")) {
                List<JSONObject> quizQuestions = getQuizQuestions(NUM_QUESTIONS);
                int score = startQuiz(scanner, quizQuestions);
                displayResult(score);
            }
        }
    }

    private static JSONObject authenticateUser(String username, String password) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray users = (JSONArray) parser.parse(new FileReader(USERS_FILE_PATH));
            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                String userUsername = (String) user.get("username");
                String userPassword = (String) user.get("password");
                if (username.equals(userUsername) && password.equals(userPassword)) {
                    return user;
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addQuestions(Scanner scanner) {
        JSONArray questions = new JSONArray();
        JSONParser parser = new JSONParser();

        try {
            questions = (JSONArray) parser.parse(new FileReader(QUIZ_FILE_PATH));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        while (true) {
            JSONObject question = new JSONObject();
            System.out.println("System:> Input your question");
            String questionText = scanner.nextLine();
            System.out.println("Admin:> Input option 1:");
            String option1 = scanner.nextLine();
            System.out.println("Admin:> Input option 2:");
            String option2 = scanner.nextLine();
            System.out.println("Admin:> Input option 3:");
            String option3 = scanner.nextLine();
            System.out.println("Admin:> Input option 4:");
            String option4 = scanner.nextLine();
            System.out.println("System:> What is the answer key?");
            int answerKey = scanner.nextInt();

            question.put("question", questionText);
            question.put("option 1", option1);
            question.put("option 2", option2);
            question.put("option 3", option3);
            question.put("option 4", option4);
            question.put("answerkey", answerKey);

            questions.add(question);

            System.out.println("System:> Saved successfully! Do you want to add more questions? (press 's' for start and 'q' for quit)");
            scanner.nextLine();
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }
        }

        try (FileWriter fileWriter = new FileWriter(QUIZ_FILE_PATH)) {
            fileWriter.write(questions.toJSONString());
            System.out.println("System:> Questions saved to " + QUIZ_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<JSONObject> getQuizQuestions(int numQuestions) {
        List<JSONObject> quizQuestions = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            JSONArray questions = (JSONArray) parser.parse(new FileReader(QUIZ_FILE_PATH));
            int totalQuestions = questions.size();
            if (totalQuestions < numQuestions) {
                System.out.println("System:> Insufficient questions in the quiz bank. Please add more questions.");
                return quizQuestions;
            }

            Random random = new Random();
            Set<Integer> selectedQuestionIndices = new HashSet<>();

            while (selectedQuestionIndices.size() < numQuestions) {
                int randomIndex = random.nextInt(totalQuestions);
                if (!selectedQuestionIndices.contains(randomIndex)) {
                    JSONObject question = (JSONObject) questions.get(randomIndex);
                    quizQuestions.add(question);
                    selectedQuestionIndices.add(randomIndex);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return quizQuestions;
    }

    private static int startQuiz(Scanner scanner, List<JSONObject> quizQuestions) {
        int score = 0;
        int questionNum = 1;

        for (JSONObject question : quizQuestions) {
            System.out.println("[Question " + questionNum + "] " + question.get("question"));
            System.out.println("1. " + question.get("option 1"));
            System.out.println("2. " + question.get("option 2"));
            System.out.println("3. " + question.get("option 3"));
            System.out.println("4. " + question.get("option 4"));

            System.out.print("Student:> ");
            int userAnswer = scanner.nextInt();
            int answerKey = ((Long) question.get("answerkey")).intValue();

            if (userAnswer == answerKey) {
                score++;
            }

            questionNum++;
        }

        return score;
    }

    private static void displayResult(int score) {
        System.out.println("Quiz has been completed successfully!");

        if (score >= 8) {
            System.out.println("Excellent! You have scored " + score + " out of " + NUM_QUESTIONS);
        } else if (score >= 5) {
            System.out.println("Good. You have scored " + score + " out of " + NUM_QUESTIONS);
        } else if (score >= 2) {
            System.out.println("Very poor! You have scored " + score + " out of " + NUM_QUESTIONS);
        } else {
            System.out.println("Very sorry you are failed. You have scored " + score + " out of " + NUM_QUESTIONS);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Would you like to start again? Press 's' for start or 'q' for quit");
        String input = scanner.nextLine();
        if (input.equals("s")) {
            System.out.println();
            main(null);
        }
    }
}
