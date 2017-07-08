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

(defn next-nth
  [loc n]
  (->> loc
       (iterate z/next)
       (take (inc n))
       last))

;; up-nth, down-nth, left-nth, right-nth, etc...

;; more generic -- any movement given by move-fn

(defn move-nth
  [move-fn loc n]
  (some->> loc
           (iterate move-fn)
           (take (inc n))
           last))

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

(def up-left-nth (partial move-nth (comp z/left z/up)))

(up-left-nth
  (-> (z/vector-zip [:a [:b :c [[:e] :d]]]) z/down z/right z/down z/right z/right z/down)
  1)


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
   (bf-zip identity (queue [loc]) []))
  ([loc visit-fn]
   (bf-zip visit-fn (queue [loc]) []))
  ([visit-fn q visited]
   (if-let [head (peek q)]
     (recur visit-fn (into (pop q) (children head)) (visit head visit-fn visited))
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
    <tr>
     <th>Name</th>
     <th>Age</th>
     <th>Favorite Food</th>
    </tr>
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
   </table>")

(-> (hzip/hickory-zip (hick/as-hickory (hick/parse html)))
    (bf-zip (fn [x]
              (cond
                (map? x) (:tag x)
                (string? x) (-> x string/trim not-empty))))
    pp/pprint)
