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
  "Given surname strokes and stroke-range then return all combinations of strokes.
  Assume given name 2 characters. Surname should be a vector"
  [surname-strokes stroke-range single-given-name]
  (if single-given-name
    (map (fn [given-name-strokes] [surname-strokes [given-name-strokes]]) stroke-range)
    (mapcat
      (fn [m-stroke]
        (map (fn [b-stroke] [surname-strokes [m-stroke b-stroke]])
             stroke-range))
      stroke-range)))

(defn name-strokes->gers
  "Given name strokes and return value of 五格 (called gers here)
  Assume surname have 1 character and given name 2 characters"
  [surname-strokes given-name-strokes]
  (let [single-surname? (= 1 (count surname-strokes))
        single-given-name? (= 1 (count given-name-strokes))
        full-name-strokes-sum (->> (into surname-strokes given-name-strokes)
                                   (apply +))
        n1 (if single-surname? 1 (first surname-strokes))
        n2 (last surname-strokes)
        n3 (first given-name-strokes)
        n4 (if single-given-name? 1 (last given-name-strokes))]
    [(+ n1 n2)
     (+ n2 n3)
     (+ n3 n4)
     (as-> (- full-name-strokes-sum n2 n3) $
       (cond
         (and single-surname? single-given-name?) 2
         (or single-surname? single-given-name?) (inc $)))
     full-name-strokes-sum]))

;; https://www.163.com/dy/article/DQJQ7PK60528ETV2.html
(defn name-strokes->ger-elements
  "given name strokes return element of each character.
  Assume surname have 1 character and given name 2 characters"
  [surname-strokes given-name-strokes]
  (let [element-keys ["水" "木" "木" "火" "火" "土" "土" "金" "金" "水"]]
    (->> (name-strokes->gers surname-strokes given-name-strokes)
         (mapv #(get element-keys (rem % 10))))))

(defn gers->81pts
  [eighty-one gers]
  (->> gers
       (map #(get-in eighty-one [(dec %) :points]))
       (apply +)
       (* 2)))

(defn sort-by-wuger-pts-and-strokes
  "wuger-pts desc, strokes asc"
  [combinations]
  (sort-by (fn [{:keys [wuger-pts top middle bottom]}]
             [(* -1 wuger-pts) (:strokes top) (:strokes middle) (:strokes bottom)])
           combinations))

(defn add-combination-label
  [{:keys [wuger-pts strokes] :as comb}]
  (let [combined-strokes (->> (into (:surname strokes) (:given-name strokes))
                              (cs/join ", "))]
    (assoc comb :label (str "適合筆畫：" combined-strokes " (綜合分數：" wuger-pts "）"))))

(defn string->char-set
  [string]
  (->> (mapcat seq string)
       (into #{})))

(defn normal-characters
  [chinese-characters better-chars worse-chars strokes]
  (let [same-stroke-chars (->> chinese-characters
                               (filter #(= (:strokes %) strokes))
                               (map :characters)
                               string->char-set)]
    (cset/difference same-stroke-chars better-chars worse-chars)))

(defn dissoc-in
  [m ks]
  (let [ks' (pop ks)
        deepest-m (get-in m ks')]
    (if deepest-m
      (assoc-in m ks' (dissoc deepest-m (peek ks)))
      m)))
