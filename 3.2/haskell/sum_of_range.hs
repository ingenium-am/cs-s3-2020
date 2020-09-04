import System.IO (hFlush, stdout)

-- get int and return array of range
makeArray :: Int -> [Int]
makeArray n = [1..n]

main :: IO ()
main = do
  putStr "Set number: "
  -- flush buffer (laziness)
  hFlush stdout

  -- get input as string
  input   <- getLine
  -- try to parse as an Int
  let n   = (read input :: Int)
  -- call makeArray on n and set result to arr
  let arr = makeArray n

  -- print Nums (as array) and Sum
  -- putStrLn adds 'newline'
  putStrLn $ "Nums: " ++ show arr       
  putStrLn $ "Sum: " ++ show (sum arr)
