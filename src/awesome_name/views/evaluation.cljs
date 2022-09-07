(ns awesome-name.views.evaluation
  (:require
    [awesome-name.views.shared :as shared]
    [re-frame.core :as rf]
    [awesome-name.subs :as sub]
    [awesome-name.events :as evt]
    [reagent-mui.components :as mui]))

(defn form
  []
  [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
   [mui/grid {:item true}
    [mui/text-field {:label "姓"
                     :value (or @(rf/subscribe [::sub/evaluation-page :surname]) "")
                     :variant "outlined"
                     :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:surname]] (.. % -target -value)))}]]
   [mui/grid {:item true}
    [mui/text-field {:label "名"
                     :value (or @(rf/subscribe [::sub/evaluation-page :given-name]) "")
                     :variant "outlined"
                     :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:given-name]] (.. % -target -value)))}]]])

(defn evaluation-result
  [result]
  (let [surname @(rf/subscribe [::sub/evaluation-page :surname])
        eighty-one @(rf/subscribe [::sub/eighty-one])
        sancai-attrs-of-selected-combination @(rf/subscribe [::sub/sancai-attrs-of-selected-combination result])]
    [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
     [shared/sancai-calc result surname]
     [shared/sancai-table result sancai-attrs-of-selected-combination]
     [shared/wuger-table result eighty-one]]))

(defn main
  []
  [:<>
   [form]
   (let [{:keys [valid?] :as result} @(rf/subscribe [::sub/evaluation-result])]
     (when valid?
       [evaluation-result result]))])
