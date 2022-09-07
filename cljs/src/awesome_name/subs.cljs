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

(rf/reg-sub ::dictionary-stroke-ranges
            :<- [::chinese-characters]
            (fn [chinese-characters]
              (let [strokes (map :stroke chinese-characters)]
                (range (apply min strokes) (inc (apply max strokes))))))

(rf/reg-sub ::valid-combinations
            :<- [::sancai :combinations]
            :<- [::eighty-one]
            :<- [::chinese-characters]
            :<- [::dictionary-stroke-ranges]
            :<- [::form :surname]
            :<- [::form :min-luck-val]
            :<- [::form :min-pts]
            (fn [[sancai-combinations eighty-one chinese-characters dictionary-stroke-ranges surname min-luck-val min-pts]]
              (let [top-stroke (u/strokes-of chinese-characters surname)]
                (->> (u/all-strokes-combinations top-stroke dictionary-stroke-ranges)
                     (map (fn [[ts ms bs]]
                            (let [[te me be] (u/name-strokes->elements ts ms bs)
                                  gers (u/name-strokes->gers ts ms bs)
                                  comb (str te me be)]
                              {:comb   comb
                               :top    {:stroke ts :ele te}
                               :middle {:stroke ms :ele me}
                               :bottom {:stroke bs :ele be}
                               :gers   gers
                               :pts    (u/gers->81pts eighty-one gers)
                               :sancai (sancai-combinations comb)})))
                     (filter (fn [{:keys [sancai pts]}] (and (>= (:value sancai) min-luck-val)
                                                             (>= pts min-pts))))
                     (map u/add-combination-label)
                     u/sort-by-pts-and-strokes
                     vec))))
