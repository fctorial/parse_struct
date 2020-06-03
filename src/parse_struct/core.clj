(ns parse_struct.core
  (:require [parse_struct.serialize :as s]
            [parse_struct.serialize_lazy :as sl]
            [parse_struct.deserialize :as d]
            [parse_struct.utils :as u]
            [potemkin :refer [import-vars]]))

(import-vars [s serialize]
             [sl serialize-lazy]
             [d deserialize]
             [u type-size trim-nulls-end])

