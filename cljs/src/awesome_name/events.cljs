(ns awesome-name.events
  (:require
    [awesome-name.db :refer [default-db]]
    [clojure.set :as cset]
    [clojure.walk :as walk]
    [file-saver :as fs]
    [re-frame.core :as rf]))

(rf/reg-event-db :app/init-db
                 (fn [_ _]
                   default-db))

(rf/reg-event-db ::set-form-field
                 (fn [db [_ field value]]
                   (-> db
                       (assoc-in (into [:form] field) value))))

(defn update-chars-to-remove
  [db]
  (let [default-taboo-characters (into #{} (get-in db [:app :default-taboo-characters]))
        chars-to-remove          (into #{} (get-in db [:form :advanced-option :chars-to-remove]))
        result                   (if (get-in db [:form :advanced-option :use-default-taboo-characters])
                                   (cset/union chars-to-remove default-taboo-characters)
                                   (cset/difference chars-to-remove default-taboo-characters))]
    (assoc-in db [:form :advanced-option :chars-to-remove] (apply str result))))

(rf/reg-event-db ::set-use-default-taboo-characters
                 (fn [db [_ value]]
                   (-> db
                       (assoc-in [:form :advanced-option :use-default-taboo-characters] value)
                       update-chars-to-remove)))

(rf/reg-event-db ::update-strokes-to-remove
                 (fn [db [_ strokes checked]]
                   (let [action (if checked conj disj)]
                     (-> db
                         (update-in [:form :advanced-option :strokes-to-remove] #(action % strokes))))))

(rf/reg-event-db ::add-chars-to-remove
                 (fn [db [_ value]]
                   (let [click-to-remove (get-in db [:form :advanced-option :click-to-remove])]
                     (cond-> db
                       click-to-remove (update-in [:form :advanced-option :chars-to-remove] #(str % value))))))

(defn str->keywordize-map
  [string]
  (-> (.parse js/JSON string)
      js->clj
      walk/keywordize-keys))

(defn import-setting
  [value]
  (let [^js/File file value]
    (-> (.text file)
        (.then #(rf/dispatch [::bulk-update-form (str->keywordize-map %)])))))

(rf/reg-event-db ::bulk-update-form
                 (fn [db [_ form-data]]
                   (-> db
                       (assoc :form form-data))))

(rf/reg-event-db ::export
                 (fn [db _]
                   (let [content (.stringify js/JSON (clj->js (:form db)))
                         blob (new js/Blob [content] {:type "text/plain;charset=utf-8"})]
                     (fs/saveAs blob "命名設定檔.txt"))))
