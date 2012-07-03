(ns libvirt
  (:import [org.libvirt Connect])
  (:use libvirt.utils))

(deftype JavaWrapper [wrapped]
  clojure.lang.ILookup
  (valAt [this key]
    (if (= key :o)
      wrapped
      (try
        (str-invoke wrapped (k-to-getter key))
        (catch RuntimeException e nil)))) ; no method, nil
  clojure.lang.Associative
  (assoc [this key val]
    (str-invoke wrapped (k-to-setter key) val)
    this)
  Object
  (toString [this] (.toString wrapped))
  (equals [this other] (.equals wrapped other))
  (hashCode [this] (.hashCode wrapped)))


(defn conn
  "Gets a connection to the specified URI. By default this is read-only, override by specifing ::rw true"
  [uri & {:keys [ro] :or {ro false}}]
  (Connect. uri ro))

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