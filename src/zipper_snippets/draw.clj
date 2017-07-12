(ns zipper-snippets.draw
  (:require 
    [clojure.walk :as walk]
    [vijual :as v]))

(defn draw 
  "Draws an ASCII tree for a seq-zip or vector-zip data structure."
  [pred data]
  (->> data
       (walk/postwalk
         (fn [x]
           (if (pred x) 
             (cons :* x)
             (cond-> x (not (sequential? x)) vector))))
       vector
       v/draw-tree))

(def draw-seq (partial draw seq?))
(def draw-vec (partial draw vector?))
