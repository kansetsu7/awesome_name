(ns awesome-name.views.combinations
  (:require
    [awesome-name.component.core :as cpt]
    [awesome-name.views.shared :as shared]
    [clojure.string :as cs]
    [re-frame.core :as rf]
    [awesome-name.subs :as sub]
    [awesome-name.events :as evt]
    [reagent-mui.components :as mui]
    [reagent-mui.icons.expand-more :as icon-expand-more]
    [reagent-mui.icons.visibility :as icon-visibility]
    [reagent-mui.icons.visibility-off :as icon-visibility-off]
    [reagent-mui.icons.download :as icon-download]
    [reagent-mui.icons.upload :as icon-upload]
    [reagent.core :as r]))

(defn form
  []
  [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
   [mui/grid {:item true :xs 12}
    [mui/text-field {:label "姓氏"
                     :value (or @(rf/subscribe [::sub/combinations-page :surname]) "")
                     :variant "outlined"
                     :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:surname]] (.. % -target -value)))}]]
   [mui/grid {:item true :xs 12 :sm 2}
    [mui/text-field {:value (or @(rf/subscribe [::sub/combinations-page :zodiac]) "")
                     :label "生肖"
                     :select true
                     :full-width true
                     :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:zodiac]] (.. % -target -value)))}
     (doall
       (for [[option-idx [value label]] (map-indexed vector @(rf/subscribe [::sub/zodiac :select-options]))]
         [mui/menu-item {:key option-idx :value value} label]))]]

   [mui/grid {:item true :xs 12 :sm 3}
    [mui/text-field {:value (or @(rf/subscribe [::sub/combinations-page :combination-idx]) "")
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
     [mui/text-field {:value (or @(rf/subscribe [::sub/advanced-option :min-wuger-pts]) 0)
                      :label "五格分數低標"
                      :full-width true
                      :variant "outlined"
                      :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:advanced-option :min-wuger-pts]] (.. % -target -value)))}]]
    [mui/grid {:item true :xs 3}
     [mui/text-field {:value @(rf/subscribe [::sub/advanced-option :min-sancai-pts])
                      :label "三才分數低標"
                      :select true
                      :full-width true
                      :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:advanced-option :min-sancai-pts]] (.. % -target -value)))}
      (doall
        (for [[option-idx [value label]] (map-indexed vector @(rf/subscribe [::sub/sancai-luck-options]))]
          [mui/menu-item {:key option-idx :value value} label]))]]]])

(defn given-name-tab
  [{:keys [single-given-name]}]
  [mui/grid {:container true :spacing 2}
   [mui/grid {:item true :xs 12}
    [mui/form-control-label
     {:label "使用單名"
      :control (r/as-element
                 [mui/switch {:checked single-given-name
                              :on-change #(rf/dispatch-sync (conj [::evt/set-form-field [:advanced-option :single-given-name]] (.. % -target -checked)))}])}]]])

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

(defn import-export-tab
  []
  [mui/grid {:container true :spacing 2}
   (when-let [err-msg @(rf/subscribe [::sub/error :import])]
     [mui/grid {:item true :xs 12}
      [mui/form-helper-text {:error true :sx {:font-size "2rem"}} err-msg]])
   [mui/grid {:item true}
    [mui/button {:variant "outlined" :on-click #(rf/dispatch-sync [::evt/export])}
     [icon-download/download]
     "匯出"]]
   [mui/grid {:item true}
    [mui/button {:component "label"
                 :variant "outlined"
                 :start-icon (r/as-element [icon-upload/upload])}
     "匯入"
     [mui/input {:on-change #(evt/import-setting (first (.. % -target -files)))
                 :type  "file"
                 :style {:display "none"}}]]]])

(defn advanced-option
  []
  (let [advanced-option @(rf/subscribe [::sub/advanced-option])]
    [mui/accordion {:sx {:margin-top "10px"}}
     [mui/accordion-summary {:expand-icon (r/as-element [icon-expand-more/expand-more])
                             :aria-controls :adv-opt-content
                             :id :adv-opt-header
                             :sx {:background-color "darkblue"
                                  :color "white"}}
      [mui/typography "進階選項"]]
     [mui/accordion-details
      [cpt/tab-context {:value (:tab advanced-option)}
       [cpt/tab-list {:on-change #(rf/dispatch-sync [::evt/set-form-field [:advanced-option :tab] %2])}
        [mui/tab {:label "設定分數" :value "points"}]
        [mui/tab {:label "單名" :value "given-name"}]
        [mui/tab {:label "設定筆劃" :value "strokes"}]
        [mui/tab {:label "設定禁字" :value "chars"}]
        [mui/tab {:label "匯出/匯入設定" :value "import-export"}]]
       [cpt/tab-panel {:value "points"}
        [points-tab]]
       [cpt/tab-panel {:value "given-name"}
        [given-name-tab advanced-option]]
       [cpt/tab-panel {:value "strokes"}
        [strokes-tab advanced-option]]
       [cpt/tab-panel {:value "chars"}
        [chars-tab advanced-option]]
       [cpt/tab-panel {:value "import-export"}
        [import-export-tab]]]]]))

(defn zodiac-table
  [{:keys [strokes]}]
  (let [surname @(rf/subscribe [::sub/combinations-page :surname])
        hide-zodiac-chars @(rf/subscribe [::sub/combinations-page :hide-zodiac-chars])
        given-name-chars-count (if (:single-given-name @(rf/subscribe [::sub/advanced-option])) 1 2)]
    [mui/grid {:item true :xs 11}
     [:table {:width "100%" :style {:border-collapse "collapse"}}
      [:tbody
       [:tr
        [:th {:width "15%" :style {:border-style "solid" :border-width "1px"}} "欄位"]
        [:th {:width "70%" :style {:border-style "solid" :border-width "1px"} :col-span 2} "選字"]]
       [:tr
        [:td {:style {:border-style "solid" :border-width "1px"}}
         "姓" [:br]
         (str "筆劃:" (cs/join ", " (:surname strokes)))]
        [:td {:col-span 2 :style {:border-style "solid" :border-width "1px"}}
         surname]]
       (doall
         (for [idx (range given-name-chars-count)]
           (let [{:keys [better normal worse]} @(rf/subscribe [::sub/preferred-characters idx])
                 hide-normal-chars (get-in hide-zodiac-chars [:normal idx])
                 hide-worse-chars (get-in hide-zodiac-chars [:worse idx])]
             [:<> {:key idx}
              [:tr
               [:td {:row-span 3 :style {:border-style "solid" :border-width "1px"}}
                (str "名(第" (inc idx) "字)") [:br]
                (str "筆劃:" (get-in strokes [:given-name idx]))]
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

(defn main
  []
  [:<>
   [form]
   (when-let [selected-combination @(rf/subscribe [::sub/selected-combination])]
     (let [surname @(rf/subscribe [::sub/combinations-page :surname])
           sancai-attrs-of-selected-combination @(rf/subscribe [::sub/sancai-attrs-of-selected-combination selected-combination])
           eighty-one @(rf/subscribe [::sub/eighty-one])]
       [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
        [shared/sancai-calc selected-combination surname]
        [zodiac-table selected-combination]
        [shared/sancai-table selected-combination sancai-attrs-of-selected-combination]
        [shared/wuger-table selected-combination eighty-one]]))
   [advanced-option]])
