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

(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down pp/pprint)
;     ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right pp/pprint)
;       ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right zip/right pp/pprint)
;               ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right zip/down pp/pprint)
;        ^


;; zippers vs tree-seq and walk

;; tree-seq only allows /visiting/ nodes, and only in a depth-first fashion
(tree-seq vector? identity [0 [[1] 2] 3 4 [5]])

;; walk allows pre- and post-order traversal (depth-first) and node modification
(walk/prewalk (fn [x] (prn x) x) [0 [[1] 2] 3 4 [5]])
(walk/postwalk (fn [x] (prn x) x) [0 [[1] 2] 3 4 [5]])

(walk/postwalk (fn [x] (cond-> x (number? x) str)) [0 [[1] 2] 3 4 [5]])


;; why are zippers useful?
;;
;; - allow movement in any direction (up, down, left, right)
;;
;; - intuitive graph/tree traversal
;;
;; - avoid recursion!
;;
;; - parse an arbitrary graph description where the shape isn't known until the
;;   entire graph is parsed
