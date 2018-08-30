(ns edge.doc.routes
  (:require
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [edge.asciidoctor :refer [load-doc]]
   [yada.yada :as yada]))

(defn documentation-routes [engine]
  (assert engine)
  ["/"
   [
    ["" (yada/redirect ::doc-resource {:route-params {:name "index"}})]
    ["doc"
     [
      [#{"" "/"} (yada/redirect ::doc-resource {:route-params {:name "index"}})]
      [["/" :name ".html"]
       (yada/resource
         {:id ::doc-resource
          :methods
          {:get
           {:produces [{:media-type "text/html;q=0.8" :charset "utf-8"}
                       {:media-type "application/json"}]
            :response (fn [ctx]
                        (let [path (str "doc/sources/" (-> ctx :parameters :path :name) ".adoc")]
                          (try
                            (.convert
                              (load-doc
                                ctx
                                engine
                                (-> ctx :parameters :path :name)
                                (slurp (io/resource path))))
                            (catch Exception e
                              (throw (ex-info (format "Failed to convert %s" path)
                                              {:path path} e))
                              ))))}}})]]]]])

(defmethod ig/init-key :edge.doc/routes [_ {:keys [edge.asciidoctor/engine]}]
  (documentation-routes engine))
