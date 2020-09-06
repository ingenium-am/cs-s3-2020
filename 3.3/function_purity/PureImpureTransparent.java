public class PureImpureTransparent {

    // accessible and mutable from anywhere
    public static int MUTABLE_X = 6;

    // not accessible from outside and immutable
    final private static int IMMUTABLE_X = 168;

    public static void main(String[] args) {
        System.out.println("\nPURE FUNCTIONS:");
        System.out.println("pure_zero() => " + pure_zero());
        System.out.println("pure_square(4) => " + pure_square(4));

        System.out.println("\nIMPURE FUNCTIONS:");
        System.out.println("impure_id(42) => " + impure_id(42));
        MUTABLE_X = 101;    // change by assigning another value
        System.out.println("impure_id(42) => " + impure_id(42));

        System.out.println("\nREFERENTIAL TRANSPARENCY:");
        System.out.println("transparent_id(2) => " + transparent_id(2));
        //IMMUTABLE_X = 3; // this won't be compiled - field is final
        System.out.println("transparent_id(2) => " + transparent_id(2));
    }

    // FUNCTIONS

    // Acts like immutable variable (constant)
    static int pure_zero() {
        return 0;               // always return 0
    }

    // For the same input always return the same output
    static int pure_square(int num) {
        return num * num;       // nothing involved from outside
    }

    // Function behaviour depends on ENVIRONMENT and RUNTIME
    static int impure_id(int num) {
        if (MUTABLE_X < 100)
            return num;         // id
        return num * ++num;     // not id
    }

    // Function behaviour depends on ENVIRONMENT
    // but NEVER going to change on RUNTIME
    static int transparent_id(int num) {
        if (IMMUTABLE_X < 100)
            return num;         // id
        return num * ++num;     // not id
    }
}
