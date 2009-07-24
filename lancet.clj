(ns lancet
  (:gen-class)
  (:use [clojure.contrib.except :only (throw-if)] 
        clojure.contrib.shell-out
	      [clojure.contrib.str-utils :only (re-split)])
  (:import (java.beans Introspector) 
           (java.util.concurrent CountDownLatch)
           (org.apache.tools.ant.types Path)))

(def
 #^{:doc "Dummy ant project to keep Ant tasks happy"}   
 ant-project                                            
 (let [proj (org.apache.tools.ant.Project.)             
       logger (org.apache.tools.ant.NoBannerLogger.)]
   (doto logger                                         
     (.setMessageOutputLevel org.apache.tools.ant.Project/MSG_INFO)
     (.setOutputPrintStream System/out)
     (.setErrorPrintStream System/err))
   (doto proj                                           
     (.init)                                                                             
     (.addBuildListener logger))))

(defmulti coerce (fn [dest-class src-inst] [dest-class (class src-inst)]))

(defmethod coerce [java.io.File String] [_ str] 
  (java.io.File. str))
(defmethod coerce [Boolean/TYPE String] [_ str]
  (contains? #{"on" "yes" "true"} (.toLowerCase str)))
(defmethod coerce [Path String] [_ str]
  (new Path ant-project str))
(defmethod coerce :default [dest-cls obj]
  (cast dest-cls obj))

(defn env [val]
  (System/getenv (name val)))

(defn- build-sh-args [args]
  (concat (re-split #"\s+" (first args)) (rest args)))

(defn system [& args]
  (println (apply sh (build-sh-args args))))

(defn property-descriptor [inst prop-name]
  (first
   (filter #(= prop-name (.getName %)) 
	   (.getPropertyDescriptors 
	    (Introspector/getBeanInfo (class inst))))))

(defn get-property-class [write-method]
  (first (.getParameterTypes write-method)))

(defn set-property! [inst prop value]
  (let [pd (property-descriptor inst prop)]
    (throw-if (nil? pd) (str "No such property " prop))
    (let [write-method (.getWriteMethod pd)
	  dest-class (get-property-class write-method)]
      (.invoke write-method inst (into-array [(coerce dest-class value)])))))

(defn set-properties! [inst prop-map]
  (doseq [[k v] prop-map] (set-property! inst (name k) v)))
  
(defn update-property [property args add-subproperty]
  (let [property-map (if (map? (first args)) (first args) nil)
        sub-properties (if property-map (rest args) args)]
    (if property-map (set-properties! property property-map))
    (when (property-descriptor property "project")
      (set-property! property "project" ant-project))
    (doseq [sub-property sub-properties]
      (add-subproperty property sub-property))))
  
(defn add-subproperty [task property]
  (if (vector? property)
    (let [property-name (. (str (first property)) substring 1)
          creator-method-name (str "create" (. (. property-name substring 0 1) toUpperCase) (. property-name substring 1))]
      (try
        (let [task-class (. task getClass)
              creator-method (. task-class getMethod creator-method-name (into-array Class []))
              new-property (. creator-method invoke task (into-array Class []))
              new-property-args (rest property)]
          (update-property new-property new-property-args add-subproperty))
        (catch java.lang.IllegalArgumentException ex
          (throw (new RuntimeException (str "Could not find creator (" creator-method-name ") for " property-name " on task " (.. task getClass getName)) ex)))))
    (try
      (let [add-method (str "add" (.. property getClass getName))]
        (. task (symbol add-method) property ))
      (catch java.lang.IllegalArgumentException ex
        (try
          (.add task property)
          (catch java.lang.IllegalArgumentException ex
            (throw (new RuntimeException (str "Could not find adder for" (. property getClass) "on task" (. task getClass)) ex))))))))

(defn instantiate-task [project name props & filesets]
  (let [task (.createTask project name)]
    (throw-if (nil? task) (str "No task named " name))
    (doto task
      (.init)
      (.setProject project)
      (set-properties! props))
    (doseq [fs filesets]
      (add-subproperty task fs))	
    task))

(defn runonce
 "Create a function that will only run once. All other invocations
  return the first calculated value. The function *can* have side effects,
  and calls to runonce *can* be composed. Deadlock is possible
  if you have circular dependencies.
  Returns a [has-run-predicate, reset-fn, once-fn]"
  [function] 
  (let [sentinel (Object.)
	result (atom sentinel)
	reset-fn (fn [] (reset! result sentinel))
	has-run-fn (fn [] (not= @result sentinel))]
    [has-run-fn
     reset-fn
     (fn [& args]
       (locking sentinel
	 (if (= @result sentinel)
	   (reset! result (function))
	   @result)))]))
	    
(defmacro has-run? [f]
  `((:has-run (meta (var ~f)))))

(defmacro reset [f]
  `((:reset-fn (meta (var ~f)))))

(def targets (atom #{}))

(defmacro deftarget [sym doc & forms]
  (swap! targets #(conj % sym)) 
  (let [has-run (gensym "hr-") reset-fn (gensym "rf-")]
    `(let [[~has-run ~reset-fn once-fn#] (runonce (fn [] ~@forms))]
       (def ~(with-meta sym {:doc doc :has-run has-run :reset-fn reset-fn}) 
	    once-fn#))))

(defmacro define-ant-task [clj-name ant-name]
  `(defn ~clj-name [& props#]
     (let [task# (apply instantiate-task ant-project ~(name ant-name) props#)]
       (.execute task#)
       task#)))

(defmacro define-ant-type [clj-name ant-name & constructor-args]
  `(defn ~clj-name [& args#]
     (let [bean# (new ~ant-name ~@constructor-args)]
       (update-property bean# args# add-subproperty)
	     bean#)))

(defn task-names [] (map symbol (seq (.. ant-project getTaskDefinitions keySet))))

(defn safe-ant-name [n]
  (if (ns-resolve 'clojure.core n) (symbol (str "ant-" n)) n))

(defmacro define-all-ant-tasks []
  `(do ~@(map (fn [n] `(define-ant-task ~(safe-ant-name n) ~n)) (task-names))))

(define-all-ant-tasks)

(define-ant-type files org.apache.tools.ant.types.resources.Files)
(define-ant-type fileset org.apache.tools.ant.types.FileSet)

(defn run-targets [& targs]
  (if targs
    (doseq [targ (map symbol targs)]
      (eval (list targ)))
    (println "Available targets: " @targets)))

(defn -main [& targs]
  (load-file "build.clj")
  (apply run-targets targs))