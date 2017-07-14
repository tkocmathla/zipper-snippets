(ns zipper-snippets.range
  (:require 
    [clojure.zip :as z]
    [zipper-snippets.draw :refer :all]
    [zipper-snippets.traversal :refer [next-nth]]))

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

(-> (z/vector-zip [:a [:b :c [[:e] :d]]])
    (next-nth 8)
    (copy-zip (comp z/left z/up z/up) :replace? true)
    z/root
    draw-vec)

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

(def top? (comp nil? second)) ; true when path of zipper is nil

(defn root-loc [loc]
  (->> loc
       (iterate z/up)
       (take-while (complement top?))
       last))

(defn cut-zip
  [loc move-fns]
  (->> (reduce (fn [l mf] (z/replace (mf l) :cut)) loc move-fns)
       root-loc
       (iterate (fn [l] (if (= :cut (z/node l)) (z/remove l) (z/next l))))
       (take-while (complement z/end?))
       last))

(-> (z/vector-zip [:a [:b :c [[:e] :d]]])
    (next-nth 5)
    (cut-zip [z/left z/left])
    ;(cut-zip [identity (comp z/left z/left)])
    z/root
    draw-vec)
