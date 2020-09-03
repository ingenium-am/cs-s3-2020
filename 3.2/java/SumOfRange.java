import java.util.Arrays;
import java.util.Scanner;

public class SumOfRange {
  public static void main(String[] args) {
      Scanner in = new Scanner(System.in);

      System.out.print("Set number: ");
      // n is declared and initialized
      int n = in.nextInt();

      int[] range = new int[n];
      int sum = 0;

      for (int i = 0; i < range.length; i++) {
          range[i] = i+1;
      }

      System.out.println("Nums: " + Arrays.toString(range));
      System.out.printf("Sum: %d\n", Arrays.stream(range).sum());
  }
}
