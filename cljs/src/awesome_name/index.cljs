(ns awesome-name.index
  (:require
    [awesome-name.component.core :as cpt]
    [clojure.string :as cs]
    [re-frame.core :as rf]
    [awesome-name.subs :as sub]
    [awesome-name.events :as evt]
    [reagent-mui.components :as mui]
    [reagent-mui.icons.expand-more :as icon-expand-more]
    [reagent-mui.icons.visibility :as icon-visibility]
    [reagent-mui.icons.visibility-off :as icon-visibility-off]
    [reagent.core :as r]))

(defn form
  []
  [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
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

(defn points-tab
  []
  [cpt/tab-panel {:value "points"}
   [mui/grid {:container true :spacing 2}
    [mui/grid {:item true :xs 1}
     [mui/text-field {:value (or @(rf/subscribe [::sub/form :min-wuger-pts]) 0)
                      :label "五格分數低標"
                      :full-width true
                      :variant "outlined"
                      :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:min-wuger-pts]] (.. % -target -value)))}]]
    [mui/grid {:item true :xs 1}
     [mui/text-field {:value (or @(rf/subscribe [::sub/form :min-sancai-pts]) 0)
                      :label "三才分數低標"
                      :full-width true
                      :variant "outlined"
                      :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:min-sancai-pts]] (.. % -target -value)))}]]]])

