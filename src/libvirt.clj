(ns libvirt
  (:import [org.libvirt Connect]))

(defn conn
  "Gets a connection to the specified URI. By default this is read-only, override by specifing ::rw true"
  [uri & {:keys [ro] :or {ro false}}]
  (Connect. uri ro))

(defn list-domains
  "Lists domains for the connection"
  [conn]
  (seq (.listDomains conn)))