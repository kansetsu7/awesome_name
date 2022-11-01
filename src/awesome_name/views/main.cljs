(ns awesome-name.views.main
  (:require
    [awesome-name.component.core :as cpt]
    [awesome-name.events :as evt]
    [awesome-name.subs :as sub]
    [awesome-name.views.combinations :as combinations]
    [awesome-name.views.evaluation :as evaluation]
    [awesome-name.views.faq :as faq]
    [re-frame.core :as rf]
    [reagent-mui.components :as mui]
    [reagent-mui.icons.git-hub :as icon-github]))

(defn main
  []
  (let [current-page @(rf/subscribe [::sub/current-page])]
    [mui/grid {:container true :spacing 2}
     [mui/grid {:item true :xs 12}
      [cpt/tab-context {:value current-page}
       [cpt/tab-list {:on-change #(rf/dispatch-sync [::evt/set-page %2])}
        [mui/tab {:label "姓名組合" :value "combinations"}]
        [mui/tab {:label "姓名評分" :value "evaluation"}]
        [mui/tab {:label "常見問題" :value "faq"}]]
       [cpt/tab-panel {:value "combinations"}
        [combinations/main]]
       [cpt/tab-panel {:value "evaluation"}
        [evaluation/main]]
       [cpt/tab-panel {:value "faq"}
        [faq/main]]]
      [mui/box {:sx {:position :fixed :right 5 :top 5}}
       [:a {:href "https://github.com/kansetsu7/awesome_name"}
        [icon-github/git-hub]]]]]))
