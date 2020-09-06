import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterTest {

    public static void main(String[] args) {

        // INITIAL LIST
        List<Integer> initialList = Arrays.asList(-42, -18, -7, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        System.out.println("INITIAL LIST: " + initialList);

        // Filtering by dedicated function
        List<Integer> listOfPositiveNums = FilterTest.filterPositiveNums(initialList);
        System.out.println("Positive nums: " + listOfPositiveNums);

        // In-place filtering
        List<Integer> listOfNegativeNums = new ArrayList<>();
        for (int l : initialList) {
            if (l < 0) {
                listOfNegativeNums.add(l);
            }
        }
        System.out.println("Negative nums: " + listOfNegativeNums);

        // In-place filtering by LAMBDA
        List<Integer> listOfPositiveNums_ = initialList.stream()
                .filter(l -> l > 0)  // anonymous function
                .collect(Collectors.toList());
        System.out.println("Positive nums (lambda): " + listOfPositiveNums_);

        // In-place filtering by LAMBDA
        List<Integer> listOfNegativeNums_ = initialList.stream()
                .filter(l -> {      // anonymous function
                    return l < 0;   // body syntax
                })
                .collect(Collectors.toList());
        System.out.println("Positive nums (lambda): " + listOfNegativeNums_);
    }

    // filtering function declaration
    static List<Integer> filterPositiveNums(List<Integer> list) {
        List<Integer> tempList = new ArrayList<>();

        for (int l : list) {
            if (l > 0) {
                tempList.add(l);
            }
        }
        return tempList;
    }
}
