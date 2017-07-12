(ns zipper-snippets.basics
  (:require
    [clojure.pprint :as pp]
    [clojure.walk :as walk]
    [clojure.zip :as zip]))

;; concepts

;; data structure behind a zipper
;;   2-element vector (focus + path)
;;
;;   :l - backward list
;;     vector of left siblings
;;     allows constant-time access to left sibling (prev)
;;   :r - forward list
;;     list of right siblings
;;     allows constant-time access to right sibling (next)
;;   :pnodes - path nodes
;;     seq of nodes leading to current loc
;;   :ppath - parent path
;;     allows constant-time access to parent (up)

;; example
;;
;; [0 [[1] 2] 3 4 [5]]
;; hint: think of left-open-brackets as branches, and numbers as leaves
;;
;;       ___ __ [ __ ___
;;      /   /   |   \   \
;;     0   [    3   4    [
;;        / \            |
;;       [   2]          5]]
;;       |
;;       1]

(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down pp/pprint)
;     ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right pp/pprint)
;       ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right zip/right pp/pprint)
;               ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right zip/down pp/pprint)
;        ^


;; why are zippers useful?
;;
;; - allow arbitrary movement in any direction (up, down, left, right)
;;
;; - intuitive graph/tree traversal
;;
;; - avoid recursion!
;;
;; - use case: parse an arbitrary graph description in a single pass where the
;;   shape isn't known until the entire graph is parsed


;; zippers vs tree-seq and walk
(do
  ;; tree-seq only allows /visiting/ nodes, and only in a recursive pre-order depth-first fashion
  (tree-seq vector? identity [0 [[1] 2] 3 4 [5]])

  ;; walk allows recursive pre- and post-order traversal (depth-first) and node modification
  (walk/prewalk (fn [x] (prn x) x) [0 [[1] 2] 3 4 [5]])  ;; visits root first
  (walk/postwalk (fn [x] (prn x) x) [0 [[1] 2] 3 4 [5]]) ;; visits leaves first

  (walk/postwalk (fn [x] (cond-> x (number? x) str)) [0 [[1] 2] 3 4 [5]]))
