(ns core
  (:gen-class)
  (:require [compojure.core :refer [POST routes]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]))

(def app-routes
  (apply routes
         (concat '()
                 [(POST "/ci" request
                        (let [{:keys [scripts-path valid-ref]} (:configuration request)
                              name (-> request :params :repository :name)
                              ref (-> request
                                      :params
                                      :ref
                                      (str/split #"/")
                                      (last))]
                          (println "New event: " ref name)
                          (when (= ref valid-ref)
                            (println "Run: " (str scripts-path name ".sh"))
                            (sh "bash" (str scripts-path name ".sh")))
                          (response/response "ok")))])))

(defn wrap-ci-configuration
  [handler configuration]
  (fn [request]
    (handler (assoc request :configuration configuration))))


(defn -main [& args]
  (jetty/run-jetty
    (-> app-routes
        (wrap-ci-configuration {:scripts-path (second args)
                                :valid-ref (first args)})
        (wrap-keyword-params)
        (wrap-params)
        (wrap-json-params))
    {:port 8097 :join? false}))