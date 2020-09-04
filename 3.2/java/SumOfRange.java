import java.util.Arrays;
import java.util.Scanner;

public class SumOfRange {
    public static void main(String[] args) {

        // create Scanner object
        Scanner in = new Scanner(System.in);

        System.out.print("Set number: ");
        // trying to parse input as an int
        // n is declared and initialized
        int n = in.nextInt();

        // make array with 'n' empty elements
        int[] range = new int[n];

        int sum = 0;
      
        // set array interating 'for' loop
        for (int i = 0; i < range.length; i++) {
            range[i] = i+1;
        }

        // print Nums (as array), Sum and 'newline'
        System.out.println("Nums: " + Arrays.toString(range));
        System.out.printf("Sum: %d\n", Arrays.stream(range).sum());
    }
}
