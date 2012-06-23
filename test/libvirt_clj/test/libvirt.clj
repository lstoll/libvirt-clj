(ns libvirt-clj.test.libvirt
  (:use [libvirt])
  (:use [clojure.test]))

(def turi "test:///default")

(deftest conn-returns-conn
  (is org.libvirt.Connect (type (conn turi))))

(deftest domain-list
  (is 1 (count (list-domains (conn turi)))))