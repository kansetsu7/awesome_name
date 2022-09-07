(ns awesome-name.events
  (:require
    [awesome-name.db :refer [default-db]]
    [re-frame.core :as rf]))

(rf/reg-event-db :app/init-db
                 (fn [_ _]
                   default-db))

(rf/reg-event-db ::set-form-field
                 (fn [db [_ field value]]
                   (-> db
                       (assoc-in (into [:form] field) value))))
