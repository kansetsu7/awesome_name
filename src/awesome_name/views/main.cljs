(ns awesome-name.views.main
  (:require
    [awesome-name.component.core :as cpt]
    [awesome-name.subs :as sub]
    [awesome-name.events :as evt]
    [awesome-name.views.combinations :as combinations]
    [awesome-name.views.evaluation :as evaluation]
    [reagent-mui.components :as mui]
    [re-frame.core :as rf]))

(defn main
  []
  (let [current-page @(rf/subscribe [::sub/current-page])]
   [mui/grid {:container true :spacing 2}
    [mui/grid {:item true :xs 12}
     [cpt/tab-context {:value current-page}
      [cpt/tab-list {:on-change #(rf/dispatch-sync [::evt/set-page %2])}
       [mui/tab {:label "姓名組合" :value "combinations"}]
       [mui/tab {:label "姓名評分" :value "evaluation"}]]
      [cpt/tab-panel {:value "combinations"}
       [combinations/main]]
      [cpt/tab-panel {:value "evaluation"}
       [evaluation/main]]]]]))
