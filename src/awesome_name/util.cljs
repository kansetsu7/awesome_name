(ns awesome-name.util
  (:require
    [cljs-time.format :as cljs-time]
    [clojure.set :as cset]
    [clojure.string :as cs]
    [lunar-calendar :as lc]))

(defn strokes-of
  "Get strokes of a character"
  [chinese-characters character]
  (-> (filter (fn [{:keys [characters]}]
                (cs/includes? characters character))
              chinese-characters)
      first
      :strokes))

(defn string->strokes
  "Get each strokes of character in the string"
  [string chinese-characters]
  (->> (seq string)
       (map #(strokes-of chinese-characters (str %)))))

(defn all-strokes-combinations
  "Given surname strokes and stroke-range then return all combinations of strokes."
  [surname-strokes stroke-range single-given-name]
  (if single-given-name
    (map (fn [given-name-strokes] [surname-strokes [given-name-strokes]]) stroke-range)
    (mapcat
      (fn [m-stroke]
        (map (fn [b-stroke] [surname-strokes [m-stroke b-stroke]])
             stroke-range))
      stroke-range)))

(defn name-strokes->gers
  "Given name strokes and return value of 五格 (called gers here)"
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
         (or single-surname? single-given-name?) (inc $)
         :else $))
     full-name-strokes-sum]))

;; https://www.163.com/dy/article/DQJQ7PK60528ETV2.html
(defn name-strokes->ger-elements
  "given name strokes return element of each character."
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

(defn name-strokes-evaluation
  [surname-strokes given-name-strokes eighty-one sancai-combinations]
  (let [ger-elements (name-strokes->ger-elements surname-strokes given-name-strokes)
        sancai-elements (->> (take 3 ger-elements)
                             (apply str))
        gers (name-strokes->gers surname-strokes given-name-strokes)
        eighty-one-pts (gers->81pts eighty-one gers)
        sancai-pts (get-in sancai-combinations [sancai-elements :value])]
    {:elements ger-elements
     :strokes {:surname (vec surname-strokes)
               :given-name (vec given-name-strokes)}
     :gers gers
     :points {:eighty-one eighty-one-pts
              :sancai sancai-pts
              :average (/ (+ sancai-pts eighty-one-pts) 2)}
     :sancai-elements sancai-elements}))

(defn sort-by-points-and-strokes
  "average points desc, strokes asc"
  [combinations]
  (sort-by (fn [{:keys [points strokes]}]
             (let [s-strokes (:surname strokes)
                   g-strokes (:given-name strokes)]
               (reduce #(into %1 %2) [(* -1 (:average points))] [s-strokes g-strokes])))
           combinations))

(defn add-combination-label
  [{:keys [points strokes] :as comb}]
  (let [combined-strokes (->> (into (:surname strokes) (:given-name strokes))
                              (cs/join ", "))]
    (assoc comb :label (str "適合筆畫：" combined-strokes " (綜合分數：" (:average points) "）"))))

(defn string->char-set
  [string]
  (->> (mapcat seq string)
       (into #{})))

(defn normal-characters
  [chinese-characters better-chars worse-chars strokes]
  (let [same-stroke-chars (->> chinese-characters
                               (filter #(= (:strokes %) strokes))
                               first
                               :characters
                               string->char-set)]
    (cset/difference same-stroke-chars better-chars worse-chars)))

(defn dissoc-in
  [m ks]
  (let [ks' (pop ks)
        deepest-ele (get-in m ks')]
    (cond
      (= 1 (count ks)) (dissoc m (first ks))
      (map? deepest-ele) (assoc-in m ks' (dissoc deepest-ele (peek ks)))
      :else m)))

(defmulti name-error-message
  (fn [err-type & _] err-type))

(defmethod name-error-message :not-found
  [_err-type name-str indexes]
  (let [name-chars (-> name-str seq vec)
        not-found-chars (map #(get name-chars %) indexes)]
    (str "抱歉，字典內找不到 " (cs/join "、" (distinct not-found-chars)))))

(defmethod name-error-message :invalid-count
  [_]
  "姓與名只允許 1 ~ 2 個字")

(defn name-errors
  [name-str chinese-characters]
  (let [strokes (string->strokes name-str chinese-characters)
        nil-strokes-idx (->> strokes
                             (map-indexed vector)
                             (filter (fn [[_ s]] (nil? s)))
                             (map first))]
    (cond-> []
      (seq nil-strokes-idx) (conj (name-error-message :not-found name-str nil-strokes-idx))
      (> (count strokes) 2) (conj (name-error-message :invalid-count)))))

(def heavenly-stems ;; 天干
  ["甲" "乙" "丙" "丁" "戊" "己" "庚" "辛" "壬" "癸"])

(def earthly-branches ;; 地支
  ["子" "丑" "寅" "卯" "辰" "巳" "午" "未" "申" "酉" "戌" "亥"])

(def cljs-time-formatter (cljs-time/formatter "yyyy-MM-dd"))

(defn goog-datetime->str
  [date]
  (cljs-time/unparse cljs-time-formatter date))

(defn str->goog-date
  [s]
  (cljs-time/parse cljs-time-formatter s))

(defn goog-date->lunar-data
  [date]
  (let [y (.getYear date)
        m (inc (.getMonth date))
        d (.getDate date)]
    (-> (lc/calendar y m)
        js->clj
        (get-in ["monthData" (dec d)]))))

(defn goog-date->sexagenary-cycle-info
  [date]
  (let [lunar-data (goog-date->lunar-data date)
        date-info (->> ["GanZhiYear" "GanZhiMonth" "GanZhiDay"]
                       (map #(get lunar-data %))
                       (map seq)
                       (mapv #(mapv str %))
                       (zipmap [:year :month :day]))]
    {:four-pillars date-info
     :zodiac (get lunar-data "zodiac")}))

(defn sexagenary-cycle->elements
  "Transform heavenly-stem(天干) and earthly-branch(地支) into elements(五行)"
  [[heavenly-stem earthly-branch]]
  (let [hs-ele (->> (.indexOf heavenly-stems heavenly-stem)
                    (get ["木" "木" "火" "火" "土" "土" "金" "金" "水" "水"]))
        eb-ele (case earthly-branch
                 "亥" "水"
                 "子" "水"
                 "寅" "木"
                 "卯" "木"
                 "巳" "火"
                 "午" "火"
                 "申" "金"
                 "酉" "金"
                 "土")]
    [hs-ele eb-ele]))

(defn earthly-branch-hour->sexagenary-hour
  "Given earthly-branch-hour(時辰地支) and sexagenary-day(干支紀日), get sexagenary-hour(干支紀時)"
  [ebt [day-hs _]]
  (let [zi-hs (case day-hs
                "甲" "甲"
                "己" "甲"
                "乙" "丙"
                "庚" "丙"
                "丙" "戊"
                "辛" "戊"
                "丁" "庚"
                "壬" "庚"
                "戊" "壬"
                "癸" "壬")
        offset (.indexOf earthly-branches ebt)
        hs-idx (-> (.indexOf heavenly-stems zi-hs)
                   (+ offset)
                   (mod 10))]
    [(get heavenly-stems hs-idx)
     ebt]))
