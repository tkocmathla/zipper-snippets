(ns zipper-snippets.basics
  (:require 
    [clojure.pprint :as pp]
    [clojure.zip :as zip]))

;; concepts

;; data structure behind a zipper
;;   a data structure + a cursor
;;   forward list + backward list (:l and :r)
;;   l is a vector, r is a seq
;;     access to last of l is constant (prev)
;;     access to first of r is constant (next)

(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down pp/pprint)
;     ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right pp/pprint)
;       ^
(->> [0 [[1] 2] 3 4 [5]] zip/vector-zip zip/down zip/right zip/right pp/pprint)
;               ^

;; efficient
;; each op takes constant time and constant allocations

;; contrast with tree-seq and walk
;; tree-seq only allows /visiting/ nodes, and only in a depth-first fashion
;; walk allows pre- and post-order traversal (depth-first) and node modification

;; why useful? 
;;
;; pose problem of building a nested data structure like rf trees or hickory
;; solve in a later ns
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


;; good primer 
;; https://pavpanchekha.com/blog/zippers/huet.html

