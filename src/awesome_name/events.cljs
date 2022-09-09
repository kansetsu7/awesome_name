(ns awesome-name.events
  (:require
    [awesome-name.db :refer [default-db]]
    [awesome-name.util :as util]
    [clojure.set :as cset]
    [clojure.string :as cs]
    [clojure.walk :as walk]
    [file-saver :as fs]
    [re-frame.core :as rf]))

(rf/reg-event-db :app/init-db
                 (fn [_ _]
                   default-db))

(defn trim-if-string
  [value]
  (cond-> value
    (string? value) cs/trim))

(rf/reg-event-db ::set-form-field
                 (fn [db [_ field value]]
                   (let [page (-> db
                                  (get-in [:app :current-page])
                                  keyword)]
                     (-> db
                         (assoc-in (into [:form page] field) (trim-if-string value))))))

(defn may-reset-combination-idx
  [db field]
  (let [reset? (#{:min-81-pts :min-sancai-pts :single-given-name :strokes-to-remove} field)]
    (cond-> db
      reset? (assoc-in [:form :combinations :combination-idx] ""))))

(rf/reg-event-db ::set-advanced-option
                 (fn [db [_ field value]]
                   (-> db
                     (assoc-in [:form :combinations :advanced-option field] (trim-if-string value))
                     (may-reset-combination-idx field))))

(rf/reg-event-db ::set-error-field
                 (fn [db [_ field value]]
                   (-> db
                       (assoc-in (into [:field-error-message] field) value))))

(rf/reg-event-db ::clear-error-field
                 (fn [db [_ field]]
                   (-> db
                       (util/dissoc-in (into [:field-error-message] field)))))

(rf/reg-event-db ::set-page
                 (fn [db [_ page]]
                   (-> db
                       (assoc-in [:app :current-page] page))))

(defn update-chars-to-remove
  [db]
  (let [default-taboo-characters (into #{} (get-in db [:app :default-taboo-characters]))
        chars-to-remove          (into #{} (get-in db [:form :combinations :advanced-option :chars-to-remove]))
        result                   (if (get-in db [:form :combinations :advanced-option :use-default-taboo-characters])
                                   (cset/union chars-to-remove default-taboo-characters)
                                   (cset/difference chars-to-remove default-taboo-characters))]
    (assoc-in db [:form :combinations :advanced-option :chars-to-remove] (apply str result))))

(rf/reg-event-db ::set-use-default-taboo-characters
                 (fn [db [_ value]]
                   (-> db
                       (assoc-in [:form :combinations :advanced-option :use-default-taboo-characters] value)
                       update-chars-to-remove)))

(rf/reg-event-db ::update-strokes-to-remove
                 (fn [db [_ strokes checked]]
                   (let [action (if checked conj disj)]
                     (-> db
                         (update-in [:form :combinations :advanced-option :strokes-to-remove] #(action % strokes))
                         (may-reset-combination-idx :strokes-to-remove)))))

(rf/reg-event-db ::add-chars-to-remove
                 (fn [db [_ value]]
                   (let [click-to-remove (get-in db [:form :combinations :advanced-option :click-to-remove])]
                     (cond-> db
                       click-to-remove (update-in [:form :combinations :advanced-option :chars-to-remove] #(str % value))))))

(defn str->keywordize-map
  [string]
  (-> (.parse js/JSON string)
      js->clj
      walk/keywordize-keys))

(defn import-setting
  [value]
  (let [^js/File file value
        file-type (.-type file)]
    (if (= "text/plain" file-type)
      (-> (.text file)
          (.then #(rf/dispatch [::bulk-update-combinations-page-form (str->keywordize-map %)])))
      (rf/dispatch [::set-error-field [:import] "檔案格式錯誤，請上傳 .txt 檔"]))))

(rf/reg-event-fx ::bulk-update-combinations-page-form
                 (fn [{:keys [db]} [_ form-data]]
                   {:db (-> db
                            (assoc-in [:form :combinations] form-data))
                    :dispatch [::clear-error-field [:import]]}))

(rf/reg-event-db ::export
                 (fn [db _]
                   (let [content (.stringify js/JSON (clj->js (get-in db [:form :combinations])))
                         blob (new js/Blob [content] {:type "text/plain;charset=utf-8"})]
                     (fs/saveAs blob "命名設定檔.txt"))))
