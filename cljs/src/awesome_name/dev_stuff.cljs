(ns awesome-name.dev-stuff
  (:require
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.http-fx]
    [devtools.core :as devtools]))

(set! *warn-on-infer* true)
(goog-define DEV_DARK_MODE true)

(enable-console-print!)

;; Setup custom formatter color for Chrome in dark mode
;; Ref: https://github.com/binaryage/cljs-devtools/issues/30#issuecomment-671504127
(when DEV_DARK_MODE
  (let [{:keys [cljs-land-style]} (devtools/get-prefs)]
    (devtools/set-pref! :cljs-land-style (str "filter:invert(1);" cljs-land-style))))

