## 1.2.2

### Bug fixes

* `RealmRecyclerViewAdapter` uses RealmModel instead of RealmObject (#29).


## 1.2.1

### Bug fixes

* Fixed crash when auto-updating RealmResults (#25).


## 1.2.0

### Enhancements

* Removed `appcompat-v7` from the library dependencies list.


## 1.1.0

### Enhancements

* `RealmRecyclerViewAdapter`, a new adapter base class for `RecyclerView`s.

### Bug fixes

* Removed `allowBackup` and `supportsRtl` from the Android manifest file.

### Credits

* Paweł Surówka (@thesurix) for adding the `RealmRecyclerViewAdapter`.
* Mitchell Tilbrook (@marukami) for cleaning up `AndroidManifest.xml`.`


## 1.0.1

### Bug fixes

* Javadoc and sources are now properly distributed as well.
* RealmBaseAdapter: Fixed listeners potentially leaking when using a RealmResults.


## 1.0.0

Initial release.
