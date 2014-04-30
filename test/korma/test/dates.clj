(ns korma.test.dates
  (:require [clojure.test :refer [deftest is]]
            [korma.core :refer :all]
            [korma.db :refer :all])
  (:import [java.util Calendar GregorianCalendar TimeZone Date]))

(defdb dates (mysql
               {:db "dates"
                :user "root"
                :make-pool? true
                :useLegacyDateTimeCode false
                :serverTimeZone "UTC"}))

(defentity my-dates (database dates) (table "dates"))

(def utc-tz (TimeZone/getTimeZone "UTC"))

(defn at-midnight
  "Sets the time portion of the Date object to 0."
  [date]
  (let [cal (doto (GregorianCalendar.)
              (.setTimeZone utc-tz)
              (.setTime date))
        new-cal (doto (GregorianCalendar.)
                  (.setTimeZone utc-tz))]
    (doto new-cal
      (.set Calendar/YEAR (.get cal Calendar/YEAR))
      (.set Calendar/MONTH (.get cal Calendar/MONTH))
      (.set Calendar/DAY_OF_MONTH (.get cal Calendar/DAY_OF_MONTH))
      (.set Calendar/HOUR_OF_DAY 0)
      (.set Calendar/MINUTE 0)
      (.set Calendar/SECOND 0)
      (.set Calendar/MILLISECOND 0))
    (.getTime new-cal)))

(deftest insert-mysql
  (let [midnight (at-midnight (Date. 2014 01 01))]
    (delete my-dates)
    (insert my-dates (values {:date midnight}))
    (let [date (:date (first (select my-dates)))]
      (is (= midnight date)))))
