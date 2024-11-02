(ns listener
  (:gen-class)
  (:require [compojure.core :refer [POST routes]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]))

(def scripts-path "/home/dudosyka/lumin/listener-scripts/")

(def app-routes
  (apply routes
         (concat '()
                 [(POST "/ci" request
                        (let [name (-> request :params :repository :name)
                              ref (-> request
                                      :params
                                      :ref
                                      (str/split #"/")
                                      (last))]
                          (println "New event: " ref name)
                          (when (= ref "master")
                            (println "Run: " (str scripts-path name ".sh"))
                            (sh "bash" (str scripts-path name ".sh")))
                          (response/response "ok")))])))


(defn -main []
  (jetty/run-jetty
    (-> app-routes
        (wrap-keyword-params)
        (wrap-params)
        (wrap-json-params))
    {:port 8082 :join? false}))