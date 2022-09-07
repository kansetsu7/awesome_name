(ns awesome-name.index
  (:require
    [clojure.string :as cs]
    [re-frame.core :as rf]
    [awesome-name.subs :as sub]
    [awesome-name.events :as evt]
    [reagent-mui.components :as mui]))

(defn form
  []
  [mui/grid {:container true :spacing 2}
   [mui/grid {:item true :xs 12}
    [mui/text-field {:label "姓氏"
                     :value (or @(rf/subscribe [::sub/form :surname]) "")
                     :variant "outlined"
                     :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:surname]] (.. % -target -value)))}]]
   [mui/grid {:item true :xs 12 :sm 2}
    [mui/text-field {:value (or @(rf/subscribe [::sub/form :zodiac]) "")
                     :label "生肖"
                     :select true
                     :full-width true
                     :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:zodiac]] (.. % -target -value)))}
     (doall
       (for [[option-idx [value label]] (map-indexed vector @(rf/subscribe [::sub/zodiac :select-options]))]
         [mui/menu-item {:key option-idx :value value} label]))]]

   [mui/grid {:item true :xs 12 :sm 3}
    [mui/text-field {:value (or @(rf/subscribe [::sub/form :combination-idx]) "")
                     :label "分數"
                     :select true
                     :full-width true
                     :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:combination-idx]] (.. % -target -value)))}
     (doall
       (for [[option-idx comb] (map-indexed vector @(rf/subscribe [::sub/valid-combinations]))]
         [mui/menu-item {:key option-idx :value option-idx} (:label comb)]))]]])

(defn render-element
  [ele]
  (when ele
    (let [color {"木" "green"
                 "火" "red"
                 "土" "brown"
                 "金" "gold"
                 "水" "blue"}]
      [:b {:style {:color (get color ele)}}
        (str "(" ele ")")])))

(defn sancai-calc
  []
  (let [surname @(rf/subscribe [::sub/form :surname])
        surname-ele @(rf/subscribe [::sub/character-element surname])
        {:keys [stroke elements]} @(rf/subscribe [::sub/selected-combination])]
    [mui/grid {:container true :spacing 2}
     [mui/grid {:item true}
      [:table {:style {:max-width "300px"}}
       [:tbody
        [:tr
         [:td {:valign "middle" :align "center" :width 60}
          "外格" [:br]
          (render-element (get elements 3))]
         [:td {:valign "top" :align "left" :width 20}
          "┌" [:br]
          "│" [:br]
          "│" [:br]
          "┤" [:br]
          "│" [:br]
          "│" [:br]
          "└" [:br]]
         [:td {:valign "top" :align "left" :width 45}
          "(1 劃)" [:br]
          [:br]
          [:span surname
           (render-element surname-ele) " "]
          [:b (str (:top stroke)" 劃")]
          [:br]
          [:br]
          (str (:middle stroke) " 劃")
          [:br]
          [:br]
          (str (:bottom stroke) " 劃")]
         [:td {:valign "top" :align "left" :width 100}
          "┐" [:br]
          "├天格" (render-element (get elements 0)) [:br]
          "┤" [:br]
          "├人格" (render-element (get elements 1)) [:br]
          "┤" [:br]
          "├地格" (render-element (get elements 2)) [:br]
          "┘"]]
        [:tr
         [:td {:valign "top" :align "center" :col-span 4}
          "______________" [:br]
          "總格"
          (render-element (get elements 4))]]]]]]))

(defn zodiac-table
  []
  (let [surname @(rf/subscribe [::sub/form :surname])
        {:keys [stroke]} @(rf/subscribe [::sub/selected-combination])]
    [mui/grid {:container true :sx {:margin-top "10px"}}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tbody
       [:tr
        [:th {:width "15%" :style {:border-style "solid" :border-width "1px"}} "欄位"]
        [:th {:width "70%" :style {:border-style "solid" :border-width "1px"} :col-span 2} "選字"]]
       [:tr
        [:td {:style {:border-style "solid" :border-width "1px"}}
         "姓" [:br]
         (str "筆劃:" (:top stroke))]
        [:td {:col-span 2 :style {:border-style "solid" :border-width "1px"}}
         surname]]
       (doall
         (for [[idx position] (map-indexed vector [:middle :bottom])]
           (let [{:keys [better normal worse]} @(rf/subscribe [::sub/preferred-characters position])]
             [:<> {:key idx}
              [:tr
               [:td {:row-span 3 :style {:border-style "solid" :border-width "1px"}}
                (str "名(第" (inc idx) "字)") [:br]
                (str "筆劃:" (get stroke position))]
               [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
                "生肖喜用"]
               [:td {:style {:border-style "solid" :border-width "1px"}}
                (->> (map str better)
                     (cs/join ", "))]]
              [:tr
               [:td {:style {:border-style "solid" :border-width "1px"}}
                "不喜不忌"]
               [:td {:style {:border-style "solid" :border-width "1px"}}
                (->> (map str normal)
                     (cs/join ", "))]]
              [:tr
               [:td {:style {:border-style "solid" :border-width "1px"}}
                "生肖忌用"]
               [:td {:style {:border-style "solid" :border-width "1px"}}
                (->> (map str worse)
                     (cs/join ", "))]]])))]]]))

(defn sancai-table
  []
  (let [{:keys [sancai-elements]} @(rf/subscribe [::sub/selected-combination])
        {:keys [description luck]} (get @(rf/subscribe [::sub/sancai :combinations]) sancai-elements)]
    [mui/grid {:container true :sx {:margin-top "10px"}}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tbody
       [:tr
        [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
         "三才姓名學"]]
       [:tr
        [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
         sancai-elements]
        [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
         luck]
        [:td {:width "70%" :style {:border-style "solid" :border-width "1px"}}
         description]]]]]))

(defn wuger-table
  []
  (let [{:keys [gers elements]} @(rf/subscribe [::sub/selected-combination])]
    [mui/grid {:container true :sx {:margin-top "10px"}}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tbody
       [:tr
        [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
         "五格姓名學"]]
       (doall
         (for [[idx ger] (map-indexed vector gers)]
           (let [{:keys [description luck]} (get @(rf/subscribe [::sub/eighty-one]) (dec ger))
                 element (get elements idx)
                 ger-zh (-> (get ["天格" "人格" "地格" "外格" "總格"] idx)
                            (str "(" ger ")劃"))]
             [:tr {:key idx}
              [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
               ger-zh
               (render-element element)]
              [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
               luck]
              [:td {:width "70%" :style {:border-style "solid" :border-width "1px"}}
               description]])))]]]))

(defn index
  []
  [:<>
   [form]
   [sancai-calc]
   [zodiac-table]
   [sancai-table]
   [wuger-table]])
