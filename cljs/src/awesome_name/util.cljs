(ns awesome-name.util
  (:require
    [clojure.set :as cset]
    [clojure.string :as cs]))

(defn character-attrs
  [chinese-characters character]
  (-> (filter (fn [{:keys [characters]}]
                (cs/includes? characters character))
              chinese-characters)
      first))

(defn strokes-of
  "Get strokes of a character"
  [chinese-characters character]
  (:strokes (character-attrs chinese-characters character)))

(defn all-strokes-combinations
  "Given surname stroke and stroke-range then return all combinations of strokes.
  Assume surname have 1 character and given name 2 characters"
  [top-strokes stroke-range]
  (mapcat
    (fn [m-stroke]
      (map (fn [b-stroke] [top-strokes m-stroke b-stroke])
           stroke-range))
    stroke-range))

(defn name-strokes->gers
  "Given name strokes and return value of 五格 (called gers here)
  Assume surname have 1 character and given name 2 characters"
  [top middle bottom]
  [(inc top)
   (+ top middle)
   (+ middle bottom)
   (inc bottom)
   (+ top middle bottom)])

;; https://www.163.com/dy/article/DQJQ7PK60528ETV2.html
(defn name-strokes->ger-elements
  "given name strokes return element of each character.
  Assume surname have 1 character and given name 2 characters"
  [top middle bottom]
  (let [element-keys ["水" "木" "木" "火" "火" "土" "土" "金" "金" "水"]]
    (->> (name-strokes->gers top middle bottom)
         (mapv #(get element-keys (rem % 10))))))

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
             [(* -1 pts) (:strokes top) (:strokes middle) (:strokes bottom)])
           combinations))

(defn add-combination-label
  [{:keys [pts strokes] :as comb}]
  (assoc comb :label (str "適合筆畫：" (:top strokes) ", " (:middle strokes) ", " (:bottom strokes) " (綜合分數：" pts "）")))

(defn string->char-set
  [string]
  (->> (mapcat seq string)
       (into #{})))

(defn normal-characters
  [chinese-characters better-chars worse-chars strokes]
  (let [same-stroke-chars (->> chinese-characters
                               (filter #(= (:stroke %) strokes))
                               (map :characters)
                               string->char-set)]
    (cset/difference same-stroke-chars better-chars worse-chars)))
