(ns zipper-snippets.traversal
  (:require
    [clojure.pprint :as pp]
    [clojure.string :as string]
    [hickory.core :as hick]
    [hickory.zip :as hzip]
    [medley.core :refer [queue]]
    [clojure.zip :as z]))

;; basic movement --------------------------------------------------------------

;; next, prev
;; move to next or previous node in depth-first traversal

;; up, down
;; move to parent or leftmost child node

;; right, left
;; move to right or left sibling node


;; batched movement ------------------------------------------------------------

(->> [1 2 3 4 5]
     z/vector-zip
     (iterate z/next)
     (take 3)
     last
     z/node)

;; up-nth, down-nth, left-nth, right-nth, etc...

;; more generic -- any movement given by move-fn

(defn move-nth
  [move-fn loc n]
  (some->> loc
           (iterate move-fn)
           (take (inc n))
           last))

(def next-nth (partial move-nth z/next))
(def up-nth (partial move-nth z/up))
(def up-left-nth (partial move-nth (comp z/left z/up)))

;; for example:
;;
;;     *
;;   /   \
;; :a     *
;;      / | \
;;    :b :c  *
;;           | \
;;  start -> *  :d
;;           |
;;          :e

(-> [:a [:b :c [[:e] :d]]]
    z/vector-zip
    (next-nth 6)
    (up-left-nth 2)
    z/node)


;; movement with editing -------------------------------------------------------

(defn zmap [f loc]
  (if (z/end? loc)
    (z/root loc)
    (recur f (z/next (z/edit loc f)))))

(->> [-1 [8 0 [2] 4] 9]
     z/vector-zip
     (zmap #(cond-> % (number? %) inc)))

(->> {:tag :p :content [{:tag :b :content ["foo"]} "bar"]}
     z/xml-zip
     (zmap #(cond-> % (map? %) (assoc :SEEN 1))))


;; advanced movement -----------------------------------------------------------

;; breadth-first traversal

(defn visit [loc visit-fn visited]
  (let [data (visit-fn (z/node loc))]
    (cond-> visited data (conj data))))

(defn children [loc]
  (some->> loc
           z/down
           (iterate z/right)
           (take-while identity)))

(defn bf-zip
  ([loc]
   (bf-zip (queue [loc]) identity []))
  ([loc visit-fn]
   (bf-zip (queue [loc]) visit-fn []))
  ([locs visit-fn visited]
   (if-let [loc (peek locs)]
     (recur (into (pop locs) (children loc)) visit-fn (visit loc visit-fn visited))
     visited)))


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
(bf-zip (z/seq-zip '(1 (2 (3 (5) 4))))
        #(when (number? %) %))


;;    _ * _
;;   /  |  \
;; :a   *   *_
;;    / | \   \
;;  :b :c  *   :z
;;         | \
;;         *  :d
;;         |
;;        :e
;;
(bf-zip (z/vector-zip [:a [:b :c [[:e] :d]] [:z]])
        #(when (keyword? %) %))


(def html
  "<table>
     <thead>
       <tr>
         <th>Name</th>
         <th>Age</th>
         <th>Favorite Food</th>
       </tr>
     </thead>
     <tbody>
       <tr>
         <td>Mark</td>
         <td>NaN</td>
         <td>Tacos</td>
       </tr>
       <tr>
         <td>Thomas</td>
         <td>Youthful</td>
         <td>Peanut butter</td>
       </tr>
     </tbody>
   </table>")

;; +--------+----------+---------------+
;; | Name   | Age      | Favorite Food |
;; +--------+----------+---------------+
;; | Mark   | NaN      | Tacos         |
;; +--------+----------+---------------+
;; | Thomas | Youthful | Peanut butter |
;; +--------+----------+---------------+

(-> (hzip/hickory-zip (hick/as-hickory (first (hick/parse-fragment html))))
    (bf-zip (fn [x]
              (cond
                (map? x) (:tag x)
                (string? x) (-> x string/trim not-empty))))
    pp/pprint)
