// link in html and run in browser
var n = prompt("Set number: ");                 // pop-up prompt and get input
var arr = [...Array(parseInt(n)).keys()];       // parse input as an int
var sum = arr.reduce((a, b) => a + b, 0)        // make array of ints
alert("Nums: [" + arr + "]\n" + "Sum: " + sum)  // pop-up windows with Nums and Sum
