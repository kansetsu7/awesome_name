(ns awesome-name.core
  (:require
    [awesome-name.views.main :as main]
    [devtools.core :as devtools]
    [re-frame.core :as rf]
    [reagent.dom :as rd]))

(enable-console-print!)
(goog-define DEV_DARK_MODE true)

;; Setup custom formatter color for Chrome in dark mode
;; Ref: https://github.com/binaryage/cljs-devtools/issues/30#issuecomment-671504127
(when DEV_DARK_MODE
  (let [{:keys [cljs-land-style]} (devtools/get-prefs)]
    (devtools/set-pref! :cljs-land-style (str "filter:invert(1);" cljs-land-style))))

(defn ^:dev/after-load start []
  (rf/clear-subscription-cache!)
  (rd/render [main/main] (js/document.getElementById "cljs")))

(defn ^:dev/before-load stop [])

(rf/dispatch-sync [:app/init-db])
