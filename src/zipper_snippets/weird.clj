(ns zipper-snippets.weird
  (:require
    [clojure.walk :as walk]
    [clojure.zip :as z]
    [zipper-snippets.traversal :refer [bf-zip]]))


(defn string-zip [root]
  (z/zipper
    string?
    (fn [[f & r]] (if r [f (apply str r)] [f]))
    identity
    root))

(->> "bar" string-zip bf-zip)


(defn prime-fact-zip [root]
  (let [divisible? (fn [n d] (zero? (mod n d)))] 
    (z/zipper
      #(> % 1)
      (fn [n] 
        (loop [d 2]
          (if (divisible? n d) 
            (when (not= n d)
              [(/ n d) d])
            (recur (inc d)))))
      identity
      root)))

(-> 102 prime-fact-zip bf-zip)
