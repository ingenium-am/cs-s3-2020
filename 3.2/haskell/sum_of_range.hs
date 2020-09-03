import System.IO (hFlush, stdout)

makeArray :: Int -> [Int]
makeArray n = [1..n]

main :: IO ()
main = do
  putStr "Set number: "
  hFlush stdout
  input   <- getLine
  let n   = (read input :: Int)
  let arr = makeArray n
  putStrLn $ "Nums: " ++ show arr
  putStrLn $ "Sum: " ++ show (sum arr)
