import javax.swing.*;
import java.awt.event.*;
import java.text.*;
public class ODE_Applet
{
   static boolean debug = false;
   // This program does not have any precision beyond 1E-10
   static DecimalFormat noSci = new DecimalFormat("#.##########");
   static DecimalFormat xRound = new DecimalFormat("#.#############");

   public static void main(String[] args)
   {
      JFrame inputWindow = new JFrame("Numerical ODE solver. Euler and 4th "
            + "order Runge_Kutta methods.");
      inputWindow.setSize(1100, 400);
      inputWindow.setLocationRelativeTo(null);
      inputWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      //labels
      JLabel eqLabel = new JLabel("<html>Please enter your G(x,y). Use x "
            + "for dependent and y for independent. <br>This version supports "
            + "basic operators, e, parenthesis, and basic trig functions. <br>"
            + "Multiplication symbol is always necessary, even between "
            + "coefficients and variables.</html>");
      JLabel yPrimeLabel = new JLabel("y' = ");
      JLabel x0Label = new JLabel("Initial x value (use decimals rather than "
            + "fractions for non-integers): ");
      JLabel x0Equals = new JLabel("x initial = ");
      JLabel y0Label = new JLabel("Initial y value (use decimals rather than "
            + "fractions for non-integers): ");
      JLabel y0Equals = new JLabel("y initial = ");
      JLabel xFLabel = new JLabel("Final x value (use decimals rather than "
            + "fractions for non-integers): ");
      JLabel xFEquals = new JLabel("x final = ");
      JLabel hLabel = new JLabel("Initial incremetal value h (use decimals "
            + "rather than fractions for non-integers): ");
      JLabel hEquals = new JLabel("h = ");
      //fields
      final JTextField eqField = new JTextField(20);
      final JTextField x0Field = new JTextField(5);
      final JTextField y0Field = new JTextField(5);
      final JTextField xFField = new JTextField(5);
      final JTextField hField = new JTextField(5);
      //button
      JButton submit = new JButton("Compute");
      submit.setActionCommand("compute");
      submit.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            // TODO Auto-generated method stub

            String usrEquation = eqField.getText();
            usrEquation = removeSpacesFromEquation(usrEquation);
            String x0String = x0Field.getText();
            String y0String = y0Field.getText();
            String xFString = xFField.getText();
            String hString = hField.getText();
            
            if (legalInput(usrEquation, x0String, y0String, xFString,
                  hString))
            {
               double xo = Double.parseDouble(x0String);
               double yo = Double.parseDouble(y0String);
               double xf = Double.parseDouble(xFString);
               double h = Double.parseDouble(hString);
               if(legalDoubles(xo, xf, h))
               {
                  JFrame output = new JFrame("Output");
                  output.setSize(400, 700);
                  output.setLocationRelativeTo(null);
                  String outputString = "<html>" + 
                        euler(xo, yo, xf, h, usrEquation) +
                        "<br>" + rk4(xo, yo, xf, h, usrEquation) + "</html>";
                  JLabel outputText = new JLabel(outputString);
                  JPanel container = new JPanel();
                  container.add(outputText);
                  JScrollPane jsp = new JScrollPane(container);
                  output.add(jsp);
                  
                  //Display the window.
                  output.setVisible(true);
                  
               }
            }
         }
         
      });
    //grouplayout instructions
      GroupLayout layout = new GroupLayout(inputWindow.getContentPane());
      inputWindow.getContentPane().setLayout(layout);
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);
      //adds
      layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                  .addComponent(eqLabel)
                  .addComponent(x0Label)
                  .addComponent(y0Label)
                  .addComponent(xFLabel)
                  .addComponent(hLabel))
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.
                           TRAILING)
                        .addComponent(yPrimeLabel)
                        .addComponent(x0Equals)
                        .addComponent(y0Equals)
                        .addComponent(xFEquals)
                        .addComponent(hEquals))
                        .addGroup(layout.createParallelGroup(GroupLayout.
                                 Alignment.TRAILING)
                              .addComponent(eqField)
                              .addComponent(x0Field)
                              .addComponent(y0Field)
                              .addComponent(xFField)
                              .addComponent(hField)
                              .addComponent(submit)));
      layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                  .addComponent(eqLabel)
                  .addComponent(yPrimeLabel)
                  .addComponent(eqField))
                  .addGroup(layout.createParallelGroup()
                        .addComponent(x0Label)
                        .addComponent(x0Equals)
                        .addComponent(x0Field))
                        .addGroup(layout.createParallelGroup()
                              .addComponent(y0Label)
                              .addComponent(y0Equals)
                              .addComponent(y0Field))
                              .addGroup(layout.createParallelGroup()
                                    .addComponent(xFLabel)
                                    .addComponent(xFEquals)
                                    .addComponent(xFField))
                                    .addGroup(layout.createParallelGroup()
                                          .addComponent(hLabel)
                                          .addComponent(hEquals)
                                          .addComponent(hField))
                                             .addComponent(submit));
      inputWindow.setVisible(true);
      

      if (debug)
      {
         errorWindow("Testing");
      }

   }
   
   // Recursive method for standard notation.
   // 1st ready equation by removing spaces and checking for illegal symbols
   static String removeSpacesFromEquation(String usrEquation)
   {
      String eqNoSpaces = "";
      char testChar;
      for (int k = 0; k < usrEquation.length(); k++)
      {
         testChar = usrEquation.charAt(k);
         if (testChar != ' ')
            eqNoSpaces += testChar;
      }
      return eqNoSpaces;
   }
   // recursive equation evaluator. Takes the equation string without spaces
   // and x and y values as parameters
   static Double G(String equation, double x_in, double y_in)
   {
      char testChar;
      int openParenthesis, firstIndice, lastIndice, negativeCount;
      double numberFromFunction, firstNumber, secondNumber;
      String subEq, preSubEq, postSubEq, numBuilder;
      // first run replaces x, y, and e;
      for (int k = 0; k < equation.length(); k++)
      {
         testChar = equation.charAt(k);
         if (testChar == 'x' || testChar == 'y' || testChar == 'e')
         {
            preSubEq = equation.substring(0, k);
            postSubEq = equation.substring(++k, equation.length());
            if (testChar == 'x')
               equation = preSubEq + noSci.format(x_in);
            else if (testChar == 'y')
               equation = preSubEq + noSci.format(y_in);
            else
               equation = preSubEq + Math.E;
            k = equation.length();
            equation += postSubEq;
            if (debug)
               System.out.println("x, y and e " + equation);
         }
      }
      // second run reduces parenthesis
      for (int k = 0; k < equation.length(); k++)
      {
         testChar = equation.charAt(k);
         if (testChar == '(')
         {
            subEq = "";
            openParenthesis = 1;
            firstIndice = k + 1;
            preSubEq = equation.substring(0, k);
            while (openParenthesis > 0)
            {
               testChar = equation.charAt(++k);
               if (testChar == '(')
                  openParenthesis++;
               else if (testChar == ')')
                  openParenthesis--;
            }
            subEq = equation.substring(firstIndice, k);
            postSubEq = equation.substring(++k, equation.length());
            numberFromFunction = G(subEq, x_in, y_in);
            equation = preSubEq + numberFromFunction;
            k = equation.length();
            equation += postSubEq;
            if (debug)
               System.out.println("Parenthesis " + equation);
         }
      }
      for (int k = 0; k < equation.length(); k++)
      {
         testChar = equation.charAt(k);
         // sin
         if (testChar == 's')
         {
            preSubEq = equation.substring(0, k);
            k += 3;
            // first char afterwards must be a number or . or -
            numBuilder = "" + equation.charAt(k++);
            // then we keep going till number is complete if not end of string
            while(isPartOfNumber(k, equation))
            {
               numBuilder += equation.charAt(k++);
            }
            numberFromFunction = Math.sin(Double.parseDouble(numBuilder));
            postSubEq = equation.substring(k);
            equation = preSubEq + noSci.format(numberFromFunction);
            k = equation.length();
            equation += postSubEq;
            if (debug)
               System.out.println("sin done " + equation);
         }

         // cos
         else if (testChar == 'c')
         {
            preSubEq = equation.substring(0, k);
            k += 3;
            // first char afterwards must be a number or . or -
            numBuilder = "" + equation.charAt(k++);
            // then we keep going till number is complete if not end of string
            while(isPartOfNumber(k, equation))
            {
               numBuilder += equation.charAt(k++);
            }
            numberFromFunction = Math.cos(Double.parseDouble(numBuilder));
            postSubEq = equation.substring(k);
            equation = preSubEq + noSci.format(numberFromFunction);
            k = equation.length();
            equation += postSubEq;
            if (debug)
               System.out.println("cos done " + equation);
         }
         // tan
         else if (testChar == 't')
         {
            preSubEq = equation.substring(0, k);
            k += 3;
            // first char afterwards must be a number or . or -
            numBuilder = "" + equation.charAt(k++);
            // then we keep going till number is complete if not end of string
            while(isPartOfNumber(k, equation))
            {
               numBuilder += equation.charAt(k++);
            }
            numberFromFunction = Math.tan(Double.parseDouble(numBuilder));
            postSubEq = equation.substring(k);
            equation = preSubEq + noSci.format(numberFromFunction);
            k = equation.length();
            equation += postSubEq;
            if (debug)
               System.out.println("tan done " + equation);
         }
      }
      // exponents
      for (int k = 0; k < equation.length(); k++)
      {
         testChar = equation.charAt(k);
         if (testChar == '^')
         {
            // determining base
            firstIndice = preNumber(equation, k);
            firstNumber = Double.parseDouble(equation.substring
                  (firstIndice, k));
            preSubEq = equation.substring(0, firstIndice);
            // determining exponent
            lastIndice = postNumber(equation, ++k);
            secondNumber = Double.parseDouble(equation.substring
                  (k, lastIndice));
            postSubEq = equation.substring(lastIndice);
            equation = preSubEq + noSci.format((Math.pow(firstNumber,
                  secondNumber)));
            k = equation.length()-1;
            equation += postSubEq;
            if (debug)
               System.out.println("Exponents " + equation);
         }
      }
      // multiplication and division
      for (int k = 0; k < equation.length(); k++)
      {
         testChar = equation.charAt(k);
         if (testChar == '*' || testChar == '/')
         {
            // determining first number
            firstIndice = preNumber(equation, k);
            firstNumber = Double.parseDouble(equation.substring
                  (firstIndice, k));
            preSubEq = equation.substring(0, firstIndice);
            // determining second number
            lastIndice = postNumber(equation, ++k);
            secondNumber = Double.parseDouble(equation.substring
                  (k, lastIndice));
            postSubEq = equation.substring(lastIndice);
            if (testChar == '*')
               equation = preSubEq + noSci.format((firstNumber*secondNumber));
            else
               equation = preSubEq + noSci.format((firstNumber/secondNumber));
            k = equation.length()-1;
            equation += postSubEq;
            if (debug)
               System.out.println("multiply and divide " + equation);
         }
      }
      // last run for addition and subtraction. Needs negative combiner
      negativeCount = 0;
      for (int k = 0; equation.charAt(k) == '-'; k++, negativeCount++);
      if (negativeCount%2 == 0)
         equation = equation.substring(negativeCount);
      else
         equation = "-" + equation.substring(negativeCount);
      for (int k = 1; k < equation.length(); k++)
      {
         testChar = equation.charAt(k);
         if (testChar == '+' || testChar == '-')
         {
            // determining first number
            firstIndice = preNumber(equation, k);
            firstNumber = Double.parseDouble(equation.substring
                  (firstIndice, k));
            preSubEq = equation.substring(0, firstIndice);
            // determining second number
            lastIndice = postNumber(equation, ++k);
            secondNumber = Double.parseDouble(equation.substring
                  (k, lastIndice));
            postSubEq = equation.substring(lastIndice);
            if (testChar == '+')
               equation = preSubEq + noSci.format((firstNumber+secondNumber));
            else
               equation = preSubEq + noSci.format((firstNumber-secondNumber));
            k = equation.length()-1;
            equation += postSubEq;
            if (debug)
               System.out.println("add and subtract " + equation);
         }
      }
      if (debug)
         System.out.println("Final answer " + equation);
      return Double.parseDouble(equation);
   }
   
   // helper method that determines if something is part of the number.
   static boolean isPartOfNumber(int k, String equation)
   {
      if (k == equation.length() || k == -1)
         return false;
      if (Character.isDigit(equation.charAt(k)) || equation.charAt(k) == '.')
      {
         return true;
      }
      return false;
   }
   // helper method for pre-number. Returns index of where the number starts.
   static int preNumber(String equation, int k)
   {
      while(isPartOfNumber(--k, equation));
      if (equation.charAt(k+1) == '-')
      {
         if (!isPartOfNumber(k, equation))
         {
            return k;
         }
      }
      return k+1;
   }
   
   // helper method for post-number. Returns index of where number ends
   static int postNumber(String equation, int k)
   {
      while(isPartOfNumber(++k, equation));
      return k;
   }
   
   // Euler numerical method. Takes initial coordinates, x_initial and
   // y_initial, an increment h, and a final point x_final.
   static String euler(double x_initial, double y_initial, double x_final,
         double h, String usrEquation)
   {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Euler numerical solution of " + usrEquation +
            "<br>Initial point: (" + x_initial + ", " + y_initial + ")"
            + "<br>Increment: " + h + "<br>");
      double y = y_initial;
      for (double x = x_initial; x <= x_final; x += h)
      {
         stringBuilder.append("(" + xRound.format(x) + ", " + y + ")<br>");
         y = y + h * G(usrEquation, x,y);
      }
      return stringBuilder.toString();
   }
   
   // 4th order Runge-Kutta numerical method. Takes initial coordinates,
   // x_initial and y_initial, an increment h, and a final point x_final.
   static String rk4(double x_initial, double y_initial,
         double x_final, double h, String usrEquation)
   {
      //helper variables
      double k1, k2, k3, k4;
      
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("<br>4th order Runge-Kutta numerical solution of "
            + usrEquation +
            "<br>Initial point: (" + x_initial + ", " + y_initial + ")"
            + "<br>Increment: " + h + "<br>");
      double y = y_initial;
      for (double x = x_initial; x <= x_final; x += h)
      {
         stringBuilder.append("(" + xRound.format(x) + ", " + y + ")<br>");
         k1 = G(usrEquation, x,y);
         k2 = G(usrEquation, x + h/2., y + h*k1/2.);
         k3 = G(usrEquation, x + h/2., y + h*k2/2.);
         k4 = G(usrEquation, x + h, y + h*k3);
         y = y + h*(k1 + 2*k2 + 2*k3 + k4)/6.;
      }
      return stringBuilder.toString();
   }
   
   // Tests strings for legality for turning them into equations or doubles
   static boolean legalInput(String usrEquation, String x0String,
         String y0String, String xFString, String hString)
   {
      if (!isLegalEquation(usrEquation))
      {
         errorWindow("Invalid Equation");
         return false;
      }
      if (!isLegalDouble(x0String))
      {
         errorWindow("Illegal x inital");
         return false;
      }
      if (!isLegalDouble(y0String))
      {
         errorWindow("Illegal y initial");
         return false;
      }
      if (!isLegalDouble(xFString))
      {
         errorWindow("Illegal x final");
         return false;
      }
      if (!isLegalDouble(hString))
      {
         errorWindow("Illegal h");
         return false;
      }
      return true;
   }
   
   // tests doubles to ensure xF > x0 and that h > 0
   static boolean legalDoubles(double x0, double xF, double h)
   {
      if (xF <= x0)
      {
         errorWindow("xF must be larger than x0");
         return false;
      }
      if (h <= 0)
      {
         errorWindow("h must be larger than 0");
         return false;
      }
      return true;
   }
   
   // Error window generator
   static void errorWindow(String errorMessage)
   {
      JFrame errorWindow = new JFrame("Error");
      errorWindow.setSize(300, 100);
      errorWindow.setLocationRelativeTo(null);
      JLabel textLabel = new JLabel(errorMessage, 
            SwingConstants.CENTER);
      errorWindow.getContentPane().add(textLabel);
      //Display the window.
      errorWindow.setLocationRelativeTo(null); 
      errorWindow.setVisible(true); 
   }
   // Determines legality of an incoming double.
   static boolean isLegalDouble(String usrInString)
   {
      if (usrInString.isEmpty())
         return false;
      char testChar;
      testChar = usrInString.charAt(0);
      if (!Character.isDigit(testChar) && testChar != '.' && testChar != '-')
         return false;
      for (int k = 1; k < usrInString.length(); k++)
      {
         testChar = usrInString.charAt(k);
         if (!Character.isDigit(testChar) && testChar != '.')
            return false;
      }
      return true;
   }

   // Determines legality of an incoming equation
   static boolean isLegalEquation(String usrInString)
   {
      if (usrInString.isEmpty())
         return false;
      char testChar;
      for (int k = 0; k < usrInString.length(); k++)
      {
         testChar = usrInString.charAt(k);
         if (!Character.isLetterOrDigit(testChar) && testChar != '.' &&
               testChar != '+' && testChar != '-' && testChar != '('
               && testChar != ')' && testChar != '^'
               && testChar != '*' && testChar != '/')
            return false;
      }
      return true;
   }
}
