(ns libvirt-clj.test.libvirt-test
  (:use [libvirt])
  (:use [clojure.test]))

(deftest connection-tests
  (let [c (connect "test:///default")]
    (is (= "Test" (:type c)) "getType")
    (is (= (:URI c) "test:///default") "getURI")
    (is (= (max-vcpus c "xen") 32) "getMaxVcpus(xen)")
    (is (not (nil? (:host-name c))) "getHostName()")
    (is (not (nil? (:capabilities c))) "getCapabilities()")
    ;; (is (> 6000 (:connection-version c)) "getConnectionVersion()")
    (is (not (:encrypted c)) "isEncrypted()")
    ))

(deftest node-info-tests
  (let [c (connect "test:///default")
        ni (node-info c)]
    (is (= "i686" (:model ni)) "nodeInfo.model")
    (is (= 3145728 (:memory ni)) "nodeInfo.memory")
    (is (= 2 (:cores ni)) "nodeInfo.cores")
    ))

(deftest network-create-test
  (let [c (connect "test:///default")
        network-1-xml "<network><name>createst</name><uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e68</uuid><bridge name='createst'/><forward dev='eth0'/><ip address='192.168.66.1' netmask='255.255.255.0'><dhcp><range start='192.168.66.128' end='192.168.66.253'/></dhcp></ip></network>"
        network-2-xml "<network><name>deftest</name><uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e67</uuid><bridge name='deftest'/><forward dev='eth0'/><ip address='192.168.88.1' netmask='255.255.255.0'><dhcp><range start='192.168.88.128' end='192.168.88.253'/></dhcp></ip></network>"
        network-1 (network-create c network-1-xml)
        network-2 (network-define c network-2-xml)]
    (is (= 2 (:num-of-networks c)) "Number of Networks")
    (is (= 1 (:num-of-defined-networks c)) "Number of Defined Networks")
    (is (:active network-1) "Network 1 should be active")
    (is (not (:active network-2)) "Network 2 should not be active")
    ))

(deftest domain-create-test
  (let [c (connect "test:///default")
        domain-1-xml "<domain type='test' id='2'><name>deftest</name><uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e70</uuid><memory>8388608</memory><vcpu>2</vcpu><os><type arch='i686'>hvm</type></os><on_reboot>restart</on_reboot><on_poweroff>destroy</on_poweroff><on_crash>restart</on_crash></domain>"
        domain-2-xml "<domain type='test' id='3'><name>createst</name><uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e67</uuid><memory>8388608</memory><vcpu>2</vcpu><os><type arch='i686'>hvm</type></os><on_reboot>restart</on_reboot><on_poweroff>destroy</on_poweroff><on_crash>restart</on_crash></domain>"
        domain-1 (define-domain c domain-1-xml)
        domain-2 (create-linux-domain c domain-1-xml 0)]
    (is (= 2 (:num-of-domains c)) "Number of Domains")
    (is (:active domain-1) "Domain1 should not be active")
    ))
