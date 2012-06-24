(ns libvirt
  (:import [org.libvirt Connect]))

(defn conn
  "Gets a connection to the specified URI. By default this is read-only, override by specifing ::rw true"
  [uri & {:keys [ro] :or {ro false}}]
  (Connect. uri ro))

(defn domain-list
  "Gets list of Domains - returns their IDs"
  [conn]
  (seq (.listDomains conn)))

(defn domains
  "Gets domains for the connection"
  [conn]
  (map #(.domainLookupByID conn %) (domain-list conn)))

(defn create-domain
  "Creates a domain from the given XML config format. Will automatically start it"
  [conn domain-xml]
  ;; Just use default flags
  (.domainCreateXML conn domain-xml 0))