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
        surname-strokes @(rf/subscribe [::sub/surname-strokes])
        surname-ele @(rf/subscribe [::sub/character-element surname])
        selected-combination @(rf/subscribe [::sub/selected-combination])
        top-ele (get-in selected-combination [:top :ele])
        middle-ele (get-in selected-combination [:middle :ele])
        bottom-ele (get-in selected-combination [:bottom :ele])]
    [mui/grid {:container true :spacing 2}
     [mui/grid {:item true}
      [:table {:style {:max-width "300px"}}
       [:tbody
        [:tr
         [:td {:valign "middle" :align "center" :width 60}
          "外格" [:br]]
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
          [:b (str surname-strokes " 劃")]
          [:br]
          [:br]
          (str (get-in selected-combination [:middle :stroke]) " 劃")
          [:br]
          [:br]
          (str (get-in selected-combination [:bottom :stroke]) " 劃")]
         [:td {:valign "top" :align "left" :width 100}
          "┐" [:br]
          "├天格" (render-element top-ele) [:br]
          "┤" [:br]
          "├人格" (render-element middle-ele) [:br]
          "┤" [:br]
          "├地格" (render-element bottom-ele) [:br]
          "┘"]]
        [:tr
         [:td {:valign "top" :align "center" :col-span 4}
          "______________" [:br]
          "總格"
          (render-element (get selected-combination :ttl-e))]]]]]]))

(defn zodiac-table
  []
  (let [surname @(rf/subscribe [::sub/form :surname])
        selected-combination @(rf/subscribe [::sub/selected-combination])]
    [mui/grid {:container true :sx {:margin-top "10px"}}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tr
       [:th {:width "15%" :style {:border-style "solid" :border-width "1px"}} "欄位"]
       [:th {:width "70%" :style {:border-style "solid" :border-width "1px"} :col-span 2} "選字"]]
      [:tr
       [:td {:style {:border-style "solid" :border-width "1px"}}
        "姓" [:br]
        (str "筆劃:" (get-in selected-combination [:top :stroke]))]
       [:td {:col-span 2 :style {:border-style "solid" :border-width "1px"}}
        surname]]
      (doall
        (for [[idx position] (map-indexed vector [:middle :bottom])]
          (let [{:keys [better normal worse]} @(rf/subscribe [::sub/preferred-characters position])]
            [:<> {:key idx}
             [:tr
              [:td {:row-span 3 :style {:border-style "solid" :border-width "1px"}}
               (str "名(第" (inc idx) "字)") [:br]
               (str "筆劃:" (get-in selected-combination [position :stroke]))]
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
                    (cs/join ", "))]]])))]]))

(defn index
  []
  [:<>
   [form]
   [sancai-calc]
   [zodiac-table]])
