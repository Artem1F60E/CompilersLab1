package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Analyzer {

    private static List<List<String>> table;
    private static final String INCORRECT_PROGRAM = "\nIncorrect program - ";
    private static final Pattern textPattern = Pattern.compile("[a-z][a-zA-Z\\d]*");
    private static final Pattern numberPattern = Pattern.compile("-?\\d+");

    public static void main(String[] args) throws IOException {
        table = Files.readAllLines(Paths.get("src/main/resources/table.csv")).stream().map(line -> List.of(line.split(",", -1))).collect(Collectors.toList());

        String input = new Scanner(System.in).nextLine();
        System.out.println("\n");

        Stack<String> stack = new Stack<>();
        stack.add("$");
        stack.add("stmt_list");
        input += " $";

        List<String> inputList = new ArrayList<>(List.of(input.split(" ")));

        while (!inputList.get(0).equals("$")) {
            String X = stack.peek();
            String in = inputList.get(0);
            if (numberPattern.matcher(in).matches()) {
                in = "INT";
            } else if (textPattern.matcher(in).matches()) {
                if (!in.equals("if") && !in.equals("else")) {
                    in = "ID";
                }
            }
            if (isNonTermExist(X) || X.equals("$")) {
                if (X.equals(in)) {
                    stack.pop();
                    inputList.remove(0);
                } else {
                    System.out.println(INCORRECT_PROGRAM + input);
                    return;
                }
            } else {
                try {
                    String rule = getRule(stack.peek(), in);
                    if (rule.equals("ϵ")) {
                        System.out.println(stack.pop() + " ::= " + rule);
                        continue;
                    }

                    String[] spl = rule.split("::=");
                    String[] ruleTokens = spl.length == 1 ? new String[]{} : spl[1].trim().split(" ");
                    String non = stack.pop();

                    if (ruleTokens.length != 0)
                        for (int k = ruleTokens.length - 1; k >= 0; k--) {
                            stack.add(ruleTokens[k]);
                        }

                    System.out.println(non + " ::= " + String.join(" ", ruleTokens));
                } catch (RuntimeException e) {
                    System.out.println(INCORRECT_PROGRAM + input);
                    e.printStackTrace();
                    return;
                }
            }
        }
        System.out.println("\nProgram is correct");
    }

    private static String getRule(String nonTerm, String term) {
        int termColumn = table.get(0).indexOf(term);
        if (termColumn == -1)
            throw new RuntimeException("Not found terminal - " + term);

        return table.stream().filter(row -> row.get(0).equals(nonTerm)).findFirst()
                .orElseThrow(() -> new RuntimeException("Not found not terminal - " + nonTerm))
                .get(termColumn);
    }

    private static boolean isNonTermExist(String nonTerm) {
        return table.get(0).contains(nonTerm);
    }
}