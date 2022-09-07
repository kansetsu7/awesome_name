(ns awesome-name.subs
  (:require
    [clojure.set :as cset]
    [clojure.string :as cs]
    [awesome-name.util :as u]
    [re-frame.core :as rf]))

(rf/reg-sub ::combinations-page
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form :combinations] fields)))))

(rf/reg-sub ::error
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:field-error-message] fields)))))

(rf/reg-sub ::advanced-option
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form :combinations :advanced-option] fields)))))

(doseq [field [::zodiac ::chinese-characters ::sancai ::eighty-one ::default-taboo-characters ::current-page]]
  (rf/reg-sub field
              (fn [db [_ & fields]]
                (-> db
                    (get-in (into [:app (-> field name keyword)] fields))))))

(rf/reg-sub ::character-element
            :<- [::chinese-characters]
            (fn [chinese-characters [_ character]]
              (:element (u/character-attrs chinese-characters character))))

(rf/reg-sub ::surname-strokes
            :<- [::combinations-page :surname]
            :<- [::chinese-characters]
            (fn [[surname chinese-characters]]
              (->> (seq surname)
                   (map #(u/strokes-of chinese-characters (str %))))))

(rf/reg-sub ::dictionary-strokes-ranges
            :<- [::chinese-characters]
            (fn [chinese-characters]
              (let [strokes (map :strokes chinese-characters)]
                (range (apply min strokes) (inc (apply max strokes))))))

(rf/reg-sub ::strokes-options
            :<- [::dictionary-strokes-ranges]
            (fn [r]
              (set r)))

(rf/reg-sub ::all-combination-data
            :<- [::sancai :combinations]
            :<- [::eighty-one]
            :<- [::dictionary-strokes-ranges]
            :<- [::surname-strokes]
            :<- [::advanced-option :strokes-to-remove]
            :<- [::advanced-option :single-given-name]
            (fn [[sancai-combinations eighty-one dictionary-strokes-ranges surname-strokes strokes-to-remove single-given-name]]
              (->> (u/all-strokes-combinations surname-strokes dictionary-strokes-ranges single-given-name)
                   (filter (fn [[_s-strokes g-strokes]] (empty? (cset/intersection (set g-strokes) strokes-to-remove))))
                   (map (fn [[s-strokes g-strokes]]
                          (let [ger-elements (u/name-strokes->ger-elements s-strokes g-strokes)
                                sancai-elements (->> (take 3 ger-elements)
                                                     (apply str))
                                gers (u/name-strokes->gers s-strokes g-strokes)]
                            {:elements ger-elements
                             :strokes {:surname (vec s-strokes)
                                       :given-name (vec g-strokes)}
                             :gers gers
                             :wuger-pts (u/gers->81pts eighty-one gers)
                             :sancai-pts (get-in sancai-combinations [sancai-elements :value])
                             :sancai-elements sancai-elements}))))))

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
            :<- [::advanced-option :min-wuger-pts]
            (fn [[all-combinations min-sancai-pts min-wuger-pts]]
              (->> all-combinations
                   (filter (fn [{:keys [wuger-pts sancai-pts]}]
                             (and (>= sancai-pts min-sancai-pts)
                                  (>= wuger-pts min-wuger-pts))))
                   (map u/add-combination-label)
                   u/sort-by-wuger-pts-and-strokes
                   vec)))

(rf/reg-sub ::selected-combination
            :<- [::combinations-page :combination-idx]
            :<- [::valid-combinations]
            (fn [[idx comb]]
              (get comb idx)))

(rf/reg-sub ::sancai-attrs-of-selected-combination
            :<- [::selected-combination]
            :<- [::sancai :combinations]
            (fn [[{:keys [sancai-elements]} sancai-combinations]]
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
