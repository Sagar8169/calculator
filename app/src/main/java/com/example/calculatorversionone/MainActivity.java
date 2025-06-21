package com.example.calculatorversionone;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import java.util.ArrayList;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView screen;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        // Initialize AdView
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Initialize buttons and screen
        initializeCalculator();
    }

    private void initializeCalculator() {
        Button on = findViewById(R.id.on);
        Button off = findViewById(R.id.off);
        Button ac = findViewById(R.id.ac);
        Button del = findViewById(R.id.del);
        Button equal = findViewById(R.id.equal);
        Button point = findViewById(R.id.point);

        screen = findViewById(R.id.screen);

        // Number Buttons
        ArrayList<Button> nums = new ArrayList<>();
        int[] numIds = {R.id.num0, R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6, R.id.num7, R.id.num8, R.id.num9};
        for (int id : numIds) {
            nums.add(findViewById(id));
        }

        // Add listeners to numeric buttons
        for (Button b : nums) {
            b.setOnClickListener(view -> {
                String currentText = screen.getText().toString();
                if ("0".equals(currentText)) {
                    screen.setText(b.getText().toString());
                } else {
                    screen.setText(currentText + b.getText().toString());
                }
            });
        }

        // Operator Buttons
        ArrayList<Button> opers = new ArrayList<>();
        int[] operIds = {R.id.div, R.id.times, R.id.plus, R.id.min};
        for (int id : operIds) {
            opers.add(findViewById(id));
        }

        // Add listeners to operator buttons
        for (Button b : opers) {
            b.setOnClickListener(view -> {
                String currentText = screen.getText().toString();
                // Prevent multiple operators in a row
                if (currentText.length() > 0 && !isOperator(currentText.charAt(currentText.length() - 1))) {
                    screen.setText(currentText + b.getText().toString());
                }
            });
        }

        // AC Button
        ac.setOnClickListener(view -> screen.setText("0"));

        // On/Off Buttons
        on.setOnClickListener(view -> {
            screen.setVisibility(View.VISIBLE);
            screen.setText("0");
        });

        off.setOnClickListener(view -> screen.setVisibility(View.GONE));

        // Delete Button
        del.setOnClickListener(view -> {
            String currentText = screen.getText().toString();
            if (currentText.length() > 1) {
                screen.setText(currentText.substring(0, currentText.length() - 1));
            } else {
                screen.setText("0");
            }
        });

        // Decimal Point Button
        point.setOnClickListener(view -> {
            String currentText = screen.getText().toString();
            if (!currentText.contains(".")) {
                screen.setText(currentText + ".");
            }
        });

        // Equal Button Logic - handle multiple operations
        equal.setOnClickListener(view -> {
            String screenText = screen.getText().toString().trim();

            // Validate input: Expression should not be empty and should not end with an operator
            if (screenText.isEmpty() || isOperator(screenText.charAt(screenText.length() - 1))) {
                screen.setText("Error: Invalid Expression");
                return;
            }

            // Check for empty expression or operator at the end of the expression
            if (screenText.isEmpty() || screenText.equals("-") || screenText.equals("+")) {
                screen.setText("Error: Invalid Expression");
                return;
            }

            // Use a background thread to evaluate the expression to prevent UI freezing
            new Thread(() -> {
                try {
                    // Evaluate the expression safely using the evaluateExpression method
                    double result = evaluateExpression(screenText);
                    runOnUiThread(() -> screen.setText(String.valueOf(result)));
                } catch (ArithmeticException e) {
                    runOnUiThread(() -> screen.setText("Error: Divide by 0"));
                } catch (Exception e) {
                    runOnUiThread(() -> screen.setText("Error"));
                }
            }).start();
        });

    }




    // Helper method to check if the character is an operator
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == 'x';
    }

    // Evaluate the expression considering operator precedence (PEMDAS)
    private double evaluateExpression(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            char currentChar = expression.charAt(i);

            // If current character is a number, handle it
            if (Character.isDigit(currentChar) || currentChar == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                numbers.push(Double.parseDouble(num.toString()));
            }

            // If current character is an operator, handle it
            else if (isOperator(currentChar)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(currentChar)) {
                    processOperator(numbers, operators);
                }
                operators.push(currentChar);
                i++;
            }
        }

        // Process remaining operators
        while (!operators.isEmpty()) {
            processOperator(numbers, operators);
        }

        return numbers.pop();
    }

    // Get precedence of operators
    private int precedence(char operator) {
        if (operator == '*' || operator == '/') {
            return 2; // Higher precedence for * and /
        }
        if (operator == '+' || operator == '-') {
            return 1; // Lower precedence for + and -
        }
        return 0;
    }

    // Process the operator from the stack
    private void processOperator(Stack<Double> numbers, Stack<Character> operators) {
        char operator = operators.pop();
        double b = numbers.pop();
        double a = numbers.pop();
        double result = 0;

        switch (operator) {
            case '+':
                result = a + b;
                break;
            case '-':
                result = a - b;
                break;
            case '*':
            case 'x':
                result = a * b;
                break;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                result = a / b;
                break;
        }

        numbers.push(result);
    }
}

