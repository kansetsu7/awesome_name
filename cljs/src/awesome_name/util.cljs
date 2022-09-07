(ns awesome-name.util
  (:require
    [clojure.string :as cs]))

(defn strokes-of
  "Get strokes of a character"
  [chinese-characters character]
  (-> (filter (fn [{:keys [characters]}]
                (cs/includes? characters character))
              chinese-characters)
      first
      :stroke))

(defn all-strokes-combinations
  "Given surname stroke and stroke-range then return all combinations of strokes.
  Assume surname have 1 character and given name 2 characters"
  [top-strokes stroke-range]
  (mapcat
    (fn [m-stroke]
      (map (fn [b-stroke] [top-strokes m-stroke b-stroke])
           stroke-range))
    stroke-range))

;; https://www.163.com/dy/article/DQJQ7PK60528ETV2.html
(defn name-strokes->elements
  "given name strokes return element of each character.
  Assume surname have 1 character and given name 2 characters"
  [top middle bottom]
  (let [element-keys ["水" "木" "木" "火" "火" "土" "土" "金" "金" "水"]]
    (->> [(inc top) (+ top middle) (+ middle bottom)]
         (map #(get element-keys (rem % 10))))))

(defn name-strokes->gers
  "Given name strokes and return value of 五格 (called gers here)
  Assume surname have 1 character and given name 2 characters"
  [top middle bottom]
  [(inc top)
   (+ top middle)
   (+ middle bottom)
   (inc bottom)
   (+ top middle bottom)])

(defn gers->81pts
  [eighty-one gers]
  (->> gers
       (map #(get-in eighty-one [(dec %) :points]))
       (apply +)
       (* 2)))

(defn sort-by-pts-and-strokes
  "pts desc, strokes asc"
  [combinations]
  (sort-by (fn [{:keys [pts top middle bottom]}]
             [(* -1 pts) (:stroke top) (:stroke middle) (:stroke bottom)])
           combinations))

(defn add-combination-label
  [{:keys [pts top middle bottom] :as comb}]
  (assoc comb :label (str "適合筆畫：" (:stroke top) ", " (:stroke middle) ", " (:stroke bottom) " (綜合分數：" pts "）")))
