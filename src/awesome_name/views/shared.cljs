(ns awesome-name.views.shared
  (:require
    [reagent-mui.components :as mui]))

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
  [{:keys [strokes gers elements]} surname]
  (let [single-surname? (-> surname seq count (= 1))
        single-given-name? (= 1 (-> strokes :given-name count))]
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
         (if single-surname?
          [:<>
           "(1 劃)" [:br]
           [:br]
           [:span surname]
           [:br]
           [:b (str (:top strokes)" 劃")]]
          [:<>
           [:span (-> surname seq first str)] [:br]
           [:b (str (-> strokes :surname first) " 劃")]
           [:br]
           [:span (-> surname seq last str)]
           [:br]
           [:b (str (-> strokes :surname last) " 劃")]])
         [:br]
         [:br]
         (str (-> strokes :given-name first) " 劃")
         [:br]
         [:br]
         (if single-given-name?
           "(1 劃)"
           (str (-> strokes :given-name last) " 劃"))]
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

(defn sancai-table
  [{:keys [sancai-elements points]} {:keys [description luck]}]
  [mui/grid {:item true :xs 11}
   [:table {:width "100%" :style {:border-collapse "collapse"}}
    [:tbody
     [:tr
      [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
       (str "三才姓名學 (" (:sancai points) "分)")]]
     [:tr
      [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
       sancai-elements]
      [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
       luck]
      [:td {:width "70%" :style {:border-style "solid" :border-width "1px" :padding-top "15px" :padding-bottom "15px"}}
       [mui/typography {:font-size "1.2rem"}
        description]]]]]])

(defn wuger-table
  [{:keys [gers elements points]} eighty-one]
  [mui/grid {:item true :xs 11}
   [:table {:width "100%" :style {:border-collapse "collapse"}}
    [:tbody
     [:tr
      [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
       (str "五格姓名學 (" (:wuger points) "分)")]]
     (doall
       (for [[idx ger] (map-indexed vector gers)]
         (let [{:keys [description luck]} (get eighty-one (dec ger))
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
