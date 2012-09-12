(ns libvirt
  (:import [org.libvirt Connect ConnectAuth ConnectAuthPasswordProvided])
  (:require [clojure.string :as str])
  (:use libvirt.utils))

(declare ^:dynamic *libvirt-connection*)

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
  [name & {:keys [callfn wrap-result pass-connection] :or {callfn name wrap-result false pass-connection false}}]
   `(defn ~name
      [& args#]
      (let [;; TODO - clean this up. How?
            target# (if (or (= 0 (count args#)) ~pass-connection) *libvirt-connection* (first args#))
            ;; call-args# (if ~pass-connection (cons *libvirt-connection* (rest args#)) (rest args#))
            call-args# (if (or (= 0 (count args#)) ~pass-connection) args# (rest args#))
            res# (if (instance? JavaWrapper target#)
                   (apply str-invoke (:o target#) ~(str callfn) call-args#)
                   (apply str-invoke target# ~(str callfn) call-args#))]
        (case ~wrap-result
          :wrapper (JavaWrapper. res#)
          :seq (seq res#)
          res#)
        )))

(defn connect
  "Gets a connection to the specified URI. By default this is read-only, override by specifing ::rw true"
  [uri & {:keys [password] :or {password nil}}]
  (JavaWrapper. (Connect. uri (ConnectAuthPasswordProvided. password) 0)))

(defn hostspec-to-url
  [hostspec]
  (let [query (if-not (empty? (:opts hostspec)) (str "?" (str/join "&" (for [[k v] (:opts hostspec)] (str (name k) "=" v)))) "")
        url (str (name (:type hostspec)) "://"
                 (if (:username hostspec) (str (:username hostspec) "@"))
                 (:host hostspec)
                 (if-not (empty? query) "/")
                 query)]
    url))

(defn connect-from-hostspec
  "From a hostspec {:type, :host, :username, :password, :opts{} }, return a connection"
  [hostspec]
  (connect (hostspec-to-url hostspec) :password (:password hostspec)))


(defmacro with-libvirt
  "Sets a binding for a libvirt connection to act on"
  [hostspec & body]
  `(binding [*libvirt-connection* (connect-from-hostspec ~hostspec)]
     ~@body))
;;
;; Domains
;;

(defn pool-list)

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

(defn conn
  "Returns the bound connection"
  []
  *libvirt-connection*)

;;
;;  Wrapper Functions
;;

;; Shared
(defwrapperfn create)

;; Connect
(defwrapperfn max-vcpus :callfn getMaxVcpus)
(defwrapperfn network-create :callfn networkCreateXML :wrap-result :wrapper :pass-connection true)
(defwrapperfn network-define :callfn networkDefineXML :wrap-result :wrapper :pass-connection true)
(defwrapperfn node-info :callfn nodeInfo :wrap-result :wrapper)
(defwrapperfn define-domain :callfn domainDefineXML :wrap-result :wrapper :pass-connection true)
(defwrapperfn create-linux-domain :callfn domainCreateLinux :wrap-result :wrapper :pass-connection true)
(defwrapperfn list-storage-pools :callfn listStoragePools, :wrap-result :seq)