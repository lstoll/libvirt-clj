(ns libvirt
  (:import [org.libvirt Connect])
  (:use libvirt.utils))

(deftype JavaWrapper [wrapped]
  clojure.lang.ILookup
  (valAt [this key]
    (if (= key :o)
      wrapped
      (call-getter wrapped key))) ; no method, nil
  clojure.lang.Associative
  (assoc [this key val]
    (str-invoke wrapped (k-to-setter key) val)
    this)
  Object
  (toString [this] (.toString wrapped))
  (equals [this other] (.equals wrapped other))
  (hashCode [this] (.hashCode wrapped)))

(defmacro defwrapperfn
  "Creates an accessor function for a Java object"
  [name & {:keys [callfn wrap-result] :or {callfn name wrap-result false}}]
   `(defn ~name
      [target# & args#]
      (let [res# (if (instance? JavaWrapper target#)
                   (apply str-invoke (:o target#) ~(str callfn) args#)
                   (apply str-invoke target# ~(str callfn) args#))]
        (if ~wrap-result (JavaWrapper. res#) res#)
        )))


(defn connect
  "Gets a connection to the specified URI. By default this is read-only, override by specifing ::rw true"
  [uri & {:keys [ro] :or {ro false}}]
  (JavaWrapper. (Connect. uri ro)))

;;
;; Domains
;;


(defn domain-list
  "Gets list of Domains - returns their IDs"
  [conn]
  (seq (.listDomains conn)))

(defn domains
  "Gets domains for the connection"
  [conn]
  (map #(JavaWrapper. (.domainLookupByID conn %))
       (domain-list conn)))

(defn create-domain
  "Creates a domain from the given XML config format. Will automatically start it"
  [conn domain-xml]
  ;; Just use default flags
  (JavaWrapper. (.domainCreateXML conn domain-xml 0)))


;;
;;  Wrapper Functions
;;

;; Shared
(defwrapperfn create)

;; Connect
(defwrapperfn max-vcpus :callfn getMaxVcpus)
(defwrapperfn network-create :callfn networkCreateXML :wrap-result true)
(defwrapperfn network-define :callfn networkDefineXML :wrap-result true)
(defwrapperfn node-info :callfn nodeInfo :wrap-result true)
(defwrapperfn define-domain :callfn domainDefineXML :wrap-result true)
(defwrapperfn create-linux-domain :callfn domainCreateLinux :wrap-result true)