(defn strokes-tab
  [{:keys [strokes-to-remove]}]
  [mui/grid {:container true :spacing 2}
   [mui/grid {:item true :xs 12}
    "排除筆劃"]
   (doall
     (for [[idx strokes] (map-indexed vector @(rf/subscribe [::sub/strokes-options]))]
       [mui/grid {:item true :xs 1 :key idx}
        [mui/form-control-label
         {:label (str strokes)
          :control (r/as-element
                     [mui/checkbox {:checked (boolean (strokes-to-remove strokes))
                                    :on-change #(rf/dispatch-sync [::evt/update-strokes-to-remove strokes (.. % -target -checked)])}])}]]))])

(defn chars-tab
  [{:keys [remove-chars click-to-remove use-default-taboo-characters chars-to-remove]}]
  [mui/grid {:container true :spacing 2}
   [mui/grid {:item true :xs 12}
    [mui/form-control-label
     {:label "刪除特定字"
      :control (r/as-element
                 [mui/switch {:checked remove-chars
                              :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:advanced-option :remove-chars]] (.. % -target -checked)))}])}]]
   (when remove-chars
     [:<>
      [mui/grid {:item true :xs 12 :sx {:margin-left "10px"}}
       [mui/form-control-label
        {:label "啟用點擊隱藏字"
         :control (r/as-element
                    [mui/switch {:checked click-to-remove
                                 :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:advanced-option :click-to-remove] (.. % -target -checked)]))}])}]]
      [mui/grid {:item true :xs 12 :sx {:margin-left "10px"}}
       [mui/form-control-label
        {:label "載入預設禁字"
         :control (r/as-element
                    [mui/switch {:checked use-default-taboo-characters
                                 :on-change #(rf/dispatch-sync (conj [::evt/set-use-default-taboo-characters] (.. % -target -checked)))}])}]]
      [mui/grid {:item true :xs 12 :sx {:margin-left "10px"}}
       [mui/text-field {:value chars-to-remove
                        :variant "outlined"
                        :full-width true
                        :multiline true
                        :disabled (not remove-chars)
                        :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:advanced-option :chars-to-remove]] (.. % -target -value)))}]]])])

(defn advanced-option
  []
  (let [advanced-option @(rf/subscribe [::sub/advanced-option])]
    [mui/accordion {:sx {:margin-top "10px"}}
     [mui/accordion-summary {:expand-icon (r/as-element [icon-expand-more/expand-more])
                             :aria-controls :adv-opt-content
                             :id :adv-opt-header
                             :sx {:background-color "indigo"
                                  :color "white"}}
      [mui/typography "進階選項"]]
     [mui/accordion-details
      [cpt/tab-context {:value (:tab advanced-option)}
       [cpt/tab-list {:on-change #(rf/dispatch-sync [::evt/set-form-field [:advanced-option :tab] %2])}
        [mui/tab {:label "設定分數" :value "points"}]
        [mui/tab {:label "設定筆劃" :value "strokes"}]
        [mui/tab {:label "設定禁字" :value "chars"}]]
       [cpt/tab-panel {:value "points"}
        [points-tab]]
       [cpt/tab-panel {:value "strokes"}
        [strokes-tab advanced-option]]
       [cpt/tab-panel {:value "chars"}
        [chars-tab advanced-option]]]]]))

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
  [{:keys [strokes gers elements]}]
  (let [surname @(rf/subscribe [::sub/form :surname])
        surname-ele @(rf/subscribe [::sub/character-element surname])]
    [mui/grid {:item true :xs 12}
     [:table {:style {:max-width "300px"}}
      [:tbody
       [:tr
        [:td {:valign "middle" :align "center" :width 70}
         (str "外格:" (get gers 3)) [:br]
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
         [:b (str (:top strokes)" 劃")]
         [:br]
         [:br]
         (str (:middle strokes) " 劃")
         [:br]
         [:br]
         (str (:bottom strokes) " 劃")]
        [:td {:valign "top" :align "left" :width 100}
         "┐" [:br]
         "├天格" (str ":" (get gers 0)) (render-element (get elements 0)) [:br]
         "┤" [:br]
         "├人格" (str ":" (get gers 1)) (render-element (get elements 1)) [:br]
         "┤" [:br]
         "├地格" (str ":" (get gers 2)) (render-element (get elements 2)) [:br]
         "┘"]]
       [:tr
        [:td {:valign "top" :align "center" :col-span 4}
         "______________" [:br]
         (str "總格:" (get gers 4))
         (render-element (get elements 4))]]]]]))

(defn zodiac-table
  [{:keys [strokes]}]
  (let [surname @(rf/subscribe [::sub/form :surname])
        hide-zodiac-chars @(rf/subscribe [::sub/form :hide-zodiac-chars])]
    [mui/grid {:item true :xs 11}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tbody
       [:tr
        [:th {:width "15%" :style {:border-style "solid" :border-width "1px"}} "欄位"]
        [:th {:width "70%" :style {:border-style "solid" :border-width "1px"} :col-span 2} "選字"]]
       [:tr
        [:td {:style {:border-style "solid" :border-width "1px"}}
         "姓" [:br]
         (str "筆劃:" (:top strokes))]
        [:td {:col-span 2 :style {:border-style "solid" :border-width "1px"}}
         surname]]
       (doall
         (for [[idx position] (map-indexed vector [:middle :bottom])]
           (let [{:keys [better normal worse]} @(rf/subscribe [::sub/preferred-characters position])
                 hide-normal-chars (get-in hide-zodiac-chars [:normal idx])
                 hide-worse-chars (get-in hide-zodiac-chars [:worse idx])]
             [:<> {:key idx}
              [:tr
               [:td {:row-span 3 :style {:border-style "solid" :border-width "1px"}}
                (str "名(第" (inc idx) "字)") [:br]
                (str "筆劃:" (get strokes position))]
               [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
                "生肖喜用"]
               [:td {:style {:border-style "solid" :border-width "1px" :padding-top "15px" :padding-bottom "15px"}}
                (doall
                  (for [[c-idx c] (map-indexed vector better)]
                    (if (= c-idx (-> better count dec))
                      [mui/typography {:key c-idx :variant :span :font-size "1.2rem" :on-click #(rf/dispatch-sync [::evt/add-chars-to-remove (.. % -target -textContent)])}
                       c]
                      [:<> {:key c-idx}
                       [mui/typography {:variant :span :font-size "1.2rem" :on-click #(rf/dispatch-sync [::evt/add-chars-to-remove (.. % -target -textContent)])}
                        c]
                       [mui/typography {:variant :span :font-size "1.2rem"}
                        ", "]])))]]
              [:tr
               [:td {:style {:border-style "solid" :border-width "1px" :border-top-width "1.5px"}}
                "不喜不忌"
                [mui/icon-button {:aria-label "vis-normal" :size "small" :on-click #(rf/dispatch-sync [::evt/set-form-field [:hide-zodiac-chars :normal idx] (not hide-normal-chars)])}
                 (if hide-normal-chars
                   [icon-visibility-off/visibility-off]
                   [icon-visibility/visibility])]]
               [:td {:style {:border-style "solid" :border-width "1px" :border-top-width "1.5px" :padding-top "15px" :padding-bottom "15px"}}
                (when-not hide-normal-chars
                  (doall
                    (for [[c-idx c] (map-indexed vector normal)]
                      (if (= c-idx (-> normal count dec))
                        [mui/typography {:key c-idx :variant :span :font-size "1.2rem" :on-click #(rf/dispatch-sync [::evt/add-chars-to-remove (.. % -target -textContent)])}
                         c]
                        [:<> {:key c-idx}
                         [mui/typography {:variant :span :font-size "1.2rem" :on-click #(rf/dispatch-sync [::evt/add-chars-to-remove (.. % -target -textContent)])}
                          c]
                         [mui/typography {:variant :span :font-size "1.2rem"}
                          ", "]]))))]]
              [:tr
               [:td {:style {:border-style "solid" :border-width "1px"}}
                "生肖忌用"
                [mui/icon-button {:aria-label "vis-worse" :size "small" :on-click #(rf/dispatch-sync [::evt/set-form-field [:hide-zodiac-chars :worse idx] (not hide-worse-chars)])}
                 (if hide-worse-chars
                   [icon-visibility-off/visibility-off]
                   [icon-visibility/visibility])]]
               [:td {:style {:border-style "solid" :border-width "1px" :padding-top "15px" :padding-bottom "15px"}}
                (when-not hide-worse-chars
                  (doall
                    (for [[c-idx c] (map-indexed vector worse)]
                      (if (= c-idx (-> worse count dec))
                        [mui/typography {:key c-idx :variant :span :font-size "1.2rem" :on-click #(rf/dispatch-sync [::evt/add-chars-to-remove (.. % -target -textContent)])}
                         c]
                        [:<> {:key c-idx}
                         [mui/typography {:variant :span :font-size "1.2rem" :on-click #(rf/dispatch-sync [::evt/add-chars-to-remove (.. % -target -textContent)])}
                          c]
                         [mui/typography {:variant :span :font-size "1.2rem"}
                          ", "]]))))]]])))]]]))

(defn sancai-table
  [{:keys [sancai-elements sancai-pts]}]
  (let [{:keys [description luck]} (get @(rf/subscribe [::sub/sancai :combinations]) sancai-elements)]
    [mui/grid {:item true :xs 11}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tbody
       [:tr
        [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
         (str "三才姓名學 (" sancai-pts "分)")]]
       [:tr
        [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
         sancai-elements]
        [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
         luck]
        [:td {:width "70%" :style {:border-style "solid" :border-width "1px" :padding-top "15px" :padding-bottom "15px"}}
         [mui/typography {:font-size "1.2rem"}
          description]]]]]]))

(defn wuger-table
  [{:keys [gers elements wuger-pts]}]
  [mui/grid {:item true :xs 11}
   [:table {:width "100%" :style {:border-collapse "collapse"}}
    [:tbody
     [:tr
      [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
       (str "五格姓名學 (" wuger-pts "分)")]]
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
            [:td {:width "70%" :style {:border-style "solid" :border-width "1px" :padding-top "15px" :padding-bottom "15px"}}
             [mui/typography {:font-size "1.2rem"}
              description]]])))]]])


(defn index
  []
  [:<>
   [form]
   (when-let [selected-combination @(rf/subscribe [::sub/selected-combination])]
     [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
      [sancai-calc selected-combination]
      [zodiac-table selected-combination]
      [sancai-table selected-combination]
      [wuger-table selected-combination]])
   [advanced-option]])
