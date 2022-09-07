(ns awesome-name.subs
  (:require
    [awesome-name.util :as u]
    [re-frame.core :as rf]))

(rf/reg-sub ::form
            (fn [db [_ & fields]]
              (-> db
                  (get-in (into [:form] fields)))))

(doseq [field [::zodiac ::chinese-characters ::sancai ::eighty-one]]
  (rf/reg-sub field
              (fn [db [_ & fields]]
                (-> db
                    (get-in (into [:app (-> field name keyword)] fields))))))

(rf/reg-sub ::character-element
            :<- [::chinese-characters]
            (fn [chinese-characters [_ character]]
              (:element (u/character-attrs chinese-characters character))))

(rf/reg-sub ::surname-strokes
            :<- [::form :surname]
            :<- [::chinese-characters]
            (fn [[surname chinese-characters]]
              (u/strokes-of chinese-characters surname)))


(rf/reg-sub ::dictionary-stroke-ranges
            :<- [::chinese-characters]
            (fn [chinese-characters]
              (let [strokes (map :stroke chinese-characters)]
                (range (apply min strokes) (inc (apply max strokes))))))

(rf/reg-sub ::valid-combinations
            :<- [::sancai :combinations]
            :<- [::eighty-one]
            :<- [::dictionary-stroke-ranges]
            :<- [::surname-strokes]
            :<- [::form :min-luck-val]
            :<- [::form :min-pts]
            (fn [[sancai-combinations eighty-one dictionary-stroke-ranges surname-strokes min-luck-val min-pts]]
              (->> (u/all-strokes-combinations surname-strokes dictionary-stroke-ranges)
                   (map (fn [[ts ms bs]]
                          (let [[te me be ttl-e] (u/name-strokes->elements ts ms bs)
                                gers (u/name-strokes->gers ts ms bs)
                                comb (str te me be)]
                            {:comb   comb
                             :top    {:stroke ts :ele te}
                             :middle {:stroke ms :ele me}
                             :bottom {:stroke bs :ele be}
                             :ttl-e  ttl-e
                             :gers   gers
                             :pts    (u/gers->81pts eighty-one gers)
                             :sancai (sancai-combinations comb)})))
                   (filter (fn [{:keys [sancai pts]}] (and (>= (:value sancai) min-luck-val)
                                                           (>= pts min-pts))))
                   (map u/add-combination-label)
                   u/sort-by-pts-and-strokes
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
            (fn [[preferred-characters zodiac selected-combination chinese-characters] [_ position]]
              (let [stroke (get-in selected-combination [position :stroke])
                    stroke-key (-> stroke str keyword)
                    {:keys [better worse]} (get preferred-characters (keyword zodiac))
                    b-chars (u/string->char-set (or (get better stroke-key) []))
                    w-chars (u/string->char-set (get worse stroke-key))]
                {:better b-chars
                 :normal (u/normal-characters chinese-characters b-chars w-chars stroke)
                 :worse  w-chars})))
