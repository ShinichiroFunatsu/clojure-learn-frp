(ns stock-market-monitor.core
  (:require [rx.lang.clojure.core :as rx]
            [seesaw.core :refer :all])
  (:import (java.util.concurrent TimeUnit)
           (rx Observable)))

(native!)

;;
;; GUI
;;
(def main-frame (frame :title "Stock price monitor",
                       :width 200, :height 100,
                       :on-close :exit))
(def price-label       (label "Price: -"))
(def running-avg-label (label "Running agerage: -"))

(config! main-frame :content 
        (border-panel
         :north price-label
         :center running-avg-label
         :border 5))

;;
;; Logics
;;

;;prices
(defn make-price-obs [company-code]
  (rx/return (share-price company-code)))

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))

;;rolling(moiving) average
(defn roll-buffer [buffer num buffer-size]
  (let [buffer (conj buffer num)]
    (if (> (count buffer) buffer-size)
    (pop buffer)
    buffer)))

(defn avg [numbers]
  (float (/ (reduce + numbers)
            (count numbers))))

(defn make-running-avg [buffer-size]
  (let [buffer (atom clojure.lang.PersistentQueue/EMPTY)]
    (fn [n]
      (swap! buffer roll-buffer n buffer-size)
      (avg @buffer))))

(def running-avg (make-running-avg 5))

;;
;; main
;;
(defn -main [& args]
  (show! main-frame)
  (let [price-obs (rx/flatmap (fn [_] (make-price-obs "XYZ"))
                             (Observable/interval 500 TimeUnit/MILLISECONDS))]
    (rx/subscribe price-obs
                  (fn [price]
                    (text! price-label (str "Price: " price))))))