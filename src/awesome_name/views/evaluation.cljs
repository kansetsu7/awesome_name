(ns awesome-name.views.evaluation
  (:require
    [awesome-name.events :as evt]
    [awesome-name.subs :as sub]
    [awesome-name.views.shared :as shared]
    [re-frame.core :as rf]
    [reagent-mui.components :as mui]))

(defn form
  [surname-err-msg given-name-err-msg]
  [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
   [mui/grid {:item true}
    [mui/text-field {:label "姓"
                     :value (or @(rf/subscribe [::sub/evaluation-page :surname]) "")
                     :variant "outlined"
                     :error (boolean (seq surname-err-msg))
                     :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:surname]] (.. % -target -value)))
                     :helper-text surname-err-msg}]]
   [mui/grid {:item true}
    [mui/text-field {:label "名"
                     :value (or @(rf/subscribe [::sub/evaluation-page :given-name]) "")
                     :variant "outlined"
                     :error (boolean (seq given-name-err-msg))
                     :on-change  #(rf/dispatch-sync (conj [::evt/set-form-field [:given-name]] (.. % -target -value)))
                     :helper-text given-name-err-msg}]]])

(defn evaluation-result
  []
  (let [result @(rf/subscribe [::sub/evaluation-result])
        surname @(rf/subscribe [::sub/evaluation-page :surname])
        given-name @(rf/subscribe [::sub/evaluation-page :given-name])
        eighty-one @(rf/subscribe [::sub/eighty-one])
        sancai-attrs-of-selected-combination @(rf/subscribe [::sub/sancai-attrs-of-selected-combination result])]
    [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
     [shared/sancai-calc result surname given-name]
     [shared/sancai-table result sancai-attrs-of-selected-combination]
     [shared/eighty-one-table result eighty-one]]))

(defn main
  []
  (let [surname-err-msg @(rf/subscribe [::sub/name-errors :surname])
        given-name-err-msg @(rf/subscribe [::sub/name-errors :given-name])
        show-evaluation? @(rf/subscribe [::sub/show-evaluation?])]
    [:<>
     [form surname-err-msg given-name-err-msg]
     (when show-evaluation?
       [evaluation-result])]))
