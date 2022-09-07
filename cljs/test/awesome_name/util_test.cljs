(ns awesome-name.util-test
  (:require
    [awesome-name.util :as sut]
    [awesome-name.db :refer [default-db]]
    [clojure.test :refer [deftest testing is]]))

(def chen "陳")

(def combinations
  [{:expected-idx 4 :pts 85  :top 1 :middle 2 :bottom 3}
   {:expected-idx 0 :pts 100 :top 1 :middle 2 :bottom 3}
   {:expected-idx 2 :pts 92  :top 1 :middle 1 :bottom 3}
   {:expected-idx 1 :pts 100 :top 2 :middle 2 :bottom 3}
   {:expected-idx 5 :pts 85  :top 1 :middle 2 :bottom 4}
   {:expected-idx 3 :pts 92  :top 1 :middle 2 :bottom 3}])

(defn get-in-db
  [args]
  (get-in default-db (into [:app] args)))

(deftest strokes-of
  (testing "strokes-of"
    (is (= 16 (sut/strokes-of (get-in-db [:chinese-characters]) chen)))))

(deftest all-strokes-combinations
  (testing "all-strokes-combinations"
    (is (= [[10 1 1]
            [10 1 2]
            [10 1 3]
            [10 2 1]
            [10 2 2]
            [10 2 3]
            [10 3 1]
            [10 3 2]
            [10 3 3]]
           (sut/all-strokes-combinations 10 (range 1 4))))))

(deftest name-strokes->gers
  (testing "name-strokes->gers"
    (is (= [16 16 3 3 18] (sut/name-strokes->gers 15 1 2)))))

(deftest gers->81pts
  (testing "gers->81pts"
    (doseq [[pts gers] [[100 [16 16 3 3 18]]
                        [92  [16 16 15 15 30]]
                        [86  [16 37 26 5 41]]]]
      (let [res (sut/gers->81pts (get-in-db [:eighty-one]) gers)]
        (is (= pts res) (str "Expect total potins = " pts " but get " res))))))

(deftest sort-by-pts-and-strokes
  (testing "sort-by-pts-and-strokes"
    (let [combinations [{:expected-idx 4 :pts 85  :top 1 :middle 2 :bottom 3}
                        {:expected-idx 0 :pts 100 :top 1 :middle 2 :bottom 3}
                        {:expected-idx 2 :pts 92  :top 1 :middle 1 :bottom 3}
                        {:expected-idx 1 :pts 100 :top 2 :middle 2 :bottom 3}
                        {:expected-idx 5 :pts 85  :top 1 :middle 2 :bottom 4}
                        {:expected-idx 3 :pts 92  :top 1 :middle 2 :bottom 3}]]
      (is (= [0 1 2 3 4 5]
             (map :expected-idx (sut/sort-by-pts-and-strokes combinations)))))))

(deftest add-combination-label
  (testing "add-combination-label"
    (let [expected-res {:pts 100
                        :strokes {:top 1
                                  :middle 2
                                  :bottom 3}
                        :label "適合筆畫：1, 2, 3 (綜合分數：100）"}
          input (dissoc expected-res :lable)]
      (is (= expected-res (sut/add-combination-label input))))))

(deftest string->char-set
  (testing "string->char-set"
    (= [\好 \讚 \喔] (sut/string->char-set "好讚喔"))))

(deftest normal-characters
  (testing "normal-characters"
    (let [chinese-characters (get-in-db [:chinese-characters])
          better-chars "了勹"
          worse-chars "刀丁力"
          stroke 2]
      (= #{\匕 \人 \入 \厶 \卜 \乜 \刁 \二 \又}
         (sut/normal-characters chinese-characters better-chars worse-chars stroke)))))
