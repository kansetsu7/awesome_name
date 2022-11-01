(ns awesome-name.views.faq
  (:require
    [reagent-mui.components :as mui]
    [reagent.core :as r]
    [reagent-mui.icons.expand-more :as icon-expand-more]))

(defn main
  []
  [mui/grid {:container true :spacing 2 :sx {:margin-top "10px"}}
   [mui/grid {:item true :xs 12}
    [mui/accordion
     [mui/accordion-summary {:expand-icon (r/as-element [icon-expand-more/expand-more])}
      [mui/typography "評分的邏輯是什麼？"]]
     [mui/accordion-details
      [mui/typography "簡單來說一切的基礎都是根據筆劃。"]
      [mui/typography {:component :li}
       "五格：由每個字的筆畫計算出天格、人格、地格、總格、外格的數字。"]
      [mui/typography {:component :li}
       "81 數理：每個數字皆有吉凶評價，例如1為吉、4為凶等等。"]
      [mui/typography {:component :li}
       "五格 + 81 數理：將五格數字以81數理的吉凶進行評價，轉換為分數後加總，例如五個吉就是100分。"]
      [mui/typography {:component :li}
       "三才：由天格、人格、地格的數字轉換為五行（金木水火土）。再根據五行元素間相生相剋的屬性，得到對應的吉凶值。"]
      [:br]
      [mui/typography "（生肖對評分不產生影響，用途只有在姓名組合中把字分類為生肖喜/忌用/不喜不忌）"]
      [:br]
      [mui/typography "參考資料："]
      [mui/typography {:component :li}
       [mui/link {:href "https://www.163.com/dy/article/dqjq7pk60528etv2.html"}
        "三才五格計算方式"]]
      [mui/typography {:component :li}
       [mui/link {:href "http://211-75-223-181.hinet-ip.hinet.net/mobile/destiny/name/16.html"}
        "三才吉凶"]]
      [mui/typography {:component :li}
       [mui/link {:href "http://www.131.com.tw/word/b1_4.htm"}
        "十二生肖姓名學"]]
      [mui/typography {:component :li}
       [mui/link {:href "http://www.chaostec.com/destiny.htm"}
        "81數理姓名學"]]]]

    [mui/accordion
     [mui/accordion-summary {:expand-icon (r/as-element [icon-expand-more/expand-more])}
      [mui/typography "字的筆畫數不正確"]]
     [mui/accordion-details
      [mui/typography
       [:span "依照參考資料"]
       [mui/link {:href "https://www.163.com/dy/article/dqjq7pk60528etv2.html"}
        "三才五格計算方式"]
       [:span "，筆劃數是以"]
       [mui/link {:href "https://kangxizidian.com/"}
        "康熙字典網上版"]
       [:span "為準。"]]
      [mui/typography
       [:span "康熙字典的筆劃在某些字上會不太一樣。例如「陳」在一般字典為 11 劃，但在康熙字典中的「"]
       [mui/link {:href "https://kangxizidian.com/kxhans/%E9%99%B3"}
        "陳"]
       [:span "」拆為部首的「阜」8劃 +「東」8劃 = 16 劃"]]]]

    [mui/accordion
     [mui/accordion-summary {:expand-icon (r/as-element [icon-expand-more/expand-more])}
      [mui/typography "字典內找不到我要的字"]]
     [mui/accordion-details
      [mui/typography
       [:span "程式的字典是由"]
       [mui/link {:href "https://kangxizidian.com/"}
        "康熙字典網上版"]
       [:span
        "擷取下來的。如果找不到字，可已先到連結的康熙字典內找看看有沒有。"]]
      [mui/typography
       "如果有，代表本程式還沒有跟到他的更新；如果沒有，可能康熙字典網上版那邊還沒有處理到這個字，或是康熙字典內真的沒這個字。"]]]

    [mui/accordion
     [mui/accordion-summary {:expand-icon (r/as-element [icon-expand-more/expand-more])}
      [mui/typography "為什麼我都沒有滿分的選項"]]
     [mui/accordion-details
      [mui/typography "先說結論：姓氏造成的。"]
      [mui/typography "可以參考第一點「評分的邏輯是什麼？」"]
      [mui/typography "由於評分是根據筆劃在經過一系列邏輯算出分數，若您在姓名組合中沒有滿分的名字選項，代表您的姓氏計算出來的天格數字在 81 數理內不是「吉」，就拿不到滿分。"]]]]])
