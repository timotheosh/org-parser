(ns org-parser.transform-test
  (:require [org-parser.transform :as sut]
            [org-parser.parser :as parser]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing]])))


(def props
  [[:stars "*"] [:title "hello" "world"]])


(deftest property
  (testing "helper fn"
    (is (= ["hello" "world"]
           (#'sut/property :title props)))))


(deftest append
  (testing "already in content block"
    (is (= [[:content "hello\nworld\n"]]
           (#'sut/append [[:content "hello\n"]] "world")))))


(deftest reducer
  (testing "content-line begins block"
    (is (= [[:headline] [:content "hello world\n"]]
           (#'sut/reducer [[:headline]] [:content-line "hello world"])))))


(def parse-tree
  [:S
   [:headline [:stars "*"] [:title "hello" "world"]]
   [:content-line [:text [:text-normal "this is the first section"]]]
   [:empty-line]
   [:headline [:stars "**"] [:title "and" "this"]]
   [:empty-line]
   [:content-line [:text [:text-normal "is another section"]]]])


(def transformed
  [[:headline {:level 1, :title "hello world"}]
   [:content
    {:raw "this is the first section\n\n",
     :parsed [[:content-line [:text [:text-normal "this is the first section"]]]
              [:empty-line]]}]
   [:headline {:level 2, :title "and this"}]
   [:content
    {:raw "\nis another section\n",
     :parsed [[:empty-line]
              [:content-line [:text [:text-normal "is another section"]]]]}
    ]])


(deftest regression
  (testing "`transform` works on structures provided by parser"
    (let [parse parser/org]
      (is (= parse-tree
             (parse "* hello world
this is the first section

** and this

is another section"))))))


(deftest transform
  (testing "a full parsetree"
    (is (= transformed
           (sut/transform parse-tree)))))
