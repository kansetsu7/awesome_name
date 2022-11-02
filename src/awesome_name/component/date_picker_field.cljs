(ns awesome-name.component.date-picker-field
  (:require-macros [reagent-mui.util :refer [react-component]])
  (:require
    [reagent-mui.cljs-time-adapter :refer [cljs-time-adapter]]
    [reagent-mui.components :as mui]
    [reagent-mui.x.date-picker :refer [date-picker]]
    [reagent-mui.x.localization-provider :refer [localization-provider]])
  (:import (goog.i18n DateTimeSymbols_en_US)))

(defn date-picker-field
  [{:keys [value-sub on-change-evt label input-format] :or {input-format "yyyy/MM/dd"}}]
  [localization-provider {:date-adapter   cljs-time-adapter
                          :adapter-locale DateTimeSymbols_en_US}
   [date-picker {:value        value-sub
                 :render-input (react-component [props] [mui/text-field props])
                 :on-change    on-change-evt
                 :input-format input-format
                 :label        label}]])
