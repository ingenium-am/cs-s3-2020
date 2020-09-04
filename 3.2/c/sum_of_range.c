#include <stdio.h>

// main() is standard entrypoint
// int is return type
int main()
{
    // n is declared but not initialized
    int n;

    printf("Set number: ");

    // parse input as int and place at n's address
    scanf("%d", &n);

    int sum = 0;

    // set and print array interating 'for' loop
    printf("Nums: [");
    for (int i = 0; i < n; i++)
    {
        // sum = sum + <next num>
        sum += i+1;

        // print elements on each iteration
        printf("%d", i+1);

        // suffix depends on iteration
        if (i == n-1)
            printf("]\n");
        else
            printf(", ");
    }

    // print Sum and 'newline'
    printf("Sum: %d\n", sum);

    // must return an int (0 on succes by default)
    return 0;
}

