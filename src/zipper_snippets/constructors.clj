(ns zipper-snippets.constructors
  (:require 
    [clojure.zip :as zip]
    [zipper-snippets.draw :refer :all]))

;; zipper
;; creates arbitrary zipper given fns to determine leaf/branch and generate nodes and children
;;
;; fns stored as meta on zipper object


;; seq-zip
;; node is a branch if (seq? node)
;;
;;     *
;;   /   \
;;  1     *
;;      /   \
;;    "2"    *
;;         / | \
;;        3  *  4
;;           |
;;           5
;;
(->> '(1 ("2" (3 (5) 4)))
     zip/seq-zip ; (1 ("2" (3 (5) 4)))
                 ; ^
     zip/down    ; (1 ("2" (3 (5) 4)))
                 ;  ^
     zip/right   ; (1 ("2" (3 (5) 4)))
                 ;    ^
     zip/down    ; (1 ("2" (3 (5) 4)))
                 ;      ^
     zip/right   ; (1 ("2" (3 (5) 4)))
                 ;         ^
     zip/down    ; (1 ("2" (3 (5) 4)))
                 ;          ^
     zip/node)   ; 3


;; vec-zip
;; node is a branch if (vec? node)
;;
;;      *
;;    /   \
;; '(:a)   *
;;       / | \
;;     :b :c  *
;;            | \
;;            *  :e
;;            |
;;           :d
;;
(->> ['(:a) [:b :c [[:d] :e]]]
     zip/vector-zip ; ['(:a) [:b :c [[:d] :e]]]
                    ; ^
     zip/down       ; ['(:a) [:b :c [[:d] :e]]]
                    ;  ^
     zip/right      ; ['(:a) [:b :c [[:d] :e]]]
                    ;        ^
     zip/down       ; ['(:a) [:b :c [[:d] :e]]]
                    ;         ^
     zip/right      ; ['(:a) [:b :c [[:d] :e]]]
                    ;            ^
     zip/node)      ; :c


;; xml-zip
;;
;; node is a branch if ((complement string?) node)
;; children are in :content
;;
;; <p><b>foo</b>bar</p>
;;
;;       p
;;     /   \
;;    b   "bar"
;;    |
;;  "foo"

(->> {:tag :p :content [{:tag :b :content ["foo"]} "bar"]}
     zip/xml-zip
     zip/next  ; {:tag :p :content [{:tag :b :content ["foo"]} "bar"]}
               ;                    ^
     zip/down  ; {:tag :p :content [{:tag :b :content ["foo"]} "bar"]}
               ;                                       ^
     zip/next  ; {:tag :p :content [{:tag :b :content ["foo"]} "bar"]}
               ;                                               ^
     zip/node) ; "bar"
