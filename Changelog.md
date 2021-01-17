## v0.9.0

* Struct deserialization now returns a map with keys in the same order as struct entries (for upto 64 entries).

## v0.8.0

* `common-types` has been renamed to `common_types`

* `serialize` now uses ByteBuf and returns a single byte array, instead of a `concat` of smaller byte arrays. Manifold performance improvement.

* Change in usage of `common_types/padding`. Now it should be used like this

```clj
(deserialize {:type        :struct
              :definition [...
                           (ct/padding 2)
                           ...]}
             byte-seq)
```

instead of

```clj
(deserialize {:type        :struct
              :definition [...
                           [nil (ct/padding 2)]
                           ...]}
             byte-seq)
```

* Added new `:bytes` type

* Added optional `:adapter` attribute

* `:trim_nulls` for strings has been removed, use `:adapter` and `trim-nulls-end` instead
