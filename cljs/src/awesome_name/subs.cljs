(ns awesome-name.subs
  (:require
    [clojure.set :as cset]
    [awesome-name.util :as u]
    [re-frame.core :as rf]))

(rf/reg-sub ::form
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form] fields)))))

(rf/reg-sub ::advanced-option
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form :advanced-option] fields)))))

(doseq [field [::zodiac ::chinese-characters ::sancai ::eighty-one ::default-taboo-characters]]
  (rf/reg-sub field
              (fn [db [_ & fields]]
                (-> db
                    (get-in (into [:app (-> field name keyword)] fields))))))

(rf/reg-sub ::character-element
            :<- [::chinese-characters]
            (fn [chinese-characters [_ character]]
              (:element (u/character-attrs chinese-characters character))))

(rf/reg-sub ::surname-char-count
            :<- [::form :surname]
            (fn [surname]
              (-> surname seq count)))

(rf/reg-sub ::surname-strokes
            :<- [::form :surname]
            :<- [::chinese-characters]
            (fn [[surname chinese-characters]]
              (u/strokes-of chinese-characters surname)))


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
            :<- [::surname-char-count]
            :<- [::advanced-option :strokes-to-remove]
            (fn [[sancai-combinations eighty-one dictionary-strokes-ranges surname-strokes surname-char-count strokes-to-remove]]
              (->> (u/all-strokes-combinations surname-strokes dictionary-strokes-ranges)
                   (filter (fn [strokes] (empty? (cset/intersection (set (drop surname-char-count strokes)) strokes-to-remove))))
                   (map (fn [[ts ms bs]]
                          (let [ger-elements (u/name-strokes->ger-elements ts ms bs)
                                sancai-elements (->> (take 3 ger-elements)
                                                     (apply str))
                                gers (u/name-strokes->gers ts ms bs)]
                            {:elements ger-elements
                             :strokes {:top    ts
                                       :middle ms
                                       :bottom bs}
                             :gers gers
                             :wuger-pts (u/gers->81pts eighty-one gers)
                             :sancai-pts (get-in sancai-combinations [sancai-elements :value])
                             :sancai-elements sancai-elements}))))))

(rf/reg-sub ::valid-combinations
            :<- [::all-combination-data]
            :<- [::form :min-sancai-pts]
            :<- [::form :min-wuger-pts]
            (fn [[all-combinations min-sancai-pts min-wuger-pts]]
              (->> all-combinations
                   (filter (fn [{:keys [wuger-pts sancai-pts]}]
                             (and (>= sancai-pts min-sancai-pts)
                                  (>= wuger-pts min-wuger-pts))))
                   (map u/add-combination-label)
                   u/sort-by-wuger-pts-and-strokes
                   vec)))

(rf/reg-sub ::selected-combination
            :<- [::form :combination-idx]
            :<- [::valid-combinations]
            (fn [[idx comb]]
              (get comb idx)))

(rf/reg-sub ::preferred-characters
            :<- [::zodiac :preferred-characters]
            :<- [::form :zodiac]
            :<- [::selected-combination]
            :<- [::chinese-characters]
            :<- [::advanced-option]
            (fn [[preferred-characters zodiac selected-combination chinese-characters advanced-option] [_ position]]
              (let [strokes (get-in selected-combination [:strokes position])
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
