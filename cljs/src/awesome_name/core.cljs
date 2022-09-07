(ns awesome-name.core
  (:require
    [awesome-name.index :as index]
    [re-frame.core :as rf]
    [reagent.dom :as rd]))

(enable-console-print!)

(defn init []
  (js/console.log "Hello World"))

(defn ^:dev/after-load start []
  (rf/clear-subscription-cache!)
  (rd/render [index/index] (js/document.getElementById "cljs")))

(defn ^:dev/before-load stop [])
