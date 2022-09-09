(ns awesome-name.views.shared
  (:require
    [reagent-mui.components :as mui]
    [goog.string :as gstring]))

(def padding-str (gstring/unescapeEntities "&nbsp;&nbsp;&nbsp;&nbsp;"))

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

(defn sancai-calc-surname
  [{:keys [strokes]} surname]
  (let [single-surname? (-> surname seq count (= 1))
        surname-strokes (cond->> (:surname strokes)
                          single-surname? (into [1]))
        surname-chars (cond-> (seq surname)
                        single-surname? (conj padding-str))]
    [:<>
     [:span (first surname-chars)]
     [:b (str " (" (first surname-strokes) " 劃)")]
     [:br]
     [:br]
     [:span (last surname-chars)]
     [:b (str " (" (last surname-strokes) " 劃)")]
     [:br]]))

(defn sancai-calc-given-name
  [{:keys [strokes]} given-name]
  (let [single-given-name? (= 1 (-> strokes :given-name count))
        blank-given-name? (= "" given-name)
        given-name-strokes (cond-> (:given-name strokes)
                             single-given-name? (conj 1))
        given-name-chars (cond
                           blank-given-name? [padding-str padding-str]
                           single-given-name?  (-> (seq given-name) vec (conj padding-str))
                           :else (-> (seq given-name) vec))]
    [:<>
     [:br]
     [:span (first given-name-chars)]
     [:b (str " (" (first given-name-strokes) " 劃)")]
     [:br]
     [:br]
     [:span (last given-name-chars)]
     [:b (str " (" (last given-name-strokes) " 劃)")]]))

(defn sancai-calc
  ([evaluation surname] (sancai-calc evaluation surname ""))
  ([{:keys [gers elements] :as evaluation} surname given-name]
   [mui/grid {:item true :xs 12}
    [:table {:style {:max-width "300px"}}
     [:tbody
      [:tr
       [:td {:valign "middle" :align "center" :width 80}
        (str "外格:" (get gers 3))
        (render-element (get elements 3))]
       [:td {:valign "top" :align "left" :width 20}
        "┌" [:br]
        "│" [:br]
        "│" [:br]
        "┤" [:br]
        "│" [:br]
        "│" [:br]
        "└" [:br]]
       [:td {:valign "top" :align "left"}
        [sancai-calc-surname evaluation surname]
        [sancai-calc-given-name evaluation given-name]]
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

(defn eighty-one-table
  [{:keys [gers points]} eighty-one]
  [mui/grid {:item true :xs 11}
   [:table {:width "100%" :style {:border-collapse "collapse"}}
    [:tbody
     [:tr
      [:th {:col-span 3 :style {:border-style "solid" :border-width "1px"}}
       (str "81數理 (" (:eighty-one points) "分)")]]
     (doall
       (for [[idx ger] (map-indexed vector gers)]
         (let [{:keys [description luck]} (get eighty-one (dec ger))
               ger-zh (-> (get ["天格" "人格" "地格" "外格" "總格"] idx)
                          (str "(" ger ")劃"))]
           [:tr {:key idx}
            [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
             ger-zh]
            [:td {:width "15%" :style {:border-style "solid" :border-width "1px"}}
             luck]
            [:td {:width "70%" :style {:border-style "solid" :border-width "1px" :padding-top "15px" :padding-bottom "15px"}}
             [mui/typography {:font-size "1.2rem"}
              description]]])))]]])
