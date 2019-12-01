(ns vm-agent.besu
  (:require [io.pedestal.interceptor :as intc]
            [io.pedestal.interceptor.chain :as chain]
            [vm-agent.json-rpc :as json-rpc]))

(def connection "http://localhost:8545")

(defn interceptor
  [method & params]
  (intc/interceptor
   {:name ::interceptor
    :enter (fn [context]
             (-> context
                 (update-in [:request]
                            assoc
                            :json-rpc-connection connection
                            :json-rpc-method method
                            :json-rpc-params params)
                 (chain/enqueue* json-rpc/interceptor)))
    :leave (fn [context]
             (update-in context [:request] dissoc :json-rpc-connection))}))

(def read-block-number
  {:name ::read-block-number
   :enter (fn [context]
            (chain/enqueue* context (interceptor "eth_blockNumber")))})

(def read-accounts
  {:name ::read-accounts
   :enter (fn [context]
            (chain/enqueue* context (interceptor "eth_accounts")))})

(def read-validators
  {:name ::read-validators
   :enter (fn [context]
            (chain/enqueue* context
                            (interceptor "ibft_getValidatorsByBlockNumber" "latest")))})