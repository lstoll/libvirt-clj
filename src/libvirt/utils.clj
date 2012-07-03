(ns libvirt.utils)

;;Source: https://bitbucket.org/kumarshantanu/clj-argutil/overview
(defn not-nil?   "Same as (not (nil? x))"   [x] (not (nil? x)))
(defn not-empty? "Same as (not (empty? x))" [x] (not (empty? x)))

(defn camelstr-to-k
  "Given a camel-case string, convert it into a dash-delimited keyword. Upper
  case character triggers insertion of the dash."
  [cs]
  (let [b (StringBuilder.)
        f #(do
             (if (and (Character/isUpperCase %) (not (empty? b)))
               (.append b \-))
             (.append b (Character/toLowerCase %)))
        _ (doall (map f cs))
        a (filter not-empty? (into [] (.split (.toString b) "-")))
        s (apply str (interpose \- a))]
    (keyword s)))

(defn k-to-camelstr
  "Convert keyword to camel-case string and treat dash as case-changer.
  :-            --> \"\"
  :to-do        --> \"toDo\"       ; dash triggers upper-case
  :to_do        --> \"to_do\"      ; underscore stays intact
  :to-do-       --> \"toDo\"       ; trailing dash is ignored
  :-from-here   --> \"fromHere\"   ; leading dash is ignored too
  :hello--there --> \"helloThere\" ; consecutive dashes are treated as one"
  [k]
  (let [s (name k)
        tokens (filter not-empty? (into [] (.split s "-")))
        ;; ucase1 converts first character to upper case
        ucase1 #(str (Character/toUpperCase (first %))
                     (apply str (rest %)))
        lcase  #(if (not-nil? %) (.toLowerCase %))
        cctoks (map ucase1 tokens)]
    (apply str (lcase (first cctoks)) (rest cctoks))))

(defn k-to-methodname
  "Given a keyword and a bunch of method-name prefixes (collection of string),
  construct the method name (string). When called with only a keyword as an
  argument, k-to-methodname behaves like k-to-camelstr.
  See also: k-to-camelstr, camelstr-to-k"
  ([k prefixes]
     (let [s (name (camelstr-to-k (name k)))
           n (if (some #(.startsWith s (str % \-)) prefixes) s
                 (str (first prefixes) \- s))]
       (k-to-camelstr (keyword n))))
  ([k]
     (k-to-methodname k [""])))


(defn k-to-setter [k] (k-to-methodname k ["set"]))
(defn k-to-getter [k] (k-to-methodname k ["get" "is"]))

;; source: http://en.wikibooks.org/wiki/Clojure_Programming/Examples#Invoking_Java_method_through_method_name_as_a_String
(defn str-invoke [instance method-str & args]
  (clojure.lang.Reflector/invokeInstanceMethod
   instance
   method-str
   (to-array args)))

(defn str-field [instance field-str]
  (clojure.lang.Reflector/getInstanceField instance field-str))

(defn call-getter
  "Calls a getter, trying for get first, then is, then the method directly. Returns nil if field doesnt exist"
  [obj key]
  (try
    (str-invoke obj (k-to-getter (str "get-" (name key))))
    (catch IllegalArgumentException e
      (try
        ;; is- is truthy. less than 1 is false, anything else is true
        (if (> 1 (str-invoke obj (k-to-getter (str "is-" (name key))))) false true)
        (catch IllegalArgumentException e
          (try
            (str-invoke obj (k-to-camelstr (name key)))
            (catch IllegalArgumentException e
              (try
                (str-field obj (k-to-camelstr (name key)))
                (catch IllegalArgumentException e nil)))))))))