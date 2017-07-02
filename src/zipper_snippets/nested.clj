(ns zipper-snippets.nested
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pp]
    [clojure.string :as string]
    [clojure.zip :as zip]
    [zipper-snippets.traversal :refer [move-nth]]))

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

(defn up-nth [loc n] #(move-nth loc zip/up n))

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
  [lines]
  (loop [lines lines, last-depth -1, ztree (zip/xml-zip {})]
    (if-let [[depth node] (first lines)] 
      (recur (rest lines) 
             depth 
             (cond-> ztree
               ;; sibling
               (= depth last-depth) (-> (zip/insert-right node) zip/right)
               ;; parent
               (< depth last-depth) (-> (up-nth (- last-depth depth)) (zip/insert-right node) zip/right)
               ;; child
               (> depth last-depth) (-> (zip/append-child node) zip/down)))
      ztree)))

(->> "tree.txt"
     io/resource
     slurp
     parse-tree-str
     lines-to-graph
     zip/root
     pp/pprint)
