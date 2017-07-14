(ns zipper-snippets.nested
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pp]
    [clojure.string :as string]
    [clojure.zip :as z]
    [zipper-snippets.traversal :refer [up-nth]]))

;; outlook = sunny
;; |   temp = hot : no
;; |   temp = mild
;; |   |   windy = false : no
;; |   |   windy = true : yes
;; |   temp = cool : yes
;; outlook = overcast : yes
;; outlook = rainy
;; |   temp = hot : no
;; |   temp = mild : yes
;; |   temp = cool
;; |   |   windy = false : yes
;; |   |   windy = true : no

;; -----------------------------------------------------------------------------

(def xform-tree
  (comp (take-while seq)
        (map #(string/split % #"\s+"))
        (map (fn [toks] (cons (count (take-while #{"|"} toks)) (drop-while #{"|"} toks))))
        (map (partial remove #{":"}))
        (map (fn [[depth & toks]] [depth (zipmap [:attr :pred :val :class] toks)]))))

(defn parse-tree-str
  [tree-str]
  (into [] xform-tree (string/split-lines tree-str)))

(->> "tree.txt"
     io/resource
     slurp
     parse-tree-str
     pp/pprint)

;; -----------------------------------------------------------------------------

(defn lines-to-graph
  ([lines]
   (lines-to-graph lines -1 (z/xml-zip {})))
  ([[[depth node] & lines] last-depth ztree]
   (if node
     (recur lines depth 
            (cond-> ztree
              ;; sibling of last node
              (= depth last-depth) (-> (z/insert-right node) z/right)
              ;; sibling of ancestor
              (< depth last-depth) (-> (up-nth (- last-depth depth)) (z/insert-right node) z/right)
              ;; child of last node
              (> depth last-depth) (-> (z/append-child node) z/down)))
     ztree)))

(->> "tree.txt"
     io/resource
     slurp
     parse-tree-str
     lines-to-graph
     z/root
     pp/pprint)
