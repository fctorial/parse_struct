## v8.0

Change in usage of `common_types/padding`. Now it should be used like this

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