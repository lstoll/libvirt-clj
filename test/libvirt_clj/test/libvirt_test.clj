(ns libvirt-clj.test.libvirt-test
  (:use [libvirt])
  (:use [clojure.test]))

(def turi "test:///default")
(defn tc [] (conn turi))
(def new-domain-xml
"<domain type='test'>
  <name>testnew</name>
  <memory unit='KiB'>8388608</memory>
  <currentMemory unit='KiB'>2097152</currentMemory>
  <vcpu>2</vcpu>
  <os>
    <type arch='i686'>hvm</type>
    <boot dev='hd'/>
  </os>
  <clock offset='utc'/>
  <on_poweroff>destroy</on_poweroff>
  <on_reboot>restart</on_reboot>
  <on_crash>destroy</on_crash>
  <devices>
  </devices>
</domain>")

(deftest conn-returns-conn
  (is org.libvirt.Connect (type (conn turi))))

(deftest bad-conn-throws
  (is (thrown? org.libvirt.LibvirtException (conn "test:///defaultBROKEN"))))

(deftest domain-list-test
  (is 1 (count (domain-list (tc)))))

(deftest domains-test
  (is org.libvirt.Domain (first (domains (tc)))))

(deftest create-domain-test
  (let [new (create-domain (tc) new-domain-xml)]
    (is org.libvirt.Domain (type new))
    (is "testnew" (.getName new))))