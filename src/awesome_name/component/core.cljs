(ns awesome-name.component.core
  (:require
    ["@mui/lab/TabContext" :as MuiTabContext]
    ["@mui/lab/TabList"    :as MuiTabList]
    ["@mui/lab/TabPanel"   :as MuiTabPanel]
    [awesome-name.component.date-picker-field]
    [reagent-mui.util :refer [adapt-react-class]]))

;; === Manual adapt-react-class ===
;; because arttuka/reagent-material-ui doesn't include below components so adapt react class by ourselves.
;; Example https://github.com/arttuka/reagent-material-ui/blob/master/src/core/reagent_material_ui/lab/alert.cljs
;; tips:
;;   (.-default   _xx) => same as export default ClassName in JS
;;   (.-ClassName _xx) => same as export {ClassName}       in JS
;;   last argument for debug used.
(def tab-context (adapt-react-class (or (.-default MuiTabContext) (.-TabContext MuiTabContext)) "mui-tab-context"))
(def tab-list    (adapt-react-class (or (.-default MuiTabList)    (.-TabList MuiTabList))       "mui-tab-list"))
(def tab-panel   (adapt-react-class (or (.-default MuiTabPanel)   (.-TabPanel MuiTabPanel))     "mui-tab-panel"))

(def date-picker-field #'awesome-name.component.date-picker-field/date-picker-field)
