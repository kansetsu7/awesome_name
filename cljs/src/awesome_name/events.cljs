(ns awesome-name.events
  (:require
    [clojure.set :as cset]
    [awesome-name.db :refer [default-db]]
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
