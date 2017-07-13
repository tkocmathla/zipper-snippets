(ns zipper-snippets.weird
  (:require
    [clojure.walk :as walk]
    [clojure.zip :as z]
    [zipper-snippets.traversal :refer [bf-zip]]))


(defn string-zip [root]
  (z/zipper
    (every-pred string? #(> (count %) 1))
    (fn [s]
      (let [len (count s)]
        (if (> len 1)
          (map (partial apply str) (split-at (/ len 2) s))
          s)))
    identity
    root))

(->> "bamboozle" string-zip bf-zip)


(defn prime-fact-zip [root]
  (let [divisible? (fn [n d] (zero? (mod n d)))] 
    (z/zipper
      #(> % 1)
      (fn [n] 
        (loop [d 2]
          (if (divisible? n d) 
            (when (not= n d)
              [d (/ n d)])
            (recur (inc d)))))
      identity
      root)))

(->> 720720
     prime-fact-zip
     bf-zip
     ((fn [[_ & r]] (conj (vec (take-nth 2 r)) (last r)))))
