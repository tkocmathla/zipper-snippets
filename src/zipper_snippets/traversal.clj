(ns zipper-snippets.traversal
  (:require [clojure.zip :as z]))

;; basic movement --------------------------------------------------------------

;; next, prev
;; move to next or previous node in depth-first traversal



;; up, down
;; move to parent or leftmost child node



;; right, left
;; move to right or left sibling node




;; batched movement ------------------------------------------------------------

(defn next-nth
  [loc n]
  (->> loc
       (iterate z/next)
       (take (inc n))
       last))

;; next-nth, down-nth, left-nth, right-nth, etc...

;; more generic -- any movement

(defn move-nth
  [loc move-fn n]
  (->> loc
       (iterate move-fn)
       (take (inc n))
       last))

;;     *
;;   /   \
;; :a     *
;;      / | \
;;    :b :c  *
;;           | \
;;  start -> *  :d
;;           |
;;          :e

(z/node
  (move-nth 
    (-> (z/vector-zip [:a [:b :c [[:e] :d]]]) z/down z/right z/down z/right z/right z/down)
    (comp z/left z/up)
    1))

;; advanced movement -----------------------------------------------------------

;; breadth-first traversal

(def leaf? (every-pred (complement nil?) (complement z/branch?)))

(defn descend [loc]
  (some->> (z/leftmost loc)
           (iterate z/right)
           (drop-while leaf?)
           first
           z/down))

(defn visit [loc visited]
  (cond-> visited (leaf? loc) (conj! (z/node loc))))

(defn bf-zip
  ([loc] 
   (bf-zip loc (transient [])))
  ([loc visited] 
   (cond
     (nil? loc) (persistent! visited)  
     (seq (z/rights loc)) (recur (z/right loc) (visit loc visited))
     :else (recur (descend loc) (visit loc visited)))))

;;     *
;;   /   \
;; :a     *
;;      / | \
;;    :b :c  *
;;           | \
;;           *  :d
;;           |
;;          :e
;;
(bf-zip (z/vector-zip [:a [:b :c [[:e] :d]]]))

;;     *
;;   /   \
;;  1     *
;;      /   \
;;     2     *
;;         / | \
;;        3  *  4
;;           |
;;           5
;;
(bf-zip (z/seq-zip '(1 (2 (3 (5) 4)))))
