(ns zipper-snippets.weird
  (:require
    [clojure.walk :as walk]
    [clojure.zip :as z]
    [zipper-snippets.draw :refer :all]
    [zipper-snippets.traversal :refer [move-nth]]))

;; copy ------------------------------------------------------------------------

;;          *
;;        /   \
;;      :a     *
;; copy -^   / | \
;; here    :b :c  *
;;                | \
;;                *  :d  <- start here
;;                |
;;               :e

(defn copy-zip
  [loc move-fn & {:keys [replace?]}]
  (let [node (z/node loc)]
    (if replace?
      (-> loc move-fn (z/replace node))
      (-> loc move-fn z/up (z/append-child node)))))

(let [next-nth (partial move-nth z/next)]
  (-> (z/vector-zip [:a [:b :c [[:e] :d]]])
      (next-nth 8)
      (copy-zip (comp z/left z/up z/up) :replace? true)
      z/root
      draw-vec))

;; cut -------------------------------------------------------------------------

;;        *
;;      /   \
;;    :a     *
;;         / | \
;;       :b :c  *  <- start here
;;  cut --^ -^  | \
;; these        *  :d
;;              |
;;             :e

(defn root-loc [loc]
  (->> loc
       (iterate z/up)
       (take-while identity)
       last))

(defn cut-zip
  [loc move-fns]
  (->> (reduce (fn [l mf] (z/replace (mf l) :cut)) loc move-fns)
       root-loc
       (iterate (fn [l] (if (= :cut (z/node l)) (z/remove l) (z/next l))))
       (take-while (complement z/end?))
       last))

(let [next-nth (partial move-nth z/next)]
  (-> (z/vector-zip [:a [:b :c [[:e] :d]]])
      (next-nth 5)
      (cut-zip [z/left z/left])
      z/root
      draw-vec))
