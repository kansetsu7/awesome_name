(ns awesome-name.subs
  (:require
    [awesome-name.util :as u]
    [clojure.set :as cset]
    [clojure.string :as cs]
    [re-frame.core :as rf]))

(rf/reg-sub ::combinations-page
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form :combinations] fields)))))

(rf/reg-sub ::evaluation-page
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form :evaluation] fields)))))

(rf/reg-sub ::advanced-option
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form :combinations :advanced-option] fields)))))

(doseq [field [::zodiac ::sancai ::eighty-one ::default-taboo-characters ::current-page]]
  (rf/reg-sub field
              (fn [db [_ & fields]]
                (-> db
                    (get-in (into [:app (-> field name keyword)] fields))))))

(rf/reg-sub ::chinese-characters
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:app :dictionary :kang-xi] fields)))))

(rf/reg-sub ::element-characters
            (fn [db]
              (-> db
                  (get-in [:app :element-characters]))))

(rf/reg-sub ::character-element
            :<- [::element-characters]
            (fn [element-characters [_ character]]
              (->> element-characters
                   (filter (fn [[_ characters]] (cs/includes? characters character)))
                   first
                   first)))

(rf/reg-sub ::birth-hour-options
            (fn [db]
              (-> db
                  (get-in [:app :birth-hour-options]))))

(rf/reg-sub ::dictionary-strokes
            :<- [::chinese-characters]
            (fn [chinese-characters]
              (->> chinese-characters
                   (map :strokes)
                   sort
                   vec)))

(rf/reg-sub ::all-combination-data
            :<- [::sancai :combinations]
            :<- [::eighty-one]
            :<- [::chinese-characters]
            :<- [::dictionary-strokes]
            :<- [::combinations-page :surname]
            :<- [::combinations-page :zodiac]
            :<- [::advanced-option :strokes-to-remove]
            :<- [::advanced-option :single-given-name]
            (fn [[sancai-combinations eighty-one chinese-characters dictionary-strokes surname zodiac strokes-to-remove single-given-name]]
              (if (or (= "" surname) (= "" zodiac))
                []
                (let [surname-strokes (u/string->strokes surname chinese-characters)]
                  (->> (u/all-strokes-combinations surname-strokes dictionary-strokes single-given-name)
                       (filter (fn [[_s-strokes g-strokes]] (empty? (cset/intersection (set g-strokes) strokes-to-remove))))
                       (map (fn [[s-strokes g-strokes]]
                              (u/name-strokes-evaluation s-strokes g-strokes eighty-one sancai-combinations))))))))

(rf/reg-sub ::sancai-luck-options
            :<- [::sancai :combinations]
            (fn [comb]
              (->> (vals comb)
                   (map #((juxt :luck :value) %))
                   (reduce (fn [memo [luck pts]]
                              (update memo pts #(if (nil? %) #{luck} (conj % luck))))
                           {})
                   (map (fn [[pts lucks]] [pts (->> (vec lucks)
                                                    (cs/join " / ")
                                                    (str pts "分："))]))
                   (sort-by first)
                   reverse
                   vec)))

(rf/reg-sub ::valid-combinations
            :<- [::all-combination-data]
            :<- [::advanced-option :min-sancai-pts]
            :<- [::advanced-option :min-81-pts]
            (fn [[all-combinations min-sancai-pts min-81-pts]]
              (->> all-combinations
                   (filter (fn [{:keys [points]}]
                             (and (>= (:sancai points) min-sancai-pts)
                                  (>= (:eighty-one points) min-81-pts))))
                   (map u/add-combination-label)
                   u/sort-by-points-and-strokes
                   vec)))

(rf/reg-sub ::selected-combination
            :<- [::combinations-page :combination-idx]
            :<- [::valid-combinations]
            (fn [[idx comb]]
              (get comb idx)))

(rf/reg-sub ::sancai-attrs-of-selected-combination
            :<- [::sancai :combinations]
            (fn [sancai-combinations [_ {:keys [sancai-elements]}]]
              (get sancai-combinations sancai-elements)))

(rf/reg-sub ::preferred-characters
            :<- [::zodiac :preferred-characters]
            :<- [::combinations-page :zodiac]
            :<- [::selected-combination]
            :<- [::chinese-characters]
            :<- [::advanced-option]
            (fn [[preferred-characters zodiac selected-combination chinese-characters advanced-option] [_ idx]]
              (let [strokes (get-in selected-combination [:strokes :given-name idx])
                    strokes-key (-> strokes str keyword)
                    {:keys [better worse]} (get preferred-characters (keyword zodiac))
                    b-chars (u/string->char-set (or (get better strokes-key) []))
                    w-chars (u/string->char-set (get worse strokes-key))
                    {:keys [remove-chars chars-to-remove]} advanced-option
                    char-set-to-remove (into #{} chars-to-remove)]
                (cond->> [b-chars
                          (u/normal-characters chinese-characters b-chars w-chars strokes)
                          w-chars]
                  remove-chars (map #(cset/difference % char-set-to-remove))
                  :always (zipmap [:better :normal :worse])))))

(rf/reg-sub ::four-pillars-element-ratio
            :<- [::combinations-page :elements]
            (fn [elements]
              (->> (-> elements vals flatten frequencies)
                   (map (fn [[ele feq]] (str ele " " feq)))
                   (cs/join " : "))))

(rf/reg-sub ::evaluation-result
            :<- [::evaluation-page :surname]
            :<- [::evaluation-page :given-name]
            :<- [::chinese-characters]
            :<- [::eighty-one]
            :<- [::sancai :combinations]
            (fn [[surname given-name chinese-characters eighty-one sancai-combinations]]
              (let [surname-strokes (u/string->strokes surname chinese-characters)
                    given-name-strokes (u/string->strokes given-name chinese-characters)]
                (u/name-strokes-evaluation surname-strokes given-name-strokes eighty-one sancai-combinations))))

(rf/reg-sub ::show-evaluation?
            :<- [::evaluation-page :surname]
            :<- [::evaluation-page :given-name]
            :<- [::name-errors :surname]
            :<- [::name-errors :given-name]
            (fn [[surname given-name s-err g-err]]
              (and (seq surname)
                   (seq given-name)
                   (cs/blank? s-err)
                   (cs/blank? g-err))))

;; errors

(rf/reg-sub ::error
            (fn [db [_ field]]
              (let [current-page (keyword (get-in db [:app :current-page]))]
                (-> db
                    (get-in [:field-error-message current-page field])))))

(rf/reg-sub ::name-errors
            (fn [db [_ field]]
              (let [current-page (keyword (get-in db [:app :current-page]))
                    chinese-characters (get-in db [:app :dictionary :kang-xi])
                    name-str (get-in db [:form current-page field])]
                (cs/join " / " (u/name-errors name-str chinese-characters)))))
