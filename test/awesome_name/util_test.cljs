(ns awesome-name.util-test
  (:require
    [awesome-name.util :as sut]
    [awesome-name.db :refer [default-db]]
    [clojure.test :refer [deftest testing is are]]))

(def chen "陳")

(def combinations
  [{:expected-idx 4 :wuger-pts 85  :top 1 :middle 2 :bottom 3}
   {:expected-idx 0 :wuger-pts 100 :top 1 :middle 2 :bottom 3}
   {:expected-idx 2 :wuger-pts 92  :top 1 :middle 1 :bottom 3}
   {:expected-idx 1 :wuger-pts 100 :top 2 :middle 2 :bottom 3}
   {:expected-idx 5 :wuger-pts 85  :top 1 :middle 2 :bottom 4}
   {:expected-idx 3 :wuger-pts 92  :top 1 :middle 2 :bottom 3}])

(defn get-in-db
  [args]
  (get-in default-db (into [:app] args)))

(deftest strokes-of
  (testing "strokes-of"
    (is (= 16 (sut/strokes-of (get-in-db [:chinese-characters]) chen)))))

(deftest string->strokes
  (testing "return list of character strokes"
    (is (= [5 10 22] (sut/string->strokes "司馬懿" (get-in-db [:chinese-characters])))))
  (testing "got nil if not found in dictionary"
    (is (= [nil nil nil] (sut/string->strokes "9讚A" (get-in-db [:chinese-characters]))))))

(deftest all-strokes-combinations
  (let [surname-strokes [10 5]]
    (testing "NOT single-given-name"
      (is (= [[surname-strokes [1 1]]
              [surname-strokes [1 2]]
              [surname-strokes [1 3]]
              [surname-strokes [2 1]]
              [surname-strokes [2 2]]
              [surname-strokes [2 3]]
              [surname-strokes [3 1]]
              [surname-strokes [3 2]]
              [surname-strokes [3 3]]]
             (sut/all-strokes-combinations surname-strokes (range 1 4) false))))
    (testing "IS single-given-name"
      (is (= [[surname-strokes [1]]
              [surname-strokes [2]]
              [surname-strokes [3]]]
             (sut/all-strokes-combinations surname-strokes (range 1 4) true))))))

(deftest name-strokes->gers
  (testing "name-strokes->gers"
    (are [exp-res surname-strokes given-name-strokes]
         (= exp-res (sut/name-strokes->gers surname-strokes given-name-strokes))
      [16 16 3 3 18]  [15]   [1 2]
      [15 26 13 2 26] [14]   [12]
      [15 32 23 6 37] [5 10] [22])))

(deftest gers->81pts
  (testing "gers->81pts"
    (doseq [[wuger-pts gers] [[100 [16 16 3 3 18]]
                              [92  [16 16 15 15 30]]
                              [86  [16 37 26 5 41]]]]
      (let [res (sut/gers->81pts (get-in-db [:eighty-one]) gers)]
        (is (= wuger-pts res) (str "Expect total potins = " wuger-pts " but get " res))))))

(deftest name-strokes-evaluation
  (testing "name-strokes-evaluation"
    (let [eighty-one (get-in-db [:eighty-one])
          sancai-combinations (get-in-db [:sancai :combinations])]
      (= {:elements ["土" "木" "火" "土" "金"]
           :strokes {:surname [5 10]
                     :given-name [22]}
           :gers [15 32 23 6 37]
           :points {:wuger 100
                    :sancai 80
                    :average 90}
           :sancai-elements "土木火"}
         (sut/name-strokes-evaluation [5 10] [22] eighty-one sancai-combinations)))))

(deftest sort-by-points-and-strokes
  (testing "sort-by-points-and-strokes"
    (let [combinations [{:expected-idx 4 :points {:average 85}  :strokes {:surname [1] :given-name [2 3]}}
                        {:expected-idx 0 :points {:average 100} :strokes {:surname [1] :given-name [2 3]}}
                        {:expected-idx 2 :points {:average 92}  :strokes {:surname [1] :given-name [2 3]}}
                        {:expected-idx 1 :points {:average 100} :strokes {:surname [1] :given-name [2 3]}}
                        {:expected-idx 5 :points {:average 85}  :strokes {:surname [1] :given-name [2 4]}}
                        {:expected-idx 3 :points {:average 92}  :strokes {:surname [1] :given-name [2 3]}}]]
      (is (= [0 1 2 3 4 5]
             (map :expected-idx (sut/sort-by-points-and-strokes combinations)))))))

(deftest add-combination-label
  (testing "add-combination-label"
    (are [input label] (let [exp-res (assoc input :label label)]
                         (= exp-res (sut/add-combination-label input)))
      {:points {:average 95}   :strokes {:surname [1]   :given-name [2 3]}} "適合筆畫：1, 2, 3 (綜合分數：95）"
      {:points {:average 88.5} :strokes {:surname [1 2] :given-name [3 4]}} "適合筆畫：1, 2, 3, 4 (綜合分數：88.5）"
      {:points {:average 40}   :strokes {:surname [10]  :given-name [20]}}  "適合筆畫：10, 20 (綜合分數：40）")))

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

(deftest dissoc-in
  (testing "dissoc-in"
    (let [base-map {:a {:b {:c {:d 1
                                :e 2}
                            :f 3}
                        :g 4}
                    :h 5}]
      (are [exp-res ks] (= exp-res (sut/dissoc-in base-map ks))
           base-map [:a :b :c :d :e]
           base-map [:x]
           {:h 5} [:a]
           {:a {:g 4} :h 5} [:a :b]
           {:a {:b {:f 3} :g 4} :h 5} [:a :b :c]
           {:a {:b {:c {:e 2} :f 3} :g 4} :h 5} [:a :b :c :d]))))

(deftest valid-strokes?
  (testing "valid-strokes?"
    (are [exp-res ss gs] (= exp-res (sut/valid-strokes? ss gs))
      true  [1]     [2]
      true  [1 1]   [2]
      true  [1 1]   [2 3]
      true  [1]     [2 3]
      false [1]     []
      false []      [1]
      false [nil]   []
      false []      [nil]
      false [1]     [nil]
      false [1]     [2 nil]
      false [nil]   [1]
      false [1 nil] [2]
      false [1 2 3] [2]
      false [1 2]   [3 4 5]
      false [1 2 3] [2])))
