(ns awesome-name.views.main
  (:require
    [awesome-name.subs :as sub]
    [awesome-name.events :as evt]
    [awesome-name.views.combinations :as combinations]
    [awesome-name.views.evaluation :as evaluation]
    [reagent-mui.components :as mui]
    [re-frame.core :as rf]))

(defn main
  []
  (let [current-page @(rf/subscribe [::sub/current-page])]
    [:<>
     [mui/button {:variant "outlined" :on-click #(rf/dispatch-sync [::evt/set-page :evaluation])}
      "evaluation"]
     [mui/button {:variant "outlined" :on-click #(rf/dispatch-sync [::evt/set-page :combinations])}
      "combinations"]
     (case current-page
       :combinations [combinations/main]
       :evaluation   [evaluation/main])]))
