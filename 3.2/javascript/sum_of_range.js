var n = prompt("Set number: ");
var arr = [...Array(5).keys()];
var sum = arr.reduce((a, b) => a + b, 0)
alert("Nums: " + arr + "\n" + "Sum: " + sum)
